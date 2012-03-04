package org.aksw.update;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.diff.HashSetDiff;
import org.aksw.commons.jena.util.QuadUtils;
import org.aksw.commons.sparql.core.SparqlEndpoint;

import sparql.DnfUtils;
import sparql.FilterCompiler;
import sparql.FilterUtils;
import sparql.PatternUtils;
import sparql.ViewTable;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;



/**
 * Keeps track of changes to a sparql query result
 * in the course of updates.
 * 
 * 
 * @author Claus Stadler
 *
 */
public class IncrementalQueryUpdateManager
	implements GraphListener
{
	//private Map<QuadSignature, Set<FilteredGraph>> sigToGraphs = new HashMap<QuadSignature, Set<FilteredGraph>>();
	Set<QuadFilter> quadFilters = new HashSet<QuadFilter>();
	
	
	private SparqlEndpoint sparqlEndpoint;
	private Query query;
	
	//private Set<Quad> dirtyQuads = new HashSet<Quad>();

	HashSetDiff<Quad> diff = new HashSetDiff<Quad>();

	HashSetDiff<Binding> aggregatedChanges = new HashSetDiff<Binding>();

	
	/**
	 * Checks whether the given quad is relevant to this view.
	 * 
	 * @param quad
	 * @return
	 */
	public boolean isRelevant(Quad quad)
	{
		for(QuadFilter graph : quadFilters) {
			
			if(graph.doesAccept(quad)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Checks whether the given quad already exists in the store.
	 * FIXME Implement for batched checking
	 * 
	 * @param quad
	 * @return
	 */
	public boolean doesExist(Quad quad) {
		return sparqlEndpoint.executeAsk(FilterCompiler.askForQuad(quad));
	}
	
	public ResultSet processQuads(Iterable<Quad> quads)
	{
		
		List<String> orFilters = new ArrayList<String>();
		
		for(Quad quad : quads) {
			for(QuadFilter graph : quadFilters) {
				//graph.add(quad);
				
				if(graph.doesAccept(quad)) {
					
					// Take the original query, and evaluate it against the
					// new triple
					
					// TODO Currently we create a single huge filter statement
					//      A different strategy would be to create unions.
					//      Might be worth evaluating what the performance
					//      differenc is.
					
					Map<Node, Node> varMap = QuadUtils.getVarMapping(graph.getPattern(), quad);
					
					List<String> andFilters = new ArrayList<String>();
					for(Map.Entry<Node, Node> entry : varMap.entrySet()) {
						String str = FilterCompiler.compileFilter(entry.getValue(), "?" + entry.getKey().getName());
						
						andFilters.add(str);
						//filters += FilterCompiler.wrapFilter(FilterCompiler.compileFilter(entry.getValue(), "?" + entry.getKey().getName()));
					}
					
					orFilters.add(Joiner.on(" && ").join(andFilters));
				}
				
				
			}
		}
		
		if(orFilters.isEmpty()) {
			return new ResultSetMem();
		}
		
		String filter = FilterCompiler.wrapFilter("(" + Joiner.on(") || (").join(orFilters) + ")");
		
		// TODO This is a string hack to append our filter to the original query.
		// Maybe there is a clean, structured jena way to do that? 
		
		String qs = query.toString();		
		int hackPos = qs.lastIndexOf("}");
		qs = qs.substring(0, hackPos) + filter + "}";
		
		
		ResultSet rs = sparqlEndpoint.executeSelect(qs);
		return rs;
	}
	
	/**
	 * This method must only be called for triples that were actually removed
	 * from the underlying store. 
	 * Otherwise the view will go out of sync.
	 * 
	 * @param quad
	 */
	public void removeSeen(Quad quad)
	{
		if(!isRelevant(quad)) {
			return;
		}
		
	}

	/**
	 * This method must only be called for triples that were actually added
	 * to the underlying store. 
	 * Otherwise the view will go out of sync.
	 * 
	 * @param quad
	 */
	public void add(Quad quad)
	{
		if(!isRelevant(quad)) {
			return;
		}
		
		diff.add(quad);
	}

	
	public static Set<Binding> resultSetToBindings(ResultSet rs) {
		Set<Binding> result = new HashSet<Binding>();
		
		while(rs.hasNext()) {
			result.add(rs.nextBinding());			
		}
		
		return result;
	}

	public HashSetDiff<Binding> getChanges()
	{
		HashSetDiff<Binding> result = new HashSetDiff<Binding>();
		result.getAdded().addAll(aggregatedChanges.getAdded());
		result.getRemoved().addAll(aggregatedChanges.getRemoved());

		aggregatedChanges.clear();
		
		return result;
		
	}
	
	
	
	public void computeInserts()
	{
		System.out.println("#dirty quads: " + diff.getAdded().size());

		Set<Binding> added = resultSetToBindings(processQuads(diff.getAdded()));
		diff.getAdded().clear();

		for(Binding item : added) {
			aggregatedChanges.add(item);
		}
	}

	public void computeDeletions()
	{
		System.out.println("#deletion dirty quads: " + diff.getRemoved().size());

		Set<Binding> removed = resultSetToBindings(processQuads(diff.getRemoved()));		
		diff.getRemoved().clear();
		
		for(Binding item : removed) {
			aggregatedChanges.remove(item);
		}		
	}

	
	public void remove(Quad quad)
	{
		if(!isRelevant(quad)) {
			return;
		}
		
		diff.remove(quad);
	}

	public IncrementalQueryUpdateManager(String queryString, SparqlEndpoint sparqlEndpoint)
		throws SQLException
	{		
		this.sparqlEndpoint = sparqlEndpoint;
		
		
		query = QueryFactory.create(queryString);
		
		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);
		System.out.println(op);

		//op = Algebra.optimize(op);
		//System.out.println(op);


		//String queryHash = StringUtils.md5Hash(queryString);
		//viewTable = new ViewTable(dataSource, "sparql_matview_data_" + queryHash, query.getQueryPattern().varsMentioned());
		
		//viewTable.clear();
		
		// fill the table
		//sparqlIntoView(sparqlEndpoint, queryString, viewTable);
		
		

		// TODO Hack! Does not work with unions (because they scope filters)
		ExprList exprs = FilterUtils.collectExprs(op, new ExprList());
		Collection<Quad> quads =  PatternUtils.collectQuads(op, new ArrayList<Quad>());

				
		List<ExprList> clauses = DnfUtils.toClauses(exprs);
		System.out.println("DNF = " + clauses);

		Set<Set<Expr>> dnf = FilterUtils.toSets(clauses);
		
		
		boolean isSatisfiable = dnf == null || DnfUtils.isSatisfiable(dnf);
		if(!isSatisfiable) {
			throw new RuntimeException("The view definition was detected to not be satisfiable");
		}
		
		//System.out.println("Satisfiable? " + DnfExprTransformer.isSatisfiable(dnf));
		
		// Determine those expressions that are common to all clauses
		/*
		Set<Set<Expr>> clausesSet = toSets(clauses);

		Set<Expr> common = new HashSet<Expr>();
		
		if(!clausesSet.isEmpty()) {
			common.addAll(clausesSet.iterator().next());
		
			for(Set<Expr> entry : clausesSet) {
				common.retainAll(entry);
				if(common.isEmpty()) {
					break;
				}
			}
		}
				
		
		for(Quad quad : quads) {
			
			List<ExprList> filter = new ArrayList<ExprList>();			
			for(Expr x : common) {
				if(PatternUtils.getVariables(quad).containsAll(x.getVarsMentioned())) {
					
					filter.add(new ExprList(x));
					System.out.println("For quad " + quad + " got expr " + x);
				}
			}


			graphs.add(new FilteredGraph(quad, filter));
			//QuadSignature signature = Equivalence.getSignature(quad);
			//MultiMaps.put(sigToGraphs, signature, new FilteredGraph(quad, filter));
		}
		*/

		for(Quad quad : quads) {
			Set<Set<Expr>> filter = FilterUtils.determineFilterDnf(quad, dnf);
			System.out.println("For quad " + quad + " got expr " + filter);
			quadFilters.add(new QuadFilter(quad, filter));
		}
		
	}
	
	
	// TODO The incemental update manager is separate from the view
	public static void sparqlIntoView(SparqlEndpoint sparqlEndpoint, String queryString, ViewTable viewTable)
		throws SQLException
	{
		ResultSet rs = sparqlEndpoint.executeSelect(queryString);
		while(rs.hasNext()) {
			Binding binding = rs.nextBinding();
			
			viewTable.insert(binding);
		}		
	}


	@Override
	public void onPreBatchStart() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPreInsert(Quad quad) {
		this.add(quad);
	}


	@Override
	public void onPreDelete(Quad quad) {
		this.remove(quad);
	}


	@Override
	public void onPreBatchEnd() {
		computeDeletions();
	}


	@Override
	public void onPostBatchStart() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPostInsert(Quad quad) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPostDelete(Quad quad) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPostBatchEnd() {
		computeInserts();
	}
	

}

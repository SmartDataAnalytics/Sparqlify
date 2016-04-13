package org.aksw.sparqlify.config.syntax;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.VarDef;
import org.aksw.sparqlify.config.lang.Constraint;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.system.PrefixMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.util.ExprUtils;



public class ViewDefinition {
	private String name;
	
	// FIXME: Either make ViewDefinition inherit from ViewTemplateDefinition, or get rid of this nesting alltogether
	private ViewTemplateDefinition viewTemplateDefinition;
	private ExprList filters = new ExprList();
	
	private SqlOp relation;
	
	private List<Constraint> constraints;
	
	public ViewDefinition() {
		this.viewTemplateDefinition = new ViewTemplateDefinition();
	}
	
	public ViewDefinition(String name, ViewTemplateDefinition viewTemplateDefinition, SqlOp relation, List<Constraint> constraints) {
		this.name = name;
		this.viewTemplateDefinition = viewTemplateDefinition;
		this.relation = relation;
		this.constraints = constraints;
	}
	
	public ViewTemplateDefinition getViewTemplateDefinition() {
		return viewTemplateDefinition;
	}
	
	public void setViewTemplateDefinition(
			ViewTemplateDefinition viewTemplateDefinition) {
		this.viewTemplateDefinition = viewTemplateDefinition;
	}

	public void setConstructTemplate(ViewTemplateDefinition viewTemplateDefinition) {
		this.viewTemplateDefinition = viewTemplateDefinition;
	}
	
	public String getName()
	{
		return name;
	}

	public SqlOp getRelation() {
		return relation;
	}
	public void setRelation(SqlOp relation) {
		this.relation = relation;
	}
	
	public ExprList getFilters() {
		return filters;
	}

	public void setFilters(ExprList filters) {
		this.filters = filters;
	}
	
	public List<Constraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}

	@Override
	public String toString() {
		return "ViewDefinition [constructTemplate=" + viewTemplateDefinition
				+ ", relation=" + relation
				+ "]";
	}

	
	public String getDefinitionString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//PrintStream out = new PrintStream(baos);
		
		IndentedWriter out = new IndentedWriter(baos);
		
		out.print("Create View ");
		out.print(this.getName());
		out.println(" As");
		out.incIndent();
		
		out.println("Construct {");
		
		out.incIndent();
		
		QuadPattern template = getViewTemplateDefinition().getConstructTemplate(); 
		Map<Node, Set<Triple>> map = QuadPatternUtils.indexSorted(template);
		
		for(Entry<Node, Set<Triple>> entry : map.entrySet()) {
			Node g = entry.getKey();
			Set<Triple> triples = entry.getValue();
			
			boolean isDefaultGraph = Quad.defaultGraphNodeGenerated.equals(g);

			if(!isDefaultGraph) {
				out.println("GRAPH " + g + " {");
				out.incIndent();
			}
			
			for(Triple triple : triples) {
				String triplesStr = toNTripleString(triple, null);
				out.println(triplesStr);
			}
			
			if(!isDefaultGraph) {
				out.decIndent();
				out.println("}");
			}
		}
		
		
//		for(Triple triple : getViewTemplateDefinition().getConstructTemplate().getBGP().getList()) {
//			out.println(toNTripleString(triple, null));
//			//ModelUtils.
//			//out.println(triple);
//		}
		/*
		Model model = ModelFactory.createDefaultModel();
		for(Triple triple : getViewTemplateDefinition().getConstructTemplate().getBGP().getList()) {
			ModelUtils.tripleToStatement(model, triple);
			//out.println(triple);
		}*/
		
		
		out.decIndent();
		
		out.println("}");
		out.println("With");
		out.incIndent();
		for(Expr expr : getViewTemplateDefinition().getVarBindings()) {
			if(!(expr instanceof E_Equals)) {
				throw new RuntimeException("Should not happen.");
			}
			
			E_Equals e = (E_Equals)expr;
			out.println(e.getArg1() + " = " + ExprUtils.fmtSPARQL(e.getArg2()));
		}
		out.decIndent();
		
		out.println("From");
		out.incIndent();
		if(relation instanceof QueryString) {
			out.println("[[" + ((QueryString)relation).getQueryString() + "]]");
		} else if (relation instanceof RelationRef) {
			out.println(((RelationRef)relation).getRelationName());
		} else {
			throw new RuntimeException("Should not happen");
		}
		
		out.decIndent();
		out.decIndent();
		
		out.flush();
		out.close();
		
		return baos.toString();
	}
	
	public static String toNTripleString(Triple triple, PrefixMap map) {
		return
				toNTripleString(triple.getSubject(), map) + " " + 
				toNTripleString(triple.getPredicate(), map) + " " +
				toNTripleString(triple.getObject(), map) + " .";
	}
		
	public static String toNTripleString(Node node, PrefixMap map) {
		if(node.isURI()) {
			
			String result = null;;
			if(map != null) {
				result = map.abbreviate(node.getURI());
			}

			if(result == null) {
				return "<" + node.getURI() + ">";
			} else {
				return result;
			}
		}
		
		return node.toString();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addVarDef(Var var, VarDef termDef) {
		addVarDef(var, termDef.getExpr());
	}
	
	// Methods for construction
	public void addVarDef(Var var, Expr expr) {
		Expr tmp = new E_Equals(new ExprVar(var), expr);
		
		this.viewTemplateDefinition.getVarBindings().add(tmp);
	}
	
	
	// Convenience method
	public QuadPattern getConstructPattern() {
		return this.viewTemplateDefinition.getConstructTemplate();
	}
}

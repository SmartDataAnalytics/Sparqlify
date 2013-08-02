package sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ExprUtils;


class Utils
{
	public static boolean eval(ExprList exprs, Binding binding)
	{
		boolean result = true;
		if(exprs == null) {
			return result;
		}
		
		for(Expr expr : exprs) {
			result = result && ExprUtils.eval(expr, binding).getBoolean();
			if(result == false) {
				break;
			}
		}
		
		return result;
	}
	
	
	/**
	 * TODO Probably actually rewrite the expression
	 * 
	 * Rewrites the expressions encountered in the algebra so that when given
	 * a binding, evaluating the expression will tell us whether the binding
	 * satisfies all predicates.
	 * 
	 * 
	 * 
	 * Optional
	 * 
	 * Union(Filter(A, ?s = x), Filter(B, ?s != x))
	 * 
	 * @param op
	 * @param binding
	 * @param env
	 * @return
	 */
	public static boolean isSolutionCandidate(Op op, Binding binding)
	{
		if(op instanceof OpLeftJoin) {
			OpLeftJoin x = (OpLeftJoin)op;
			return (
				eval(x.getExprs(), binding) &&
				isSolutionCandidate(x.getLeft(), binding) && 
				isSolutionCandidate(x.getRight(), binding)
			);
		} else if(op instanceof OpFilter) {
			OpFilter x = (OpFilter)op;
			return (
					eval(x.getExprs(), binding) &&
					isSolutionCandidate(x.getSubOp(), binding)
			);
		} else if(op instanceof OpJoin) {
			OpJoin x = (OpJoin)op;
			
			return (
					isSolutionCandidate(x.getLeft(), binding) && 
					isSolutionCandidate(x.getRight(), binding)
				);
		} else if(op instanceof OpUnion) {
			OpUnion x = (OpUnion)op;

			return (
					isSolutionCandidate(x.getLeft(), binding) || 
					isSolutionCandidate(x.getRight(), binding)
				);
		} else if(op instanceof OpQuadPattern) {
			return (
					true
			);
		} else {
			throw new NotImplementedException("Type: " + op.getClass());
		}
	}

}


public class Equivalence
{
	/**
	 * Ideally this function would do the following:
	 * .) Check for structural equivalence of the joins
	 * .) Return a set of pairs of graph patterns that need to be matched to each other
	 * 
	 * @param opA
	 * @param opB
	 * @param state
	 * @param constraint
	 * @return
	 */
	public static Set<Pair<QuadPattern, QuadPattern>> findIsomorphy(Op opA, Op opB, Map<Node, Node> state, Map<Node, Node> constraint)
	{
		Set<Pair<QuadPattern, QuadPattern>> result = new HashSet<Pair<QuadPattern, QuadPattern>>();

		if(!opA.getClass().equals(opB.getClass())) {
			return Collections.emptySet();
		}
		
		if(opA instanceof OpLeftJoin) {
			//System.out.println("left join");
			
			Set<Pair<QuadPattern, QuadPattern>> resultPartLeft = findIsomorphy(((OpLeftJoin)opA).getLeft(), ((OpLeftJoin)opB).getLeft(), state, constraint);
			Set<Pair<QuadPattern, QuadPattern>> resultPartRight = findIsomorphy(((OpLeftJoin)opA).getRight(), ((OpLeftJoin)opB).getRight(), state, constraint);
			
			result.addAll(resultPartLeft);
			result.addAll(resultPartRight);
			
			return result;
			
		} else if(opA instanceof OpQuadPattern) {
			//System.out.println("quad pattern");
			
			QuadPattern a = ((OpQuadPattern)opA).getPattern();
			QuadPattern b = ((OpQuadPattern)opB).getPattern();
			
			return Collections.singleton(Pair.create(a, b));
			
		} else if(opA instanceof OpJoin) {
			//System.out.println("join");
			
			Set<Pair<QuadPattern, QuadPattern>> resultPartLeft = findIsomorphy(((OpJoin)opA).getLeft(), ((OpJoin)opB).getLeft(), state, constraint);
			Set<Pair<QuadPattern, QuadPattern>> resultPartRight = findIsomorphy(((OpJoin)opA).getRight(), ((OpJoin)opB).getRight(), state, constraint);
			
			result.addAll(resultPartLeft);
			result.addAll(resultPartRight);
			
			return result;
		} else if(opA instanceof OpFilter) {

			Op a = ((OpFilter)opA).getSubOp();
			Op b = ((OpFilter)opB).getSubOp();
			
			return findIsomorphy(a, b, state, constraint);
		} else if(opA instanceof OpUnion) {
			Set<Pair<QuadPattern, QuadPattern>> resultPartLeft = findIsomorphy(((OpUnion)opA).getLeft(), ((OpUnion)opB).getLeft(), state, constraint);
			Set<Pair<QuadPattern, QuadPattern>> resultPartRight = findIsomorphy(((OpUnion)opA).getRight(), ((OpUnion)opB).getRight(), state, constraint);
			
			result.addAll(resultPartLeft);
			result.addAll(resultPartRight);
			
			return result;
		} else if(opA instanceof OpTable) {
			return result;
			// TODO Anything else to do here? Don't think so.
		} else {
			throw new NotImplementedException(opA.getClass());
		}
	}	
	

	public static Set<Map<Node, Node>> findIsomorphy(Query a, Query b)
	{
		Op opA = Algebra.compile(a);
		opA = Algebra.toQuadForm(opA);
		
		Op opB = Algebra.compile(b);
		opB = Algebra.toQuadForm(opB);
		
		return findIsomorphy(opA, opB);
	}
	
	public static Set<Map<Node, Node>> findIsomorphy(Op a, Op b)
	{
		Map<Node, Node> xstate = new HashMap<Node, Node>();
		Map<Node, Node> constraint = new HashMap<Node, Node>();
		
		
		Set<Pair<QuadPattern, QuadPattern>> pairs = findIsomorphy(a, b, xstate, constraint);
		
		List<Pair<Set<Quad>, Set<Quad>>> blockPairs = new ArrayList<Pair<Set<Quad>, Set<Quad>>>();
		
		// TODO do some smart ordering on the blockPairs, so that those with
		// fewest variables per triple are taken first
		for(Pair<QuadPattern, QuadPattern> pair : pairs) {
			PatternSignatureIndex qA = new PatternSignatureIndex(pair.getKey());
			PatternSignatureIndex qB = new PatternSignatureIndex(pair.getValue());
			
			
			
			List<IPatternSignature> patternOrder =
				new ArrayList<IPatternSignature>(qA.getTypeToPattern().keySet());
			Collections.sort(patternOrder, new PatternTypeComparator());		
			
			for(IPatternSignature signature : patternOrder) {
				Set<Quad> qAs = qA.getTypeToPattern().get(signature);
				Set<Quad> qBs = qB.getTypeToPattern().get(signature);

				if(qBs == null) {
					return Collections.emptySet();
				}

				blockPairs.add(Pair.create(qAs, qBs));				
			}
		}


		return findIsomorphy(new HashMap<Node, Node>(), blockPairs, 0);
	}
	
	
	public static Set<Map<Node, Node>> findIsomorphy(Map<Node, Node> in,
			List<Pair<Set<Quad>, Set<Quad>>> order, int index)
	{
		Set<Map<Node, Node>> result = new HashSet<Map<Node, Node>>();
		
		if(index >= order.size()) {
			if(in.isEmpty()) {
				return Collections.emptySet();
			} else {
				return Collections.singleton(in);
			}
		}
		
		Pair<Set<Quad>, Set<Quad>> block = order.get(index);

		Set<Quad> qAs = block.getKey();
		Set<Quad> qBs = block.getValue();

		Map<Node, Node> state = new HashMap<Node, Node>();

		Set<Map<Node, Node>> mappings = getCompatibleMappings(qAs, qBs, state, in);
		
		for(Map<Node, Node> mapping : mappings) {
			Map<Node, Node> union = new HashMap<Node, Node>(in);
			union.putAll(mapping);
			
			result.addAll(findIsomorphy(union, order, index + 1));
		}
		
		return result;
	}


	public static Set<Map<Node, Node>> getCompatibleMappings(QuadPattern a, QuadPattern b, Map<Node, Node> state, Map<Node, Node> constraint)
	{
		return getCompatibleMappings(Lists.newArrayList(a), Lists.newArrayList(b), state, constraint);
	}
	
	public static Set<Map<Node, Node>> getCompatibleMappings(Collection<Quad> as, Collection<Quad> bs, Map<Node, Node> state, Map<Node, Node> constraint)
	{		
		Set<Map<Node, Node>> result = new HashSet<Map<Node, Node>>();

		if(as.size() != bs.size()) {
			return result; 
		}

		
		if(bs.isEmpty()) {
			result.add(state);
			return result;
		}
		
		
		for(Quad a : as) {
			List<Quad> remainingAs = new ArrayList<Quad>(as);
			remainingAs.remove(a);

			for(Quad b : bs) {
				
				
				List<Quad> remainingBs = new ArrayList<Quad>(bs);				
				remainingBs.remove(b);
				
				Map<Node, Node> nodeMap = getVarMappingSpecialized(a, b);
				if(nodeMap == null) {
					continue;
				}
				
				if(!MapUtils.isCompatible(state, nodeMap)) {
					continue;
				}

				Map<Node, Node> newState = new HashMap<Node, Node>(state);
				newState.putAll(nodeMap);
				
				if(!MapUtils.isCompatible(constraint, newState)) {
					continue;
				}

				
				Set<Map<Node, Node>> resultPart = getCompatibleMappings(remainingAs, remainingBs, newState, constraint);
				
				result.addAll(resultPart);
			}
		}
		
		return result;
	}
	

	/**
	 * Important: This method assumes that the quads have the same
	 * pattern signature and sanity checks that using java assertions.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Map<Node, Node> getVarMappingSpecialized(Quad a, Quad b)
	{
		List<Node> nAs = QuadUtils.quadToList(a);
		List<Node> nBs = QuadUtils.quadToList(b);
		
		Map<Node, Node> result = new HashMap<Node, Node>();
		for(int i = 0; i < 4; ++i) {
			Node nA = nAs.get(i);
			Node nB = nBs.get(i);
			
			assert nA.isVariable() == nB.isVariable() : "Should not happen, since the indexing by pattern type should prevent this case";
			
			
			if(nA.isVariable()) {
				Map<Node, Node> newEntry = Collections.singletonMap(nA, nB);		
				
				assert MapUtils.isCompatible(result, newEntry) : "Should not happen, since the indexing by pattern type should prevent this case";
				
				result.putAll(newEntry);
			} else {
				assert nA.equals(nB) : "Should not happen, since the indexing by pattern type should prevent this case";
			}
		}
		
		return result;
	}
	
	public static QuadSignature getSignature(Quad quad)
	{
		List<Node> result = new ArrayList<Node>();


		Map<Node, Integer> variableToId = new HashMap<Node, Integer>();

		for (Node node : QuadUtils.quadToList(quad)) {
			// TODO What about blank nodes?
			// Do we need consider mapping blank nodes to vars and vice
			// versa
			// or only vars-vars and blanks-blanks?
			if (node.isVariable()) {
				Integer id = variableToId.get(node);
				if (id == null) {
					id = variableToId.size();
					variableToId.put(node, id);
				}

				result.add(Node.createURI("http://var.xxx/" + id));

			} else {
				result.add(node);
			}
		}
		

		return new QuadSignature(result);
	}
	
	
	
	


}


class PatternTypeComparator
		implements Comparator<IPatternSignature>
{
	@Override
	public int compare(IPatternSignature a, IPatternSignature b)
	{
		return b.getVarsMentioned().size() - a.getVarsMentioned().size();
	}
}

class Junk {
	
	
	@Deprecated
	public static Set<Map<Node, Node>> findIsomorphyOld(Op opA, Op opB, Map<Node, Node> state, Map<Node, Node> constraint)
	{
		Set<Map<Node, Node>> result = new HashSet<Map<Node, Node>>();

		System.out.println("TypeA: " + opA.getClass());
		System.out.println("TypeB: " + opB.getClass());

		if(!opA.getClass().equals(opB.getClass())) {
			return result;
		}
		
		if(opA instanceof OpLeftJoin) {
			//System.out.println("left join");
			
			Set<Map<Node, Node>> resultPartLeft = findIsomorphyOld(((OpLeftJoin)opA).getLeft(), ((OpLeftJoin)opB).getLeft(), state, constraint);
			Set<Map<Node, Node>> resultPartRight = findIsomorphyOld(((OpLeftJoin)opA).getRight(), ((OpLeftJoin)opB).getRight(), state, constraint);

			Set<Map<Node, Node>> union = getCompatibleCombinations(resultPartLeft, resultPartRight);
			
			return union;
			
		} else if(opA instanceof OpQuadPattern) {
			//System.out.println("quad pattern");
			
			QuadPattern a = ((OpQuadPattern)opA).getPattern();
			QuadPattern b = ((OpQuadPattern)opB).getPattern();
			
			return Equivalence.getCompatibleMappings(a, b, state, constraint);
		} else if(opA instanceof OpJoin) {
			//System.out.println("join");
			
			Set<Map<Node, Node>> resultPartLeft = findIsomorphyOld(((OpJoin)opA).getLeft(), ((OpJoin)opB).getLeft(), state, constraint);
			Set<Map<Node, Node>> resultPartRight = findIsomorphyOld(((OpJoin)opA).getRight(), ((OpJoin)opB).getRight(), state, constraint);

			Set<Map<Node, Node>> union = getCompatibleCombinations(resultPartLeft, resultPartRight);
			
			return union;			
		} else {
			throw new NotImplementedException();
		}
		
		//return null;
	}	
	
	

	/*
	public static Set<Map<Node, Node>> findIsomorphySet(Map<Node, Node> in,
			List<IPatternSignature> order, int index, QueryIndex a, QueryIndex b)
	{
		if(index >= order.size()) {
			return Collections.singleton(in);
		}
		
		Set<Map<Node, Node>> result = new HashSet<Map<Node, Node>>();
		
		
		IPatternSignature type = order.get(index);

		Set<Quad> qAs = a.getTypeToPattern().get(type);
		Set<Quad> qBs = b.getTypeToPattern().get(type);

		if(qBs == null) {
			return Collections.emptySet();
		}

		Map<Node, Node> state = new HashMap<Node, Node>();

		Set<Map<Node, Node>> mappings = getCompatibleMappings(qAs, qBs, state, in);
		
		for(Map<Node, Node> mapping : mappings) {
			Map<Node, Node> union = new HashMap<Node, Node>(in);
			union.putAll(mapping);
			
			result.addAll(findIsomorphySet(union, order, index + 1, a, b));
		}
		
		return result;
	}
	*/
	
	@Deprecated
	public static Map<Node, Node> findIsomorphyOld(Query a, Query b)
	{
		PatternSignatureIndex ia = new PatternSignatureIndex(a);
		PatternSignatureIndex ib = new PatternSignatureIndex(b);

		// Sort the patterns by the number of variables - least number first
		List<IPatternSignature> patternOrder = new ArrayList<IPatternSignature>(ia
				.getTypeToPattern().keySet());
		Collections.sort(patternOrder, new PatternTypeComparator());

		Map<Node, Node> out = new HashMap<Node, Node>();

		return findIsomorphy(out, patternOrder, 0, ia, ib);
	}


	/**
	 * What I want is something like a PermutationIterator?
	 * Or maybe its more like a path and therefore tree iterator?
	 * I guess its the second:
	 * 
	 * Tree tree = getTree(a, b);
	 * tree.getChildren();
	 * 
	 * 
	 * 
	 * @param a
	 * @param bs
	 * @param state
	 * @param constraint
	 * @return
	 */
	Set<Map<Node, Node>> getCompatibleMappings(Quad a, Collection<Quad> bs, Map<Node, Node> state, Map<Node, Node> constraint) {
		
		Set<Map<Node, Node>> result = new HashSet<Map<Node, Node>>();
		
		for(Quad b : bs) {
			List<Quad> remainingBs = new ArrayList<Quad>(bs);
			remainingBs.remove(b);
			
			Map<Node, Node> nodeMap = Equivalence.getVarMappingSpecialized(a, b);
			if(nodeMap != null) {
				continue;
			}
			
			if(!MapUtils.isCompatible(state, nodeMap)) {
				continue;
			}
			
			Map<Node, Node> newState = new HashMap<Node, Node>(state);
			newState.putAll(nodeMap);
			
			Set<Map<Node, Node>> resultPart = getCompatibleMappings(a, remainingBs, state, constraint);
			
			result.addAll(resultPart);
		}
		
		return result;
	}

	/**
	 * Mapping sets of quads:
	 * Basically we have to try all possible combinations whether they are
	 * compatible
	 * 
	 * 
	 * 
	 * @param in
	 * @param order
	 * @param index
	 * @param a
	 * @param b
	 * @return
	 */
	public static Map<Node, Node> findIsomorphy(Map<Node, Node> in,
			List<IPatternSignature> order, int index, PatternSignatureIndex a, PatternSignatureIndex b)
	{
		if(index >= order.size()) {
			return in;
		}
		
		IPatternSignature type = order.get(index);

		Set<Quad> qAs = a.getTypeToPattern().get(type);
		Set<Quad> qBs = b.getTypeToPattern().get(type);

		if(qBs == null) {
			return Collections.emptyMap();
		}

		Map<Node, Node> state = new HashMap<Node, Node>();

		Set<Map<Node, Node>> mappings = Equivalence.getCompatibleMappings(qAs, qBs, state, in);
		
		for(Map<Node, Node> mapping : mappings) {
			Map<Node, Node> union = new HashMap<Node, Node>(in);
			union.putAll(mapping);
			
			Map<Node, Node> result = findIsomorphy(union, order, index + 1, a, b);
			if(result != null) {
				return result;
			}
		}
		
		/*
		if(!isCompatible(in, nodeMap)) {
			continue;
		}
		*/
					
		
		
		
		
		
		/*
		List<Map<Node, Node>> typeIso = new ArrayList<Map<Node, Node>>();
		
		for(Quad qA : qAs) {
			List<Node> nAs = quadToList(qA);
			
			// We need to establish a mapping for each element of qA
			
			for(Quad qB : qBs) {
				List<Node> nBs = quadToList(qB);
					
				Map<Node, Node> nodeMap = new HashMap<Node, Node>();
				for(int i = 0; i < 4; ++i) {
					Node nA = nAs.get(i);
					Node nB = nBs.get(i);
					
					assert nA.isVariable() == nB.isVariable() : "Should not happen, since the indexing by pattern type should prevent this case";
					
					
					if(nA.isVariable()) {
						Map<Node, Node> newEntry = Collections.singletonMap(nA, nB);		
						
						assert isCompatible(nodeMap, newEntry) : "Should not happen, since the indexing by pattern type should prevent this case";
						
						nodeMap.putAll(newEntry);
					} else {
						assert nA.equals(nB) : "Should not happen, since the indexing by pattern type should prevent this case";
					}
				}

				// Check if the node map is compatible to previous mappings
				if(!isCompatible(in, nodeMap)) {
					continue;
				}
					
				Map<Node, Node> union = new HashMap<Node, Node>(in);
				union.putAll(nodeMap);
				
				Map<Node, Node> result = findIsomorphy(union, order, index + 1, a, b);
				if(result != null) {
					return result;
				}
			}
		}
		*/
		
		// We now need to find a mapping between qA and qB that is compatible
		// with 
		return null;
	}

	public static <K, V> Set<Map<K, V>> getCompatibleCombinations(Iterable<Map<K, V>> as, Iterable<Map<K, V>> bs) {
		Set<Map<K, V>> result = new HashSet<Map<K, V>>();
		
		for(Map<K, V> a : as) {
			for(Map<K, V> b : bs) {
				if(MapUtils.isCompatible(a, b)) {
					Map<K, V> resultPart = new HashMap<K, V>(a);
					resultPart.putAll(b);
					
					result.add(resultPart);
				}
			}
		}
	
		return result;
	}
	
	/**
	 * Tries to figure out whether a variable is actually bound to a value, such
	 * as ?s = <http://s.org>
	 * 
	 * The motivation for such function is to capture cases such as: A: Select *
	 * { <http://s.org> ?p ?o . } B: Select * { ?s ?p ?o . Filter(?s =
	 * <http://s.org>) . }
	 * 
	 * However now that i think about it, the much better solution would be to
	 * rewrite all queries to use filters rather than literals. So that every
	 * query is rewritten to form B.
	 * 
	 * TODO Write such rewrite function
	 */
	public static void findEquivalences()
	{
		OpFilter x;
	}
}

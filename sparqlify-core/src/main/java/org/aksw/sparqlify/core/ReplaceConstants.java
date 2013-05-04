package org.aksw.sparqlify.core;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;


/**
 * Replaces constants with variables and filter conditions.
 * 
 * Example:
 * {?s a ?t .}
 * 
 * becomes:
 * 
 * {
 *     ?s ?var ?t .
 *     Filter(?var = rdf:type) .
 * }
 * 
 * 
 * @author raven
 *
 */
public class ReplaceConstants {
	private static Generator generator = Gensym.create("var", 0);
	 
	
	
	public static Triple listToTriple(List<Node> nodes) {
		return new Triple(nodes.get(0), nodes.get(1), nodes.get(2));
	}

	public static List<Node> tripleToList(Triple triple)
	{
		List<Node> result = new ArrayList<Node>();
		result.add(triple.getSubject());
		result.add(triple.getPredicate());
		result.add(triple.getObject());

		return result;
	}

	
	
	public static Op replace(Op op)
	{
		return MultiMethod.invokeStatic(ReplaceConstants.class, "_replace", op);
	}
	
		
	public static Node transform(Node node, Generator generator, ExprList filters) {
		if(node.isConcrete()) {
			Var var = Var.alloc(generator.next());			
			
			Expr condition = new E_Equals(new ExprVar(var), NodeValue.makeNode(node));
			filters.add(condition);
			
			return var;
		}
		
		return node;
	}

	public static Op _replace(OpGroup op) {
		return new OpGroup(replace(op.getSubOp()), op.getGroupVars(), op.getAggregators());
	}

	public static Op _replace(OpTable op) {
		return null;
	}
	
	public static Op _replace(OpExtend op) {
		return OpExtend.extend(replace(op.getSubOp()), op.getVarExprList());
	}

	public static Op _replace(OpAssign op) {
		Op newSubOp = replace(op.getSubOp());
		Op result = OpExtend.extend(newSubOp, op.getVarExprList());
		return result;
	}

	
	public static Op _replace(OpSlice op)
	{
		return new OpSlice(replace(op.getSubOp()), op.getStart(), op.getLength());
	}
	
	public static Op _replace(OpOrder op) {
		return new OpOrder(replace(op.getSubOp()), op.getConditions());
	}

	
	public static Op _replace(OpProject op) {
		return new OpProject(replace(op.getSubOp()), op.getVars());
	}

	public static Op _replace(OpTopN op) {
		return new OpTopN(replace(op.getSubOp()), op.getLimit(), op.getConditions());
	}
	
	public static Op _replace(OpDistinct op) {
		return new OpDistinct(replace(op.getSubOp()));
	}
	
	public static Op _replace(OpLeftJoin op) {
		return OpLeftJoin.create(replace(op.getLeft()), replace(op.getRight()), op.getExprs());
	}
	
	public static Op _replace(OpSequence op) {
		List<Op> members = op.getElements();
		
		List<Op> newMembers = new ArrayList<Op>(members.size());
		for(Op member : members) {
			Op newMember = replace(member);
			newMembers.add(newMember);
		}
				
		Op result = OpSequence.create().copy(newMembers);
		return result;
	}
	
	
	public static Op _replace(OpConditional op) {
		Op newLeft = replace(op.getLeft());
		Op newRight = replace(op.getRight());
		
		Op result = new OpConditional(newLeft, newRight);
		return result;
	}

	
	public static Op _replace(OpJoin op) {
		return OpJoin.create(replace(op.getLeft()), replace(op.getRight()));
	}
	
	public static Op _replace(OpUnion op) {
		return OpUnion.create(replace(op.getLeft()), replace(op.getRight()));
	}

	public static Op _replace(OpDisjunction op) {
		OpDisjunction result = OpDisjunction.create();
		for(Op member : op.getElements()) {
			Op newMember = replace(member);
			
			result.add(newMember);
		}
		
		return result;
	}
	
	public static Op _replace(OpFilter op) {
		return OpFilter.filter(op.getExprs(), replace(op.getSubOp()));
	}
	
	public static Op _replace(OpQuadPattern op) {
		
		ExprList filters = new ExprList();

		BasicPattern triples = new BasicPattern();
		
		Node graphNode = transform(op.getGraphNode(), generator, filters);
		
		
		List<Node> nodes = new ArrayList<Node>();
		for(Triple triple : op.getBasicPattern().getList()) {
			
 
			for(Node node : tripleToList(triple)) {
				Node n = transform(node, generator, filters);
				nodes.add(n);
			}
		
			Triple t = listToTriple(nodes);
			
			triples.add(t);
			
			nodes.clear();			
		}
		
		Op result = new OpQuadPattern(graphNode, triples); 
		
		if(!filters.isEmpty()) {
			result = OpFilter.filter(filters, result);
		}
		
		return result;
	}
	
}

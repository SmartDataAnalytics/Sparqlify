package org.aksw.sparqlify.core;

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
//public class ReplaceConstants {
//	private static Generator generator = Gensym.create("var", 0);
//
//
//
//	public static Triple listToTriple(List<Node> nodes) {
//		return new Triple(nodes.get(0), nodes.get(1), nodes.get(2));
//	}
//
//	public static Quad listToQuad(List<Node> nodes) {
//        return new Quad(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));
//    }
//
//	public static List<Node> tripleToList(Triple triple)
//	{
//		List<Node> result = new ArrayList<Node>();
//		result.add(triple.getSubject());
//		result.add(triple.getPredicate());
//		result.add(triple.getObject());
//
//		return result;
//	}
//
//
//
//	public static Op replace(Op op)
//	{
//		return MultiMethod.invokeStatic(ReplaceConstants.class, "_replace", op);
//	}
//
//	public static Node transform(Node node, Generator generator, ExprList filters) {
//		if(node.isConcrete()) {
//			Var var = Var.alloc(generator.next());
//
//			// Use of the constant Quad.defaultGraphNodeGenerated in the graph position results in a free variable.
//			//if(!(isGraphNode && node.equals(Quad.defaultGraphNodeGenerated))) {
//				Expr condition = new E_Equals(new ExprVar(var), NodeValue.makeNode(node));
//				filters.add(condition);
//			//}
//
//			return var;
//		}
//
//		return node;
//	}
//
//	public static Op _replace(OpGroup op) {
//		return new OpGroup(replace(op.getSubOp()), op.getGroupVars(), op.getAggregators());
//	}
//
//	public static Op _replace(OpTable op) {
//		return null;
//	}
//
//	public static Op _replace(OpExtend op) {
//		return OpExtend.extend(replace(op.getSubOp()), op.getVarExprList());
//	}
//
//	public static Op _replace(OpAssign op) {
//		Op newSubOp = replace(op.getSubOp());
//		Op result = OpExtend.extend(newSubOp, op.getVarExprList());
//		return result;
//	}
//
//
//	public static Op _replace(OpSlice op)
//	{
//		return new OpSlice(replace(op.getSubOp()), op.getStart(), op.getLength());
//	}
//
//	public static Op _replace(OpOrder op) {
//		return new OpOrder(replace(op.getSubOp()), op.getConditions());
//	}
//
//
//	public static Op _replace(OpProject op) {
//		return new OpProject(replace(op.getSubOp()), op.getVars());
//	}
//
//	public static Op _replace(OpTopN op) {
//		return new OpTopN(replace(op.getSubOp()), op.getLimit(), op.getConditions());
//	}
//
//	public static Op _replace(OpDistinct op) {
//		return new OpDistinct(replace(op.getSubOp()));
//	}
//
//	public static Op _replace(OpLeftJoin op) {
//		return OpLeftJoin.create(replace(op.getLeft()), replace(op.getRight()), op.getExprs());
//	}
//
//	public static Op _replace(OpSequence op) {
//		List<Op> members = op.getElements();
//
//		List<Op> newMembers = new ArrayList<Op>(members.size());
//		for(Op member : members) {
//			Op newMember = replace(member);
//			newMembers.add(newMember);
//		}
//
//		Op result = OpSequence.create().copy(newMembers);
//		return result;
//	}
//
//
//	public static Op _replace(OpConditional op) {
//		Op newLeft = replace(op.getLeft());
//		Op newRight = replace(op.getRight());
//
//		Op result = new OpConditional(newLeft, newRight);
//		return result;
//	}
//
//
//	public static Op _replace(OpJoin op) {
//		return OpJoin.create(replace(op.getLeft()), replace(op.getRight()));
//	}
//
//	public static Op _replace(OpUnion op) {
//		return OpUnion.create(replace(op.getLeft()), replace(op.getRight()));
//	}
//
//	public static Op _replace(OpDisjunction op) {
//		OpDisjunction result = OpDisjunction.create();
//		for(Op member : op.getElements()) {
//			Op newMember = replace(member);
//
//			result.add(newMember);
//		}
//
//		return result;
//	}
//
//	public static Op _replace(OpFilter op) {
//		return OpFilter.filter(op.getExprs(), replace(op.getSubOp()));
//	}
//
//	public static Op _replace(OpQuadPattern op) {
//
//		ExprList filters = new ExprList();
//
//		//BasicPattern triples = new BasicPattern();
//        QuadPattern quadPattern = new QuadPattern();
//
//		Node rawGraphNode = op.getGraphNode();
//
//		Node commonGraphNode = null;
//		if(rawGraphNode.isConcrete()) {
//		    // If the graph node is a concrete value - except for the default graph,
//		    // replace it with a variable that is constrained to that value
//		    if(!rawGraphNode.equals(Quad.defaultGraphNodeGenerated)) {
//		        commonGraphNode = transform(rawGraphNode, generator, filters);
//		    }
//		}
//		else {
//		    // If the graph node is a variable, use it.
//		    commonGraphNode = rawGraphNode;
//		}
//
//
//		List<Node> nodes = new ArrayList<Node>(4);
//		for(Triple triple : op.getBasicPattern().getList()) {
//
//            Node graphNode;
//            if(commonGraphNode != null) {
//                graphNode = commonGraphNode;
//            } else {
//                graphNode = Var.alloc(generator.next());
//            }
//            nodes.add(graphNode);
//
//
//			for(Node node : tripleToList(triple)) {
//
//				Node n = transform(node, generator, filters);
//				nodes.add(n);
//			}
//
//			//Triple t = listToTriple(nodes);
//
//			//triples.add(t);
//			Quad q = listToQuad(nodes);
//			quadPattern.add(q);
//			nodes.clear();
//		}
//
//		Op result = new OpQuadPattern2(quadPattern);
//
//		if(!filters.isEmpty()) {
//			result = OpFilter.filter(filters, result);
//		}
//
//		return result;
//	}
//
//}

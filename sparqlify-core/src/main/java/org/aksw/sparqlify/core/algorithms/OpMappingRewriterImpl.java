package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.views.OpViewInstanceJoin;
import org.aksw.jena_sparql_api.views.Ops;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.jena_sparql_api.views.ViewInstance;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.database.OpFilterIndexed;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.ExprList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OpMappingRewriterImpl
	implements OpMappingRewriter
{
	private static final Logger logger = LoggerFactory.getLogger(OpMappingRewriterImpl.class);
	
	private MappingOps ops;

	//private Map<Class<?>, OpMappingRewriter> map;
	
	/*
	public OpMappingRewriterImpl(DatatypeAssigner datatypeAssigner) {
		this.ops = new MappingOpsImpl();
	}
	*/
	/*
	public OpMappingRewriterMap(Map<Class<?>, OpMappingRewriter> map) {
		this.map = map;
	}
	*/
	

	public OpMappingRewriterImpl(MappingOps ops) {
		this.ops = ops;
	}
	
	public MappingOps getMappingOps() {
		return this.ops;
	}

	public Mapping rewrite(OpViewInstanceJoin op) {
		Collection<ViewInstance> vis = op.getJoin().getViewInstances();
		
		Mapping result = null;
		for(ViewInstance vi : vis) {

			Mapping tmp = ops.createMapping(vi);
			//System.out.println(vi + "\n    --> " + tmp);
			
			if(result == null) {				
				result = tmp;				
			} else {				
				result = ops.join(result, tmp);
			}
		}
		
		return result;
	}
	
	public List<Mapping> rewriteList(List<Op> ops) {
		List<Mapping> mappings = new ArrayList<Mapping>(ops.size());
		
		for(Op op : ops) {
			Mapping tmp = rewrite(op);
			mappings.add(tmp);	
		}
				
		return mappings;	
	}

	public Mapping rewrite(OpDisjunction op) {
		
		List<Op> elements = op.getElements();
		List<Mapping> mappings = rewriteList(elements);
		
		Mapping result = ops.union(mappings);
		return result;
	}

	public Mapping rewrite(OpSequence op) {
		List<Op> members = op.getElements();
		if(members.isEmpty()) {
			MappingOpsImpl.createEmptyMapping();
		}
		
		Mapping a = null;
		for(Op member : members) {
			Mapping b = rewrite(member);
			
			if(a == null) {
				a = b;
			} else {
				a = ops.join(a, b);
			}
		}
		
		return a;
	}

	public Mapping join(Op a, Op b) {
		Mapping ma = rewrite(a);
		Mapping mb = rewrite(b);
		
		Mapping result = ops.join(ma, mb);
		return result;		
	}
	
	public Mapping rewrite(OpJoin op) {
		Mapping result = join(op.getLeft(), op.getRight());
		return result;
	}
	
	public Mapping rewrite(OpLeftJoin op) {
		Mapping a = rewrite(op.getLeft());
		Mapping b = rewrite(op.getRight());
		ExprList exprs = op.getExprs();
		
		Mapping result = ops.leftJoin(a, b, exprs);
		return result;
	}
	
	public Mapping rewrite(OpConditional op) {
		OpLeftJoin tmp = (OpLeftJoin)OpLeftJoin.create(op.getLeft(), op.getRight(), new ExprList());
		
		Mapping result = rewrite(tmp);
		
		return result;
	}

	
	public Mapping rewrite(OpFilterIndexed op) {
		Mapping a = rewrite(op.getSubOp());
		
		Mapping result = ops.filter(a, op.getRestrictions().getExprs());
		return result;
	}
	
	public Mapping rewrite(OpSlice op) {
		Mapping a = rewrite(op.getSubOp());
		
		Long limit = (op.getLength() == Query.NOLIMIT) ? null : op.getLength();
		Long offset = (op.getStart() == Query.NOLIMIT) ? null : op.getStart();
		
		Mapping result = ops.slice(a, limit, offset);
		return result;
	}
	
	public Mapping rewrite(OpAssign op) {
		OpExtend tmp = (OpExtend)OpExtend.extend(op.getSubOp(), op.getVarExprList());
		
		Mapping result = rewrite(tmp);
		
		return result;
	}
	
	public Mapping rewrite(OpExtend op) {
		Mapping a = rewrite(op.getSubOp());

		logger.warn("OpExtend: We need to check whether term constructors must be injected");
		/*
		 * A thought on injecting term constructors:
		 *   Can we just rewrite the expression to SQL, and then make it a typed literal?
		 *   No, because the expression might actually compute a URI, so we can't just guess the type.
		 *   
		 * Maybe for now we could assume, that the expressions that go here are all sane (i.e. have the appropriate term ctors)
		 * 
		 */

		//Multimap<Var, RestrictedExpr> map = HashMultimap.create(a.getVarDefinition().getMap());
				
				
		VarDefinition varDef = VarDefinition.create(op.getVarExprList());
		Mapping result = ops.extend(a, varDef);

		
		return result;
	}
	
	public Mapping rewrite(OpProject op) {
		Mapping a = rewrite(op.getSubOp());
		
		Mapping result = ops.project(a, op.getVars());
		
		return result;
	}

	public Mapping rewrite(OpDistinct op) {
		Mapping a = rewrite(op.getSubOp());
		
		Mapping result = ops.distinct(a);
		
		return result;
	}

	public Mapping rewrite(OpOrder op) {
		// If the op is a union, we try to sort the members based on their prefix
		// constraints
		
		
		Mapping a = rewrite(op.getSubOp());
		
		List<SortCondition> sortConditions = op.getConditions();
		Mapping result = ops.order(a, sortConditions);
		
		return result;
	}

	
	/**
	 * The aggregators need to be wrapped with an appropriate term ctor.
	 * E.g. count(*) -> typedLiteral(count(*), xsd:long)
	 * 
	 * @param op
	 * @return
	 */
	public Mapping rewrite(OpGroup op) {
		
		Mapping a = rewrite(op.getSubOp());
		
		Mapping result = ops.groupBy(a, op.getGroupVars(), op.getAggregators());
		
		return result;
	}
	
	/*
	public Mapping rewrite(OpNull op) {
		Mapping a = new Mapping(SqlOpEmpty.create(schema));
	}
	*/

	public Mapping rewrite(OpTopN op) {		
		// We convert this 'back' into an order by followed by limit
		Op sub = op.getSubOp();
		OpOrder inner = new OpOrder(sub, op.getConditions());
		OpSlice outer = new OpSlice(inner, 0, op.getLimit());
		
		Mapping result = rewrite(outer);
		
		return result;
	}

	public Mapping rewrite(OpMapping op) {
		Mapping result = op.getMapping();
		return result;
	}
	
	@Override
	public Mapping rewrite(Op op) {

		Mapping result;
		
		Ops type = Ops.valueOf(op.getClass().getSimpleName());
		
		switch(type) {

		case OpOrder:
			result = rewrite((OpOrder)op);
			break;
			
		case OpDistinct:
			result = rewrite((OpDistinct)op);
			break;

		case OpFilter:
			result = rewrite((OpFilter)op);
			break;

		case OpGroup:
			result = rewrite((OpGroup)op);
			break;
			
		case OpJoin:
			result = rewrite((OpJoin)op);
			break;

		case OpLeftJoin:
			result = rewrite((OpLeftJoin)op);
			break;

		case OpExtend:
			result = rewrite((OpExtend)op);
			break;			
			
		case OpQuadPattern:
			result = rewrite((OpQuadPattern)op);
			break;			
		
		case OpSlice:
			result = rewrite((OpSlice)op);
			break;
			
		case OpProject:
			result = rewrite((OpProject)op);
			break;
			
		case OpFilterIndexed:
			result = rewrite((OpFilterIndexed)op);
			break;
			
		case OpMapping:
			result = rewrite((OpMapping)op);
			break;

		case OpUnion:
			result = rewrite((OpUnion)op);
			break;
			
		case OpDisjunction:
			result = rewrite((OpDisjunction)op);
			break;

		case OpViewInstanceJoin:
			result = rewrite((OpViewInstanceJoin)op);
			break;
			
		default:
			throw new RuntimeException("Unknown op type: " + op.getClass());
		}
		
		return result;
		
		
//		if(op instanceof OpViewInstanceJoin) {
//			result = rewrite((OpViewInstanceJoin)op);
//		}
//		else if (op instanceof OpMapping) {
//			result = ((OpMapping)op).getMapping();
//		}
//		else if (op instanceof OpDisjunction) {
//			result = rewrite((OpDisjunction)op);
//		} 
//		else if (op instanceof OpFilterIndexed) {
//			result = rewrite((OpFilterIndexed)op);
//		}
//		else if (op instanceof OpProject) {
//			result = rewrite((OpProject)op);
//		}
//		else if (op instanceof OpJoin) {
//			result = rewrite((OpJoin)op);
//		}
//		else if (op instanceof OpLeftJoin) {
//			result = rewrite((OpLeftJoin)op);
//		}
//		else if (op instanceof OpSequence) {
//			result = rewrite((OpSequence)op);
//		}
//		else if (op instanceof OpConditional) {
//			result = rewrite((OpConditional)op);
//		}		
//		else if (op instanceof OpSlice) {
//			result = rewrite((OpSlice)op);
//		}
//		else if (op instanceof OpDistinct) {
//			result = rewrite((OpDistinct)op);
//		}
//		else if (op instanceof OpGroup) {
//			result = rewrite((OpGroup)op);
//		}
//		else if (op instanceof OpExtend) {
//			result = rewrite((OpExtend)op);
//		}
//		else if (op instanceof OpAssign) {
//			result = rewrite((OpAssign)op);
//		}
//		else if (op instanceof OpOrder) {
//			result = rewrite((OpOrder)op);
//		}
//		else if(op instanceof OpTopN) {
//			OpTopN o = (OpTopN)op;
//			
//			// We convert this 'back' into an order by followed by limit
//			Op sub = o.getSubOp();
//			OpOrder inner = new OpOrder(sub, o.getConditions());
//			OpSlice outer = new OpSlice(inner, 0, o.getLimit());
//			
//			result = rewrite(outer);
//			
////			return result;
//			
//		}
//		/*
//		else if(op instanceof OpNull) {
//			result = rewrite((OpNull) op);
//		}*/
//		else {
//			throw new RuntimeException("Unhandled op type: " + op.getClass() + "; " + op);
//		}
//		
//		return result;
	}

	
	/*
	public Map<Class<?>, OpMappingRewriter> createDefaultMap() {
		
		Map<Class<?>, OpMappingRewriter> map = new HashMap<Class<?>, OpMappingRewriter>();
		
		
	}*/
	
}
package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.core.domain.Mapping;
import org.aksw.sparqlify.core.domain.ViewInstance;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.database.OpFilterIndexed;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;


public class OpMappingRewriterImpl
	implements OpMappingRewriter
{
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

	public Mapping rewrite(OpViewInstanceJoin op) {
		List<ViewInstance> vis = op.getJoin().getViewInstances();
		
		Mapping result = null;
		for(ViewInstance vi : vis) {

			Mapping tmp = ops.createMapping(vi);
			System.out.println(vi + "\n    --> " + tmp);
			
			if(result == null) {				
				result = tmp;				
			} else {				
				result = ops.join(result, tmp);
			}
		}
		
		return result;
	}
	

	public Mapping rewrite(OpDisjunction op) {
		List<Mapping> mappings = new ArrayList<Mapping>(op.size());
		
		for(Op member : op.getElements()) {
			
			Mapping tmp = rewrite(member);
			mappings.add(tmp);
			
		}
		
		
		Mapping result = ops.union(mappings);
		return result;
	}


	public Mapping rewrite(OpJoin op) {
		Mapping a = rewrite(op.getLeft());
		Mapping b = rewrite(op.getRight());
		
		Mapping result = ops.join(a, b);
		return result;
	}

	public Mapping rewrite(OpLeftJoin op) {
		Mapping a = rewrite(op.getLeft());
		Mapping b = rewrite(op.getRight());
		
		Mapping result = ops.leftJoin(a, b);
		return result;
	}

	
	public Mapping rewrite(OpFilterIndexed op) {
		Mapping a = rewrite(op.getSubOp());
		
		Mapping result = ops.select(a, op.getRestrictions().getExprs());
		return result;
	}
	
	public Mapping rewrite(OpSlice op) {
		Mapping a = rewrite(op.getSubOp());
		
		Long limit = (op.getLength() == Query.NOLIMIT) ? null : op.getLength();
		Long offset = (op.getStart() == Query.NOLIMIT) ? null : op.getStart();
		
		Mapping result = ops.slice(a, limit, offset);
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

	
	@Override
	public Mapping rewrite(Op op) {

		Mapping result;
		if(op instanceof OpViewInstanceJoin) {
			result = rewrite((OpViewInstanceJoin)op);
		} else if (op instanceof OpDisjunction) {
			result = rewrite((OpDisjunction)op);
		} else if (op instanceof OpFilterIndexed) {
			result = rewrite((OpFilterIndexed)op);
		} else if (op instanceof OpProject) {
			result = rewrite((OpProject)op);
		} else if (op instanceof OpJoin) {
			result = rewrite((OpJoin)op);
		} else if (op instanceof OpLeftJoin) {
			result = rewrite((OpLeftJoin)op);
		} else if (op instanceof OpSlice) {
			result = rewrite((OpSlice)op);
		} else if (op instanceof OpDistinct) {
			result = rewrite((OpDistinct)op);
		} else {
			throw new RuntimeException("Unhandled op type: " + op.getClass() + "; " + op);
		}
		
		return result;
	}

	
	/*
	public Map<Class<?>, OpMappingRewriter> createDefaultMap() {
		
		Map<Class<?>, OpMappingRewriter> map = new HashMap<Class<?>, OpMappingRewriter>();
		
		
	}*/
	
}
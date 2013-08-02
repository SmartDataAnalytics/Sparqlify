package org.aksw.sparqlify.algebra.sparql.domain;

import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpSqlBridge
	extends Op0
{
	private SqlNodeOld sqlNode;
	
	public OpSqlBridge(SqlNodeOld sqlNode) {
		this.sqlNode = sqlNode;
	}
	
	public SqlNodeOld getSqlNode()
	{
		return sqlNode;
	}

	
	@Override
	public void visit(OpVisitor opVisitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Op apply(Transform transform) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Op0 copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
		// TODO Auto-generated method stub
		return false;
	}

}

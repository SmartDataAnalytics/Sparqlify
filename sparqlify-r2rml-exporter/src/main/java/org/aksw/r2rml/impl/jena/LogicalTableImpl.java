/**
 * 
 */
package org.aksw.r2rml.impl.jena;

import org.aksw.r2rml.api.LogicalTable;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;


public class LogicalTableImpl
	extends AbstractR2rmlResourceImpl
	implements LogicalTable
{
	public LogicalTableImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	@Override
	public String getTableName() {
		String result = getLiteralValue(this, RR.tableName, Literal::getString).orElse(null);
		
		return result;
	}
	
	@Override
	public LogicalTableImpl setTableName(String tableName) {
		removeAll(RR.tableName);
		addLiteral(RR.tableName, tableName);
		return this;
	}

	@Override
	public String getSqlQuery() {
		String result = getLiteralValue(this, RR.sqlQuery, Literal::getString).orElse(null);
		
		return result;
	}
	
	@Override
	public LogicalTableImpl setSqlQuery(String sqlQuery) {
		removeAll(RR.sqlQuery);
		addLiteral(RR.sqlQuery, sqlQuery);
		return this;
	}

}
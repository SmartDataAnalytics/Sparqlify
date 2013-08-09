package org.aksw.sparqlify.core.sparql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.aksw.sparqlify.core.MakeNodeValue;
import org.springframework.jdbc.core.RowMapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class RowMapperSparqlifyBinding
	implements RowMapper<Binding>
{
	//private long nextRowId;
	private Var rowIdVar;

	public RowMapperSparqlifyBinding()
	{
		this("rowId");
	}


	public RowMapperSparqlifyBinding(String rowIdName)
	{
		this.rowIdVar = rowIdName == null ? null : Var.alloc(rowIdName);
 	}


	@Override
	public Binding mapRow(ResultSet rs, int rowId) {
		Binding result = _map(rs, rowId, rowIdVar);
		return result;
	}
	
	public static Binding _map(ResultSet rs, long rowId, Var rowIdVar) {
		Binding result;
		try {
			result = map(rs, rowId, rowIdVar);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;		
	}
	
	public static Binding map(ResultSet rs, long rowId, Var rowIdVar) throws SQLException {

		// OPTIMIZE refactor these to attributes
		//NodeExprSubstitutor substitutor = new NodeExprSubstitutor(sparqlVarMap);
		BindingMap binding = new BindingHashMap();

		
		ResultSetMetaData meta = rs.getMetaData();
		
		/*
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			binding.add(Var.alloc("" + i), node)
		}*/	

		
		// Substitute the variables in the expressions
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			String colName = meta.getColumnLabel(i);
			Object colValue = rs.getObject(i);

			NodeValue nodeValue;

			// NOTE Char right padding is handled as a special expression (similar to urlEncode)
//			String colType = meta.getColumnTypeName(i);
//			
//			//System.out.println(colValue == null ? "null" : colValue.getClass());
//			
//			// TODO: Make datatype serialization configurable
//			if(isCharType(colType)) {
//				if(colValue == null) {
//					nodeValue = null;
//				} else {
//					int displaySize = meta.getPrecision(i);
//					int scale = meta.getScale(i);
//					String tmp = "" + colValue;
//					String v = StringUtils.rightPad(tmp, displaySize);
//					nodeValue = NodeValue.makeString(v);
//				}
//			}
//			else
			if(colValue instanceof Date) {
				String tmp = colValue.toString();
				nodeValue = NodeValue.makeDate(tmp); 
			}
			else if(colValue instanceof Timestamp) {
				String tmp = colValue.toString();
				String val = tmp.replace(' ', 'T');
				nodeValue = NodeValue.makeDateTime(val);
			} else {
				nodeValue = MakeNodeValue.makeNodeValue(colValue);
			}
			
			if(nodeValue == null) {
				continue;
			}
			
//			if(nodeValue.isDateTime()) {
//				XSDDateTime val = nodeValue.getDateTime();
//				String str = val.timeLexicalForm();
//				String b = val.toString();
//
//				System.out.println("foo");
//			}
			
			Node node = nodeValue.asNode();
			
			
			// FIXME We also add bindings that enable us to reference the columns by their index
			// However, indexes and column-names are in the same namespace here, so there might be clashes
			Var indexVar = Var.alloc("" + i);
			binding.add(indexVar, node);
			
			Var colVar = Var.alloc(colName);
			if(!binding.contains(colVar)) {
				binding.add(colVar, node);
			}
		}
		

		
		// Additional "virtual" columns
		// FIXME Ideally this should be part of a class "ResultSetExtend" that extends a result set with additional columns
		if(rowIdVar != null) {
			Node node = NodeValue.makeInteger(rowId).asNode();
			
			binding.add(rowIdVar, node);
		}
		
		return binding;
	}
}
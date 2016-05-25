package org.aksw.sparqlify.core.sparql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Multimap;

public class RowMapperSparqlifyCombined
	implements RowMapper<Binding>
{
	private Var rowIdVar;
	private Multimap<Var, RestrictedExpr> sparqlVarMap;

	public RowMapperSparqlifyCombined(Multimap<Var, RestrictedExpr> sparqlVarMap) {
		this(sparqlVarMap, (Var)null);
	}
	
	public RowMapperSparqlifyCombined(Multimap<Var, RestrictedExpr> sparqlVarMap, String rowIdName) {
		this(sparqlVarMap, rowIdName == null ? null : Var.alloc(rowIdName));
	}
	
	public RowMapperSparqlifyCombined(Multimap<Var, RestrictedExpr> sparqlVarMap, Var rowIdVar) {
		this.sparqlVarMap = sparqlVarMap;
		this.rowIdVar = rowIdVar;
	}
	
	@Override
	public Binding mapRow(ResultSet rs, int rowNum) throws SQLException {
		Binding tmp = RowMapperSparqlifyBinding.map(rs, rowNum, rowIdVar);
		Binding result = ItemProcessorSparqlify.process(sparqlVarMap, tmp);
		
		return result;
	}
	
}
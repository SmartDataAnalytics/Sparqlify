package org.aksw.sparqlify.batch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.TemplateLib;

public class RowMapperSparqlify
	implements RowMapper<QuadPattern>
{
	private RowMapper<Binding> rowMapper;
	private QuadPattern quadPattern;
	
	public RowMapperSparqlify(RowMapper<Binding> rowMapper, QuadPattern quadPattern) {
		this.rowMapper = rowMapper;
		this.quadPattern = quadPattern;
	}
	
	//public 
	
	@Override
	public QuadPattern mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Binding binding = rowMapper.mapRow(rs, rowNumber);
		
		QuadPattern result = map(quadPattern, binding);
		return result;
	}
	
	public static QuadPattern map(QuadPattern quadPattern, Binding binding) {
		Map<Node, Node> bNodeMap = Collections.emptyMap();
		
		QuadPattern result = new QuadPattern();
		
		for(Quad quad : quadPattern.getList()) {
			Quad tmp = TemplateLib.subst(quad, binding, bNodeMap);
			result.add(tmp);
		}
		
		return result;
		
	}
}
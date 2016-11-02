package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Multimap;

public class IteratorResultSetSparqlifyBinding
	extends SinglePrefetchIterator<Binding>
{
	private static final Logger logger = LoggerFactory.getLogger(IteratorResultSetSparqlifyBinding.class);

	// Canonicalize values, e.g. 20.0 -> 2.0e1
	//private static CanonicalizeLiteral canonicalizer = CanonicalizeLiteral.get();

	private Connection conn;
	private ResultSet rs;
	//private NodeExprSubstitutor substitutor;// = new NodeExprSubstitutor(sparqlVarMap);
	//private Multimap<Var, RestrictedExpr> sparqlVarMap;

	//private transient ResultSetMetaData meta;
	//private Var rowIdVar;
	private RowMapper<Binding> rowMapper;

	private long nextRowId;

	public IteratorResultSetSparqlifyBinding(Connection conn, ResultSet rs, Multimap<Var, RestrictedExpr> sparqlVarMap)
	{
		this(conn, rs, sparqlVarMap, 0, null);
	}


	public IteratorResultSetSparqlifyBinding(Connection conn, ResultSet rs, Multimap<Var, RestrictedExpr> sparqlVarMap, long nextRowId, String rowIdName)
	{
		this(conn, rs, nextRowId, new RowMapperSparqlifyCombined(sparqlVarMap, rowIdName));
//		this.conn = conn;
//		this.rs = rs;
//		//this.sparqlVarMap = sparqlVarMap;
//		this.nextRowId = nextRowId;
//
//		this.rowMapper = new RowMapperSparqlifyBinding(sparqlVarMap);
//
//		//this.rowIdVar = rowIdName == null ? null : Var.alloc(rowIdName);
//
//		try {
//			this.meta = rs.getMetaData();
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
	}

	public IteratorResultSetSparqlifyBinding(Connection conn, ResultSet rs, long nextRowId, RowMapper<Binding> rowMapper) {
		this.conn = conn;
		this.rs = rs;
		this.nextRowId = nextRowId;
		this.rowMapper = rowMapper;
	}


//	public IteratorResultSetSparqlifyBinding(ResultSet rs, Multimap<Var, RestrictedExpr> sparqlVarMap)
//	{
//		this.rs = rs;
//		this.sparqlVarMap = sparqlVarMap;
//
////		ResultSetMetaData meta;
////		try {
////			rs.next();
////			System.out.println("h__4: " + rs.getObject("h__4"));
////			System.out.println("AGE: " + rs.getObject("AGE"));
////
////
////			meta = rs.getMetaData();
////			for(int i = 1; i <= meta.getColumnCount(); ++i) {
////				String colName = meta.getColumnName(i);
////
////				System.out.println("Column [" + i + "]: " + colName);
////			}
////		} catch (SQLException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//
//	}

//	public static Binding fetchBinding(ResultSetMetaData meta, ResultSet rs, Multimap<Var, RestrictedExpr> sparqlVarMap) {
//
//	}

	@Override
	protected Binding prefetch() throws Exception {
		if(!rs.next()) {
			return super.finish();
		}

		long rowId = nextRowId++;

		Binding result = rowMapper.mapRow(rs, (int)rowId);

		return result;
	}


	@Override
	public void close()
	{
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
                logger.warn("Something went wrong", e);
			}
		}

//		if(conn != null) {
//			try {
//				conn.commit();
//			} catch (SQLException e) {
//			    logger.warn("Something went wrong", e);
//			}
//		}
	}
}
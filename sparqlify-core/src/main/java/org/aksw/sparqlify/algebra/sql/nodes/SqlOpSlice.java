package org.aksw.sparqlify.algebra.sql.nodes;


public class SqlOpSlice
	extends SqlOpBase1
{
	private Long offset;
	private Long limit;
	
	public SqlOpSlice(Schema schema, SqlOp subOp, Long offset, Long limit) {
		super(schema, subOp);
		this.offset = offset;
		this.limit = limit;
	}
	
	
	public Long getOffset() {
		return offset;
	}


	public Long getLimit() {
		return limit;
	}


	public static SqlOpSlice create(SqlOp subOp, Long offset, Long limit) {
		SqlOpSlice result = new SqlOpSlice(subOp.getSchema(), subOp, offset, limit);
		
		return result;
	}

}

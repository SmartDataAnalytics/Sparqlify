package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlSlice
	extends SqlNodeBase1
{
    private long start ;
    private long length ;

    public SqlSlice(String aliasName, SqlNodeOld subNode, long start, long length)
    {
		// XXX WAS NULL
        super(aliasName, subNode);
        this.start = start ;
        this.length = length ;
    }

    public SqlSlice(SqlNodeOld subNode, long start, long length)
    {
		// XXX WAS NULL
        super(subNode.getAliasName(), subNode);
        this.start = start ;
        this.length = length ;
    }
    
    public long getLength()         { return length ; }
    public long getStart()          { return start ; }

	@Override
	SqlNodeOld copy1(SqlNodeOld subNode) {
		return new SqlSlice(this.getSubNode(), start, length);
	}
}

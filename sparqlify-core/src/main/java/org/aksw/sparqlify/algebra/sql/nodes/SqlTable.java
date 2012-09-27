package org.aksw.sparqlify.algebra.sql.nodes;



public class SqlTable
	extends SqlNodeBase0
{
    private String tableName ;
    
    public SqlTable(String tableName)
    {
        this(tableName, tableName);
    }

    public SqlTable(String aliasName, String tableName)
    {
    	super(aliasName);
    	this.tableName = tableName;
    	//, new ScopeBase(), new ScopeBase()
        //this(aliasName, tableName) ;
    }

    public String getTableName() {
    	return tableName;
    }
    
    // ScopeBase idScope, ScopeBase nodeScope
    /*
    private SqlTable(String aliasName, String tableName)
    {
        super(aliasName) ;
        this.tableName = tableName ;
    }*/

	@Override
	SqlNodeOld copy0() {
		return new SqlTable(tableName);
	}
}

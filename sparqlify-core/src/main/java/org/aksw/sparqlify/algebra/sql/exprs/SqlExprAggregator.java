package org.aksw.sparqlify.algebra.sql.exprs;



public class SqlExprAggregator
	extends SqlExpr0
{
    protected SqlAggregator aggregator ;
    protected String columnName; //Var var ;
    //protected ExprVar exprVar = null ;
    
    /**
     * NOTE The datatype of the aggregator must be fixed
     * 
     * @param columnName
     * @param aggregator
     */
    public SqlExprAggregator(String columnName, SqlAggregator aggregator) { 
    	super(aggregator.getDatatype());
    	this.columnName = columnName;
    	this.aggregator = aggregator;
    }
    
    public SqlAggregator getAggregator() {
    	return this.aggregator;
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aggregator == null) ? 0 : aggregator.hashCode());
		result = prime * result
				+ ((columnName == null) ? 0 : columnName.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SqlExprAggregator other = (SqlExprAggregator) obj;
		if (aggregator == null) {
			if (other.aggregator != null)
				return false;
		} else if (!aggregator.equals(other.aggregator))
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return aggregator + " AS " + columnName;
	}    
    
}

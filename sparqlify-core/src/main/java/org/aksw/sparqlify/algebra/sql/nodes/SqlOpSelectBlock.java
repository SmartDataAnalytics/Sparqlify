package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.ScopeBase;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;



/**
 * Similar to SqlSelectBlock:
 * 
 * The sql generation process is to generate SqlSelectBlock from the
 * algebra, which are then turned into strings.
 * 
 * 
 * TODO: What is the difference between schema and projection???
 * It seems as if I used schema for the subOp, whereas projection is a collection of changes to it
 * If this is the case, the concept is flawed:
 * The schema should be the one of this op's result set.
 * 
 * @author raven
 *
 *
 */
public class SqlOpSelectBlock
	extends SqlOpBase1
{
    // TODO This is a private constant from Jena SqlSelectBlock
    public static final Long NOT_SET = null;

    private boolean isLocked = false;
    
    public void setLock(boolean isLocked) {
    	this.isLocked = isLocked;
    }
    
    public boolean isLocked() {
    	return isLocked;
    }
    
    public void setSchema(Schema schema) {
    	this.schema = schema;
    }
    
    private String aliasName;
    
    
    
    public SqlOpSelectBlock(SqlOp node) {
    	this(node.getSchema(), node);
    	
    	/*
    	this.getAliasToColumn().putAll(node.getAliasToColumn());
    	this.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
    	*/
    }
    
    
	public SqlOpSelectBlock(Schema schema, SqlOp sqlOp) {
		super(schema, sqlOp);
	}

	public SqlOpSelectBlock(Schema schema, SqlOp sqlOp, String aliasName) {
		super(schema, sqlOp);
		this.aliasName = aliasName;
	}

	
	public void setAliasName(String aliasName)
	{
		this.aliasName = aliasName;
	}
	
	public String getAliasName() {
		return this.aliasName;
	}
	
	/*
	@Override
	public String getAliasName()
	{
		return subNode.getAliasName();
	}*/
	
	public void setSubOp(SqlOp subOp) {
		if(this.subOp != null) {
			throw new RuntimeException("Child already set");
		}
		this.subOp = subOp;
	}
	
	
	// Projection
	//private List<ColAlias> cols = new ArrayList<ColAlias>() ;
	// NOTE: For now we assume that the projection as ALWAYS rewritable to SQL
	// TODO This map should actually be Map<SqlColumn, SqlExpr>
	//private Map<Var, Expr> projection = new HashMap<Var, Expr>();

	
	// This is a hack: acutally a "computed projection" should simply be a rewrite
	// of the original expression - but for now it isn't
	/*
	private Map<Var, SqlExpr> computedProjection = new HashMap<Var, SqlExpr>();
	
	public Map<Var, SqlExpr> getComputedProjection() {
		return computedProjection;
	}*/
	

	// The projection
    //private Map<String, List<ColAlias> cols = new ArrayList<ColAlias>() ;
	private Projection projection = new Projection();
	
    
    public Projection getProjection() {
    	return projection;
    }
    
	// The order of the projection
	//private List<Var> order;
	
	// Selection
	private List<SqlExpr> conditions = new ArrayList<SqlExpr>() ;
    
	// Slicing
	private Long offset = null;
    private Long limit = null;
    private boolean distinct = false ;
    
    
    //private SqlNode join;
    //private SqlTable vTable ;           // Naming base for renamed columns
    
    //private List<SqlExprAggregator> aggregators = new ArrayList<SqlExprAggregator>();

    
    private List<SqlExpr> groupByExprs = new ArrayList<SqlExpr>();
    
    
    
    private List<SqlSortCondition> sortConditions = new ArrayList<SqlSortCondition>();

    /*
	public Map<Var, Expr> getProjection() {
		return projection;
	}
	*/

	public List<SqlExpr> getConditions() {
		return conditions;
	}

	public Long getOffset() {
		return offset;
	}

	public Long getLimit() {
		return limit;
	}

	public boolean isDistinct() {
		return distinct;
	}


	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public List<SqlExpr> getGroupByExprs() {
		return groupByExprs;
	}
	
	public List<SqlSortCondition> getSortConditions() {
		return sortConditions;
	}
	
	
	//@Override
	public SqlOpSelectBlock copy1(SqlOp subOp) {
		return SqlOpSelectBlock.create(subOp, this.getAliasName());
	}

    private SqlTable vTable ;           // Naming base for renamed columns

    // Calculate renames
    // Map all vars in the scope to names in the rename.
    /*
    private void merge(Scope scope, ScopeBase newScope, Generator gen)
    {
        String x = "" ;
        String sep = "" ;
    
        
        for ( ScopeEntry e : scope.findScopes() )
        {
            SqlColumn oldCol = e.getColumn() ;
            Var v = e.getVar() ;
            String colName = gen.next() ;
            SqlColumn newCol = new SqlColumn(vTable, colName) ;
            /*
            this.add(new ColAlias(oldCol, newCol)) ;
            * /
            newScope.setColumnForVar(v, newCol) ;
            // Annotations
            x = String.format("%s%s%s:(%s=>%s)", x, sep, v, oldCol, newCol) ;
            sep = " " ;
        }
        
        /*
        if ( x.length() > 0 ) {
            addNote(x) ;
        }
        * /
    }
*/

    //public static final Long NOT_SET = SqlBlock.NOT_SET;

    static public SqlOp distinct(Generator generator, SqlOp sqlOp)
    { 
        SqlOpSelectBlock block = blockWithView(generator, sqlOp) ;
        block.setDistinct(true) ;
        return block ;
    }
    
    /*
    static public SqlNode project(Generator generator, SqlNode sqlOp)
    { return project(generator, sqlOp, (ColAlias)null) ; }
    */

    
    /*
    static public SqlSelectBlock project(Generator generator, SqlNode sqlOp, Map<Var, Expr> cols)
    {
        // If already a view, not via a project, - think harder
        
        SqlSelectBlock block = blockNoView(generator, sqlOp) ;
        /*
        if ( block.idScope != null || block.nodeScope != null )
            System.err.println("SqlSelectBlock.project : already a view") ; 
        
        if ( cols != null )
            block.addAll(cols) ;
        * /
        
        block.getProjection().putAll(cols);
        
        return block ;
    }
    */
    
    /*
    static public SqlNode project(SDBRequest request, SqlNode sqlOp, ColAlias col)
    {
        SqlSelectBlock block = blockNoView(request, sqlOp) ;
        if ( col != null )
            block.add(col) ;
        return block ;
    }*/


    public static SqlOp restrict(Generator generator, SqlOp sqlOp, List<SqlExpr> exprs)
    {
        if ( exprs.size() == 0 )
            return sqlOp ;
        
        // Single table does not need renaming of columns 
        SqlOpSelectBlock block = (sqlOp instanceof SqlOpTable) ? blockPlain(generator, sqlOp) : blockWithView(generator, sqlOp) ;
        block.getConditions().addAll(exprs) ;
        return block ;
    }
  
        
    private static SqlOpSelectBlock _create(SqlOp sqlOp, Generator generator)
    {
        String alias = getAliasName(sqlOp);
        //if ( ! sqlOp.isTable() )
        alias = generator.next() ;
        SqlOpSelectBlock block = SqlOpSelectBlock.create(sqlOp, alias);
        //addNotes(block, sqlOp) ;
        return block ;
    }
    
    
    /*
    public static SqlBlock project(SqlNode node, Generator generator) {
    	SqlBlock block = blockWithView(generator, node);
    	block.getProjection().putAll(node.getSparqlVarToExpr());
    	
    	return block;
    }*/
            
    public static void order(Generator generator, SqlOpSelectBlock block, List<SqlSortCondition> conditions)
    {
    	block.getSortConditions().addAll(conditions);
    }
    
    public static void distinct(Generator generator, SqlOpSelectBlock block) {
    	block.setDistinct(true);
    }

    
    public static void slice(SqlOpSelectBlock block, Long start, Long length)
    {
        //SqlSelectBlock block = blockWithView(generator, sqlOp) ;
        
        if (start != null && start >= 0 )
        {
            if (block.getOffset() != null &&  block.getOffset() > 0 )
                start = start + block.getOffset() ;
            block.setOffset(start) ;
        }
        
        if (length != null && length >= 0 )
        {
            if (block.getLimit() != null && block.getLimit() >= 0 )
                length = Math.min(length, block.getLimit()) ;
            block.setLimit(length) ;
        }
        //return block ;
    }

    
    public static SqlOpSelectBlock create() {
    	return new SqlOpSelectBlock(null, null);
    }
    
    public static SqlOpSelectBlock create(SqlOp sqlOp) {
    	SqlOpSelectBlock result = create(sqlOp, null);
    	
    	
    	return result;
    }

    public static SqlOpSelectBlock create(SqlOp sqlOp, String aliasName) {
    	SqlOpSelectBlock result = new SqlOpSelectBlock(sqlOp.getSchema(), sqlOp, aliasName); //SqlOpSelectBlock.create(sqlOp, aliasName);
    	
    	return result;
    }
    
    public static String getAliasName(SqlOp op) {
    	if(op instanceof SqlOpTable) {
    		return ((SqlOpTable) op).getAliasName();
    	} else if (op instanceof SqlOpQuery) {
    		return ((SqlOpQuery) op).getAliasName();
    	} else if(op instanceof SqlOpSelectBlock) {
    		return ((SqlOpSelectBlock) op).getAliasName();
    	} else if(op instanceof SqlOpUnionN) {
    		return ((SqlOpUnionN) op).getAliasName();
    	}
    	
    	return null;
    }
    
    private static SqlOpSelectBlock blockPlain(Generator generator, SqlOp sqlOp)
    {
        if ( sqlOp instanceof SqlOpSelectBlock )
            return (SqlOpSelectBlock)sqlOp ;
        // Same alias (typically, sqlOp is a table or view and this is the table name) 
        SqlOpSelectBlock block = SqlOpSelectBlock.create(sqlOp, getAliasName(sqlOp)) ;
        //addNotes(block, sqlOp) ;
        return block ;
    }

    private static SqlOpSelectBlock blockWithView(Generator generator, SqlOp sqlOp)
    {
        if (sqlOp instanceof SqlOpSelectBlock )
        {
            SqlOpSelectBlock block = (SqlOpSelectBlock)sqlOp ;
            if(block.getSchema().getColumnCount() == 0)  //block.getAliasToColumn().size() == 0 )
            {
                // Didn't have a column view - force it
                calcView(block) ;
            }
            
            return (SqlOpSelectBlock)sqlOp ;
        }

        SqlOpSelectBlock blk = _create(sqlOp, generator) ;
        if(blk.getSchema().getColumnCount() != 0 )
            throw new SDBInternalError("Can't set a view on Select block which is already had columns set") ; 
        
        calcView(blk) ;
        return blk ;
    }
    
    private static SqlOpSelectBlock blockNoView(Generator generator, SqlOp sqlOp)
    {
        if ( sqlOp instanceof SqlOpSelectBlock ) {
            return (SqlOpSelectBlock)sqlOp ;
        }
        
        return _create(sqlOp, generator) ;
    }

    static private void calcView(SqlOpSelectBlock block)
    {
        SqlOp sqlOp = block.getSubOp() ;
        ScopeBase idScopeRename = new ScopeBase() ;
        ScopeBase nodeScopeRename = new ScopeBase() ;
        Generator gen = Gensym.create("X") ;    // Column names.  Not global.
    
        /*
        block.merge(sqlOp.getIdScope(), idScopeRename, gen) ;
        block.merge(sqlOp.getNodeScope(), nodeScopeRename, gen) ;
    
        block.nodeScope = nodeScopeRename ;
        block.idScope = idScopeRename ;
        */
    }

    
	@Override
	public void write(IndentedWriter writer) {
		String aliasPart = aliasName == null ? " anonymous" : " AS " + aliasName; 
		
		String schemaStr = schema == null ? "null schema" : "" + schema.getColumnNames();
		
		writer.println("SqlOpSelectBlock" + aliasPart + "(" + schemaStr);
		
		writer.incIndent();
		writer.println("Where: " + conditions);
		writer.println("Limit: " + limit + ", Offset: " + offset);
		writer.println("From:");
		
		writer.incIndent();
		if(subOp == null) {
			writer.print("No SubOp");
		} else {
			subOp.write(writer);
		}
		writer.println();
		writer.decIndent();		
		
		writer.print(")");
		writer.decIndent();		
	}

    


}

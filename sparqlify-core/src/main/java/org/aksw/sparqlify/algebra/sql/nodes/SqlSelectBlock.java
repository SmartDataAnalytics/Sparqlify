package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.algebra.sql.exprs.SqlSortCondition;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.ScopeBase;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sparql.core.Var;



/**
 * Similar to SqlSelectBlock:
 * 
 * The sql generation process is to generate SqlSelectBlock from the
 * algebra, which are then turned into strings.
 * 
 * @author raven
 *
 *
 */
public class SqlSelectBlock
	extends SqlNodeBase1
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
    
    public SqlSelectBlock(SqlNode node) {
    	this(node.getAliasName(), node);
    	
    	/*
    	this.getAliasToColumn().putAll(node.getAliasToColumn());
    	this.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
    	*/
    }
    
	public SqlSelectBlock(String aliasName, SqlNode sqlNode) {
		super(aliasName, sqlNode);
	}

	
	public void setAliasName(String name)
	{
		this.aliasName = name;
	}
	/*
	@Override
	public String getAliasName()
	{
		return subNode.getAliasName();
	}*/
	
	public void setSubNode(SqlNode subNode) {
		if(this.subNode != null) {
			throw new RuntimeException("Child already set");
		}
		this.subNode = subNode;
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
	

	// The order of the projection
	private List<Var> order;
	
	// Selection
	private SqlExprList conditions = new SqlExprList() ;
    
	// Slicing
	private Long offset = null;
    private Long limit = null;
    private boolean distinct = false ;
    
    //private SqlNode join;
    //private SqlTable vTable ;           // Naming base for renamed columns
    
    private List<SqlSortCondition> sortConditions = new ArrayList<SqlSortCondition>();

    /*
	public Map<Var, Expr> getProjection() {
		return projection;
	}
	*/

	public SqlExprList getConditions() {
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

	public List<SqlSortCondition> getSortConditions() {
		return sortConditions;
	}
	
	@Override
	public SqlSelectBlock copy1(SqlNode subNode) {
		return new SqlSelectBlock(this.getAliasName(), subNode);
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

    static public SqlNode distinct(Generator generator, SqlNode sqlNode)
    { 
        SqlSelectBlock block = blockWithView(generator, sqlNode) ;
        block.setDistinct(true) ;
        return block ;
    }
    
    /*
    static public SqlNode project(Generator generator, SqlNode sqlNode)
    { return project(generator, sqlNode, (ColAlias)null) ; }
    */

    
    /*
    static public SqlSelectBlock project(Generator generator, SqlNode sqlNode, Map<Var, Expr> cols)
    {
        // If already a view, not via a project, - think harder
        
        SqlSelectBlock block = blockNoView(generator, sqlNode) ;
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
    static public SqlNode project(SDBRequest request, SqlNode sqlNode, ColAlias col)
    {
        SqlSelectBlock block = blockNoView(request, sqlNode) ;
        if ( col != null )
            block.add(col) ;
        return block ;
    }*/


    public static SqlNode restrict(Generator generator, SqlNode sqlNode, SqlExprList exprs)
    {
        if ( exprs.size() == 0 )
            return sqlNode ;
        
        // Single table does not need renaming of columns 
        SqlSelectBlock block = (sqlNode instanceof SqlTable) ? blockPlain(generator, sqlNode) : blockWithView(generator, sqlNode) ;
        block.getConditions().addAll(exprs) ;
        return block ;
    }
  
        
    private static SqlSelectBlock _create(SqlNode sqlNode, Generator generator)
    {
        String alias = sqlNode.getAliasName() ;
        //if ( ! sqlNode.isTable() )
        alias = generator.next() ;
        SqlSelectBlock block = new SqlSelectBlock(alias, sqlNode) ;
        //addNotes(block, sqlNode) ;
        return block ;
    }
    
    
    /*
    public static SqlBlock project(SqlNode node, Generator generator) {
    	SqlBlock block = blockWithView(generator, node);
    	block.getProjection().putAll(node.getSparqlVarToExpr());
    	
    	return block;
    }*/
            
    public static void order(Generator generator, SqlSelectBlock block, List<SqlSortCondition> conditions)
    {
    	block.getSortConditions().addAll(conditions);
    }
    
    public static void distinct(Generator generator, SqlSelectBlock block) {
    	block.setDistinct(true);
    }
    
    public static void slice(Generator generator, SqlSelectBlock block, Long start, Long length)
    {
        //SqlSelectBlock block = blockWithView(generator, sqlNode) ;
        
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

    private static SqlSelectBlock blockPlain(Generator generator,SqlNode sqlNode)
    {
        if ( sqlNode instanceof SqlSelectBlock )
            return (SqlSelectBlock)sqlNode ;
        // Same alias (typically, sqlNode is a table or view and this is the table name) 
        SqlSelectBlock block = new SqlSelectBlock(sqlNode.getAliasName(), sqlNode) ;
        //addNotes(block, sqlNode) ;
        return block ;
    }

    private static SqlSelectBlock blockWithView(Generator generator, SqlNode sqlNode)
    {
        if (sqlNode instanceof SqlSelectBlock )
        {
            SqlSelectBlock block = (SqlSelectBlock)sqlNode ;
            if(block.getAliasToColumn().size() == 0 )
            {
                // Didn't have a column view - force it
                calcView(block) ;
            }
            
            return (SqlSelectBlock)sqlNode ;
        }
            
        SqlSelectBlock block = _create(sqlNode, generator) ;
        if(block.getAliasToColumn().size() != 0 )
            throw new SDBInternalError("Can't set a view on Select block which is already had columns set") ; 
        
        calcView(block) ;
        return block ;
    }
    
    private static SqlSelectBlock blockNoView(Generator generator, SqlNode sqlNode)
    {
        if ( sqlNode instanceof SqlSelectBlock ) {
            return (SqlSelectBlock)sqlNode ;
        }
        
        return _create(sqlNode, generator) ;
    }

    static private void calcView(SqlSelectBlock block)
    {
        SqlNode sqlNode = block.getSubNode() ;
        ScopeBase idScopeRename = new ScopeBase() ;
        ScopeBase nodeScopeRename = new ScopeBase() ;
        Generator gen = Gensym.create("X") ;    // Column names.  Not global.
    
        /*
        block.merge(sqlNode.getIdScope(), idScopeRename, gen) ;
        block.merge(sqlNode.getNodeScope(), nodeScopeRename, gen) ;
    
        block.nodeScope = nodeScopeRename ;
        block.idScope = idScopeRename ;
        */
    }

    


}

package org.aksw.sparqlify.trash;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.compile.sparql.SqlExprOptimizerTmp;
import org.apache.jena.sparql.expr.Expr;

public class ExprBridge {

    public static SqlExprList toSql(Iterable<Expr> exprs)
    {
        SqlExprList result = new SqlExprList();
        for(Expr expr : exprs) {
            SqlExpr sqlExpr = SqlExprOptimizerTmp.translateMM(expr);
            result.add(sqlExpr);
        }

        return result;
    }

    /*
    public static String asSqlString(Iterable<Expr> exprs)
    {
        return asString(toSql(exprs));
    }

    public static String asString(Iterable<SqlExpr> sqlExprs) {
        List<String> strs = new ArrayList<String>();
        for(SqlExpr expr : sqlExprs) {
            strs.add(expr.asSQL());
        }

        String result = Joiner.on(" AND ").join(strs);
        return result;
    }
    */
}

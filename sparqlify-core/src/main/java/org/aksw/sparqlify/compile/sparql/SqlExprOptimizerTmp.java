package org.aksw.sparqlify.compile.sparql;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.exprs_ext.E_StrConcatPermissive;
import org.aksw.jena_sparql_api.views.SqlExprOptimizer;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_Regex;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.DatatypeSystemOld;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;


public class SqlExprOptimizerTmp {
    public static DatatypeSystemOld datatypeSystem = new DatatypeSystemDefault();

    /*
    public Expr optimizeCommonPrefix(E_Concat a, E_Concat b) {

    }*/

    public static SqlExpr translateMM(Expr expr) {
        return (SqlExpr) MultiMethod.invokeStatic(SqlExprOptimizer.class,
                "translate", expr);
    }

    public static List<SqlExpr> translateArgs(Iterable<Expr> exprs) {
        List<SqlExpr> result = new ArrayList<SqlExpr>();

        for (Expr arg : exprs) {
            SqlExpr sqlArg = translateMM(arg);
            result.add(sqlArg);
        }

        return result;
    }

    public static SqlExpr translate(E_Regex expr) {
        List<SqlExpr> sqlExprs = translateArgs(expr.getArgs());

        String flags = (sqlExprs.size() == 3) ? sqlExprs.get(2).toString() : "";
        String pattern = sqlExprs.get(1).toString();

        return new S_Regex(sqlExprs.get(0), pattern, flags);

    }

    public static SqlExpr translate(E_StrConcatPermissive expr) {
        return new S_Concat(translateArgs(expr.getArgs()));
    }


    /**
     * Warning: FIXME This may cause undesired effects if thero is no
     * appropriate override for Jena
     * as domain classes such as E_Regex, as it would not be convertet to S_Regex
     * @param expr
     * @return
     */
    public static SqlExpr translate(ExprFunction expr) {
        List<SqlExpr> list = new ArrayList<SqlExpr>();

        for (Expr arg : expr.getArgs()) {
            SqlExpr sqlArg = translateMM(arg);
            list.add(sqlArg);
        }


        System.err.println("Warning: 1 No datatype handling - using String");
        return new S_Function(expr.getFunctionSymbol().getSymbol(), list, DatatypeSystemDefault._STRING);
    }

    /**
     * Overrides ExprFunction
     * @param expr
     * @return
     */
    public static SqlExpr translate(E_Function expr) {
        List<SqlExpr> list = new ArrayList<SqlExpr>();

        for (Expr arg : expr.getArgs()) {
            SqlExpr sqlArg = translateMM(arg);
            list.add(sqlArg);
        }


        System.err.println("Warning: 2 No datatype handling - using String");
        return new S_Function(expr.getFunctionIRI(), list, DatatypeSystemDefault._STRING);
    }


    // TODO We need a mapping of column name to datatype
    public static SqlExprColumn translateVar(Var var)
    {
        String[] parts = var.getName().split("\\.", 2);

        //SqlTable sqlTable = parts.length == 1 ? null : new SqlTable(parts[0]);
        String tableName = parts.length == 1 ? null : parts[0];
        String colName = parts[parts.length - 1];

        System.err.println("Warning: 3 No datatype handling - using String");
        return new SqlExprColumn(tableName, colName, DatatypeSystemDefault._STRING);
    }

    /**
     *
     * Currently we translate specially named sparql variables to sql column
     * references: ?tableAlias.columnName
     *
     *
     * @param expr
     * @return
     */
    public static SqlExpr translate(ExprVar expr) {
        return translateVar(expr.asVar());
    }

    public static SqlExpr translate(NodeValue expr) {

        if (expr.isNumber()) {
            return new SqlExprValue(expr.getDecimal());
        } else if (expr.isBoolean() ){
            return new SqlExprValue(expr.getBoolean());
        } else if (expr.isString()) {
            return new SqlExprValue(expr.asString());
        } else {
            throw new RuntimeException("Unsupported datatype");
        }
    }

    public static SqlExpr translate(E_NotEquals expr) {
        return new S_LogicalNot(translateMM(new E_Equals(expr.getArg1(),
                expr.getArg2())));
    }

    public static SqlExpr translate(E_StrConcat expr) {
        return new S_Concat(translateList(expr.getArgs()));
    }

    public static SqlExpr translate(E_Equals expr) {
        SqlExpr a = translateMM(expr.getArg1());
        SqlExpr b = translateMM(expr.getArg2());

        return S_Equals.create(a, b, datatypeSystem);
    }

    /*
     * public SqlExpr translate(E_Not expr) {
     *
     * }
     */

    public static List<SqlExpr> translateList(List<Expr> exprs) {
        List<SqlExpr> result = new ArrayList<SqlExpr>();

        for (Expr item : exprs) {
            result.add(item == null ? null : translateMM(item));
        }

        return result;
    }

    /*
     * public SqlExpr translateSql(S_Vector expr) { return new
     * S_Vector(translateSql(expr.getExprs()); }
     */

}

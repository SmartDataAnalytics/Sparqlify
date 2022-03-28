package org.aksw.sparqlify.core.sparql.algebra.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs2.S_IsNotNull;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.util.CnfTransformer;
import org.aksw.sparqlify.util.ExprAccessor;
import org.aksw.sparqlify.util.SqlExprAccessor;

import com.google.common.collect.Iterables;

public class SqlExprUtils
{
    public static SqlExpr orifyBalanced(Iterable<SqlExpr> exprs) {
        return ExprUtils.opifyBalanced(exprs, S_LogicalOr::new);
    }

    public static SqlExpr andifyBalanced(Iterable<SqlExpr> exprs) {
        return ExprUtils.opifyBalanced(exprs, S_LogicalAnd::new);
    }


    public static List<TypeToken> getTypes(List<SqlExpr> args) {
        List<TypeToken> argTypes = new ArrayList<TypeToken>(args.size());
        for(SqlExpr newArg : args) {
            argTypes.add(newArg.getDatatype());
        }

        return argTypes;
    }

    /**
     * Returns a null list for empty argument.
     *
     * @param expr
     * @return
     */
    public static <T> List<T> exprToList(T expr) {
        List<T> result;

        if(expr == null) {
            result = Collections.emptyList();
        } else {
            result = Collections.singletonList(expr);
        }

        return result;
    }

//
//  public static List<SqlExprColumn> getColumnsMentioned(SqlExpr expr) {
//      List<SqlExprColumn> result = new ArrayList<SqlExprColumn>();
//      getColumnsMentioned(expr, result);
//      return result;
//  }
//
//  public static void getColumnsMentioned(SqlExpr expr, List<SqlExprColumn> list) {
//      for(SqlExpr arg : expr.getArgs()) {
//          if(arg instanceof SqlExprColumn) {
//              if(!list.contains(arg)) {
//                  list.add((SqlExprColumn)arg);
//              }
//          } else {
//              getColumnsMentioned(arg, list);
//          }
//      }
//  }


    public static boolean isConstantsOnly(Iterable<SqlExpr> exprs) {
        for(SqlExpr expr : exprs) {
            if(!expr.isConstant()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isConstantArgsOnly(SqlExprFunction fn) {

        boolean result = isConstantsOnly(fn.getArgs());

        return result;
    }

    public static Set<S_ColumnRef> getColumnReferences(SqlExpr expr) {
        Set<S_ColumnRef> result = new HashSet<S_ColumnRef>();

        collectColumnReferences(expr, result);

        return result;
    }

    public static void collectColumnReferences(SqlExpr expr, Collection<S_ColumnRef> result) {
        if(expr.isFunction()) {

            List<SqlExpr> args = expr.getArgs();
            for(SqlExpr arg : args) {
                collectColumnReferences(arg, result);
            }

        }
        else if(expr.isConstant()) {
            // Nothing to do
        }
        else if(expr.isVariable()) {
            S_ColumnRef columnRef = (S_ColumnRef)expr;
            result.add(columnRef);
        }
        else {
            throw new RuntimeException("Should not happen");
        }
    }



    public static List<SqlExpr> cnfAsList(Collection<? extends Iterable<SqlExpr>> nf) {
        List<SqlExpr> result = new ArrayList<SqlExpr>();

        for(Iterable<SqlExpr> clause : nf) {
            SqlExpr expr = orifyBalanced(clause);
            if(expr != null) {
                result.add(expr);
            }
        }

        return result;
    }


    public static List<SqlExpr> toDnf(Collection<? extends Iterable<SqlExpr>> clauses) {
        List<SqlExpr> result = new ArrayList<SqlExpr>();

        if(clauses.size() == 1) {
            Iterable<SqlExpr> itr = clauses.iterator().next();

            Iterables.addAll(result, itr);
        }
        else if(!clauses.isEmpty()) {
            List<SqlExpr> ors = new ArrayList<SqlExpr>();
            for(Iterable<SqlExpr> clause : clauses) {
                SqlExpr and = andifyBalanced(clause);
                ors.add(and);
            }

            SqlExpr or = orifyBalanced(ors);
            result.add(or);
        }

        return result;
    }


    public static boolean containsFalse(Iterable<SqlExpr> exprs, boolean includeTypeErrors) {
        for(SqlExpr expr : exprs) {
            if(S_Constant.FALSE.equals(expr) || (includeTypeErrors && S_Constant.TYPE_ERROR.equals(expr))) {
                return true;
            }
        }

        return false;
    }





    //public static final CnfTransformer<SqlExpr> cnfTransformer = new CnfTransformer<SqlExpr>(new SqlExprAccessor());
    public static final ExprAccessor<SqlExpr> accessor = new SqlExprAccessor();

    public static SqlExpr toCnfExpr(SqlExpr expr) {
        SqlExpr result = CnfTransformer.eval(expr, accessor);
        return result;
    }

    public static List<Collection<SqlExpr>> toCnf(SqlExpr expr) {
        SqlExpr tmp = toCnfExpr(expr);
        List<Collection<SqlExpr>> result = CnfTransformer.toCnf(tmp, accessor);
        return result;
    }

    public static List<Collection<SqlExpr>> toCnf(Iterable<SqlExpr> exprs) {

        List<Collection<SqlExpr>> result = CnfTransformer.toCnf(exprs, accessor);
        return result;
    }


    public static void optimizeEqualityInPlace(List<Collection<SqlExpr>> cnf) {
        for(Collection<SqlExpr> clause : cnf) {
            Iterator<SqlExpr> it = clause.iterator();

            while(it.hasNext()) {
                SqlExpr expr = it.next();

                if(expr instanceof S_Equals) {
                    S_Equals tmp = (S_Equals)expr;


                    boolean isSame = tmp.getLeft().equals(tmp.getRight());
                    if(isSame) {
                        it.remove();
                    }
                }
            }
        }
    }


    public static void optimizeNotNullInPlace(List<Collection<SqlExpr>> cnf) {

        for(Collection<SqlExpr> clause : cnf) {
            Iterator<SqlExpr> it = clause.iterator();

            Set<S_ColumnRef> columnRefs = new HashSet<S_ColumnRef>();

            for(SqlExpr expr : clause) {
                if(!(expr instanceof S_IsNotNull)) {
                    SqlExprUtils.collectColumnReferences(expr, columnRefs);

                }
            }

            while(it.hasNext()) {
                SqlExpr expr = it.next();

                if(expr instanceof S_IsNotNull) {
                    S_IsNotNull tmp = (S_IsNotNull)expr;
                    SqlExpr arg = tmp.getExpr();

                    boolean contained = columnRefs.contains(arg);
                    if(contained) {
                        it.remove();
                    }
                }
            }
        }
    }
}

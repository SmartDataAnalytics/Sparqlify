package sparql;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.NodeValue;

public class ClauseUtils
{
    /*
     * Use ExprIndex.filterByVars instead
    public static Set<Clause> filterByVars(Collection<Clause> clauses, Set<Var> requiredVars) {

        Set<Clause> result = new HashSet<Clause>();

        for(Clause clause : clauses) {
            Set<Var> clauseVars = clause.getVarsMentioned();

            if(!clauseVars.containsAll(requiredVars)) {
                continue;
            }

            result.add(clause);
        }


        return result;
    }*/

    public static Set<Set<Expr>> filterByVars(Set<Set<Expr>> clauses, Set<Var> requiredVars) {

        Set<Set<Expr>> result = new HashSet<Set<Expr>>();

        for(Set<Expr> clause : clauses) {
            Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

            if(!clauseVars.containsAll(requiredVars)) {
                continue;
            }

            result.add(clause);
        }


        return result;
    }

    /**
     * false means that it is no satisfiable.
     *
     * true is actually a 'maybe'
     *
     * @param clause
     * @return
     */
    public static boolean isSatisfiable(Set<Expr> clause)
    {
        for(Expr expr : clause) {
            if(expr.equals(NodeValue.FALSE)) {
                return false;
            }

            if(!isSatisfiable(expr)) {
                return false;
            }

            if(expr instanceof E_LogicalNot) {
                Expr child = ((E_LogicalNot)expr).getArg();

                if(clause.contains(child)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isSatisfiable(Expr expr)
    {
        // NOTE Folding does not detect cases such as E_LogicalAnd(E_Equals(x = y), false)
        Expr copy = expr.copySubstitute(BindingRoot.create());
        Expr folded = ExprLib.foldConstants(copy);
        return !folded.equals(NodeValue.FALSE);
    }

    public static Set<Var> getVarsMentioned(Set<Expr> clause)
    {
        Set<Var> result = new HashSet<Var>();

        for(Expr expr : clause) {
            result.addAll(expr.getVarsMentioned());
        }

        return result;
    }

    /*
    public static Set<Var> getVarsMentioned(Set<Expr> clause)
    {
        Set<Var> vars = new HashSet<Var>();

        for(Expr expr : clause) {
            Set<Var> exprVars = expr.getVarsMentioned();

            if(vars.isEmpty()) { // this happens on the first expr
                vars.addAll(exprVars);
            } else {
                vars.retainAll(exprVars);
                if(vars.isEmpty()) {
                    break;
                }
            }
        }

        return vars;
    }
     */

}

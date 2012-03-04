package sparql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.NestedNormalForm;
import org.aksw.sparqlify.expr.util.ExprUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;


// TODO There is already com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterConjunction
public class CnfUtils {
	
	public static NestedNormalForm toCnf(Expr expr) {
		Set<Set<Expr>> ss = toSetCnf(expr);
		
		Set<Clause> clauses = new HashSet<Clause>();
		for(Set<Expr> s : ss) {
			clauses.add(new Clause(s));
		}

		return new NestedNormalForm(clauses);
	}

	
	public static void main(String[] args)
	{
		//!a->b&(a|d&b)
		
		String sA = "Select * { ?s ?p ?o . Filter((!(!?a) || ?b) && (?a || ?d && ?b)) . }";
		//String sA = "Select * { ?s ?p ?o . Filter(?o != <http://Person>). Optional { { ?x ?y ?z . Filter(?x != <http://x> && ?x = ?y) . } Union { ?x a ?y . Filter(?x = <http://x>) . } } . }";
		
		//String sA = "Select * { ?s ?p ?o . Filter(!(?s = ?p || ?p = ?o && ?s = ?o || ?o = ?s)) . }";
		//String sA = "Select * { ?s ?p ?o . Filter(!(?s = ?p || ?j = <http://x>)) . }";
		Query qA = QueryFactory.create(sA);
		
		Op opA = Algebra.compile(qA);
		opA = Algebra.toQuadForm(opA);
		

		//System.out.println(opA);

		
		// How to deal with union? variables appearing in them 
		
		//System.out.println(opA.getClass());

		ExprList exprs = FilterUtils.collectExprs(opA, new ExprList());
		Expr expr = ExprUtils.andifyBalanced(exprs);
		
		Expr x = eval(expr);
		System.out.println("HERE: " + x);
		
		Set<Set<Expr>> cnf = toSetCnf(expr);		
		System.out.println("THERE: " + cnf);
		/*
		
		
		
		ExprList proc = eval(exprs);
		System.out.println(proc);
		
		List<ExprList> clauses = dnfToClauses(proc);

		System.out.println("Mentioned vars:" + proc.getVarsMentioned());
		
		System.out.println("Clauses: " + clauses);
*/
	}

	/**
	 * Return a sub cnf that where each element of a clause contains all variables
	 * 
	 * @param clauses
	 * @param requiredVars
	 * @return
	 */
	/*
	public static Set<Set<Expr>> filterByVars(Set<Set<Expr>> clauses, Set<Var> requiredVars) {
		
		Set<Set<Expr>> result = new HashSet<Set<Expr>>();
			
		for(Set<Expr> clause : clauses) {

			Set<Expr> tmp = null;
			for(Expr expr : clause) {
			
				if(!expr.getVarsMentioned().containsAll(requiredVars)) {
					continue;
				}
				
				if(tmp == null) {
					tmp = new HashSet<Expr>();
				}
				
				tmp.add(expr);
			}
			if(tmp != null) {
				result.add(tmp);
			}
		}
			

		return result;
	}
	*/

	
	public static Set<Set<Expr>> toSetCnf(ExprList exprs)
	{
		List<ExprList> clauses = toClauses(exprs);
		Set<Set<Expr>> cnf = FilterUtils.toSets(clauses);
		
		return cnf;
	}

	public static Set<Set<Expr>> toSetCnf(Expr expr)
	{
		List<ExprList> clauses = toClauses(expr);
		Set<Set<Expr>> cnf = FilterUtils.toSets(clauses);
		
		return cnf;
	}

	public static List<ExprList> toClauses(Expr expr)
	{
		Expr evaluated = eval(expr);
		return evaluated == null ? null : cnfToClauses(Collections.singleton(evaluated));				
	}
	
	public static List<ExprList> toClauses(ExprList exprs)
	{
		Expr evaluated = eval(ExprUtils.andifyBalanced(exprs));
		return evaluated == null ? null : cnfToClauses(Collections.singleton(evaluated));		
	}
	
	
	/**
	 * This method only words if the input expressions are in DNF,
	 * otherwise you will likely get junk back.
	 * 
	 * @param exprs
	 * @return
	 */
	public static List<ExprList> cnfToClauses(Iterable<Expr> exprs) {
		List<ExprList> result = new ArrayList<ExprList>();
		
		for(Expr expr : exprs) {
			collectAnd(expr, result);
		}
		
		return result;
	}

	
	public static void collectAnd(Expr expr, List<ExprList> list)
	{		
		if(expr instanceof E_LogicalAnd) {
			E_LogicalAnd e = (E_LogicalAnd)expr;

			collectAnd(e.getArg1(), list);
			collectAnd(e.getArg2(), list);
		}
		else if(expr instanceof E_LogicalOr) {
			//List<Expr> ors = new ArrayList<Expr>();
			ExprList ors = new ExprList();
			collectOr(expr, ors);

			list.add(ors);			
		} else {
			list.add(new ExprList(expr));
		}
	}

	public static void collectOr(Expr expr, ExprList list)
	{
		if(expr instanceof E_LogicalOr) {
			E_LogicalOr e = (E_LogicalOr)expr;
			
			collectOr(e.getArg1(), list);
			collectOr(e.getArg2(), list);
		} else {
			list.add(expr);			
		} 
	}

	
	public static Expr eval(Expr expr)
	{
		if(expr instanceof ExprFunction) {
			return handle((ExprFunction)expr);
		} else {
			return expr;
		}
	}
	
	public static Expr handle(ExprFunction expr)
	{
		System.out.println("Converting to KNF: [" + expr.getClass() + "]: " + expr);

		// not(and(A, B)) -> or(not A, not B)
		// not(or(A, B)) -> or(not A, not B)

		
		if(expr instanceof E_LogicalNot) {
			
			Expr tmp = ((E_LogicalNot)expr).getArg();
			if (!(tmp instanceof ExprFunction)) {
				return expr;
			}

			ExprFunction child = (ExprFunction)tmp;

			Expr newExpr = expr;
			
			if (child instanceof E_LogicalAnd) {				
				newExpr = new E_LogicalOr(eval(new E_LogicalNot(child.getArg(1))), eval(new E_LogicalNot(child.getArg(2))));
			}
			else if (child instanceof E_LogicalOr) {
				newExpr = new E_LogicalAnd(eval(new E_LogicalNot(child.getArg(1))), eval(new E_LogicalNot(child.getArg(2))));
			}
			else if (child instanceof E_LogicalNot) { // Remove double negation
				newExpr = eval(child.getArg(1));
			}
			else {
				return expr;
			}
			
			return eval(newExpr);
		}
		
		
		else if (expr instanceof E_LogicalAnd) {
			//return expr;
			//return eval(expr);
			return new E_LogicalAnd(eval(expr.getArg(1)), eval(expr.getArg(2)));
		}		


		/* Given:
		 * (A or B) AND (C x D) becomes:
		 * (A and (C x D)) OR (B and (c x D))
		 * 
		 * 
		 * (A or B) AND (C or D)
		 * 
		 * Goal:
		 * (A and C) OR (A and D) OR (B and C) OR (B and D)
		 * 
		 * This method transforms any "or" children of an AND node.
		 * other nodes are left untouched:
		 * (A or B) AND (c x D) becomes:
		 * (A and (c x D)) OR (B and (c x D))
		 */
		else if (expr instanceof E_LogicalOr) {

			Expr aa = eval(expr.getArg(1));
			Expr bb = eval(expr.getArg(2));
			
			E_LogicalAnd a = null;
			Expr b = null;
			
			if (aa instanceof E_LogicalAnd) {
				a = (E_LogicalAnd)aa;
				b = bb;
			}
			else if(bb instanceof E_LogicalAnd) {
				a = (E_LogicalAnd)bb;
				b = aa;
			}
			
			if(a == null) {
				return new E_LogicalOr(aa, bb);
			} else {
				return new E_LogicalAnd(eval(new E_LogicalOr(a.getArg(1), b)), eval(new E_LogicalOr(a.getArg(2), b)));
			}
		}		

		else if (expr instanceof E_NotEquals) { // Normalize (a != b) to !(a = b) --- this makes it easier to find "a and !a" cases
			return new E_LogicalNot(eval(new E_Equals(expr.getArg(1), expr.getArg(2))));
		}

		
		return expr;
	}
}

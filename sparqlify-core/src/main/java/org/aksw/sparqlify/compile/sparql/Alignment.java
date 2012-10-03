package org.aksw.sparqlify.compile.sparql;

import java.util.List;

import org.aksw.commons.util.Pair;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * 
 * The following optimizations are currently implemented:
 * 
 * - f(argsA) = f(argsB) -> argA_0 = argB_0 && ... && argA_n = argB_n
 *   Currently this rule is always applied, however it is only true for
 *   deterministic, side-effect free functions.
 *   It will cause pitfalls if anyone every tried something like rand() = rand():
 *   This will return always true (i think - but maybe not)
 *    
 * 
 * The following optimizations are about to be implemented:
 * 
 * - Inverse function on variables
 *  op(f(expr_with_vars), const) -> op(expr_with_var, f^-1(const)) (if f^-1 exists) 
 * 
 *    op can be =, <, > ... (not sure right now whether the shift in semantics is acceptable)
 *    Also note, that this optization currently does not take function indexes into account.
 *    So if there was in index f(x) our optimization would be wrong in thinking that using x alone is better.
 *    
 * 
 * 
 * 
 * @author Claus Stadler
 *
 */

public class Alignment
extends Pair<List<Expr>, List<Expr>>
{
public Alignment(List<Expr> key, List<Expr> value) {
	super(key, value);
}		

	public boolean isSameSize() {
		return this.getKey().size() == this.getValue().size();
	}
}
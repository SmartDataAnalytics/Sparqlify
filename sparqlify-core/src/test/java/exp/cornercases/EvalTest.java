package exp.cornercases;


import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.SqlTranslationUtils;
import org.junit.Test;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.util.ExprUtils;



/**
 * TODO: If we use Expr for both SPARQL and SQL expressions, how can we distingiush between them?
 * This is might be really simple: Just look at the root node whether it has become an SQL expression.
 * NOTE But this would mean, we would need operators such as +_sql, -sql and such.
 *  
 * 
 * Essentially, this depends on the set of function symbols/iris used.
 * E.g. if all functions are in the sql: namespace, then the expression can be converted to SQL.
 * 
 * Maybe we could use the ExprFactorizer (or whatever I called this), which returns
 * the "top-level" SPARQL expressions that references variables that map to SQL exprs.
 * 
 *  This way we can tell whether the whole expression could be pushed.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class EvalTest {

	@Test
	public void FunctionTest() {
		

		FunctionRegistry registry = FunctionRegistry.get();
		
		//registry.put("http://ex.org/func", funcClass);

		String testStrs[] = new String[] {
				"<http://ex.org/func>(1 + 3, lang(<" + SparqlifyConstants.rdfTermLabel + ">(1 + 1, concat('http://foo', '/bar', concat('', ?x)), 'en', '')), lang('test'@fr) + ?a + 4 + ?b)",
		
				"<" + SparqlifyConstants.rdfTermLabel + ">(1, concat('foo', 'bar', ?a), '', '') = <" + SparqlifyConstants.rdfTermLabel + ">(1, concat('foobar', ?b), '', '')"
		};

		String str = testStrs[1];
		System.out.println(str);
		Expr expr = ExprUtils.parse(str);
		
		
		/* Chicken-egg problem: the evaluator may needs to perform transformations, but transformation
		 * may need to apply evaluations.
		 *
		 * So for now, we use a "transformer proxy" which is passed to the evaluator,
		 * but wichi is only initialized later.
		 * 
		 */
		/*
		ExprTransformerMap exprTransformer = new ExprTransformerMap();		
		ExprEvaluatorPartial evaluator = new ExprEvaluatorPartial(registry, exprTransformer);
		
		Map<String, ExprTransformer> transMap = exprTransformer.getTransformerMap();
		
		transMap.put("concat", new ExprTransformerConcatNested());
		transMap.put("lang", new ExprTransformerLang());
		transMap.put("=", new ExprTransformerRdfTermComparator(evaluator));
		
		transMap.put("&&", new ExprTransformerLogicalAnd());
		*/
		
		ExprEvaluator evaluator = SqlTranslationUtils.createDefaultEvaluator();
		
		
		//exprTransformer.put(SparqlifyConstants.rdfTermLabel, new ExprTransformer());
		
		//VarBinding binding = new VarBinding();
		Expr result = evaluator.eval(expr, null); //binding);
		
		System.out.println("Result: "+ result);
		
		//Expr e = ExprUtils.eval(expr);
		
		//System.out.println(e);
	}
}

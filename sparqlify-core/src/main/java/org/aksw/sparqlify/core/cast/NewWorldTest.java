package org.aksw.sparqlify.core.cast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp2;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.SqlTranslationUtils;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.SparqlFunctionImpl;
import org.aksw.sparqlify.util.MapReader;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.XSD;


/* This is the SqlExprTransformer
interface ExprTypeEvaluator {
	SqlExpr eval(Expr expr);
}

class ExprTypeEvaluatorImpl
	implements ExprTypeEvaluator
{
	@Override
	public SqlExpr eval(Expr expr) {
		//ExprEvaluato
		
		// TODO Auto-generated method stub
		return null;
	}
	
}
*/





/*
class ExprEvaluatorJena
	implements ExprEvaluator
{
	@Override
	public Expr eval(Expr expr) {
		Expr result = ExprUtils.eval(expr);
		return result;
	}

	@Override
	public Expr eval(Expr expr, Map<Var, Expr> binding) {
		// TODO Auto-generated method stub
		return null;
	}
}
*/

class SparqlEvaluatorChain
	implements SqlExprEvaluator
{
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		// TODO Auto-generated method stub
		return null;
	}

}


class SparqlEvaluatorDefault
	implements SqlExprEvaluator
{
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		
		// TODO Auto-generated method stub
		return null;
	}
}


class SparqlEvaluatorTypeSystem
	implements SqlExprEvaluator
{

	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		// TODO Auto-generated method stub
		return null;
	}
	
}


/**
 * Greatly simplified design, which unifies SPARQL and SQL functions.
 * 
 * With this design, SqlExpr should be renamed to TypedExpr:
 * It simply captures the datatype which an expression yields.
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class NewWorldTest {
	
	public static TypeSystem createDefaultDatatypeSystem() throws IOException {
		
		//String basePath = "src/main/resources";
		Map<String, String> typeNameToClass = MapReader.readFromResource("/type-class.tsv");
		Map<String, String> typeNameToUri = MapReader.readFromResource("/type-uri.tsv");
		Map<String, String> typeHierarchy = MapReader.readFromResource("/type-hierarchy.default.tsv");
		
		TypeSystem result = TypeSystemImpl.create(typeHierarchy);
	
		initSparqlModel(result);
		
		return result;
	}

	
	
	
	/**
	 * Create the SPARQL and SQL models
	 * 
	 * 
	 * 
	 */
	public static void initSparqlModel(TypeSystem typeSystem) {
		
		//FunctionRegistry functionRegistry = new FunctionRegistry();
	
		ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();
		
		// Eliminates rdf terms from Expr (this is datatype independent)
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();


		// Computes types for Expr, thereby yielding SqlExpr
		TypedExprTransformer typedExprTransformer = new TypedExprTransformerImpl(typeSystem);
		
		// Obtain DBMS specific string representation for SqlExpr
		SqlExprSerializerSystem serializerSystem = new SqlExprSerializerSystemImpl();
		
		
		
		//ExprEvaluator exprEvaluator = new ExprEvaluatorPartial(functionRegistry, typedExprTransformer)
		
//		{
//			Method m = DefaultCoercions.class.getMethod("toDouble", Integer.class);
//			XMethod x = XMethodImpl.createFromMethod("toDouble", ds, null, m);
//			ds.registerCoercion(x);
//		}
//		
//		/*
//		 * Methods that can only be rewritten
//		 */
//		
//
//		// For most of the following functions, we can rely on Jena for their
//		// evaluation
//		ExprEvaluator evaluator = new ExprEvaluatorJena();  
//
//		
		{
			// 1. Define the signature, e.g. boolean = (rdfTerm, rdfTerm)
			MethodSignature<String> sig = MethodSignature.create(false, XSD.xboolean.toString(), SparqlifyConstants.rdfTermLabel, SparqlifyConstants.rdfTermLabel);
			
			// 2. Attach Jena's evaluator to it (for constant expressions)
			//    (already defined globally)
			
			// 3. Attach a typeEvaluator to it.
			//    TODO Should we skip step 2 in favor of this?
			

			SqlExprEvaluator evaluator = new SqlExprEvaluator_Equals(typeSystem);
			
			// As a fallback where Jena can't evaluate it, register a transformation to an SQL expression. 
			SparqlFunction f = new SparqlFunctionImpl("=", sig, evaluator, null);
			
			typeSystem.registerSparqlFunction(f);

			
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("=");
			serializerSystem.addSerializer("equals", serializer);
			
			//result = new S_Serialize(TypeToken.Boolean, "AND", Arrays.asList(a, b), serializer);

		}
				
		Expr e0 = ExprUtils.parse("?a = ?b");
		Expr a = ExprUtils.parse("<http://aksw.org/sparqlify/uri>(?website)");
		Expr b = ExprUtils.parse("<http://aksw.org/sparqlify/plainLiteral>(?foo)");
		Map<Var, Expr> binding = new HashMap<Var, Expr>();
		binding.put(Var.alloc("a"), a);
		binding.put(Var.alloc("b"), b);
		
		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.put("website", TypeToken.String);
		typeMap.put("foo", TypeToken.String);
		
		System.out.println("[ExprRewrite Phase 0]: " + e0);
		
		Expr e1 = exprBindingSubstitutor.substitute(e0, binding);
		System.out.println("[ExprRewrite Phase 1]: " + e1);
		
		Expr e2 = exprTransformer.transform(e1);		
		System.out.println("[ExprRewrite Phase 2]: " + e2);

		SqlExpr e3 = typedExprTransformer.translate(e2, typeMap);
		System.out.println("[ExprRewrite Phase 3]: " + e3);

		String e4 = serializerSystem.serialize(e3);
		System.out.println("[ExprRewrite Phase 4]: " + e4);
		
		
		
		
		// The Sparql function '='(?a, ?b) has the substitution typedLiteral(sql:equals(?a, ?b), xsd:boolean) defined.
		// For sql:equals an appropriate serializer is defined.
		//

		
		//		
//		
//		
//		{
//			MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeTokenPostgis.Geometry, TypeTokenPostgis.Geometry), null);
//			
//			XMethod x = XMethodImpl.create(ds, "ST_INTERSECTS", signature);
//			ds.registerSqlFunction("http://ex.org/fn/intersects", x);
//		}		
//
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_Concat();
//			ds.createSparqlFunction("concat", evaluator);
//			
//			/*
//			MethodSignature<TypeToken> signature = MethodSignature.create(true, TypeToken.String, TypeToken.Object);
//			
//			// TODO: We need a serializer for concat
//			XMethod x = XMethodImpl.create(ds, "||", signature);
//			ds.registerSqlFunction("concat", x);
//			*/
//		}		
//
//		
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalAnd();
//			ds.createSparqlFunction("&&", evaluator);
//		}
//
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();
//			ds.createSparqlFunction("||", evaluator);
//		}
//		
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalNot();
//			ds.createSparqlFunction("!", evaluator);
//		}
//
//		
//		/*
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_Equals(ds);
//			ds.createSparqlFunction("=", evaluator);
//		}
//		*/
//		
//		
//		{
//			String[] compareSymbols = new String[]{"<=", "<", "=", ">", ">="};
//			for(String opSymbol : compareSymbols) {
//				ds.createSparqlFunction(opSymbol, new SqlExprEvaluator_Compare(opSymbol, ds));
//			}
//		}

	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		RdfViewSystemOld.initSparqlifyFunctions();
		
		TypeSystem typeSystem = createDefaultDatatypeSystem();
		
		System.out.println("Yay so far");
		
		//SqlTranslator sqlTranslator = new SqlTranslatorImpl(typeSystem);
		//ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();

		
		
	}
}

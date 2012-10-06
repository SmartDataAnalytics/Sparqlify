package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mapping.ExprCopy;

import org.aksw.commons.util.Pair;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GenericSqlExpr;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GeographyFromText;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GeomFromText;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_Intersects;
import org.aksw.sparqlify.algebra.sql.exprs.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.algebra.sql.exprs.SqlStringTransformer;
import org.aksw.sparqlify.core.DatatypeSystem;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.SqlDatatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_StrDatatype;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.XSD;

interface IFactoryN<T>
{
	T create(ExprList args);
}


interface SqlFunctionRenderer
{
	String render(S_Function function);
}


class GenericSqlFunctionDefinition
	implements SqlFunctionDefinition
{
	private static final Logger logger = LoggerFactory.getLogger(GenericSqlFunctionDefinition.class);
	
	private DatatypeSystem datatypeSystem;
	public String sqlFunctionName;
	
	private List<Pair<MethodSignature<SqlDatatype>, SqlStringTransformer>> signatures = new ArrayList<Pair<MethodSignature<SqlDatatype>, SqlStringTransformer>>();
	
	
	public GenericSqlFunctionDefinition(String sqlFunctionName, DatatypeSystem datatypeSystem) {
		this.sqlFunctionName = sqlFunctionName;
		this.datatypeSystem = datatypeSystem;
	}
	
	public static MethodSignature<SqlDatatype> resolve(MethodSignature<String> signature, DatatypeSystem datatypeSystem)
	{
		SqlDatatype returnType = datatypeSystem.requireByName(signature.getReturnType());
		
		List<SqlDatatype> paramTypes = new ArrayList<SqlDatatype>();
		for(String str : signature.getParameterTypes()) {
			SqlDatatype paramType = datatypeSystem.requireByName(str);
			paramTypes.add(paramType);
		}
		
		MethodSignature<SqlDatatype> result = new MethodSignature<SqlDatatype>(returnType, signature.isVararg(), paramTypes);

		return result;
	}
	
	/**
	 * Add a function definition - 
	 *
	 * @param parameterTypes
	 */
	public void add(SqlStringTransformer transformer, MethodSignature<String> signature)
	{		
		MethodSignature<SqlDatatype> resolved = resolve(signature, this.datatypeSystem);
		signatures.add(Pair.create(resolved, transformer));
	}

	public void add(SqlStringTransformer transformer, String returnType, boolean isVararg, String ...paramTypes) {
		add(transformer, new MethodSignature<String>(returnType, isVararg, Arrays.asList(paramTypes)));
	}

	@Override
	public SqlExpr create(SqlExprList args) {
		
		// Get the argument types
		List<SqlDatatype> argTypes = new ArrayList<SqlDatatype>();
		for(SqlExpr arg : args) {
			argTypes.add(arg.getDatatype());
		}
		
		// Check if there is an appropriate signature registered
		List<Pair<MethodSignature<SqlDatatype>, SqlStringTransformer>> candidates = new ArrayList<Pair<MethodSignature<SqlDatatype>, SqlStringTransformer>>(); 

		for(Pair<MethodSignature<SqlDatatype>, SqlStringTransformer> pair : signatures) {	
			 
			MethodSignature<SqlDatatype> signature = pair.getKey();
			
			if(signature.getParameterTypes().size() > argTypes.size()) {
				continue; // Not enough arguments provided
			}
			
			
			if(!signature.isVararg() && signature.getParameterTypes().size() < argTypes.size()) {
				continue; // Too many arguments provided
			}
			
			int n = Math.min(argTypes.size(), signature.getParameterTypes().size());
			
			boolean isCandidate = true;
			for(int i = 0; i < n ; ++i) {
				SqlDatatype a = argTypes.get(i);
				SqlDatatype b = signature.getParameterTypes().get(i);
				
				logger.warn("Implement the lookup properly - taking the type hierarchy into account");

				if(datatypeSystem.supremumDatatypes(a, b).isEmpty()) {
					isCandidate = false;
					continue;
				}
				
				
				//datatypeSystem.
			}
			
			if(isCandidate) {
				candidates.add(pair);
			}
		}
		
		switch(candidates.size()) {
		case 0: {
			logger.warn("Returning false; although it should be type-error");
			return new SqlExprValue(false);
		}
		case 1: {
			Pair<MethodSignature<SqlDatatype>, SqlStringTransformer> pair = signatures.get(0); 
			return new S_Function(this.sqlFunctionName, args, pair.getKey().getReturnType(), pair.getValue());	
		}
		default: {
			logger.warn("Multiple overloads matched: " + candidates);
			logger.warn("Returning false; although it should be type-error");
			return new SqlExprValue(false);
		}
		}
	}

	@Override
	public String getName() {
		return sqlFunctionName;
	}
}




public class FunctionExpander {
	
	private static Map<String, IFactoryN<Expr>> map = new HashMap<String, IFactoryN<Expr>>();
	
	static {
		map.put("http://www.opengis.net/rdf#intersects",
				new IFactoryN<Expr>() {
					@Override
					public Expr create(ExprList args) {
						return new E_Intersects(args.get(0), args.get(1));
					}});
		
		map.put("http://www.opengis.net/rdf#geomFromText",
				new IFactoryN<Expr>() {
					@Override
					public Expr create(ExprList args) {
						return new E_GeomFromText(args.get(0));
					}});
		
		map.put("http://www.opengis.net/rdf#geographyFromText",
				new IFactoryN<Expr>() {
					@Override
					public Expr create(ExprList args) {
						return new E_GeographyFromText(args.get(0));
					}});
		

		map.put("http://www.w3.org/2001/XMLSchema#double",
				new IFactoryN<Expr>() {
					@Override
						public Expr create(ExprList args) {
							return new E_StrDatatype(args.get(0), NodeValue.makeNode(XSD.xdouble.asNode()));
						}});
		
		DatatypeSystem datatypeSystem = new DatatypeSystemDefault();
		
		// geography
		{

			/*
			 * The best thing would be to have a function declaration syntax, e.g:
			 * Function ogc:geomFromText(geometry ?a, geometry ?b) maps to [[ST_DWithin(?a, ?b)]] 
			 * 
			 * But even better if we could re-use the macro expansion system:
			 * 
			 * Define Macro ogc:geomFromText(geometry ?a, geometry ?b) As fn:sqlFn('ST_DWithin', ?a ?b) 
			 * 
			 * 
			 */
			
			
			// The datatypeSystem architecture is somewhat broken - or at least it needs cleanup:
			// Not sure if the function definitions should have a dependency on the datatypeSystem.
			// Maybe it would be better for the definition to only use strings, and Sparqlify will
			// map these definitions to its given DatatypeSystem - so maybe we should do it like:
			// GenericSqlFunctionDefinition<String> --> GenericSqlFunctionDefinition<SqlDatatype>
			final GenericSqlFunctionDefinition ST_DWithin = new GenericSqlFunctionDefinition("ST_DWithin", datatypeSystem);
			ST_DWithin.add(new SqlStringTransformer() {
				
				@Override
				public String transform(S_Function function, List<String> args) {
					// bif:st_intersects expects the distance in km, whereas
					// st_dwithin expects m
					// therefore multiply by 1000
					
					// Actually, we could do that in a more structured way using SqlExpr objects,
					// but for now it is sufficient doing it here.
				
					// Create a copy rather than doing it in-place. Might spare me some headache in a few months.
					List<String> tmp = new ArrayList<String>(args);
					tmp.set(2, "(" + tmp.get(2) + ")" + " * 1000.0");
					tmp.add("false"); // Use spheroid - unfortunately neither true nor false mimics virtuosos behavior exactly :(
					return function.getFuncName() + "(" + Joiner.on(", ").join(tmp) +  ")";
				}
			}, "boolean", false, "geography", "geography", "float");
	
			String bifNs = "http://www.openlinksw.com/schemas/bif#"; 
	
			map.put(bifNs + "st_intersects",
					new IFactoryN<Expr>() {
						@Override
						public Expr create(ExprList args) {
							return new E_GenericSqlExpr(ST_DWithin, args);
						}});
			
			final GenericSqlFunctionDefinition ST_MakePoint = new GenericSqlFunctionDefinition("ST_MakePoint", datatypeSystem);
			ST_MakePoint.add(new SqlStringTransformer() {
				
				@Override
				public String transform(S_Function function, List<String> args) {				
					return function.getFuncName() + "(" + Joiner.on(", ").join(args) +  ")::geography";
				}
			},
					
					"geography", false, "double", "double");
	
			map.put(bifNs + "st_point",
					new IFactoryN<Expr>() {
						@Override
						public Expr create(ExprList args) {
							return new E_GenericSqlExpr(ST_MakePoint, args);
						}});
	
	
			
			 SqlStringTransformer pointExtractor = new SqlStringTransformer() {			
				@Override
				public String transform(S_Function function, List<String> args) {				
					return function.getFuncName() + "(" + args.get(0) +  "::geometry)";
				}
			 };
			
			registerFunction(datatypeSystem, "ST_X", bifNs + "st_x", pointExtractor, "double", false, "geography");
			registerFunction(datatypeSystem, "ST_Y", bifNs + "st_y", pointExtractor, "double", false, "geography");
		
		
			
		
		}
		
		// geometry
		{
			
			// The datatypeSystem architecture is somewhat broken - or at least it needs cleanup:
			// Not sure if the function definitions should have a dependency on the datatypeSystem.
			// Maybe it would be better for the definition to only use strings, and Sparqlify will
			// map these definitions to its given DatatypeSystem - so maybe we should do it like:
			// GenericSqlFunctionDefinition<String> --> GenericSqlFunctionDefinition<SqlDatatype>
			final GenericSqlFunctionDefinition ST_DWithin = new GenericSqlFunctionDefinition("ST_DWithin", datatypeSystem);
			ST_DWithin.add(new SqlStringTransformer() {
				
				@Override
				public String transform(S_Function function, List<String> args) {
					// bif:st_intersects expects the distance in km, whereas
					// st_dwithin expects m
					// therefore multiply by 1000
					
					// Actually, we could do that in a more structured way using SqlExpr objects,
					// but for now it is sufficient doing it here.
				
					// Create a copy rather than doing it in-place. Might spare me some headache in a few months.
					List<String> tmp = new ArrayList<String>(args);
					//tmp.set(2, "(" + tmp.get(2) + ")" + " / 1000.0");
					//tmp.add("false"); // Use spheroid - unfortunately neither true nor false mimics virtuosos behavior exactly :(
					return function.getFuncName() + "(" + Joiner.on(", ").join(tmp) +  ")";
				}
			}, "boolean", false, "geometry", "geometry", "float");
	
			String bifNs = "http://www.openlinksw.com/schemas/bif#"; 
	
			map.put(bifNs + "st_intersects",
					new IFactoryN<Expr>() {
						@Override
						public Expr create(ExprList args) {
							return new E_GenericSqlExpr(ST_DWithin, args);
						}});
			
			final GenericSqlFunctionDefinition ST_MakePoint = new GenericSqlFunctionDefinition("ST_MakePoint", datatypeSystem);
			ST_MakePoint.add(new SqlStringTransformer() {
				
				@Override
				public String transform(S_Function function, List<String> args) {				
					return "ST_SetSRID(" + function.getFuncName() + "(" + Joiner.on(", ").join(args) +  ")::geometry" + ", 4326)";
				}
			},
					
					"geometry", false, "double", "double");
	
			map.put(bifNs + "st_point",
					new IFactoryN<Expr>() {
						@Override
						public Expr create(ExprList args) {
							return new E_GenericSqlExpr(ST_MakePoint, args);
						}});
	
	
			
			 SqlStringTransformer pointExtractor = new SqlStringTransformer() {			
				@Override
				public String transform(S_Function function, List<String> args) {				
					return function.getFuncName() + "(" + args.get(0) +  "::geometry)";
				}
			 };
			
			registerFunction(datatypeSystem, "ST_X", bifNs + "st_x", pointExtractor, "double", false, "geometry");
			registerFunction(datatypeSystem, "ST_Y", bifNs + "st_y", pointExtractor, "double", false, "geometry");
		
		
			
		
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

	
	public static void registerFunction(DatatypeSystem datatypeSystem, final String funcName, final String sparqlFuncName, SqlStringTransformer transformer, String returnType, boolean isVararg, String ...paramTypes) {

		final GenericSqlFunctionDefinition funcDef = new GenericSqlFunctionDefinition(funcName, datatypeSystem);
		funcDef.add(transformer, returnType, isVararg, paramTypes);
		

		map.put(sparqlFuncName,
				new IFactoryN<Expr>() {
					@Override
					public Expr create(ExprList args) {
						return new E_GenericSqlExpr(funcDef, args);
					}});
		
	}
	
	
	
	
	public static Expr transform(Expr expr) {
		return expr == null ? null : (Expr) MultiMethod.invokeStatic(FunctionExpander.class,
				"_transform", expr);
	}

	public static Expr _transform(E_Function expr) {		
		ExprList args = transformList(expr.getArgs());

		IFactoryN<Expr> factory = map.get(expr.getFunctionIRI());
		if(factory == null) {
			throw new RuntimeException("No algebra-class factory registered for function '" + expr.getFunctionIRI() + "'");
		}
		Expr result = factory.create(args);
		
		return result;
	}

	public static Expr _transform(ExprFunction expr) {		
		ExprList args = transformList(expr.getArgs());

		Expr result;
		if(expr instanceof E_Function) {
			
			IFactoryN<Expr> factory = map.get(expr.getFunctionIRI());
			if(factory == null) {
				throw new RuntimeException("No algebra-class factory registered for function '" + expr.getFunctionIRI() + "'");
			}

			result = factory.create(args);

		} else {
			result = ExprCopy.getInstance().copy(expr, args);	
		}
		
		return result;
	}

	
	protected static ExprList transformList(Iterable<Expr> exprs) {
		ExprList result = new ExprList();

		for (Expr expr : exprs) {
			result.add(transform(expr));
		}

		return result;
	}

	public static Expr _transform(Expr expr) {
		return expr;
	}
}

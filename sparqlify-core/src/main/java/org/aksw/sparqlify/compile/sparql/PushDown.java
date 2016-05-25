package org.aksw.sparqlify.compile.sparql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.exprs_ext.E_GeographyFromText;
import org.aksw.jena_sparql_api.exprs_ext.E_GeomFromText;
import org.aksw.jena_sparql_api.exprs_ext.E_Intersects;
import org.aksw.jena_sparql_api.exprs_ext.NodeValueGeom;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.ExprArgs;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.aksw.jena_sparql_api.views.OgcVocab;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.jena_sparql_api.views.SqlPrePusher;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sparql.expr.old.E_GenericSqlExpr;
import org.aksw.sparqlify.algebra.sparql.expr.old.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs.S_Add;
import org.aksw.sparqlify.algebra.sql.exprs.S_Cast;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.S_GeographyFromText;
import org.aksw.sparqlify.algebra.sql.exprs.S_GeometryFromText;
import org.aksw.sparqlify.algebra.sql.exprs.S_GreaterThan;
import org.aksw.sparqlify.algebra.sql.exprs.S_GreaterThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs.S_Intersects;
import org.aksw.sparqlify.algebra.sql.exprs.S_IsNotNull;
import org.aksw.sparqlify.algebra.sql.exprs.S_LessThan;
import org.aksw.sparqlify.algebra.sql.exprs.S_LessThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.S_Regex;
import org.aksw.sparqlify.algebra.sql.exprs.S_Substract;
import org.aksw.sparqlify.algebra.sql.exprs.SqlAggregator;
import org.aksw.sparqlify.algebra.sql.exprs.SqlAggregatorCount;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprAggregator;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.DatatypeSystemOld;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Add;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_Subtract;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDT;
import org.postgis.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




interface ExprPusher
{
	SqlExpr push(SqlExprList args);
}



class ExprPusherConcat
	implements ExprPusher
{
	@Override
	public SqlExpr push(SqlExprList args) {
		return new S_Concat(args);
	}
}


class SqlPusher
{	
	private static final Logger logger = LoggerFactory.getLogger(SqlPusher.class);
	private static DatatypeSystemOld datatypeSystem = new DatatypeSystemDefault();
	
	public static SqlExpr push(Expr expr, SqlExprList args) {

		if(expr.isFunction()) {
			ExprFunction func = expr.getFunction();
			
			String funcIri = func.getFunctionIRI();
			
			if(funcIri != null) {
				if(funcIri.equals(SparqlifyConstants.urlEncode)) {
					logger.warn("HACK USED: IGNORING URLENCODE");
					return args.get(0);
				}
			}			
			
			
		}
		
		throw new RuntimeException("Should not happen: " + expr + "; " + args);
		//return null;
		//return expr;
	}

		
	public static SqlExpr push(E_StrConcatPermissive expr, SqlExprList args) {
		return new S_Concat(args);		
	}
	
	public static SqlExpr push(E_Bound expr, SqlExprList args) {
		return new S_IsNotNull(args.get(0));
	}
	
	public static SqlExpr push(E_StrConcat expr, SqlExprList args) {
		return new S_Concat(args);		
	}
	
	public static SqlExpr push(E_Equals expr, SqlExprList args) {
		return S_Equals.create(args.get(0), args.get(1), datatypeSystem);
	}

	/*
	public static SqlExpr push(E_Lang expr, SqlExprList args) {
		return args.get(2);
	}*/

	public static SqlExpr push(E_LangMatches expr, SqlExprList args) {
		return S_Equals.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_Add expr, SqlExprList args) {
		return S_Add.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_Subtract expr, SqlExprList args) {
		return S_Substract.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_LogicalAnd expr, SqlExprList args) {
		return new S_LogicalAnd(args.get(0), args.get(1));
	}

	public static SqlExpr push(E_LogicalOr expr, SqlExprList args) {
		return new S_LogicalOr(args.get(0), args.get(1));
	}

	public static SqlExpr push(E_LogicalNot expr, SqlExprList args) {
		return new S_LogicalNot(args.get(0));
	}

	public static SqlExpr push(E_LessThan expr, SqlExprList args) {
		return S_LessThan.create(args.get(0), args.get(1), datatypeSystem);
	}
	
	public static SqlExpr push(E_GreaterThan expr, SqlExprList args) {
		return S_GreaterThan.create(args.get(0), args.get(1), datatypeSystem);
	}
	
	public static SqlExpr push(E_GreaterThanOrEqual expr, SqlExprList args) {
		return S_GreaterThanOrEqual.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_LessThanOrEqual expr, SqlExprList args) {
		return S_LessThanOrEqual.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_OneOf expr, SqlExprList args) {

		List<SqlExpr> equals = new ArrayList<SqlExpr>();
		
		SqlExpr first = args.get(0);
		for(int i = 1; i < args.size(); ++i) {
			SqlExpr second = args.get(i);
			
			equals.add(S_Equals.create(first, second, datatypeSystem));
		}
		
		if(equals.size() == 1) {
			return equals.get(0);
		}
		
		List<SqlExpr> current = new ArrayList<SqlExpr>();
		List<SqlExpr> next = equals;
		while(next.size() > 1) {
			
			List<SqlExpr> tmp = next;
			next = current;
			current = tmp;
			next.clear();
			
			for(int i = 0; i < current.size(); i+=2) {
				SqlExpr a = current.get(i);

				if(i + 1 >= current.size()) {
					next.add(a);
				} else {
					SqlExpr b = current.get(i + 1);
					next.add(new S_LogicalOr(a, b));
				}				
			}
		}
		

		SqlExpr result = next.get(0);
		return result;
	}
	
	public static SqlExpr push(E_Intersects expr, SqlExprList args) {
		//return new S_Intersects(args.get(0), args.get(1));
		return S_Intersects.create(args.get(0), args.get(1));
	}
	
	public static SqlExpr push(E_GenericSqlExpr expr, SqlExprList args) {
		return expr.getFuncDef().create(args);
	}

	public static SqlExpr push(E_GeographyFromText expr, SqlExprList args) {
		return new S_GeographyFromText(args.get(0));
	}

	public static SqlExpr push(E_GeomFromText expr, SqlExprList args) {
		return new S_GeometryFromText(args.get(0));
	}

	public static SqlExpr push(E_StrDatatype expr, SqlExprList args) {
		if(!(args.get(1) instanceof SqlExprValue)) {
			throw new RuntimeException("Only constants supported for casts");
		}
		
		return S_Cast.create(args.get(0), ((SqlExprValue)args.get(1)).getObject().toString(), datatypeSystem);
	}

	
	public static SqlExpr push(E_Regex expr, SqlExprList args) {		
		String flags = (args.size() == 3) ? args.get(2).toString() : "";
		String pattern = args.get(1).toString();
		
		return new S_Regex(args.get(0), pattern, flags);
	
	}


}


/**
 * Pushes down expressions on the sparql level
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class PushDown {

	public static final Logger logger = LoggerFactory.getLogger(PushDown.class);
	
	public static final Map<String, ExprPusher> userFuncToPusher = new HashMap<String, ExprPusher>();;

	static {
		userFuncToPusher.put("concat", new ExprPusherConcat());
	}
	
	/**
	 * Based on a expr, creates a new expr where the nodes that could be pushed down
	 * are replaced with ExprSqrBridge objects. 
     *
	 * @param expr
	 * @return
	 */
	public static Expr pushDownMM(Expr expr) {
		return (Expr)MultiMethod.invokeStatic(PushDown.class, "pushDown", expr);
	}

	
	public static ExprList pushDownArgs(Iterable<Expr> exprs) {
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			Expr tmp = pushDownMM(expr);
			result.add(tmp);
		}
		
		return result;
	}
	
	
	public static Expr pushDown(Expr expr) {
		return expr;
	}

	/*
	public static Expr pushDown(E_Str expr) {
		return SqlPrePusher.asRdfTerm(expr.getArg()).getLexicalValue();
	}
	*/

	/* TODO: How to push down aggregator expressions?
	 * Example: Given (Avg(?o) As ?x) 
	 * We first need to group all *numeric* ?os into a single column,
	 * in order to ensure there are not muliple candidates for ?o.
	 * But this transformation has to happen at an earlier stage.
	 * Hm, so that means: At this point we just push the expression,
	 * but we assume that previous step did the right thing.
	 * 
	 * TODO Having thought about this for a bit longer, there is no point
	 * in pushing down the ExprAggregator object, since at this point we cannot know
	 */
	public static Expr pushDown(ExprAggregator expr) {
		
		
		SqlAggregator sqlAggregator = _pushDownAgg(expr.getAggregator());
		
		
		Expr result = new ExprSqlBridge(new SqlExprAggregator(expr.getVar().getName(), sqlAggregator));
		return result;
	}
	
	public static SqlAggregator _pushDownAgg(Aggregator agg) {
		SqlAggregator result = (SqlAggregator)MultiMethod.invokeStatic(PushDown.class, "pushDownAgg", agg);
		return result;
	}
	
	public static SqlAggregatorCount pushDownAgg(AggCount agg) {
		return new SqlAggregatorCount();
		//return new E_RdfTerm(new SqlExprValue(3), new SqlAggregatorCount(), null, null);
	}
	
	
	public static Expr pushDown(E_Lang expr) {
		// TODO Not sure if the pushDownMM is right here
		return pushDownMM(SqlPrePusher.asRdfTerm(expr.getArg()).getLanguageTag());
	}

	/*
	public static Expr pushDown(E_Str expr) {
		return SqlPrePusher.asRdfTerm(expr.getArg()).getLexicalValue();
	}*/
	
	public static Expr pushDown(ExprSqlBridge expr) {
		return expr;
	}


	/**
	 * This method returns null - unless
	 * all of list's items are instances of ExprSqlBridges.
	 * In this case the list of sql nodes is retrieved
	 * 
	 * @param expr
	 * @return
	 */
	public static SqlExprList extractSqlExprs(Iterable<Expr> exprs) {
		for(Expr expr : exprs) {
			if(expr == null || !(expr instanceof ExprSqlBridge)) {
				return null;
			}
		}
		
		SqlExprList result = new SqlExprList();
		for(Expr expr : exprs) {
			SqlExpr arg = ((ExprSqlBridge)expr).getSqlExpr(); 
			
			if(arg == null) {
				throw new RuntimeException("Null expression. Should not happen");
			}
			
			result.add(arg);
		}
		
		return result;
	}
	
	/*
	public static Object pushDown(E_Concat expr) {
		ExprList args = pushDownArgs(expr.getArgs());
		List<SqlExpr> sqlArgs = extractSqlExprs(args);

		if(sqlArgs != null) {
			return new ExprSqlBridge(new S_Function("concat", sqlArgs));
		} else {
			return new E_Concat(args);
		}
	}*/

	public static Expr pushDown(NodeValue expr)  {
		try {
			return pushDownE(expr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Expr pushDownE(NodeValue expr) throws Exception {

		SqlExpr result = null;
			
		if(expr.isIRI()){
			result = new SqlExprValue(expr.asNode().getURI());
			logger.debug("HACK - Uri constants should be converted to RdfTerms first");
		} else if(expr.isBoolean()) {
			result = new SqlExprValue(expr.getBoolean() ? true : false);
		} else if(expr.isNumber()) {
			if(expr.isDecimal()) {
				BigDecimal d = expr.getDecimal();
				if(d.scale() > 0) {
					result = new SqlExprValue(d.doubleValue());
				} else {
					result = new SqlExprValue(d.intValue());
				}
			}
			else if(expr.isDouble()) {
				result = new SqlExprValue(expr.getDouble());	
			} else if(expr.isFloat()) {
				result = new SqlExprValue(expr.getFloat());
			} else {
				result = new SqlExprValue(expr.getDecimal().longValue());
			}
		} else if(expr.isString()) {
			result = new SqlExprValue(expr.getString());
		} else if(expr instanceof NodeValueDT) {
			result = new SqlExprValue(expr.getDateTime().toGregorianCalendar());
		} //else if(expr instanceof NodeValueDateTime) {
//			result = new SqlExprValue(expr.getDateTime().toGregorianCalendar());
//		}
		else if(expr instanceof NodeValueGeom){
			result = new SqlExprValue(((NodeValueGeom) expr).getGeometry());
		} else if (expr.isLiteral()) {
			Node node = expr.asNode(); 
			if(node.getLiteralDatatypeURI().equals(OgcVocab.wktLiteral)) {
				result = new SqlExprValue(new PGgeometry(node.getLiteralLexicalForm()), DatatypeSystemDefault._GEOMETRY);
			}
		} else {
			throw new RuntimeException("Unknow datatype of constant: " + expr.getClass() + " ," + expr);
		}

		return new ExprSqlBridge(result);
	}
	
	
	/**
	 * A new attempt on the push down:
	 * Rather than first calling pre-push, we first descend depth first, and invoke push recursively.
	 * The result of the push down is always an rdf-term object.
	 * The result of the push down can eventually be obtained by taking the value object
	 * of the final rdf-term object.
	 *  
	 *  
	 * 
	 * @param expr
	 * @return
	 */
	public static Expr pushDown(ExprFunction expr) {
	
		/*
		if(expr instanceof E_Str) {
			System.out.println("DEBUG");
		}
		*/
		
		ExprList args = pushDownArgs(expr.getArgs());
		//SqlExprList sqlArgs = extractSqlExprs(args);

		// Substitute the arguments of the expression with the pushed ones
		ExprFunction subbed = (ExprFunction)ExprCopy.getInstance().copy(expr, args);

		Expr applied = SqlPrePusher.prePush(subbed);

		if(applied instanceof ExprSqlBridge) {
			return applied;
		}
		
		if(applied instanceof E_RdfTerm) {
			return subbed;
		}
		/*
		else if(applied instanceof ExprFunction) {
			// TODO Should not be needed anymore
			ExprFunction func = (ExprFunction)expr;
			
			String funcIri = func.getFunctionIRI(); 
			
			if(funcIri != null && funcIri.equals(SparqlifyConstants.rdfTermLabel)) {
				return subbed;
			}
		}*/
		
		Expr result = null;
		ExprList tmpArgs = ExprArgs.getArgs(applied);
		SqlExprList sqlArgs = extractSqlExprs(tmpArgs);		
		if(sqlArgs != null) {

			SqlExpr tmp = (SqlExpr)MultiMethod.invokeStatic(SqlPusher.class, "push", applied, sqlArgs);
			result = new ExprSqlBridge(tmp);			
		}
		

		if(result == null) {
			throw new RuntimeException("Should not happen");
			// Push did not work, apply default rule
			//result = ExprCopy.getInstance().copy(expr, args);
		}
		
		return result;
	}
		
	public static Expr pushDownOld(ExprFunction expr) {	
		// Try to push the arguments first
		// If any of the arguments could not be pushed, then we can (of course)
		// not push the function

		//Expr prePush = SqlPrePusher.prePush(expr);
		Expr simplified = SqlPrePusher.prePush(expr);
		
		if(simplified instanceof ExprFunction) {
			expr = (ExprFunction)simplified;
		} else {
			return pushDown(simplified);
		}
		
		/*
		if(prePush != expr) {
			return pushDownMM(prePush);
		}*/
		
		if(simplified instanceof ExprSqlBridge) {
			return simplified;
		}
		
		
		ExprList args = pushDownArgs(expr.getArgs());
		SqlExprList sqlArgs = extractSqlExprs(args);


		Expr result = null;

		if(sqlArgs != null) {

			SqlExpr tmp = (SqlExpr)MultiMethod.invokeStatic(SqlPusher.class, "push", expr, sqlArgs);
			result = new ExprSqlBridge(tmp);
			
			/*
			//ExprPusher pusher = userFuncToPusher.get(expr.getFunctionIRI());

			if(pusher != null) {
				SqlExpr tmp = pusher.push(sqlArgs);
				result = new ExprSqlBridge(tmp);
			}*/
		}
		

		if(result == null) {
			// Push did not work, apply default rule
			result = ExprCopy.getInstance().copy(expr, args);
		}

		return result;
	}
	

	public static Expr pushDown(E_IsIRI expr) {
		return pushDownMM(new E_Equals(SqlPrePusher.getTypeOrExpr(expr.getArg()), new ExprSqlBridge(new SqlExprValue(1))));
	}


	public static Expr pushDown(E_NotEquals expr) {
		return pushDownMM(new E_LogicalNot(new E_Equals(expr.getArg1(), expr.getArg2())));
	}	
}

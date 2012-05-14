package org.aksw.sparqlify.compile.sparql;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import mapping.ExprArgs;
import mapping.ExprCopy;
import mapping.SparqlifyConstants;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_GenericSqlExpr;
import org.aksw.sparqlify.algebra.sparql.expr.E_GeographyFromText;
import org.aksw.sparqlify.algebra.sparql.expr.E_GeomFromText;
import org.aksw.sparqlify.algebra.sparql.expr.E_Intersects;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sparql.expr.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sparql.expr.NodeValueGeom;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystem;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.exprs.S_Add;
import org.aksw.sparqlify.algebra.sql.exprs.S_Cast;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equal;
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
import org.aksw.sparqlify.core.Vocab;
import org.postgis.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_IsIRI;
import com.hp.hpl.jena.sparql.expr.E_Lang;
import com.hp.hpl.jena.sparql.expr.E_LangMatches;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.E_StrDatatype;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDate;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDateTime;




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


/**
 * Methods of this class are invoked for each rdf-function
 * after all its arguments have been pushed.
 * 
 * 
 * 
 * 
 * TODO Rename to PostPush. 
 * 
 * 
 * Documentation below is outdated.
 * -------------------------------------------------
 * Invoked on methods before a push is attempted.
 * This gives us a chance to tranform for instance
 * regex(term(id, lex, lang, dt)) into
 * regex(lex)
 * 
 * So basically it gives functions an opportunity to capture rdfterm arguments
 * 
 * By default arguments are pushed first.
 * 
 * TODO We need to identify different types of traversal
 * (probably just child first (bottom up) and parent first (top down)
 * and somehow allow to plug in the transformers
 * 
 * @author raven
 *
 */
class SqlPrePusher
{
	public static Expr prePush(ExprFunction expr) {
		return (Expr)MultiMethod.invokeStatic(SqlPrePusher.class, "_prePush", expr);
	}
	
	public static E_RdfTerm asRdfTerm(Expr expr) {
		E_RdfTerm result = null;
		
		if(expr instanceof E_RdfTerm) {
			result = (E_RdfTerm)expr; 
		}
		/*
		else if(expr instanceof E_Function) {
			// TODO Should not be needed anymore
			E_Function func = (E_Function)expr;
			if(func.getFunctionIRI().equals(SparqlifyConstants.rdfTermLabel)) {
				result = new E_RdfTerm(func.getArgs());
			}
		}*/
		
		return result;
	}

	public static Expr _prePush(Expr expr) {
		return expr;
	}
	
	/*
	public static Expr _prePush(E_Lang expr) {
		ExprRdfTerm rdfTerm = asRdfTerm(expr.getArgs().get(0));

		if(rdfTerm == null) {
			return expr;
		} else {
			//return ExprCopy.copyMM(expr, rdfTerm.getLanguageTag());
			return rdfTerm.getLanguageTag();
		}		
	}*/

	
	
	public static Expr getTypeOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getType() : expr;
		return result;
	}

	public static Expr getLexicalValueOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getLexicalValue() : expr;
		return result;
	}
	
	public static Expr getDatatypeOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getDatatype() : expr;
		return result;
	}

	public static Expr getLanguageTagOrExpr(Expr expr) {
		E_RdfTerm term = asRdfTerm(expr);
		
		Expr result = (term != null) ? term.getLanguageTag() : expr;
		return result;
	}


	/*
	public static Expr _prePush(E_Equals expr) {
		System.out.println("Here");
		
		
		return null;
	}*/
	
	
	/*
	public static Expr _prePush(E_IsIRI expr) {
		return new E_Equals(getTypeOrExpr(expr.getArg()), new ExprSqlBridge(new SqlExprValue(1)));
	}
	*/
	
	public static Expr _prePush(E_Intersects expr) {	
		return new E_Intersects(getLexicalValueOrExpr(expr.getArg1()), getLexicalValueOrExpr(expr.getArg2()));
	}
	
	public static Expr _prePush(E_GenericSqlExpr expr) {
		ExprList newArgs = new ExprList();
		for(Expr arg : expr.getArgs()) {
			newArgs.add(getLexicalValueOrExpr(arg));
		}
		return ExprCopy.getInstance().copy(expr, newArgs);
	}
	
	
	public static Expr _prePush(E_GeomFromText expr) {
		return new E_GeomFromText(getLexicalValueOrExpr(expr.getArg()));
	}
	
	//public static Expr _pre
	
	public static Expr _prePush(E_LangMatches expr) {
		return new E_LangMatches(getLanguageTagOrExpr(expr.getArg1()), getLexicalValueOrExpr(expr.getArg2()));
		/*
		ExprRdfTerm rdfTerm = asRdfTerm(expr.getArgs().get(0));
		
		if(rdfTerm == null) {
			return expr;
		} else {
			return ExprCopy.copyMM(expr, rdfTerm.getLanguageTag());
		}*/		
	}

	public static Expr _prePush(E_Str expr) {
		//return asRdfTerm(expr.getArg()).getLexicalValue();
		return getLexicalValueOrExpr(expr.getArg());
		//ExprRdfTerm result =  new ExprRdfTerm(NodeValue.makeInteger(2), getLexicalValueOrExpr(expr.getArg()), NodeValue.makeString(""), NodeValue.makeString(""));
		//return result;
	}
	
	public static Expr _prePush(E_Regex expr) {
		E_RdfTerm rdfTerm = asRdfTerm(expr.getArgs().get(0));
			
		if(rdfTerm == null) {
			return expr;
		} else {
			ExprList args = new ExprList();
			args.add(rdfTerm.getLexicalValue());
			for(int i = 1; i < expr.getArgs().size(); ++i) {
				args.add(expr.getArgs().get(i));
			}
			
			return ExprCopy.getInstance().copy(expr, args);
		}
	}
	
	
	/*
	public static Aggregator _prePush(AggCount count) {
		E_RdfTerm rdfTerm = asRdfTerm
	}*/
	
	public static Expr _prePush(ExprFunction expr) {
		return getLexicalValueOrExpr(expr);
	}
}


class SqlPusher
{	
	private static final Logger logger = LoggerFactory.getLogger(SqlPusher.class);
	private static DatatypeSystem datatypeSystem = new DatatypeSystemDefault();
	
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
		return S_Equal.create(args.get(0), args.get(1), datatypeSystem);
	}

	/*
	public static SqlExpr push(E_Lang expr, SqlExprList args) {
		return args.get(2);
	}*/

	public static SqlExpr push(E_LangMatches expr, SqlExprList args) {
		return S_Equal.create(args.get(0), args.get(1), datatypeSystem);
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
	SqlSelectBlock x;	
		SqlExpr result = null;
			
		if(expr.isIRI()){
			result = new SqlExprValue(expr.asNode().getURI());
			System.err.println("WARNING: HACK USED - Uri constants should be converted to RdfTerms first");
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
		} else if(expr instanceof NodeValueDate) {
			result = new SqlExprValue(expr.getDateTime().asCalendar());
		} else if(expr instanceof NodeValueDateTime) {
			result = new SqlExprValue(expr.getDateTime().asCalendar());			
		}
		else if(expr instanceof NodeValueGeom){
			result = new SqlExprValue(((NodeValueGeom) expr).getGeometry());
		} else if (expr.isLiteral()) {
			Node node = expr.asNode(); 
			if(node.getLiteralDatatypeURI().equals(Vocab.wktLiteral)) {
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

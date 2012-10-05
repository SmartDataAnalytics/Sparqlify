package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.exprs.ExprSql;
import org.aksw.sparqlify.core.DatatypeSystem;
import org.aksw.sparqlify.core.SqlDatatype;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Divide;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LangMatches;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Multiply;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.NodeValue;

class DatatypeAssignerConstant
	implements DatatypeAssigner
{
	private SqlDatatype datatype;
	
	public DatatypeAssignerConstant(SqlDatatype datatype) {
		this.datatype = datatype;
	}
	
	@Override
	public SqlDatatype assign(Expr expr, Map<String, SqlDatatype> typeMap) {
		return datatype;
	}

	
	public static DatatypeAssignerConstant create(SqlDatatype datatype) {
		return new DatatypeAssignerConstant(datatype);
	}
}


class DatatypeAssignerNodeValue
	implements DatatypeAssigner
{
	private DatatypeSystem datatypeSystem;
	

	public DatatypeAssignerNodeValue(DatatypeSystem datatypeSystem) {
		this.datatypeSystem = datatypeSystem;
	}
	
	@Override
	public SqlDatatype assign(Expr expr, Map<String, SqlDatatype> typeMap) {
		
		if(expr instanceof ExprSql) {
			return ((ExprSql) expr).getDatatype();
		}
		
		
		NodeValue nv = expr.getConstant();
		Node node = nv.getNode();
		
		SqlDatatype result = null;
		if(node != null) {
			
			if(node.isURI()) {
				
				result =  datatypeSystem.getByName("string");
				
			}
			
			
		} 
		
		
		if(result == null) {
			throw new RuntimeException("case not handled not implemented " + expr);
		}
		
		return result;
	}	
}



class DatatypeAssignerExpr
	implements DatatypeAssigner
{
	//private SqlDatatype datatype;
	
	public DatatypeAssignerExpr(SqlDatatype datatype) {
//		this.datatype = datatype;
	}
	
	public SqlDatatype assign(Expr expr, Map<String, SqlDatatype> typeMap) {
		//return datatype;
		throw new RuntimeException("not implemented");
	}
	
	public static DatatypeAssignerExpr create(SqlDatatype datatype) {
		return new DatatypeAssignerExpr(datatype);
	}
}



public class DatatypeAssignerMap
	implements DatatypeAssigner
{
//	private static final Logger logger = LoggerFactory.getLogger(SqlPusher.class);
	
	//private DatatypeSystem datatypeSystem;
	private Map<Class<?>, DatatypeAssigner> map;
	
	public DatatypeAssignerMap(Map<Class<?>, DatatypeAssigner> map) {
		this.map = map;
	}
	
	public SqlDatatype assign(Expr expr, Map<String, SqlDatatype> typeMap) {
		
		
		DatatypeAssigner assigner;
		
		if(expr.isConstant()) {
			assigner = map.get(NodeValue.class);
		} else {
		
			assigner = map.get(expr.getClass());
		}
		
		if(assigner == null) {
			
			if(expr.isVariable() && typeMap != null) {
				String varName = expr.getVarName();
				SqlDatatype result = typeMap.get(varName);
				return result;
			}
			
			return null;
		}
		
		SqlDatatype result = assigner.assign(expr, typeMap);
		
		return result;
	}
	
	public Map<Class<?>, DatatypeAssigner> getMap() {
		return map;
	}
	
	public static DatatypeAssignerMap createDefaultAssignments(DatatypeSystem datatypeSystem) {
				
		
		Map<Class<?>, DatatypeAssigner> map = new HashMap<Class<?>, DatatypeAssigner>();
		
		SqlDatatype xBoolean = datatypeSystem.getByName("boolean");
		DatatypeAssigner aBoolean = DatatypeAssignerConstant.create(xBoolean);

		SqlDatatype xString = datatypeSystem.getByName("string");
		DatatypeAssigner aString = DatatypeAssignerConstant.create(xString);

		
		
		map.put(E_LogicalAnd.class, aBoolean);
		map.put(E_LogicalOr.class, aBoolean);
		map.put(E_LogicalNot.class, aBoolean);

		map.put(E_LessThan.class, aBoolean);
		map.put(E_LessThanOrEqual.class, aBoolean);
		map.put(E_Equals.class, aBoolean);
		map.put(E_NotEquals.class, aBoolean);
		map.put(E_GreaterThan.class, aBoolean);
		map.put(E_GreaterThanOrEqual.class, aBoolean);

		map.put(E_OneOf.class, aBoolean);
		map.put(E_Bound.class, aBoolean);
		
		map.put(E_StrConcatPermissive.class, aString);
		map.put(E_StrConcat.class, aString);

		// The results of the following expressions depend on their overload being referred to
		map.put(E_Add.class, aString);
		map.put(E_Subtract.class, aString);
		map.put(E_Multiply.class, aString);
		map.put(E_Divide.class, aString);

		map.put(E_LangMatches.class, aString);
		
		map.put(E_Function.class, aString);
		map.put(ExprFunction.class, aString);
		

		map.put(NodeValue.class, new DatatypeAssignerNodeValue(datatypeSystem));


		DatatypeAssignerMap result = new DatatypeAssignerMap(map);

		return result;
	}
	
	
	/*
	
		public static SqlExpr push(E_OneOf expr, SqlExprList args) {

			List<SqlExpr> equals = new ArrayList<SqlExpr>();
			
			SqlExpr first = args.get(0);
			for(int i = 1; i < args.size(); ++i) {
				SqlExpr second = args.get(i);
				
				equals.add(S_Equal.create(first, second, datatypeSystem));
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
		*/
		
	/*
		public static SqlExpr push(E_GenericSqlExpr expr, SqlExprList args) {
			return expr.getFuncDef().create(args);
		}


		public static SqlExpr push(E_StrDatatype expr, SqlExprList args) {
			if(!(args.get(1) instanceof SqlExprValue)) {
				throw new RuntimeException("Only constants supported for casts");
			}
			
			return S_Cast.create(args.get(0), ((SqlExprValue)args.get(1)).getObject().toString(), datatypeSystem);
		}
*/

		/*
		public static SqlExpr push(E_Regex expr, SqlExprList args) {		
			String flags = (args.size() == 3) ? args.get(2).toString() : "";
			String pattern = args.get(1).toString();
			
			return new S_Regex(args.get(0), pattern, flags);
		
		}
		*/

	
}

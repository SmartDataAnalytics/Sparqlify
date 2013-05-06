package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;


public class S_Case
	extends SqlExprN
{
	public S_Case(TypeToken datatype, List<SqlExpr> exprs) {
		super(datatype, "case", exprs);
	}
	
	public static S_Case create(TypeToken type, List<S_When> whens, SqlExpr elseExpr) {
		List<SqlExpr> exprs = new ArrayList<SqlExpr>(whens.size() + 1);
		exprs.addAll(whens);
		
		S_Else hack = new S_Else(elseExpr);
		exprs.add(hack);
		
		S_Case result = create(TypeToken.Boolean, exprs);
		return result;
	}
	
	public static S_Case create(TypeToken type, List<SqlExpr> cases) {
		S_Case result = new S_Case(type, cases);
		return result;
	}

	@Override
	public SqlExprFunction copy(List<SqlExpr> args) {
		S_Case result = new S_Case(datatype, args);
		return result;
	}

	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}

//public class S_Case
//	extends SqlExprFunctionBase
//{
//	private List<S_When> cases;
//	private SqlExpr elseExpr;
//	
//	public S_Case(TypeToken datatype, List<S_When> cases, SqlExpr elseExpr) {
//		super(datatype, "when");
//		this.cases = cases;
//		this.elseExpr = elseExpr;
//	}
//
//	public static S_Case create(TypeToken type, List<S_When> cases, SqlExpr elseExpr) {
//		S_Case result = new S_Case(type, cases, elseExpr);
//		return result;
//	}
//	
//	public List<S_When> getCases() {
//		return cases;
//	}
//
//	public SqlExpr getElseExpr() {
//		return elseExpr;
//	}
//
//	@Override
//	public SqlExprFunction copy(List<SqlExpr> args) {
//		int n = args.size();
//		int last = n - 1;
//		
//		List<S_When> cases = new ArrayList<S_When>();
//		for(int i = 0; i < last; ++i) {
//			S_When caze = (S_When) args.get(i);
//			cases.add(caze);
//		}
//		
//		SqlExpr elseExpr = args.get(last);
//		
//		S_Case result = new S_Case(datatype, cases, elseExpr);
//		
//		return result;
//	}
//
//	@Override
//	public List<SqlExpr> getArgs() {
//		List<SqlExpr> exprs = new ArrayList<SqlExpr>();
//		for(S_When caze : cases) {
//			exprs.add(caze);
//		}
//
//		exprs.add(elseExpr);
//		
//		return exprs;
//	}
//
////	@Override
////	public void asString(IndentedWriter writer) {
////		writer.println("when - TODO implement toString");
////	}
//
//	@Override
//	public <T> T accept(SqlExprVisitor<T> visitor) {
//		T result = visitor.visit(this);
//		return result;
//	}
//
//}

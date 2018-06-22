package org.aksw.sparqlify.core.sql.expr.evaluation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;

/**
 *
 *
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_ParseDate
    extends SqlExprEvaluator1
{
    public static final SqlExprEvaluator_ParseDate DATE = new SqlExprEvaluator_ParseDate(TypeToken.Date, new SimpleDateFormat("yyyy-MM-dd"));
    //public static final SqlExprEvaluator_ParseDate DATETIME = new SqlExprEvaluator_ParseDate(TypeToken.DateTime, new SimpleDateFormat("yyyy-MM-ddThh:mm:ss"));
    public static final SqlExprEvaluator_ParseDate DATETIMESTAMP = new SqlExprEvaluator_ParseDate(TypeToken.TimeStamp, new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss"));
    
    protected TypeToken typeToken;
    protected DateFormat dateFormat;
    
    public SqlExprEvaluator_ParseDate(TypeToken typeToken, DateFormat dateFormat) {
    	this.typeToken = typeToken;
    	this.dateFormat = dateFormat;
    }
    
    public TypeToken getTypeToken() {
		return typeToken;
	}

	public DateFormat getDateFormat() {
		return dateFormat;
	}




	@Override
    public SqlExpr eval(SqlExpr a) {
        SqlValue value = a.asConstant().getValue();
        String str = "" + value.getValue();

        Date val;
        try {
            val = dateFormat.parse(str);
        } catch (Exception e){
            return S_Constant.TYPE_ERROR;
        }

        S_Constant result = S_Constant.create(new SqlValue(typeToken, val));


        return result;
    }
}

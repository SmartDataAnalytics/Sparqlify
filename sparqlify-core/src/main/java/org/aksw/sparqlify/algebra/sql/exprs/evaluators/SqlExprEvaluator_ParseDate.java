package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

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
    public static final DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    @Override
    public SqlExpr eval(SqlExpr a) {
        SqlValue value = a.asConstant().getValue();
        String str = "" + value.getValue();

        Date val;
        try {
            val = defaultDateFormat.parse(str);
        } catch (Exception e){
            return S_Constant.TYPE_ERROR;
        }

        S_Constant result = S_Constant.create(new SqlValue(TypeToken.Date, val));


        return result;
    }
}

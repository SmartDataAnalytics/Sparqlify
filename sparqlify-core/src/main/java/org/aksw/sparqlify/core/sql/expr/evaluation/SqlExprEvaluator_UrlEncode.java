package org.aksw.sparqlify.core.sql.expr.evaluation;

import org.aksw.commons.util.string.StringUtils;
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
public class SqlExprEvaluator_UrlEncode
    extends SqlExprEvaluator1
{
    @Override
    public SqlExpr eval(SqlExpr a) {
        SqlValue value = a.asConstant().getValue();
        String str = "" + value.getValue();

        String val;
        try {
            val = StringUtils.urlEncode(str);
        } catch (Exception e){
            return S_Constant.TYPE_ERROR;
        }

        S_Constant result = S_Constant.create(new SqlValue(TypeToken.String, val));


        return result;
    }
}
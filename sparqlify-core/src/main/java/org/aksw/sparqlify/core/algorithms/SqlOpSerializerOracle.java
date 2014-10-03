package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;
import org.aksw.sparqlify.algebra.sql.nodes.SqlSortCondition;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.Query;

public class SqlOpSerializerOracle
    extends SqlOpSerializerImpl
{
    private static final Logger logger = LoggerFactory.getLogger(SqlOpSerializerOracle.class);

    public SqlOpSerializerOracle(SqlExprSerializer exprSerializer) {
        super(exprSerializer);
    }

    public void _serialize(SqlOpSelectBlock op, IndentedWriter writer)
    {
        writer.print("SELECT ");

        // Distinct
        //String distinctStr = "";
        if(op.isDistinct()) {
            writer.print("DISTINCT ");
            //distinctStr += " DISTINCT";
        }

        // Projection
        String projectionStr = projection(op.getProjection());
        writer.println(projectionStr);

        writer.println("FROM");

        boolean needsGrouping = op.getSubOp() instanceof SqlOpUnionN || op.getSubOp() instanceof SqlOpSelectBlock;// || op.getSubOp() instanceof SqlOpQuery;

        if(needsGrouping) {
            writer.print("(");
        }


        writer.incIndent();
        // Joins

        serialize(op.getSubOp(), writer);
        writer.decIndent();

        if(needsGrouping) {
            String aliasName = getAliasNameNotNull(op.getSubOp());
            writer.print(")" + aliasName);
        }


        if(!writer.atLineStart()) {
        //  writer.println();
        }



        /*
        if(!joinStr.isEmpty()) {
            joinStr = "FROM " + joinStr;
        }
        */

/*
        if(node.getSubNode() instanceof SqlUnionN) {
            joinStr = "(" + joinStr + ") " + node.getAliasName();
        }
*/

        // Selection
        //String selectionStr = "";
        {
            List<String> strs = new ArrayList<String>();
            for(SqlExpr expr : op.getConditions()) {
                if(expr == null) {
                    logger.error("Null expression in: " + op);
                    continue;
                }

                String str = exprSerializer.serialize(expr);

                assert str != null : "An expression was serialized as null: " + expr;

                //String str = expr.asSQL();

                strs.add(str);
            }

            if(op.getLimit() != null) {
                if(op.getOffset() != null) {
                    strs.add("(ROWNUM BETWEEN " + (op.getOffset() + 1) + " AND " + (op.getOffset() + op.getLimit()) + ")");
                } else {
                    strs.add("(ROWNUM <= " + op.getLimit() + ")");
                }
            }
            else if(op.getOffset() != null) {
                strs.add("(ROWNUM >= " + op.getOffset() + ")");
            }



            if(!strs.isEmpty()) {
                writer.println();
                writer.print("WHERE ");
                //selectionStr += " WHERE ";
            }

            writer.println(Joiner.on(" AND ").join(strs));
            //selectionStr += Joiner.on(" AND ").join(strs);
        }


        List<String> groupByExprStrs = new ArrayList<String>();
        for(SqlExpr groupByExpr : op.getGroupByExprs()) {
            String exprStr = exprSerializer.serialize(groupByExpr);

            groupByExprStrs.add(exprStr);
        }
        if(!groupByExprStrs.isEmpty()) {
            String groupByStr = "GROUP BY " + Joiner.on(", ").join(groupByExprStrs);
            writer.println(groupByStr);
        }



        List<String> sortColumnExprStrs = new ArrayList<String>();
        for(SqlSortCondition condition : op.getSortConditions()) {
            String dirStr = null;
            if(condition.getDirection() == Query.ORDER_ASCENDING) {
                dirStr = "ASC";
            } else if(condition.getDirection() == Query.ORDER_DESCENDING) {
                dirStr = "DESC";
            }


            // TODO This is not working properly: If a sparql variable is made up
            // from multiple sql columns, we need to settle for an ordering -
            // right now we get: c1 OR c2 OR ... cn
            String exprStr = exprSerializer.serialize(condition.getExpression());

            if(dirStr != null) {
                //exprStr = dirStr + "(" + exprStr + ")";
                exprStr = exprStr + " " + dirStr;
            }

            sortColumnExprStrs.add(exprStr);
        }

        // TODO If there is limit and order by, the order by has to go into a sub query
        String orderStr = "";
        if(!sortColumnExprStrs.isEmpty()) {
            orderStr = "ORDER BY " + Joiner.on(", ").join(sortColumnExprStrs);
            writer.println(orderStr);
        }
    }
}

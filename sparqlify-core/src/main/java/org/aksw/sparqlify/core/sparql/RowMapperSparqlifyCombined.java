package org.aksw.sparqlify.core.sparql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Multimap;

public class RowMapperSparqlifyCombined
    implements RowMapper<Binding>
{
    protected Var rowIdVar;
    protected Multimap<Var, RestrictedExpr> sparqlVarMap;

    protected boolean adjustVarNames = true;
    protected Map<Var, Var> normalizedToVar = new HashMap<>();

    public RowMapperSparqlifyCombined(Multimap<Var, RestrictedExpr> sparqlVarMap) {
        this(sparqlVarMap, (Var)null);
    }

    public RowMapperSparqlifyCombined(Multimap<Var, RestrictedExpr> sparqlVarMap, String rowIdName) {
        this(sparqlVarMap, rowIdName == null ? null : Var.alloc(rowIdName));
    }

    public RowMapperSparqlifyCombined(Multimap<Var, RestrictedExpr> sparqlVarMap, Var rowIdVar) {
        this.sparqlVarMap = sparqlVarMap;
        this.rowIdVar = rowIdVar;

        for(RestrictedExpr rexpr : sparqlVarMap.values()) {
            Expr expr = rexpr.getExpr();
            Set<Var> vars = ExprVars.getVarsMentioned(expr);
            for(Var v : vars) {
                normalizedToVar.put(Var.alloc(v.getName().toLowerCase()), v);
            }
        }
    }

    @Override
    public Binding mapRow(ResultSet rs, int rowNum) throws SQLException {
        Binding tmp = RowMapperSparqlifyBinding.map(rs, rowNum, rowIdVar);

        if(adjustVarNames) {
            BindingBuilder adjust = BindingFactory.builder();

            Iterator<Var> it = tmp.vars();
            while(it.hasNext()) {
                Var v = it.next();

                Var lookupVar;
                boolean isExactMatch = sparqlVarMap.containsKey(v);
                if(isExactMatch) {
                    lookupVar = v;
                }
                else {
                    Var normalizedVar = Var.alloc(v.getName().toLowerCase());
                    lookupVar = normalizedToVar.get(normalizedVar);
                }

                if(lookupVar != null) {
                    Node node = tmp.get(v);
                    adjust.add(lookupVar, node);
                }
            }
            tmp = adjust.build();
        }

        Binding result = ItemProcessorSparqlify.process(sparqlVarMap, tmp);

        return result;
    }

}

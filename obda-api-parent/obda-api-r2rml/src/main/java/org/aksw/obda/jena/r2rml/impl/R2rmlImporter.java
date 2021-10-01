package org.aksw.obda.jena.r2rml.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.domain.impl.LogicalTableQueryString;
import org.aksw.obda.domain.impl.LogicalTableTableName;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporterLib;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;

public class R2rmlImporter {
    public void validate(Model r2rmlModel) {
        R2rmlImporterLib.validateR2rml(r2rmlModel);
    }

    public Collection<ViewDefinition> read(Model r2rmlModel, SqlCodec sqlCodec) {
        Collection<TriplesMapToSparqlMapping> mappings = R2rmlImporterLib.read(r2rmlModel);

        Collection<ViewDefinition> result = mappings.stream()
                .map(mapping -> convert(mapping, sqlCodec))
                .collect(Collectors.toList());

        return result;
    }

    public static ViewDefinition convert(TriplesMapToSparqlMapping mapping, SqlCodec sqlCodec) {
        TriplesMap tm = mapping.getTriplesMap();

        String name = Objects.toString(tm);
        List<Quad> quads = mapping.getTemplate().getQuads();
        Map<Var, Expr> rawVarDef = mapping.getVarToExpr().getExprs();
        Map<Var, Constraint> constraints = Collections.emptyMap();
        org.aksw.obda.domain.api.LogicalTable logicalTable = convert(mapping.getTriplesMap().getLogicalTable());

        // Unescape the column names (we might eventually have to pass encoded names to sparqlify)

//        if (rawVarDef.values().contains(null)) {
//            throw new NullPointerException("Null expression encountered in " + tm);
//        }

        Set<Var> usedVars = new HashSet<>();
        mapping.getVarToExpr().getExprs().values().stream().forEach(e -> ExprVars.varsMentioned(usedVars, e));
        Map<Var, String> usedVarToColumnName = usedVars.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> sqlCodec.forColumnName().decodeOrGetAsGiven(v.getName())
                ));

        NodeTransform xform = n -> Optional.ofNullable(usedVarToColumnName.get(n)).map(x -> (Node)Var.alloc(x)).orElse(n);
        Map<Var, Expr> varDef = VarExprListUtils.applyNodeTransform(rawVarDef, xform);

        for (Entry<Var, Expr> e : varDef.entrySet()) {
            Expr before = e.getValue();
            Expr after = convert(before);
            e.setValue(after);
        }

        ViewDefinition result = new ViewDefinition(name, quads, varDef, constraints, logicalTable);
        return result;
    }

    /** Adept the expression for sparqlify */
    public static Expr convert(Expr expr) {
        Expr result;
        if (expr instanceof E_Function) {
            E_Function fn = (E_Function)expr;
            result = new E_StrDatatype(fn.getArg(1), NodeValue.makeString(fn.getFunctionIRI()));
        } else if (expr instanceof ExprVar) {
            result = new E_StrLang(expr, NodeValue.makeString(""));
        } else {
            result = expr;
        }
        return result;
    }

    /**
     * Convert a logical table from the R2RML API to one of Sparqlify
     * @param logicalTable
     * @return
     */
    public static org.aksw.obda.domain.api.LogicalTable convert(LogicalTable logicalTable) {
        org.aksw.obda.domain.api.LogicalTable result;
        if (logicalTable.qualifiesAsR2rmlView()) {
            String str = logicalTable.asR2rmlView().getSqlQuery();
            result = new LogicalTableQueryString(str);
        } else if (logicalTable.qualifiesAsBaseTableOrView()) {
            String str = logicalTable.asBaseTableOrView().getTableName();
            result = new LogicalTableTableName(str);
        } else {
            result = null;
        }

        return result;
    }

}

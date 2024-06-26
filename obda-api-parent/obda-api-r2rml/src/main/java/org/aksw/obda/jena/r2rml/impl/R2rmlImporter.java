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
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.jenax.arq.util.syntax.VarExprListUtils;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.domain.impl.LogicalTableQueryString;
import org.aksw.obda.domain.impl.LogicalTableTableName;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.r2rml.jena.arq.impl.R2rmlImporterLib;
import org.aksw.r2rml.jena.arq.impl.TriplesMapToSparqlMapping;
import org.aksw.r2rml.jena.arq.lib.R2rmlLib;
import org.aksw.rmltk.model.backbone.common.IRefObjectMap;
import org.aksw.rmltk.model.backbone.common.ITriplesMap;
import org.aksw.rmltk.model.r2rml.LogicalTable;
import org.aksw.rmltk.model.r2rml.TriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrEncodeForURI;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;

public class R2rmlImporter {
    public void validate(Model r2rmlModel) {
        R2rmlImporterLib.validateR2rml(r2rmlModel);
    }

    public Collection<ViewDefinition> read(Model r2rmlModel) {
        return read(r2rmlModel, SqlCodecUtils.createSqlCodecDoubleQuotes());
    }

    public Collection<ViewDefinition> read(Model r2rmlModel, SqlCodec sqlCodec) {

        // Create a copy of the model to expand rr:join into SQL joins
        Model copy = ModelFactory.createDefaultModel();
        copy.add(r2rmlModel);

        List<TriplesMap> rawTms = R2rmlLib.streamTriplesMaps(copy).collect(Collectors.toList());
        for (TriplesMap tm : rawTms) {
            R2rmlLib.expandShortcuts(tm);
            Map<IRefObjectMap, ITriplesMap> map = R2rmlLib.expandRefObjectMapsInPlace(TriplesMap.class, tm, sqlCodec);
        }

        Collection<TriplesMapToSparqlMapping> mappings = R2rmlImporterLib.read(copy);

        Collection<ViewDefinition> result = mappings.stream()
                .map(mapping -> convert(mapping, sqlCodec))
                .collect(Collectors.toList());

        return result;
    }

    public static ViewDefinition convert(TriplesMapToSparqlMapping mapping, SqlCodec sqlCodec) {
        TriplesMap tm = mapping.getTriplesMap().as(TriplesMap.class);

        String name = Objects.toString(tm);
        List<Quad> quads = mapping.getTemplate().getQuads();
        Map<Var, Expr> rawVarDef = mapping.getVarToExpr().getExprs();
        Map<Var, Constraint> constraints = Collections.emptyMap();
        org.aksw.obda.domain.api.LogicalTable logicalTable = convert(tm.getLogicalTable());

        // Unescape the column names (we might eventually have to pass encoded names to sparqlify)


        Set<Var> usedVars = new HashSet<>();
        mapping.getVarToExpr().getExprs().values().stream().forEach(e -> ExprVars.varsMentioned(usedVars, e));
        Map<Var, String> usedVarToColumnName = usedVars.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> sqlCodec.forColumnName().decodeOrGetAsGiven(v.getName())
                ));


        NodeTransform xform = n -> Optional.ofNullable(usedVarToColumnName.get(n)).map(x -> (Node)Var.alloc(x)).orElse(n);
        Map<Var, Expr> varDef = VarExprListUtils.applyNodeTransform(rawVarDef, xform);

        ExprTransform removeEncodeXform = new ExprTransformCopy() {
            public Expr transform(ExprFunction1 func, Expr expr1) {
                Expr r = func instanceof E_StrEncodeForURI
                    ? expr1
                    : super.transform(func, expr1);
                return r;
            }
        };

        for (Entry<Var, Expr> e : varDef.entrySet()) {
            Expr e1 = e.getValue();
            Expr e2 = convert(e1);
            Expr e3 = ExprTransformer.transform(removeEncodeXform, e2);
            e.setValue(e3);
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

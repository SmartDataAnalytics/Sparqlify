package org.aksw.sparqlify.config.v0_2.bridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.aksw.jena_sparql_api.restriction.RestrictionImpl;
import org.aksw.jena_sparql_api.restriction.RestrictionSetImpl;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.aksw.jena_sparql_api.views.PrefixSet;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.SqlTranslationUtils;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.domain.api.LogicalTable;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.config.syntax.ConstraintPrefix;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.sql.schema.Schema;
import org.aksw.sparqlify.database.PrefixConstraint;
import org.aksw.sparqlify.validation.Validation;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;



class ExprTransformerUtils
{
//    public static final Function<Expr, Expr> expandRdfTerms = new Function<Expr, Expr>() {
//        @Override
//        public Expr apply(@Nullable Expr expr) {
//            Expr result = SqlTranslationUtils.expandAnyToTerm(expr);
//            if(result == null) {
//                throw new RuntimeException("Could not expand rdf terms in " + expr);
//            }
//
//            return result;
//        }
//    };

    public static final Function<Expr, Expr> injectStrsIntoConcats = new Function<Expr, Expr>() {
        @Override
        public Expr apply(@Nullable Expr expr) {
            Expr result;

            if(expr.isFunction()) {

                ExprFunction fn = expr.getFunction();
                boolean isConcat = SqlTranslationUtils.isConcatExpr(fn);


                List<Expr> args = fn.getArgs();

                List<Expr> newArgs = new ArrayList<Expr>(args.size());
                for(Expr arg : args) {
                    Expr tmpArg = apply(arg);

                    Expr newArg;

                    // FIXME
                    if(isConcat && tmpArg.isVariable()) {
                        ExprVar ev = tmpArg.getExprVar();
                        newArg = new E_Str(ev);

                    } else {
                        newArg = tmpArg;
                    }


                    newArgs.add(newArg);
                }

                result = ExprCopy.getInstance().copy(expr, newArgs);
            } else {

                result = expr;

            }

            return result;
        };
    };
}


/**
 * Takes a syntactic ViewDefinition and creates one for the Sparlqify Core package
 *
 * Adapter for Sparqlify-ML 0.1
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SyntaxBridge {

    private static final Logger logger = LoggerFactory.getLogger(SyntaxBridge.class);

    /**
     * We need the schema provider for resolving the column types
     *
     */
    private SchemaProvider schemaProvider;

    public SyntaxBridge(SchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
    }

    public SchemaProvider getSchemaProvider() {
        return schemaProvider;
    }

    // Remove trailing whitespaces and semicolons
    public static String normalizeQueryString(String queryString) {
        Pattern pattern = Pattern.compile("(\\s|;)+$", Pattern.MULTILINE);
        String result = pattern.matcher(queryString).replaceAll("");
        //String result = queryString.replaceAll(, "");


        return result;
    }



    public org.aksw.sparqlify.core.domain.input.ViewDefinition create(ViewDefinition viewDefinition) {

        String name = viewDefinition.getName();

        QuadPattern template = new QuadPattern();

        for(Quad quad : viewDefinition.getConstructTemplate()) {
            //Quad quad = new Quad(Quad.defaultGraphNodeGenerated, triple);

            template.add(quad);
        }

        //VarExprList varExprList = viewDefinition.getViewTemplateDefinition().getVarExprList();
        //List<Constraint> constraints = viewDefinition.getConstraints();

        Map<Var, PrefixConstraint> varToPrefixConstraint = new HashMap<Var, PrefixConstraint>();
        //if(constraints != null) {
        for(Constraint constraint : viewDefinition.getConstraints().values()) {
            if(constraint instanceof ConstraintPrefix) {
            	ConstraintPrefix c = (ConstraintPrefix)constraint;

            	PrefixConstraint dbConstraint = new PrefixConstraint(c.getVar(), "value", c.getPrefixes());
                varToPrefixConstraint.put(c.getVar(), dbConstraint);
            } else {
                logger.warn("Unknown constraint type: " + constraint.getClass() + " - " + constraint);
            }
        }



        Multimap<Var, RestrictedExpr> varDefs = HashMultimap.create();

        for(Entry<Var, Expr> entry : viewDefinition.getVarDefinition().entrySet()) {
            Var var = entry.getKey();
            Expr expr = entry.getValue();

            RestrictionSetImpl rs = null;

            PrefixConstraint c = varToPrefixConstraint.get(var);
            if(c != null) {
                PrefixSet ps = c.getPrefixes();
                RestrictionImpl r = new RestrictionImpl(ps);
                rs = new RestrictionSetImpl(r);
            }



            RestrictedExpr rexpr = new RestrictedExpr(expr, rs);

            varDefs.put(var, rexpr);
        }

        VarDefinition varDefinition = new VarDefinition(varDefs);


        /*
        SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecDefault();
        varDefinition.applyExprTransform(e ->
        	ExprTransformer.transform(new ExprTransformCopy(false) {
        	    @Override
        	    public Expr transform(ExprVar exprVar)       
        	    {
            		String varName = exprVar.getVarName();
            		String harmonizedName = EntityCodecUtils.harmonize(varName, sqlCodec::forColumnName);
            		ExprVar r = new ExprVar(Var.alloc(harmonizedName));
            		return r;
        	    }
        	}, e));
        */

        // Add str to concat expressions in order to play safe with Sparqlify's type model
        varDefinition.applyExprTransform(ExprTransformerUtils.injectStrsIntoConcats);

        // Expand RdfTerms
        varDefinition.applyExprTransform(E_RdfTerm::expand);//ExprTransformerUtils.expandRdfTerms);


        LogicalTable logicalTable = viewDefinition.getLogicalTable();

        // TODO: I think the adapter should be able to resolve the schema at this stage,
        // Rather than leaving the schema on null.

        SqlOp sqlOp = null;
        if(logicalTable == null) {

            logger.warn("No relation given for view '" + name + "', using Select 1");
            Schema schema = schemaProvider.createSchemaForQueryString("SELECT 1");
            sqlOp = new SqlOpQuery(schema, "SELECT 1"); //;null;

        } else if(logicalTable.isQueryString()) {

            String rawQueryString = logicalTable.getQueryString();
            String queryString = normalizeQueryString(rawQueryString);
            Schema schema = schemaProvider.createSchemaForQueryString(queryString);
            sqlOp = new SqlOpQuery(schema, queryString);

        } else if(logicalTable.isTableName()){

            String relationName = logicalTable.getTableName();
            Schema schema = schemaProvider.createSchemaForRelationName(relationName);
            sqlOp = new SqlOpTable(schema, relationName);

        } else {
            throw new RuntimeException("Unsupported relation type: " + logicalTable);
        }

        Mapping mapping = new Mapping(varDefinition, sqlOp);


        org.aksw.sparqlify.core.domain.input.ViewDefinition result = new org.aksw.sparqlify.core.domain.input.ViewDefinition(name, template, null, mapping, viewDefinition);

        return result;
    }


    public static List<org.aksw.sparqlify.core.domain.input.ViewDefinition> bridge(SyntaxBridge bridge, Collection<ViewDefinition> viewDefinitions, Logger logger) {

        List<org.aksw.sparqlify.core.domain.input.ViewDefinition> result = new ArrayList<org.aksw.sparqlify.core.domain.input.ViewDefinition>();
        for(ViewDefinition item : viewDefinitions) {
            org.aksw.sparqlify.core.domain.input.ViewDefinition virtualGraph = bridge.create(item);

            if(logger != null) {
                Validation.validateView(virtualGraph, logger);
            }
            //candidateSelector.addView(virtualGraph);

            result.add(virtualGraph);
        }

        return result;
    }
}


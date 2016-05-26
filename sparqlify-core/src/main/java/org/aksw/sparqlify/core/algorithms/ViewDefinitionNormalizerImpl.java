package org.aksw.sparqlify.core.algorithms;

import java.util.Collection;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.restriction.RestrictionImpl;
import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.restriction.RestrictionSetImpl;
import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.PrefixSet;
import org.aksw.jena_sparql_api.views.RdfTermType;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.SqlTranslationUtils;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ViewDefinitionNormalizerImpl
    implements ViewDefinitionNormalizer<ViewDefinition>
{

    private static final Logger logger = LoggerFactory.getLogger(ViewDefinitionNormalizer.class);

    public RestrictedExpr normalize(RestrictedExpr restExpr) {

        RestrictionSetImpl rs = restExpr.getRestrictions();
        if(rs == null) {
            rs = new RestrictionSetImpl();
        } else {
            rs = restExpr.getRestrictions().clone();
        }

        Expr expr = restExpr.getExpr();
        RdfTermType type = deriveType(expr);
        if(type != null) {
            rs.stateType(type);
        }

        String prefix = derivePrefix(expr);
        if(RdfTermType.URI.equals(type) && prefix != null) {
            PrefixSet ps = new PrefixSet(prefix);
            rs.stateUriPrefixes(ps);
        }

        if(rs.isUnsatisfiable()) {
            System.err.println("Unsatisfiable restriction detected: " + restExpr);
            throw new RuntimeException("Unsatisfiable restriction detected: " + restExpr);
        }

        RestrictedExpr result = new RestrictedExpr(expr, rs);
        return result;
    }


    public VarDefinition normalize(VarDefinition varDef) {
        Multimap<Var, RestrictedExpr> resultMap = HashMultimap.create();

        for(Entry<Var, RestrictedExpr> entry : varDef.getMap().entries()) {
            Var var = entry.getKey();
            RestrictedExpr old = entry.getValue();

            RestrictedExpr newRestExpr = normalize(old);

            resultMap.put(var, newRestExpr);
        }

        VarDefinition result = new VarDefinition(resultMap);
        return result;
    }

    /**
     * Combines all of a variables restrictions.
     *
     * @param varDef
     * @return
     */
    public RestrictionManagerImpl createVarRestrictions(VarDefinition varDef) {
        RestrictionManagerImpl result = new RestrictionManagerImpl();

        for(Entry<Var, Collection<RestrictedExpr>> entry : varDef.getMap().asMap().entrySet()) {
            Var var = entry.getKey();
            Collection<RestrictedExpr> restExprs = entry.getValue();

            int m = restExprs.size();
            if(m == 1) {
                RestrictedExpr restExpr = restExprs.iterator().next();
                RestrictionSetImpl rs = restExpr.getRestrictions();

                int n = rs.getRestrictions().size();
                if(n == 1) {
                    RestrictionImpl r = rs.getRestrictions().iterator().next();

                    result.stateRestriction(var, r);
                }
                else if(n > 1) {
                    logger.warn("More than 1 restriction found; having to ignore all for now: " + rs);
                }
            }
            else if(m > 1) {
                logger.warn("More than 1 definition found; can't derive restrictions because of that: " + restExprs);
            }
        }

        return result;
    }

    public ViewDefinition normalize(ViewDefinition viewDefinition) {
        VarDefinition normVarDef = normalize(viewDefinition.getMapping().getVarDefinition());

        RestrictionManagerImpl restrictionManager = createVarRestrictions(normVarDef);

        Mapping newMapping = new Mapping(normVarDef, viewDefinition.getMapping().getSqlOp());
        ViewDefinition result = new ViewDefinition(viewDefinition.getName(), viewDefinition.getTemplate(), viewDefinition.getViewReferences(), newMapping, restrictionManager, viewDefinition);

        return result;
    }


    public static String derivePrefixConcat(ExprFunction concat) {

        // TODO If all arguments are constant, we could infer a constant constraint
        String prefix = "";
        for(Expr arg : concat.getArgs()) {
            if(arg.isConstant()) {
                prefix += arg.getConstant().asUnquotedString();
            } else {
                break;
            }
        }


        return prefix;
    }


    public static String derivePrefix(E_RdfTerm termCtor) {
        String result;

        Expr expr = termCtor.getArgs().get(1);

        if(expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive) {
            result = derivePrefixConcat(expr.getFunction());
        } else {
            result = null;
        }

        return result;
    }


    public static String derivePrefix(ExprFunction fn) {
        E_RdfTerm termCtor = SqlTranslationUtils.expandRdfTerm(fn);

        String result;
        if(termCtor != null) {
            result = derivePrefix(termCtor);
        } else {
            result = null;
        }

        return result;

    }


    public static String derivePrefix(Node node) {

        String result;
        if(node.isURI()) {
            result = node.getURI();
        } else {
            result = null;
        }

        return result;
    }


    public static String derivePrefix(Expr expr) {

        String result;
        if(expr.isFunction()) {
            result = derivePrefix(expr.getFunction());
        } else if(expr.isConstant()) {
            result = derivePrefix(expr.getConstant().getNode());
        } else {
            result = null;
        }

        return result;
    }


    public static RdfTermType deriveType(Node node) {
        if(node.isURI()) {
            return RdfTermType.URI;
        } else if(node.isLiteral()) {
            return RdfTermType.LITERAL;
        } else if(node.isBlank()) {
            throw new RuntimeException("Decide on what to return here.");
            //return Type.URI;
        } else {
            return RdfTermType.UNKNOWN;
        }
    }


    public static RdfTermType deriveType(E_RdfTerm termCtor) {
        Expr arg = termCtor.getArg(1);
        if(arg.isConstant()) {
            Object o = NodeValueUtils.getValue(arg.getConstant());

            Number number = (Number)o;
            switch(number.intValue()) {
            case 1:
                return RdfTermType.URI;
            case 2:
            case 3:
                return RdfTermType.LITERAL;
            }
        }

        return RdfTermType.UNKNOWN;
    }


    public static RdfTermType deriveType(ExprFunction fn) {
        E_RdfTerm termCtor = SqlTranslationUtils.expandRdfTerm(fn);

        RdfTermType result;
        if(termCtor != null) {
            result = deriveType(termCtor);
        } else {
            result = RdfTermType.UNKNOWN;
        }

        return result;
    }


    public static RdfTermType deriveType(Expr expr) {

        RdfTermType result = null;
        if(expr.isConstant()) {
            result = deriveType(expr.getConstant().asNode());
        } else if(expr.isFunction()) {
            result = deriveType(expr.getFunction());
        }

        return result;
    }
}
package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.restriction.UnsatisfiabilityException;
import org.aksw.jena_sparql_api.views.CandidateViewSelectorBase;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.OpQuadPattern2;
import org.aksw.jena_sparql_api.views.OpViewInstanceJoin;
import org.aksw.jena_sparql_api.views.RecursionResult;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.VarBinding;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.jena_sparql_api.views.ViewInstance;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpEmpty;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.sparqlview.ViewInstanceJoin;
import org.aksw.sparqlify.views.transform.GetVarsMentioned;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;




/**
 * The candidate selector for RDB-RDF
 * @author raven
 *
 * @param <T>
 */
public class CandidateViewSelectorImpl
    extends CandidateViewSelectorBase<ViewDefinition, Mapping>
{
    private static final Logger logger = LoggerFactory.getLogger(CandidateViewSelectorImpl.class);

    //private OpMappingRewriter opMappingRewriter;// = new OpMappingRewriterImpl(new MappingOps)
    private MappingOps mappingOps;

    private ViewDefinitionNormalizer<ViewDefinition> viewDefinitionNormalizer;

    public CandidateViewSelectorImpl() {
        this(null);

        logger.warn("No mappingOps provided. This means that view candidates cannot be pruned efficently which is most likely not what you want!!!");
    }

    public CandidateViewSelectorImpl(MappingOps mappingOps) {
        this(mappingOps, new ViewDefinitionNormalizerImpl());
    }

    public CandidateViewSelectorImpl(MappingOps mappingOps, ViewDefinitionNormalizer<ViewDefinition> viewDefinitionNormalizer) {
        //super(viewDefinitionNormalizer);

        this.viewDefinitionNormalizer = viewDefinitionNormalizer;
        this.mappingOps = mappingOps;
    }

    @Override
    public ViewDefinition normalizeView(ViewDefinition view) {
        ViewDefinition normalized = viewDefinitionNormalizer.normalize(view);

        logger.trace("Normalized view:\n" + normalized);

        return normalized;
    }


    /**
     * Create a new context based on the baseContext and the current candidate viewInstance
     *
     * if null is returned, the candidateViewInstance becomes rejected, otherwise, the new context
     * will be passed to this function together with all sub candidate viewInstances.
     *
     * @return
     */
    @Override
    public Mapping createContext(Mapping baseMapping, ViewInstance<ViewDefinition> viewInstance)
        throws UnsatisfiabilityException
    {
        Mapping nextMapping = null;
        boolean enablePruningMappingRewrite = true;
        if(enablePruningMappingRewrite && mappingOps != null) {

            Mapping mapping = mappingOps.createMapping(viewInstance);

            if(baseMapping == null) {
                nextMapping = mapping;
            } else {
                nextMapping = mappingOps.join(baseMapping, mapping);
            }

            if(nextMapping.isEmpty()) {
                throw new UnsatisfiabilityException();
            }
        }

        return nextMapping;
    }

    /*
    @Override
    public ViewInstanceJoin createUnionItem(List<ViewInstance<ViewDefinition>> viewInstances, RestrictionManagerImpl subRestrictions) {
        ViewInstanceJoin viewConjunction = new ViewInstanceJoin(viewInstances, subRestrictions);

        // remove self joins
        SelfJoinEliminator.merge(viewConjunction);

        //result.add(viewConjunction);
        return viewConjunction;
    }
    */
    @Override
    public Op createOp(OpQuadPattern2 opQuadPattern, List<RecursionResult<ViewDefinition, Mapping>> conjunctions) {

        OpDisjunction result = OpDisjunction.create();

        for(RecursionResult<ViewDefinition, Mapping> entry : conjunctions) {
            Mapping mapping = entry.getFinalContext();
            RestrictionManagerImpl restrictions = entry.getViewInstances().getRestrictions();

            Op tmp = new OpMapping(mapping, restrictions);
            result.add(tmp);
        }


        // If there were no candidates, we fake a view definition where all variables of
        // the quad pattern (of the query) are bound to Node.nvNothing
        if(result.size() == 0) {
            Op tmp = createEmptyViewInstance(opQuadPattern);
            result.add(tmp);
        }

        return result;
    }


    //@Override
    public Op createOpOldButWorking(OpQuadPattern2 opQuadPattern, List<RecursionResult<ViewDefinition, Mapping>> conjunctions) {

        for(RecursionResult<ViewDefinition, Mapping> tmp : conjunctions) {
            ViewInstanceJoin<ViewDefinition> conjunction = tmp.getViewInstances();
            SelfJoinEliminator.merge(conjunction);
        }


        OpDisjunction result = OpDisjunction.create();

        for(RecursionResult<ViewDefinition, Mapping> entry : conjunctions) {
            ViewInstanceJoin<ViewDefinition> item = entry.getViewInstances();
            Op tmp = new OpViewInstanceJoin(item);
            result.add(tmp);
        }


        // If there were no candidates, we fake a view definition where all variables of
        // the quad pattern (of the query) are bound to Node.nvNothing
        if(result.size() == 0) {
            Op tmp = createEmptyViewInstance(opQuadPattern);
            result.add(tmp);
        }

        return result;
    }

    public static Generator emptyViewNameGenerator = Gensym.create("emptyView");

    public static Op createEmptyViewInstance(OpQuadPattern2 opQuadPattern) {
        Set<Var> vars = GetVarsMentioned.getVarsMentioned(opQuadPattern);


        VarBinding binding = new VarBinding();

        Multimap<Var, RestrictedExpr> varDefMap = HashMultimap.create();

        String colName = "nll";
        SqlOpEmpty sqlOpEmpty = SqlOpEmpty.create(colName);
        //MappingOpsImpl.createEmptyMapping()


        for(Var queryVar : vars) {
            // Bind the query var to a view var with the same name
            Var viewVar = queryVar;
            binding.put(queryVar, viewVar);//NodeValue.TRUE.asNode());//NodeValue.nvNothing.asNode());;

            ExprVar colVar = new ExprVar(colName);
            Expr termCtor = E_RdfTerm.createPlainLiteral(colVar);


            // Bind the view var to a column in the table
            varDefMap.put(viewVar, new RestrictedExpr(termCtor));

            //binding.put(var, null);
            //map.put(var, new RestrictedExpr(NodeValue.nvNothing));
        }

        VarDefinition varDef = new VarDefinition(varDefMap);
        Mapping mapping = new Mapping(varDef, sqlOpEmpty);

        String viewName = emptyViewNameGenerator.next();
        ViewDefinition viewDef = new ViewDefinition(viewName, new QuadPattern(), null, mapping, null);

        ViewInstance<ViewDefinition> viewInstance = new ViewInstance<ViewDefinition>(viewDef, binding);
        List<ViewInstance<ViewDefinition>> tmp = new ArrayList<ViewInstance<ViewDefinition>>();
        tmp.add(viewInstance);

        ViewInstanceJoin<ViewDefinition> join = new ViewInstanceJoin<ViewDefinition>(tmp, new RestrictionManagerImpl());
        Op result = new OpViewInstanceJoin<ViewDefinition>(join);

        return result;
    }


    @Override
    public RestrictionManagerImpl getRestrictions2(Op op) {
        if(op instanceof OpMapping) {
            OpMapping opMapping = (OpMapping)op;
            return opMapping.getRestrictions();
        } else {
            // TODO Auto-generated method stub
            return super.getRestrictions2(op);
        }
    }

    @Override
    public void getRestrictions(Op op,
            Collection<RestrictionManagerImpl> result) {
         if(op instanceof OpRdfViewPattern) {
            OpRdfViewPattern o = (OpRdfViewPattern)op;
            result.add(o.getConjunction().getRestrictions());
         } else {
             super.getRestrictions(op, result);
         }
    }

}

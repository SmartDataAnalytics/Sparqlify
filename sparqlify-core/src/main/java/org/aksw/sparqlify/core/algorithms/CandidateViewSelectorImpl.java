package org.aksw.sparqlify.core.algorithms;

import java.util.List;

import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;





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

		logger.debug("Normalized view:\n" + normalized);
		
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
	public Op createOp(List<ViewInstanceJoin<ViewDefinition>> conjunctions) {
		
		
		for(ViewInstanceJoin<ViewDefinition> conjunction : conjunctions) {
			SelfJoinEliminator.merge(conjunction);
		}
		
		
		OpDisjunction result = OpDisjunction.create();
		
		for(ViewInstanceJoin<ViewDefinition> item : conjunctions) {
			Op tmp = new OpViewInstanceJoin(item);
			result.add(tmp);
		}
		
		return result;
	}
	
}

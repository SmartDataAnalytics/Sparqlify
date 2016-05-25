package org.aksw.sparqlify.util;

import org.aksw.jena_sparql_api.views.ExprEvaluator;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.transformations.RdfTermEliminator;
import org.aksw.sparqlify.core.transformations.RdfTermEliminatorWriteable;

/**
 * This class bundles the distinct layers of expression rewriting
 * together. This is used for e.g. loading user extension functions,
 * as their registration affects all these layers. 
 * 
 * 
 * @author raven
 *
 */
public class ExprRewriteSystem
{
	private TypeSystem typeSystem;
	private ExprEvaluator exprEvaluator;
	private RdfTermEliminatorWriteable termEliminator;
	private SqlExprSerializerSystem serializerSystem;

	public ExprRewriteSystem(TypeSystem typeSystem,
			RdfTermEliminatorWriteable termEliminator,
			ExprEvaluator exprEvaluator,
			SqlExprSerializerSystem serializerSystem) {
		super();

		this.typeSystem = typeSystem;
		this.termEliminator = termEliminator;
		this.exprEvaluator = exprEvaluator;
		this.serializerSystem = serializerSystem;
	}

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public ExprEvaluator getExprEvaluator() {
		return exprEvaluator;
	}
	
	
	public RdfTermEliminator getTermEliminator() {
		return termEliminator;
	}

	public SqlExprSerializerSystem getSerializerSystem() {
		return serializerSystem;
	}
}
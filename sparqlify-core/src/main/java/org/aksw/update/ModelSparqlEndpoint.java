package org.aksw.update;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelSparqlEndpoint
    extends QueryExecutionFactoryModel
{
	public ModelSparqlEndpoint() {
		super();
	}
	
	public ModelSparqlEndpoint(Model model) {
		super(model);
	}
	
	
    public void insert(Model model, String graphName) {
    	super.getModel().add(model);
    }
    
	public void remove(Model model, String graphName) {
		super.getModel().remove(model);
	}
}

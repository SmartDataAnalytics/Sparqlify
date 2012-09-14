package org.aksw.sparqlify.platform.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.ViewDefinition;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.util.FileManager;

import de.fuberlin.wiwiss.pubby.Configuration;


public class PubbyConfigFactory {

	
	private String baseUrl;
	private File baseConfigFile;
	private Config sparqlifyConfig;
	
	//"BaseServlet.serverConfiguration"
	
	public PubbyConfigFactory() {
	}
	
	public void setBaseConfigFile(File file) {
		this.baseConfigFile = file;
	}
	
	public File getBaseConfigFile() {
		return baseConfigFile;
	}
	
	
	
	/*
	public void setOverlayModel(Model model) {
		this.overlayModel = model;
	}
	
	public Model getOverlayModel() {
		return overlayModel;
	}
	*/
	
	
	public Config getSparqlifyConfig() {
		return sparqlifyConfig;
	}

	public void setSparqlifyConfig(Config sparqlifyConfig) {
		this.sparqlifyConfig = sparqlifyConfig;
	}

	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	
	
	public Set<String> getKnownPrefixes(Var var, ViewDefinition viewDef) {
		return new HashSet<String>();
		/*
		for(Constraint constraint = viewDef.getConstraints()) {
			
		}
		*/
	}
	
	public Set<String> getKnownPrefixes(ViewDefinition viewDef) {
		Set<String> result = new HashSet<String>();
		
		Set<Node> nodes = new HashSet<Node>();
		for(Triple triple : viewDef.getConstructPattern()) {
			nodes.add(triple.getSubject());
		}

		for(Node node : nodes) {
			if(node.isVariable()) {
				Set<String> prefixes = getKnownPrefixes((Var)node, viewDef);
				result.addAll(prefixes);
			} else if(node.isURI()) {
				result.add(node.getURI());
			}
		}
		
		return result;
	}
	
	public Set<String> getKnownPrefixes() {
		Set<String> result = new HashSet<String>();
		
		for(ViewDefinition viewDef : sparqlifyConfig.getViewDefinitions()) {
			Set<String> prefixes = getKnownPrefixes(viewDef);
			
			result.addAll(prefixes);
		}
		
		return result;
	}

	public Configuration create() {
		Model baseModel = FileManager.get().loadModel(baseConfigFile.getAbsoluteFile().toURI().toString()); 
		
		
		Model model = baseModel;
				
		Configuration result = new Configuration(model);
		

		return result;
	}
}

package org.aksw.sparqlify.platform.config;

import java.io.File;

import org.aksw.sparqlify.config.syntax.Config;

import com.hp.hpl.jena.rdf.model.Model;
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

	public Configuration create() {
		Model baseModel = FileManager.get().loadModel(baseConfigFile.getAbsoluteFile().toURI().toString()); 
		
		
		Model model = baseModel;
				
		Configuration result = new Configuration(model);
		return result;
	}
}

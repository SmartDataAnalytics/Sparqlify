package org.aksw.sparqlify.platform.config;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import de.fuberlin.wiwiss.pubby.Configuration;


public class PubbyConfigFactory {

	private File baseConfigFile;
	private Model overlayModel;
	
	//"BaseServlet.serverConfiguration"
	
	public PubbyConfigFactory() {
	}
	
	public void setBaseConfigFile(File file) {
		this.baseConfigFile = file;
	}
	
	public File getBaseConfigFile() {
		return baseConfigFile;
	}
	
	public void setOverlayModel(Model model) {
		this.overlayModel = model;
	}
	
	public Model getOverlayModel() {
		return overlayModel;
	}
	
	
	public Configuration create() {
		Model baseModel = FileManager.get().loadModel(baseConfigFile.getAbsoluteFile().toURI().toString()); 
		
		
		Model model = baseModel;
				
		Configuration result = new Configuration(model);
		return result;
	}
}

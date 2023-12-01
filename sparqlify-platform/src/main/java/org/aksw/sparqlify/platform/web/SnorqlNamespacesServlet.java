package org.aksw.sparqlify.platform.web;

import java.util.Map;

import javax.annotation.Resource;

import org.aksw.sparqlify.config.syntax.Config;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Component
@Path("/platform/namespaces.js")
@Produces("application/rdf+xml")
public class SnorqlNamespacesServlet {

	@Resource
	private Config config;
	
	public Config getConfig() {
		return config;
	}
	
	public void setConfig(Config config) {
		this.config = config;
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, "application/sparql-results+json" })
	public String getNamespaces() {
		
		String jsonMap = null;
		if(config != null) {
			Map<String, String> map = config.getPrefixMapping().getNsPrefixMap();
			
			Gson gson = new Gson();
			jsonMap = gson.toJson(map);			
		} else {
			jsonMap = "{}";
		}
		
		
		String result = "var D2R_namespacePrefixes = " + jsonMap;
		
		return result;
	}

}

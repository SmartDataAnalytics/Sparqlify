package org.aksw.sparqlify.platform.web;

import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.aksw.sparqlify.config.syntax.Config;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

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

package org.aksw.sparqlify.admin.web.endpoint;

import java.util.Map;

import javax.annotation.Resource;

import org.aksw.service_framework.core.SparqlService;
import org.aksw.sparqlify.config.syntax.Config;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Service
@Path("/{path}/sparql/namespaces.js")
//@Produces("application/json")
public class SnorqlNamespacesServlet {

    @Resource(name="sparqlServiceMap")
    private Map<String, SparqlService> nameToConfig;
    //private Map<String, PrefixMapping> nameToConfig;

//    
//	@Resource
//	private Config config;
//	
//	public Config getConfig() {
//		return config;
//	}
//	
//	public void setConfig(Config config) {
//		this.config = config;
//	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, "application/sparql-results+json" })
	public String getNamespaces(@PathParam("path") String path) {
		
	    //Config config = nameToConfig.get(path);
	    //PrefixMapping prefixMapping = nameToConfig.get(path);
	    SparqlService sparqlService = nameToConfig.get(path);
	    
		String jsonMap = null;
		if(sparqlService != null) {
		    Config config = (Config)sparqlService.getConfig();
		    
		    Map<String, String> map = config.getPrefixMapping().getNsPrefixMap();
			//Map<String, String> map = config.getPrefixMapping().getNsPrefixMap();
			
			Gson gson = new Gson();
			jsonMap = gson.toJson(map);			
		} else {
			jsonMap = "{}";
		}
		
		
		String result = "var D2R_namespacePrefixes = " + jsonMap;
		
		return result;
	}

}

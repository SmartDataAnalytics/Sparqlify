package org.aksw.sparqlify.admin.web.api;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.aksw.service_framework.core.ServiceRepository;
import org.aksw.sparqlify.admin.model.JdbcDataSource;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;


class CollectionJpa<T> {
	private EntityManagerFactory emf;
	private Class<T> clazz;
	
	public CollectionJpa(Class<T> clazz, EntityManagerFactory emf) {
		this.emf = emf;
		this.clazz = clazz;
	}
	
	public T get(Object id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		T result = em.find(clazz, id);
		
		em.getTransaction().commit();
		em.close();
		
		return result;
	}
}


@Service
@Path("/api/action")
public class ServletManager
{
	@Resource(name="entityManagerFactory")
	private EntityManagerFactory emf;
	
	@Resource
	private ServiceManager serviceManager;
	
	@Resource
	private ServiceRepository<?> serviceRepo;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/test")
	public String test() {
		return "{}";
	}

	
	/**
	 * TODO Somehow we need to create servlets from a configurable set of classes.
	 * The main question is how to inject the proper resources - i.e.
	 * So we somehow need to create an appropriate servlet context and servlet mapping object.
	 * This will probably be some fun fiddling around :/
	 * 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/testCreate")
	public String testCreate(@Context HttpServletRequest req, @Context HttpServletResponse res) {

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setJdbcUrl("jdbc:postgresql//foobar");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres"); //.toCharArray());
		dataSource.setPrimaryLabel("My datasource");
		dataSource.setPrimaryComment("");

		em.persist(dataSource);
		
		em.getTransaction().commit();
		
//		Criteria c = session.createCriteria(JdbcDataSource.class); //.add(Restrictions.eq("", "test spec"));
//		List<?> l = c.list();
//		for(Object o : l) {
//			System.out.println(o);
//		}
				

		em.close();
		
		
//		res.setContentType("text/plain");
//        res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
//        res.setHeader("Location", "foobar");
        
        
//		Dynamic dynamic = context.addServlet("SNORQL-Namespaces", "com.sun.jersey.spi.spring.container.servlet.SpringServlet");
//		dynamic.addMapping("SNORQL-Namespaces", "/foobar/*");
//		dynamic.setAsyncSupported(true);
//		dynamic.setInitParameter("com.sun.jersey.config.property.packages", "org.aksw.sparqlify.platform.web");
		//dynamic.setLoadOnStartup(1);
		
		
		//context.addServlet("/sparql", HttpSparqlEndpoint.class);
		
		//ServletContext context;
//		ServletHolder sh = new ServletHolder(ServletContainer.class);
//
//		sh.setInitParameter(
//				"com.sun.jersey.config.property.resourceConfigClass",
//				"com.sun.jersey.api.core.PackagesResourceConfig");
//
//		sh.setInitParameter("com.sun.jersey.config.property.packages",
//				"org.aksw.sparqlify.web");
//
//		Server server = new Server(port);
//		ServletContextHandler context = new ServletContextHandler(server, "/",
//				ServletContextHandler.SESSIONS);
//
//		QueryExecutionFactory qef = null;
//		
//		context.getServletContext().setAttribute("queryExecutionFactory", qef);
//		context.addServlet(sh, "/*");
//		
		
		return "{}";
	}

	
//	@POST
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/deleteContext")
//	public String deleteContext(@FormParam("id") Integer id) {
//		
////		EntityManager em = emf.createEntityManager();
////		em.getTransaction().begin();
////
////		Rdb2RdfConfig proto = new Rdb2RdfConfig();
////		proto.setId(id);
////		em.remove(proto);
////		
////		em.getTransaction().commit();
////		em.close();
////		
//		
//		return "{}";
//	}
	
	/**
	 * Create a new service based on the given configuration
	 * 
	 * @param json
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/createService")
	public String createInstance(
			@FormParam("data") String json
			/*
			@FormParam("path") String path,
			@FormParam("hostname") String hostname,
			@FormParam("dbname") String dbname,
			@FormParam("username") String username,
			@FormParam("password") String password,
			@FormParam("mappingText") String mappingText
			*/
		)
	{
		// configCollection.add(rdb2rdfConfig)
		// 
		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Gson gson = new Gson();
		Rdb2RdfConfig rdb2rdfConfig = gson.fromJson(json, Rdb2RdfConfig.class);
		System.out.println(json);
		System.out.println(rdb2rdfConfig);
		
		/*
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setJdbcUrl("jdbc:postgresql//foobar");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres".toCharArray());
		dataSource.setPrimaryLabel("My datasource");
		dataSource.setPrimaryComment("");

		TextResource textResource = new TextResource();
		textResource.setData(mappingText);
		textResource.setFormat("application/rdb2rdf-sml");
		textResource.setType("sml");
		textResource.setPrimaryComment("no comment");
		textResource.setPrimaryLabel("no label");

 
		Rdb2RdfConfig rdb2rdfConfig = new Rdb2RdfConfig();
		rdb2rdfConfig.setJdbcDatasource(jdbcDataSource);
		rdb2rdfConfig.setTextResource(textResource);
		*/

		em.persist(rdb2rdfConfig);

		em.flush();
		em.getTransaction().commit();
		em.close();

		//serviceManager.registerService(rdb2rdfConfig);
		serviceRepo.startByConfigId(rdb2rdfConfig.getId());
		//serviceManager.startService(rdb2rdfConfig.getId());
		
		return "{}";
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/deleteService")
	public String deleteService(@FormParam("id") String id) {
		
		// Remove the config object
		Object configId = serviceManager.getConfigId(id);
		if(configId == null) {
			throw new RuntimeException("No config found for id " + id);
		}
		
		serviceManager.deleteService(id);

		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		//serviceManager.deleteService(id);
		
		Rdb2RdfConfig config = em.find(Rdb2RdfConfig.class, configId);
		em.remove(config);
		
		em.getTransaction().commit();
		em.close();
				
				
		return "{}";
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/startService")
	public String startService(@FormParam("id") String id) {
	
		serviceManager.startService(id);		
		
		return "{}";
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/stopService")
	public String stopService(@FormParam("id") String id) {

		serviceManager.stopService(id);
		
		return "{}";
	}


}

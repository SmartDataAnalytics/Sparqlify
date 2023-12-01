package org.aksw.sparqlify.admin.web.main;

import org.aksw.jenax.web.filter.CorsFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;


public class WebAppInitializer
    implements WebApplicationInitializer
{
    @Override
    public void onStartup(ServletContext servletContext)
        throws ServletException
    {
        //if(true) throw new RuntimeException("yay");

        // Create the 'root' Spring application context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class);

        //System.out.println("CONTEXTPATH " + servletContext.getContextPath());

        // Manage the lifecycle of the root application context
        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.addListener(new RequestContextListener());

        {
            FilterRegistration.Dynamic fr = servletContext.addFilter("CorsFilter", new CorsFilter());
            fr.addMappingForUrlPatterns(null, true, "/*");
            fr.setAsyncSupported(true);
        }

        {
            FilterRegistration.Dynamic fr = servletContext.addFilter("UrlRewriteFilter", new UrlRewriteFilter());
            fr.setInitParameter("dispatcher", "REQUEST");
            fr.setInitParameter("dispatcher", "FORWARD");
            fr.addMappingForUrlPatterns(null, true, "/*");
            fr.setAsyncSupported(true);
        }

        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(WebMvcConfig.class);

        ServletRegistration.Dynamic adminServlet = servletContext.addServlet("sparqlify-admin-api", new ServletContainer());
        // com.sun.jersey.config.property.packages
        adminServlet.setInitParameter("jersey.config.server.provider.packages", "org.aksw.sparqlify.admin.web.api");
        adminServlet.addMapping("/manager/*");
        adminServlet.setAsyncSupported(true);
        adminServlet.setLoadOnStartup(1);

        ServletRegistration.Dynamic endpointServlet = servletContext.addServlet("sparqlify-endpoints", new ServletContainer());
        endpointServlet.setInitParameter("jersey.config.server.provider.packages", "org.aksw.sparqlify.admin.web.endpoint");
        endpointServlet.setLoadOnStartup(1);
        endpointServlet.setAsyncSupported(true);
        endpointServlet.addMapping("/services/*");

        // Register and map the dispatcher servlet
        ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("sparqlify-dispatcher", new DispatcherServlet(dispatcherContext));
        //dispatcherServlet.set
//		dispatcherServlet.addMapping("");
        dispatcherServlet.addMapping("*.do");
        //dispatcherServlet.addMapping("/**");
        dispatcherServlet.setLoadOnStartup(1);

    }
}


//public class WebAppInitializer
//	extends AbstractAnnotationConfigDispatcherServletInitializer
//{
//
//    @Override
//    protected Class<?>[] getRootConfigClasses() {
//        return new Class<?>[] { AppConfig.class };
//    }
//
//    @Override
//    protected Class<?>[] getServletConfigClasses() {
//        return new Class<?>[] { WebMvcConfig.class };
//    }
//
//    @Override
//    protected String[] getServletMappings() {
//        return new String[] { "/" };
//    }

//    @Override
//    protected Filter[] getServletFilters() {
//
//        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
//        characterEncodingFilter.setEncoding("UTF-8");
//
//        return new Filter[] { characterEncodingFilter, new SiteMeshFilter()};
//    }

//public class WebAppInitializer
//	implements WebApplicationInitializer
//{
//	private static final Logger logger = LoggerFactory.getLogger(WebAppInitializer.class);
//
//	@Override
//	public void onStartup(ServletContext servletContext) {
//	   // Create the root appcontext
//	   AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
//	   rootContext.register(WebAppConfig.class);
//	   // since we registered RootConfig instead of passing it to the constructor
//	   rootContext.refresh();
//
//	   // Manage the lifecycle of the root appcontext
//	   servletContext.addListener(new ContextLoaderListener(rootContext));
//	   servletContext.setInitParameter("defaultHtmlEscape", "true");
//
//	   // now the config for the Dispatcher servlet
//	   AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
//	   mvcContext.register(WebMvcConfig.class);
//
//	   // The main Spring MVC servlet.
//	   ServletRegistration.Dynamic appServlet = servletContext.addServlet(
//	      "appServlet", new DispatcherServlet(mvcContext));
//			appServlet.setLoadOnStartup(1);
//	   Set<String> mappingConflicts = appServlet.addMapping("/");
//
//	   if (!mappingConflicts.isEmpty()) {
//	      for (String s : mappingConflicts) {
//	         logger.error("Mapping conflict: " + s);
//	      }
//	      throw new IllegalStateException(
//	         "'appServlet' cannot be mapped to '/' under Tomcat versions <= 7.0.14");
//	   }
//
//		WebApplicationContext appContext = null;
//
//		ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
//				"dispatcher", new DispatcherServlet(appContext));
//		dispatcher.setLoadOnStartup(1);
//		dispatcher.addMapping("/");
//
//
////		FilterRegistration.Dynamic fr = servletContext.addFilter(
////				"encodingFilter", new CharacterEncodingFilter());
////		fr.setInitParameter("encoding", "UTF-8");
////		fr.setInitParameter("forceEncoding", "true");
////		fr.addMappingForUrlPatterns(null, true, "/*");
//	}
//}

//Set<String> mappingConflicts = appServlet.addMapping("/");
//
//if (!mappingConflicts.isEmpty()) {
//        throw new IllegalStateException("'appServlet' cannot be mapped to '/' under Tomcat versions <= 7.0.14");
//}


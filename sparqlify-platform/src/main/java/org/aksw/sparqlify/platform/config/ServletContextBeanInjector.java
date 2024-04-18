package org.aksw.sparqlify.platform.config;


import org.springframework.web.context.ServletContextAware;

import jakarta.servlet.ServletContext;

/**
 * 
 * I would prefer a pure XML solution, but this is fine enough.
 * http://forum.springsource.org/showthread.php?74354-Referencing-ServletContext-in-applicationContext-xml
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ServletContextBeanInjector //implements BeanFactoryPostProcessor,
		implements ServletContextAware {
	private ServletContext servletContext;

	/**
	 * ServletContextAware: Inject the Servlet Context
	 * 
	 * @param context
	 *            The Servlet Context
	 */
	@Override
	public void setServletContext(ServletContext ctx) {
		this.servletContext = ctx;
	}
	
	public void setAttribute(String key, Object value) {
		servletContext.setAttribute(key, value);
	}

	/**
	 * BeanFactoryPostProcessor: Post Process the Bean Factory
	 * 
	 * @param factory
	 *            The Bean Factory
	 */
	/*
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) {
		//factory.registerSingleton("servletContext", servletContext);
	}
	*/
}
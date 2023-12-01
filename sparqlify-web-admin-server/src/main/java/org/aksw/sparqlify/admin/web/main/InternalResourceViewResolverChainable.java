package org.aksw.sparqlify.admin.web.main;

import java.io.File;
import java.util.Locale;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import jakarta.servlet.http.HttpServletRequest;


/**
 * A chainable version of the InternalResourceViewResolver.
 * 
 * i.e. resolveViewName return null if the resource does not exist
 * 
 * @author raven
 *
 */
public class InternalResourceViewResolverChainable
	extends InternalResourceViewResolver
{
	@Override
	public View resolveViewName(String viewName, Locale locale)
			throws Exception
	{
		View result = null;
		
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String viewFile = request.getServletContext().getRealPath(this.getPrefix() + viewName + this.getSuffix());
		File reqFile = new File(viewFile);

		if(reqFile.exists()) {
			result = super.resolveViewName(viewName, locale);
		}		
		
		return result;
	}
}

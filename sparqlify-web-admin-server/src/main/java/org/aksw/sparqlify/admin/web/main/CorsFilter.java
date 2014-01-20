package org.aksw.sparqlify.admin.web.main;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Source: https://github.com/Atmosphere/atmosphere/wiki/Enabling-CORS 
 *
 */
public class CorsFilter
    implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);
    
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        
        HttpServletRequest req = (HttpServletRequest)request;                                   
        HttpServletResponse res = (HttpServletResponse)response;  

        if(req.getHeader("Origin") != null){
            res.addHeader("Access-Control-Allow-Origin", "*");
            res.addHeader("Access-Control-Expose-Headers", "X-Cache-Date, X-Atmosphere-tracking-id");
        }

        if("OPTIONS".equals(req.getMethod())){
            res.addHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST");
            res.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, X-Atmosphere-Framework, X-Cache-Date, X-Atmosphere-tracking-id, X-Atmosphere-Transport");
            res.addHeader("Access-Control-Max-Age", "-1");
        }                                              
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

        logger.info("CORS filter initialized");
        
    }
}
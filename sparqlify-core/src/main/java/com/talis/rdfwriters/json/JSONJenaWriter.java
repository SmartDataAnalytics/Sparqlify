/*
 * Copyright 2008, 2009, 2010 Talis Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.talis.rdfwriters.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class JSONJenaWriter implements RDFWriter{

	private static final Logger LOG = LoggerFactory.getLogger(JSONJenaWriter.class);

	static protected final String propBase = "http://jena.hpl.hp.com/json/properties/" ;

	private String lineSeparator = JenaRuntime.getLineSeparator() ;
	private RDFErrorHandler errorHandler = null;
    private Map<String, Object> writerPropertyMap = null ;
	private String baseURIref = null ;
	private String baseURIrefHash = null ;

	private Writer out = null;
	
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		RDFErrorHandler old = errorHandler;
		errorHandler = errHandler;
		return old;
	}

	public Object setProperty(String propName, Object propValue) {
        if ( ! ( propValue instanceof String ) )
        {
            LOG.warn("JSON.setProperty: Property for '"+propName+"' is not a string") ;
            propValue = propValue.toString() ;
        }
        
        // Store absolute name of property 
        propName = absolutePropName(propName) ;
        if ( writerPropertyMap == null )
            writerPropertyMap = new HashMap<String, Object>() ;
        Object oldValue = writerPropertyMap.get(propName);
        writerPropertyMap.put(propName, propValue);
        return oldValue;
	}

    protected String absolutePropName(String propName)
    {
        if ( propName.indexOf(':') == -1 )
            return JSONJenaWriter.propBase + propName ;
        return propName ;
    }
    
	
	public void write(Model model, OutputStream output, String base) {
		try {
			Writer w =  new BufferedWriter(new OutputStreamWriter(output, "UTF-8")) ;
			write(model, w, base) ;
			try { w.flush() ; } catch (IOException ioEx) {}
		} catch (java.io.UnsupportedEncodingException ex)
		{
			System.err.println("Failed to create UTF-8 writer") ;
		}		
	}
	
	public void write(Model baseModel, Writer _out, String base) {
        if (!(_out instanceof BufferedWriter))
            _out = new BufferedWriter(_out);
        
        out = _out;

        if ( base != null )
        {
            baseURIref = base ;
            if ( !base.endsWith("#") &&! isOpaque(base) )
                baseURIrefHash = baseURIref+"#" ;
        }
        
        try {
			processModel(baseModel) ;
		} catch (IOException e) {
			LOG.error("IOError writing model.", e);
		}
	}
	
    private boolean isOpaque(String uri)
    {
        try {
            return new URI(uri).isOpaque() ;
        } catch (URISyntaxException ex) { return true ; }
    }
	
    private void processModel(Model baseModel) throws IOException
    {
    	writeLine("{");
    	boolean first = true;
    	ResIterator subjectIterator = baseModel.listSubjects();
    	while (subjectIterator.hasNext()) {
    		if (!first) {
    			writeLine(",");
    		}
    		first = false;
    		Resource subjectResource = subjectIterator.nextResource();
    		processSubject(subjectResource);
    	}
    	writeLine("");
    	writeLine("}");
    }
    
    private void processSubject(Resource subjectResource) throws IOException {
    	write("  \"");
    	if (subjectResource.isAnon()) {
    		write("_:");
        	write(escape(subjectResource.asNode().getBlankNodeId().getLabelString()));    		
    	} else {
        	write(escape(subjectResource.getURI()));
    	}
    	writeLine("\" : {");    	
    	processProperties(subjectResource);    	
    	write("  }");    	
    }

    private void processProperties(Resource subjectResource) throws IOException {
    	HashSet<Property> propertiesToProcess = new HashSet<Property>();
    	StmtIterator allPropertiesIterator = subjectResource.listProperties();
    	while (allPropertiesIterator.hasNext()) {
    		Statement statement = allPropertiesIterator.nextStatement();
    		if (!propertiesToProcess.contains(statement.getPredicate().getURI())) {
    			propertiesToProcess.add(statement.getPredicate());
    		}
    	}
    	int processed = 0;
    	for (Property property : propertiesToProcess) {
    		write("    \"");
    		write(escape(property.getURI()));
    		write("\" : [ ");
    		StmtIterator propertyIterator = subjectResource.listProperties(property);
    		boolean moreThanOneValue = false;
    		while (propertyIterator.hasNext()) {
    			Statement propertyStatement = propertyIterator.nextStatement();
    			boolean moreValues = propertyIterator.hasNext();    			
    			if (moreValues) {
    				moreThanOneValue = true;
    			}
    			if (moreThanOneValue) {
    				writeLine("");
    				write("      ");
    			}
    			processProperty(propertyStatement);
    			if (moreValues) {
    				write(",");
    			}
    		}
    		if (moreThanOneValue) {
    			writeLine("");
    			write("    ");
    		} else {
    			write(" ");
    		}
    		write("]");
        	processed++;
    		if (processed < propertiesToProcess.size()) {
    			write(",");
    		} 
    		writeLine("");
    	}
    }

	private void processProperty(Statement property) throws IOException {
		write("{ \"value\" : \"");
		if (property.getObject().isURIResource()) {
			Resource r = (Resource)property.getObject();
			write(escape(r.getURI()));
			write("\", \"type\" : \"uri\"");
		} else if (property.getObject().isLiteral()) {
			Literal l = (Literal)property.getObject();
			write(escape(l.getLexicalForm()));
			write("\", \"type\" : \"literal\"");
			String languageValue = l.getLanguage();
			if (languageValue != null && !languageValue.trim().equals("")) {
				write(", \"lang\" : \"");
				write(languageValue);
				write("\"");
			}
			String dataTypeValue = l.getDatatypeURI();
			if (dataTypeValue != null && !dataTypeValue.trim().equals("")) {
				write(", \"datatype\" : \"");
				write(escape(dataTypeValue));
				write("\"");
			}
		} else if (property.getObject().isAnon()) {
			write("_:");
			write(escape(property.getObject().asNode().getBlankNodeId().getLabelString()));
			write("\", \"type\" : \"bnode\"");
		}
		write(" }");
	}
	
	protected String escape(String s) {
		  StringBuilder builder = new StringBuilder();
		  for (int i= 0; i<s.length(); i++) {
			  char c = s.charAt(i);
			  if (c == '"') {
				  builder.append("\\\"");
			  } else if (c == '\\') {
				  builder.append("\\\\");
			  } else if (c == '/') {
				  builder.append("\\/");
			  } else if (c == '\b') {
				  builder.append("\\b");
			  } else if (c == '\f') {
				  builder.append("\\f");
			  } else if (c == '\n') {
				  builder.append("\\n");
			  } else if (c == '\r') {
				  builder.append("\\r");
			  } else if (c == '\t') {
				  builder.append("\\t");
			  } else {
				  builder.append(c);
			  }
		  }
		  return builder.toString();
	}
    
    private void writeLine(String line) throws IOException {
    	write(line);
    	write(lineSeparator);
    }

    private void write(String line) throws IOException {
    	out.write(line);
    }


}

package exp.org.aksw.sparqlify.core;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.util.NQuadUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;




/**
 * Converts NQuads to a SPARQL result set.
 * 
 * 
 * 
 * @author raven
 *
 */
public class NQuadsToResultSet {
	public static ResultSet convert(InputStream in) {
		Set<Quad> quads = NQuadUtils.readNQuads(in);
		
		ResultSet result = createResultSet(quads);		
		return result;
	}
	
	
	public static ResultSet createResultSet(Iterable<Quad> quads) {
		ResultSet result = createResultSet(quads.iterator());
		
		return result;		
	}
	
	public static ResultSet createResultSet(Iterator<Quad> itQuads) {
		Function<Quad, Binding> q2b = new FunctionQuadToBinding();
		
		
		Iterator<Binding> itBinding = Iterators.transform(itQuads, q2b);
		QueryIter itQuery = new QueryIterPlainWrapper(itBinding);
		ResultSet result = new ResultSetStream(QuadUtils.quadVarNames, null, itQuery);

		return result;
	}	
}

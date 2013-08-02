package exp.org.aksw.sparqlify.core;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.core.test.R2rmlTest;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;




class FunctionQuadToBinding
	implements Function<Quad, Binding>
{
	@Override
	public Binding apply(@Nullable Quad quad) {
		Binding result = QuadUtils.quadToBinding(quad);
		return result;
	}
}


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
		Set<Quad> quads = R2rmlTest.readNQuads(in);
		
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

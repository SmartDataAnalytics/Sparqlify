package exp.org.aksw.sparqlify.core;

import javax.annotation.Nullable;

import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class FunctionQuadToBinding
	implements Function<Quad, Binding>
{
	@Override
	public Binding apply(@Nullable Quad quad) {
		Binding result = QuadUtils.quadToBinding(quad);
		return result;
	}
}
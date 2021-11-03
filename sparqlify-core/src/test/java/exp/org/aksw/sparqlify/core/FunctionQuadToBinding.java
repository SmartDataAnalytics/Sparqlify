package exp.org.aksw.sparqlify.core;

import javax.annotation.Nullable;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.base.Function;

public class FunctionQuadToBinding
    implements Function<Quad, Binding>
{
    @Override
    public Binding apply(@Nullable Quad quad) {
        Binding result = QuadUtils.quadToBinding(quad);
        return result;
    }
}
package org.aksw.sparqlify.core.cast;



import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * TODO Rename this class.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface NodeValueTransformer {
	NodeValue transform(NodeValue nodeValue) throws CastException;
}

/**
 * Converts types based on corresponding java classes.
 * TODO: What if we mapped e.g. xsd:byte to Integer?
 * In this case we would have to say that xsd:byte is an integer in a certain range.   
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
/*
public class CastSystemDefault
	implements CastSystem
{
	private TypeSystem typeSystem;

	private CoercionSystem coercionSystem = new CoercionSystemImpl2();

	
	/**
	 * TODO: Somehow we also need to get a warning on upcasts 
	 * 
	 * @param source
	 * @param targetTypeName
	 * @return
	 * /
	public static NodeValue cast(NodeValue source, String targetTypeName)
		throws CastException
	{
		coe
	}

	@Override
	public NodeValueTransformer lookupCast(String sourceTypeName,
			String targetTypeName)
	{
	}
}
*/
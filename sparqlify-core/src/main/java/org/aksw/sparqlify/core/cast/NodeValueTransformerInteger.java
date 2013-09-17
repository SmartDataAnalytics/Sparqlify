package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.expr.util.NodeValueUtils;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class NodeValueTransformerInteger implements NodeValueTransformer {
	@Override
	public NodeValue transform(NodeValue nodeValue) throws CastException {
		String str = "" + NodeValueUtils.getValue(nodeValue);
		TypeMapper tm = TypeMapper.getInstance();

		String typeName = TypeToken.Int.toString();
		// String typeName = XSD.integer.toString();
		RDFDatatype dt = tm.getSafeTypeByName(typeName);

		Node node = Node.createLiteral(str, dt);
		NodeValue result = NodeValue.makeNode(node);
		return result;
	}
}
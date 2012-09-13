package org.aksw.sparqlify.algebra.sparql.expr;

import org.aksw.sparqlify.core.Vocab;
import org.apache.commons.lang.NotImplementedException;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class NodeValueGeom
	extends NodeValue
{
	private PGgeometry geometry;
	
	public NodeValueGeom(PGgeometry geometry) {
		this.geometry = geometry;
	}
	
	
	public PGgeometry getGeometry()
	{
		return geometry;
	}

	@Override
	protected Node makeNode() {
		RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(Vocab.wktLiteral);
		
		Geometry g = geometry.getGeometry();
		Node result = Node.createLiteral(g.getTypeString() + g.getValue(), datatype);
		
		return result;
	}

	@Override
	public void visit(NodeValueVisitor visitor) {
		throw new NotImplementedException();
	}
}

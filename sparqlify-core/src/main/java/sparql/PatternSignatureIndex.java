package sparql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.MultiMaps;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;

public class PatternSignatureIndex
{
	private Query								query;

	private Map<IPatternSignature, Set<Quad>>	typeToPattern = new HashMap<IPatternSignature, Set<Quad>>();
	//private List<IPatternSignature> order;
	
	
	public PatternSignatureIndex(QuadPattern quadPattern) 
	{
		for(Quad quad : quadPattern) {
			IPatternSignature type = Equivalence.getSignature(quad);
			
			MultiMaps.put(typeToPattern, type, quad);
		}
	}
	
	@Deprecated
	public PatternSignatureIndex(Query query)
	{
		this.query = query;

		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);

		IndexOpVisitor visitor = new IndexOpVisitor(); 
		
		op.visit(visitor);
		
		for(Quad quad : visitor.quads) {
			IPatternSignature type = Equivalence.getSignature(quad);
			
			MultiMaps.put(typeToPattern, type, quad);
		}
		
		System.out.println("Algebra: " + op);
		System.out.println("Type: " + typeToPattern);

		// op.

		// TODO this is a hack that does't take nesting (optional) into
		// account
	}

	/*
	 * public QueryIndex(Map<IPatternType, Set<QuadPattern>> typeToPattern) {
	 * this.typeToPattern = typeToPattern; }
	 */

	public Map<IPatternSignature, Set<Quad>> getTypeToPattern()
	{
		return typeToPattern;
	}
}
package org.aksw.r2rml.impl.jena;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.google.common.collect.Iterators;

public class SetFromResourceAndProperty<T extends RDFNode>
	extends AbstractSet<T>
{
	protected Resource subject;
	protected Property property;
	protected Class<T> clazz;

	public SetFromResourceAndProperty(Resource subject, Property property, Class<T> clazz) {
		super();
		this.subject = subject;
		this.property = property;
		this.clazz = clazz;
	}

	@Override
	public boolean add(T o) {
		boolean result = false;
		if(!subject.hasProperty(property, o)) {
			subject.addProperty(property, o);
			result = true;
		}

		return result;
	}
	
	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = subject
				.listProperties()
				.mapWith(Statement::getObject)
				.filterDrop(item -> item.canAs(clazz))
				.mapWith(item -> item.as(clazz));
		return result;
	}

	@Override
	public int size() {
		Iterator<T> it = iterator();
		int result = Iterators.size(it);
		return result;
	}
}

package org.aksw.sparqlify.core.test;

import java.util.Comparator;

import org.springframework.core.io.Resource;

public class ResourceComparator
	implements Comparator<Resource>
{
	@Override
	public int compare(Resource a, Resource b) {
		return a.getFilename().compareTo(b.getFilename());
	}
}
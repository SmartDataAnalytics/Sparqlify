package org.aksw.changesets;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.collections.iterators.Descender;

public class RepositoryDescender
	implements Descender<Entry<Long, File>>
{
	@Override
	public Collection<Entry<Long, File>> getDescendCollection(Entry<Long, File> item) {
		Map<Long, File> result = ChangesetRepository.getDirectories(item.getValue());
		
		return result.entrySet();
	}
}
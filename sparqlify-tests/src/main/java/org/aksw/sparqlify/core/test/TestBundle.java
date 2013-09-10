package org.aksw.sparqlify.core.test;

import java.util.List;

import org.springframework.core.io.Resource;


/**
 * A test bundle is comprised of:
 * - A name
 * - A manifest
 * - An SQL resource from which a database can be created
 * - A number of mappings on this database
 * 
 * @author Claus Stadler
 *
 */
public class TestBundle {
	private String name;
	private Resource sql;
	private Resource manifest;
	private List<MappingBundle> mappingBundles;

	public TestBundle(String name, Resource sql, List<MappingBundle> mappingBundles, Resource manifest) {
		super();
		this.name = name;
		this.sql = sql;
		this.mappingBundles = mappingBundles;
		this.manifest = manifest;
	}
	
	public String getName() {
		return name;
	}

	public Resource getSql() {
		return sql;
	}
	
	public List<MappingBundle> getMappingBundles() {
		return mappingBundles;
	}
	
	public Resource getManifest() {
		return manifest;
	}
}

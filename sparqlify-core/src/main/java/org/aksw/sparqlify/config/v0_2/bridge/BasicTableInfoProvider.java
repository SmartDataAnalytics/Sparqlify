package org.aksw.sparqlify.config.v0_2.bridge;

@FunctionalInterface
public interface BasicTableInfoProvider
	//extends Function<String, BasicTableInfo>
{
	BasicTableInfo getBasicTableInfo(String queryString);
}

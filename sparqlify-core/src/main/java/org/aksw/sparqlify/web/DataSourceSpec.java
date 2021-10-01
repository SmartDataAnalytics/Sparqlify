package org.aksw.sparqlify.web;

public interface DataSourceSpec {
    String getName();
    Integer getPort();
    String getUsername();
    String getPassword();
    String getHostname();
    String getJdbcDriverClass();
    String getJdbcUrl();

    /** Maximum number of connections */
    Integer getBacklog();
}
package org.aksw.sparqlify.cli.cmd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.aksw.commons.picocli.VersionProviderFromClasspathProperties;

public class VersionProviderSparqlify
    extends VersionProviderFromClasspathProperties
{
    @Override public String getResourceName() { return "sparqlify.metadata.properties"; }
    @Override public Collection<String> getStrings(Properties p) {
        return Arrays.asList(p.get("sparqlify.version") + " built at " + p.get("sparqlify.build.timestamp"));
    }
}

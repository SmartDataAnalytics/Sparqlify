package org.aksw.sparqlify.cli.cmd;

import org.aksw.commons.picocli.CmdCommonBase;

import picocli.CommandLine.Command;


@Command(name="sparqlify", versionProvider = VersionProviderSparqlify.class, description = "Sparqlify Subcommands", subcommands = {
        CmdSparqlifyEndpoint.class,
        CmdExportSmlAsR2rml.class
})
public class CmdSparqlifyMain
    extends CmdCommonBase
{
}

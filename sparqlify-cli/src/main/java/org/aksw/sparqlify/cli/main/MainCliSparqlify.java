package org.aksw.sparqlify.cli.main;

import org.aksw.commons.picocli.CmdUtils;
import org.aksw.sparqlify.cli.cmd.CmdSparqlifyMain;

public class MainCliSparqlify {
    public static void main(String[] args) {
        CmdUtils.callCmd(new CmdSparqlifyMain(), args);
    }
}

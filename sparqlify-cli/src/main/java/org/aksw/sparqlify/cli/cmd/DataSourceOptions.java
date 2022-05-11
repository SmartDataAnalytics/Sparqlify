package org.aksw.sparqlify.cli.cmd;

import org.aksw.sparqlify.web.DataSourceSpec;

import picocli.CommandLine.Option;


/**
 * Data source options (for jdbc connections) intended to be used as a mixin
 * for picocli
 *
 * @author raven
 *
 */
public class DataSourceOptions implements DataSourceSpec {

    @Option(names = {"-d", "--database" }, description = "Database name")
    protected String name;

    @Option(names = { "-p", "--port" }, description = "Database port")
    protected Integer port = null;

    @Option(names = { "-U", "--username" }, description = "Database username")
    protected String username;

    @Option(names = { "-W", "--password" }, description = "Database password")
    protected String password;

    @Option(names = { "-h", "--hostname" }, description = "Database hostname")
    protected String hostname;

    @Option(names = { "-c" , "--class"}, description = "JDBC driver class")
    protected String jdbcDriverClass;

    @Option(names = { "-j", "--jdbcurl" }, description = "JDBC URL")
    protected String jdbcUrl;

    @Option(names = { "-B", "--backlog"}, description = "Backlog; maximum number of jdbc connections", defaultValue = "4")
    protected Integer backlog = null;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    public void setJdbcDriverClass(String jdbcDriverClass) {
        this.jdbcDriverClass = jdbcDriverClass;
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public Integer getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

}
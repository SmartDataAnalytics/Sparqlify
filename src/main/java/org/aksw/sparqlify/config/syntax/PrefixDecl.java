package org.aksw.sparqlify.config.syntax;

public class PrefixDecl {
	private String prefix;
	private String uri;

	public PrefixDecl() {
	}

	public PrefixDecl(String prefix, String uri) {
		super();
		this.prefix = prefix;
		this.uri = uri;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}

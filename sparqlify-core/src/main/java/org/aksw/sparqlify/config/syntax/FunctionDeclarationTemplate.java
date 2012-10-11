package org.aksw.sparqlify.config.syntax;

public class FunctionDeclarationTemplate
	implements FunctionDeclaration
{
	private FunctionSignature signature;
	private FunctionTemplate template;

	public FunctionDeclarationTemplate(FunctionSignature signature,
			FunctionTemplate template) {
		super();
		this.signature = signature;
		this.template = template;
	}

	public FunctionSignature getSignature() {
		return signature;
	}

	public FunctionTemplate getTemplate() {
		return template;
	}

	@Override
	public String toString() {
		return "Declare Function " + signature + " AS " +  template;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionDeclarationTemplate other = (FunctionDeclarationTemplate) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

}

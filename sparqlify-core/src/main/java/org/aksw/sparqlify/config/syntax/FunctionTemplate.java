package org.aksw.sparqlify.config.syntax;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.sparql.expr.ExprList;




public class FunctionTemplate {
	private String name;
	private ExprList exprList;

	public FunctionTemplate(String name, ExprList exprList) {
		super();
		this.name = name;
		this.exprList = exprList;
	}

	public String getName() {
		return name;
	}

	public ExprList getExprList() {
		return exprList;
	}

	
	@Override
	public String toString() {
		return name + "(" +  Joiner.on(", ").join(exprList) + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((exprList == null) ? 0 : exprList.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FunctionTemplate other = (FunctionTemplate) obj;
		if (exprList == null) {
			if (other.exprList != null)
				return false;
		} else if (!exprList.equals(other.exprList))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}

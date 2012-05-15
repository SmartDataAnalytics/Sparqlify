package org.aksw.sparqlify.database;

import org.apache.commons.lang.NotImplementedException;

interface CompareOp
{
	
}

public class CompareConstraint
	implements Constraint
{
	protected CompareOp op;
	protected Object value;

	public CompareConstraint(String variableName, CompareOp op, Object value) {
		this.op = op;
		this.value = value;
	}

	enum ComparoOp {
		LESS,
		LESS_EQUAL,
		GREATER,
		GREATE_EQUAL
	}

	public CompareOp getOperator() {
		return op;
	}

	@Override
	public boolean isSatisfiedBy(Object value) {
		throw new NotImplementedException();
	}	
}

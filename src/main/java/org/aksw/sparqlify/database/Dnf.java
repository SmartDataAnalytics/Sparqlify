package org.aksw.sparqlify.database;

import java.util.Collection;

public class Dnf
	extends ExprNormalForm
{
	public Dnf(Collection<Clause> clauses) {
		super(clauses);
	}
}

package org.aksw.sparqlify.database;

import java.util.Collection;

public class Cnf
	extends ExprNormalForm
{
	public Cnf(Collection<Clause> clauses) {
		super(clauses);
	}
}

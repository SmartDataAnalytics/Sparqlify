package org.aksw.sparqlify.util;

import java.util.Collection;

interface NormalForm<T> {
	public Collection<Clause<T>> getClauses();
}

interface Clause<T> {
	public Collection<T> getLiterals();
	public boolean contains(T literal);
}

class ClauseImpl<T>
	implements Clause<T>
{
	private Collection<T> literals;
	
	public ClauseImpl(Collection<T> literals) {
		this.literals = literals;
	}

	@Override
	public Collection<T> getLiterals() {
		return literals;
	}

	@Override
	public boolean contains(T literal) {
		// TODO Auto-generated method stub
		return false;
	}
}

class NormalFormImpl<T>
	implements NormalForm<T>
{
	private Collection<Clause<T>> clauses;

	public NormalFormImpl(Collection<Clause<T>> clauses) {
		this.clauses = clauses;
	}
	
	@Override
	public Collection<Clause<T>> getClauses() {
		return clauses;
	}
}

//class NormalFormCollectionFactory<T> {
//	private Collection<T> newClause();
//}



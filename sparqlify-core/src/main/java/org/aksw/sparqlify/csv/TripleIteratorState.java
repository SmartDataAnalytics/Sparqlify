package org.aksw.sparqlify.csv;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.sparql.core.Var;

public class TripleIteratorState {
	private int totalTripleCount = 0;
	private int tripleGenCount = 0;
    private Map<Var, Integer> varCountMap = new HashMap<Var, Integer>();

    public TripleIteratorState(int totalTripleCount, int tripleGenCount,
			Map<Var, Integer> varCountMap) {
		super();
		this.totalTripleCount = totalTripleCount;
		this.tripleGenCount = tripleGenCount;
		this.varCountMap = varCountMap;
	}

    public int getTotalTripleCount() {
		return totalTripleCount;
	}
	public int getTripleGenCount() {
		return tripleGenCount;
	}
	public Map<Var, Integer> getVarCountMap() {
		return varCountMap;
	}
}
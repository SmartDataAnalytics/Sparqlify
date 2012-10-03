package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.sparql.expr.Expr;

public class Projection {
	private List<String> names = new ArrayList<String>();
	private Map<String, Expr> nameToExpr = new HashMap<String, Expr>();
	
	public Projection() {
		
	}

	public Projection(Map<String, Expr> nameToExpr) {
		this.nameToExpr = nameToExpr;
		this.names = new ArrayList<String>(nameToExpr.keySet());
	}
	
	public Projection(List<String> names, Map<String, Expr> nameToExpr) {
		this.names = names = names;
		this.nameToExpr = nameToExpr;
	}
	
	
	public void add(Projection other) {
		names.addAll(other.getNames());
		nameToExpr.putAll(other.getNameToExpr());
	}
	
	
	public void rename(String oldName, String newName) {
		Collections.replaceAll(names, oldName, newName);
		
		Expr val = nameToExpr.get(oldName);
		nameToExpr.remove(oldName);
		nameToExpr.put(newName, val);
	}
	
	public void renameAll(Map<String, String> oldToNew) {
		for(Entry<String, String> entry : oldToNew.entrySet()) {
			rename(entry.getKey(), entry.getValue());
		}
	}
	
	public void put(String name, Expr expr) {
		if(!nameToExpr.containsKey(name)) {
			names.add(name);
		}
		nameToExpr.put(name, expr);
	}
	
	public List<String> getNames() {
		return names;
	}
	
	
	public Map<String, Expr> getNameToExpr() {
		return nameToExpr;
	}


	@Override
	public String toString() {
		return "Projection [names=" + names + ", nameToExpr=" + nameToExpr
				+ "]";
	}
	
	
}

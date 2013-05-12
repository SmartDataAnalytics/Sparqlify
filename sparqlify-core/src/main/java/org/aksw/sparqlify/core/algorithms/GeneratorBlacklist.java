package org.aksw.sparqlify.core.algorithms;

import java.util.Collection;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;

public class GeneratorBlacklist
	implements Generator 
{
	private Generator generator;
	private Collection<String> blacklist;
	
	public GeneratorBlacklist(Generator generator, Collection<String> blacklist) {
		this.generator = generator;
		this.blacklist = blacklist;
	}

	@Override
	public String next() {
		String result;
		do {
			
			result = generator.next();
			
		} while(blacklist.contains(result));
		
		return result;
	}

	@Override
	public String current() {
		String result = generator.current();
		return result;
	}
	
	
	public static GeneratorBlacklist create(String base, Collection<String> blacklist) {
		Generator generator = Gensym.create(base);
		GeneratorBlacklist result = create(generator, blacklist);
		return result;
	}
	
	public static GeneratorBlacklist create(Generator generator, Collection<String> blacklist) {
		GeneratorBlacklist result = new GeneratorBlacklist(generator, blacklist);
		return result;
	}

	@Override
	public String toString() {
		return "current: " + generator.current() + ", blacklist: " + blacklist;
	}
}

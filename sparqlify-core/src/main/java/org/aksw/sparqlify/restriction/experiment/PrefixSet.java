package org.aksw.sparqlify.restriction.experiment;

import java.util.HashSet;
import java.util.Set;

import org.aksw.sparqlify.restriction.Polarity;
import org.aksw.sparqlify.restriction.Prefix;


/**
 * A class which denotes a set of strings with common prefix.
 * Exceptions may pertain to the set, with following rules:
 * - The exception must be part of the set
 * - The polarity of the exception is opposite of that of the base 
 * 
 * 
 * 
 * 
 * @author raven
 *
 */
public class PrefixSet {

	private Polarity polarity;

	private Prefix value;
	private Set<PrefixSet> exceptions;

	// OPTIMIZE We could use tries for speeding up prefix lookups
	// but we are not doing that many lookups anyway
	//private PatriciaTrie<String, String> exceptions;
	
	public PrefixSet(Prefix value) {
		this(value, Polarity.POSITIVE);
	}
	
	public PrefixSet(Prefix value, Polarity polarity) {
		this(value, polarity, new HashSet<PrefixSet>());
	}
	
	public PrefixSet(Prefix value, Polarity polarity, Set<PrefixSet> exceptions) {
		this.value = value;
		this.polarity = polarity;
		this.exceptions = exceptions;
	}
	


	public Polarity getPolarity() {
		return polarity;
	}

	public Prefix getValue() {
		return value;
	}
	
	public Set<PrefixSet> getExceptions() {
		return exceptions;
	}
	
	public boolean isPrefixOf(String str) {
		boolean result;

		if(value.isConstant()) {
			result = str.equals(value.getPrefix());
		} else {
			result = str.startsWith(value.getPrefix());
		}
		
		return result;
	}
	
	public boolean contains(String str) {
		boolean result = contains(new Prefix(str));
		return result;
	}
	
	public boolean contains(Prefix that) {
		boolean result = this.value.isPrefixOf(that);
		
		if(polarity.equals(Polarity.NEGATIVE)) {
			result = !result;
		}
		
		// Check if exceptions negate this
		for(PrefixSet exception : exceptions) {
			if(exception.contains(that)) {
				result = !result;
				break;
			}				
		}

		return result;
	}

	public PrefixSet getExceptionFor(Prefix prefix) {
		for(PrefixSet ex : exceptions) {
			if(ex.value.isPrefixOf(prefix)) {
				return ex;
			}
		}
		
		return null;
	}
	
	public Set<PrefixSet> getSuffixExceptions(Prefix prefix) {
		Set<PrefixSet> result = new HashSet<PrefixSet>();
		
		for(PrefixSet ex : exceptions) {
			if(prefix.isPrefixOf(ex.value)) {
				result.add(ex);
			}
		}

		return result;
	}
	
	/**
	 * Adds an exception to the current set.
	 * 
	 * Note that if {owla} is added, but {owl} is already an exception,
	 * then the exception is subsumed (i.e. not added)
	 * 
	 * On the other hand, if {owl} is added, but {owla, ..., owlz} exists, then
	 * all their exceptions are added to owl
	 * 
	 * 
	 * @param exception
	 * @return
	 */
	public PrefixSet addException(Prefix exception) {
		boolean isValid = this.contains(exception);
		if(!isValid) {
			throw new RuntimeException("Cannot add exception: " + exception + ", current state: " + this);
		}

		PrefixSet subsumed = getExceptionFor(exception);

		if(subsumed == null) {
			subsumed = new PrefixSet(exception);
			exceptions.add(subsumed);			
		}
		
		
		Set<PrefixSet> suffixes = getSuffixExceptions(exception);
		this.exceptions.removeAll(suffixes);
		
		
		for(PrefixSet suffix : suffixes) {
			subsumed.addException(suffix);
		}		
		
		return subsumed;
	}

	
	/**
	 * Adds an exception:
	 * Example:
	 *    prefix = {owl}
	 *    addException(owla)
	 *    addException(owlab)
	 * 
	 * @param exception
	 * @return
	 */
	public void addException(PrefixSet that) {
		for(PrefixSet exs : that.exceptions) {
			Prefix p = exs.value;
		
			PrefixSet added = this.addException(p);
			for(PrefixSet exs2 : exs.exceptions) {
				added.addException(exs2);
			}
		}
	}
	
	/**
	 * Returns the set of exceptions with the given prefix
	 * @param prefix
	 */
	public Set<PrefixSet> getExceptions(String prefix) {
		Set<PrefixSet> result = new HashSet<PrefixSet>();
		for(PrefixSet exception : exceptions) {
			if(exception.contains(prefix)) {
				result.add(exception);
			}				
		}
		
		return result;
	}

	/*
	public static Set<PrefixSet2> intersect(Set<PrefixSet2> as, Set<PrefixSet2> bs) {
		for(PrefixSet2 a : as) {
			for(PrefixSet2 b : bs) {
				
			}
		}
	}
	*/
	
	public static Prefix intersect(Prefix a, Prefix b) {
	
		Prefix result;
		if(a.isConstant() && b.isConstant()) {
			if(a.getPrefix().equals(b.getPrefix())) {
				result = a;
			} else {
				result = null;
			}			
		} else {
			
			String intersection = intersect(a.getPrefix(), b.getPrefix());
			
			if(a.isConstant()) {
				result = a.getPrefix().equals(intersection)
						? a
						: null;
			} else if(b.isConstant()) {
				result = b.getPrefix().equals(intersection)
						? b
						: null;				
			} else {
				result = new Prefix(intersection);
			}
		}
		
		return result;
	}
	
	public static String intersect(String a, String b) {
		String shorter;
		String longer;
		
		// Determine the shorter and loger prefix
		if(a.length() < b.length()) {
			shorter = a;
			longer = b;
		} else {
			shorter = b;
			longer = a;
		}
		
		String result;
		if(longer.startsWith(shorter)) {
			result = shorter;
		} else {
			result = null;
		}

		return result;
	}

	/**
	 * How to deal with exceptions:
	 * - This is not owl, except for owl:Class
	 * - This is rdf, except for rdf:type
	 * 
	 * 
	 * case: positive - positive
	 *     Create the intersection:
	 *        {owl} and {rdf} -> {}
	 *        {owl} and {owla} -> {owla} (owla is more specific)  
	 * 
	 * case: positive - negative
	 *        {owl} and not{owla, rdf} -> {owl} except {owla} (positive prevails)
	 *        {owl} and not{owla} -> {owl[except owla]} (exception)}
	 *        not{owla} and {owl} -> same as above 
	 *        not{owl} and {owla} -> {} (owl namespace was excluded)
	 * 
	 * case: negative - negative
	 *     Create the union:
	 *         not{owl} and not{rdf} -> not{rdf, owl}
	 *         not(owl} and not{owla} -> not{owl} (owla subsumed by the shorter prefix)
	 * 
	 */
	public PrefixSet intersects(PrefixSet that) {

		PrefixSet result;

		if(this.polarity.isPositive()) {
			if(that.polarity.isPositive()) {
				
				Prefix intersection = intersect(this.value, that.value);

				if(intersection == null) {
					Prefix p = new Prefix("");
					result = new PrefixSet(p, Polarity.NEGATIVE);
				} else {
					result = new PrefixSet(intersection, Polarity.POSITIVE);					
				}

				// Add the combined exceptions
				for(PrefixSet ps : this.exceptions) {
					result.addException(ps);
				}

				for(PrefixSet ps : that.exceptions) {
					result.addException(ps);
				}

			}
			
		}
		
		return that;
		
	}


	/**
	 * Rule of thumb: Shorter prefixes subsume longer ones
	 * {owl} union {owla} -> {owl}
	 * 
	 * The exceptions are then also unioned:
	 * for each exception it is checked, whether there is a shorter exception
	 * 
	 * case: positive - positive
	 *        {owl} union {rdf} -> {} except {owl, rdf}
	 *        {owl} union {owla} -> {owl}  
	 * 
	 * case: positive - negative
	 *        {owl} union not{owla, rdf} -> {} except {owl, rdf}
	 *        {owl} union not{owla} -> {owl[except owla]} (exception)}
	 *        not{owla} and {owl} -> same as above 
	 *        not{owl} and {owla} -> {} (owl namespace was excluded)
	 * 
	 * case: negative - negative
	 *     Create the union:
	 *         not{owl} union not{rdf} -> not{rdf, owl}
	 *         not(owl} and not{owla} -> not{owl} (owla subsumed by the shorter prefix)
	 * 
	 * 
	 * @param that
	 * @return
	 */
	public PrefixSet union(PrefixSet that) {
		return that;
		
	}

		
	public PrefixSet negate() {

		Set<PrefixSet> newExceptions = new HashSet<PrefixSet>();
		for(PrefixSet exception : exceptions) {
			
			PrefixSet newException = exception.negate();
			newExceptions.add(newException);
		}

		Polarity newPolarity
			= polarity.equals(Polarity.POSITIVE)
			? Polarity.POSITIVE
			: Polarity.NEGATIVE
			;
		
		PrefixSet result = new PrefixSet(this.value, newPolarity, newExceptions);

		return result;
	}


}

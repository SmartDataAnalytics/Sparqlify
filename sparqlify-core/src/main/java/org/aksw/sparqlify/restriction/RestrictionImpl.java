package org.aksw.sparqlify.restriction;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.sparqlify.config.lang.PrefixSet;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.graph.Node;
import com.karneim.util.collection.regex.PatternPro;




/**
 * This class represents restrictions to be used on variables.
 * 
 * 
 * Rules
 * . Stating a constant value (node) must be consistent with at least one prefix (if there are any), or equivalent to a previous value.
 *   Additionally all prefixes are removed in that case.
 * 
 * . If a restriction is inconsistent, retrieving fields is meaningless, as their values are not defined.
 * 
 * 
 * .) Methods return true if a change occurred
 * 
 * . TODO More details
 * 
 * 
 * Further statements could be:
 * 
 * statePattern()
 * stateRange(min, max)
 * stateDatatype()
 * 
 * I really hope I am not ending up with my own Datalog+Constraints engine :/
 * 
 * 
 * TODO: Maybe the set of uriPrefixes should be replaced with a single prefix -
 * so that an instance of restriction really only states a single restriction.
 * 
 * So my problem is how to deal with dis/conjunctions of restrictions efficiently
 *  
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class RestrictionImpl
	implements Restriction
{
	private RdfTermType type = RdfTermType.UNKNOWN;
	private Node node;
	private PrefixSet uriPrefixes;

	// TODO Actually it should be: isInconsistent(); - We are not making consistency guaratees here
	//private boolean isConsistent = true;
	private Boolean satisfiability = Boolean.TRUE;
	
	
	/**
	 * Return true if 'this' is equal to or less restrictive than other
	 * 
	 * @param other
	 * @return
	 */
	public boolean subsumesOrIsEqual(RestrictionImpl other) {
		if(type != other.type) {
			return false;
		}
		
		if(this.node != null) {
			if(other.node == null) {
				return false;
			} else {
				if(!this.node.equals(other.node)) {
					return false;
				}
			}
		}
		
		if(uriPrefixes != null) {
			// Check of on of the prefixes is a prefix of the constant
			if(other.node != null) {
				if(this.uriPrefixes.containsPrefixOf(other.node.toString())) {
					return true;
				}
			}
			
			if(other.getUriPrefixes() == null) {
				return false;
			} else {
				// Test whether each of this.prefixes is a prefix of other
				for(String prefix : other.getUriPrefixes().getSet()) {
					if(!this.uriPrefixes.containsPrefixOf(prefix)) {
						return false;
					}
				}
			}
		}
				
		
		return true;
	}
	
	public RestrictionImpl clone() {
		return new RestrictionImpl(this);
	}
	
	public RestrictionImpl() {
		satisfiability = Boolean.TRUE;
	}
	
	public RestrictionImpl(RdfTermType type) {
		this.stateType(type);
	}
	
	public RestrictionImpl(PrefixSet prefixSet) {
		this.stateUriPrefixes(prefixSet);
	}
	
	public RestrictionImpl(Node node) {
		this.stateNode(node);
	}
	
	public RestrictionImpl(RestrictionImpl other) {
		this.type = other.type;
		this.node = other.node;
		if(other.uriPrefixes != null) {
			this.uriPrefixes = new PrefixSet(other.uriPrefixes);
		}
		this.satisfiability = other.satisfiability;
	}

	public boolean hasConstant() {
		return isConsistent() && node != null;
	}
	
	public boolean hasPrefix() {
		return !hasConstant() && uriPrefixes != null;
	}

	/**
	 * Get the rdf term type of this restriction.
	 * Deprecated because this can be a set.
	 * 
	 * @return
	 */
	@Deprecated
	public RdfTermType getType() {
		return type;
	}
	
	public EnumSet<RdfTermType> getRdfTermTypes() {
		EnumSet<RdfTermType> result = EnumSet.noneOf(RdfTermType.class);
		result.add(type);
		
		return result;
	}
	
	public Node getNode() {
		return node;
	}
	
	public PrefixSet getUriPrefixes() {
		return uriPrefixes;
	}

	/* (non-Javadoc)
	 * @see org.aksw.sparqlify.restriction.IRestriction#stateRestriction(org.aksw.sparqlify.restriction.Restriction)
	 */
	@Override
	public boolean stateRestriction(Restriction that) {
		RestrictionImpl other = (RestrictionImpl)that;
		
		if(other.satisfiability == Boolean.TRUE) {
			return false;
		}
		
		satisfiability = TernaryLogic.and(this.satisfiability, other.satisfiability);
		//isConsistent = this.isConsistent != false && other.isConsistent != false;
		
		if(satisfiability == Boolean.FALSE) {
			return false;
		} else if(other.node != null) {
			return stateNode(other.node);
		} else if(other.uriPrefixes != null) {
			return stateUriPrefixes(other.uriPrefixes);
		} else if(other.getType() != RdfTermType.UNKNOWN) {
			return stateType(other.type);
		}
		
		throw new RuntimeException("Should not happen");
	}

	
	/* (non-Javadoc)
	 * @see org.aksw.sparqlify.restriction.IRestriction#stateType(org.aksw.sparqlify.restriction.Type)
	 */
	@Override
	public boolean stateType(RdfTermType newType) {		
		if(satisfiability == Boolean.FALSE) {
			return false;
		}

		if(type == RdfTermType.UNKNOWN) {
			if(newType != RdfTermType.UNKNOWN) {
				type = newType;
				satisfiability = null;
				return true;
			}
			return false;
		} else {
			if(type.equals(newType)) {
				return false;
			} else {			
				satisfiability = Boolean.FALSE;
				return true;
			}
		}
	}
	
	
	public static RdfTermType getNodeType(Node node) {
		if(node == null) {
			return RdfTermType.UNKNOWN;
		} else if(node.isURI()) {
			return RdfTermType.URI;
		} else if(node.isLiteral()) {
			return RdfTermType.LITERAL;
		} else {
			throw new RuntimeException("Should not happen");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.aksw.sparqlify.restriction.IRestriction#stateNode(com.hp.hpl.jena.graph.Node)
	 */
	@Override
	public boolean stateNode(Node newNode) {
		boolean change = stateType(getNodeType(newNode));
		
		if(satisfiability == Boolean.FALSE) {
			return change;
		}
		
		if(node == null) {			
			if(uriPrefixes != null) {
				/*
				if(!node.isURI()) {
					satisfiability = Boolean.FALSE;
					return true;
				}*/

				if(!uriPrefixes.containsPrefixOf(newNode.getURI())) {
					satisfiability = Boolean.FALSE;
					return true;
				}			
			}		

			node = newNode;
			satisfiability = null;

			return true;
			
		} else {
		
			if(!node.equals(newNode)) {
				satisfiability = Boolean.FALSE;
				return true;
			}
			
			return false;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.aksw.sparqlify.restriction.IRestriction#stateUriPrefixes(org.aksw.sparqlify.config.lang.PrefixSet)
	 */
	@Override
	public boolean stateUriPrefixes(PrefixSet prefixes) {
		if(prefixes.isEmpty()) {
			throw new RuntimeException("Should not happen");
		}
		
		boolean change = stateType(RdfTermType.URI);		
		
		if(satisfiability == Boolean.FALSE) {
			return change;
		}

		
		if(node != null) {
			if(!node.isURI() || !prefixes.containsPrefixOf(node.getURI())) {
				satisfiability = Boolean.FALSE;
				
				return true;
			}

			// We have a constant, no need to track the prefixes
			return false;
		}
		
		// If no prefixes have been stated yet, state them
		if(uriPrefixes == null) {
			uriPrefixes = new PrefixSet();
			for(String s : prefixes.getSet()) {
				Set<String> ps = uriPrefixes.getPrefixesOf(s);			
				uriPrefixes.removeAll(ps);
				uriPrefixes.add(s);
			}

			satisfiability = uriPrefixes.isEmpty() ? false : null;
			return true;
		} else if(prefixes.isEmpty()) {
			
			// If we get here, then we were not inconsistent yet
			// TODO Not sure if the satisfiability computation also works for TRUE
			if(uriPrefixes.isEmpty()) {
				satisfiability = Boolean.FALSE;
				return true;
			} else {
				return false;
			}
		}
		
		// {http:, mailto:addr} {http://foo, mailto:}
		
		// Note: If we have prefixes Foo and FooBar, we keep FooBar, which is more restrictive.
		for(String s : prefixes.getSet()) {
			Set<String> ps = uriPrefixes.getPrefixesOf(s, false);			
			if(!ps.isEmpty()) {
				uriPrefixes.removeAll(ps);
				uriPrefixes.add(s);
			}
		}
		
		// Remove all entries that do not have a prefix in the other set
		Iterator<String> it = uriPrefixes.getSet().iterator();
		while(it.hasNext()) {
			String s = it.next();
			Set<String> ps = prefixes.getPrefixesOf(s);
			if(ps.isEmpty()) {
				it.remove();
			}
		}
		
		if(uriPrefixes.isEmpty()) {
			satisfiability = Boolean.FALSE;
			return true;
		}
		
		// TODO Could sometimes return false
		return true;
		//return change;
	}
	
	// To be done.
	/* (non-Javadoc)
	 * @see org.aksw.sparqlify.restriction.IRestriction#statePattern(com.karneim.util.collection.regex.PatternPro)
	 */
	@Override
	public void statePattern(PatternPro pattern) {
		// If there is a pattern already, make it the intersection with the new pattern
		
		// If there is a node, check if it conforms to the pattern
		
		// If there are prefixes, check if they conform to the pattern
		
		throw new NotImplementedException();
	}

	
	/**
	 * If the restriction is unconstrained, we return true
	 * If it is inconsistent false,
	 * and null otherwise 
	 * @return
	 */
	public Boolean getSatisfiability() {
		return satisfiability;
	}
	
	public boolean isConsistent() {
		return satisfiability != Boolean.FALSE;
	}


	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result
				+ ((satisfiability == null) ? 0 : satisfiability.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((uriPrefixes == null) ? 0 : uriPrefixes.hashCode());
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
		RestrictionImpl other = (RestrictionImpl) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (satisfiability == null) {
			if (other.satisfiability != null)
				return false;
		} else if (!satisfiability.equals(other.satisfiability))
			return false;
		if (type != other.type)
			return false;
		if (uriPrefixes == null) {
			if (other.uriPrefixes != null)
				return false;
		} else if (!uriPrefixes.equals(other.uriPrefixes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if(satisfiability != null) {
			return satisfiability.toString();
		} else if(hasConstant()) {
			return "" + node;
		} else if(hasPrefix()){
			return "" + uriPrefixes;
		} else if(type != RdfTermType.UNKNOWN) {
			return "" + type;
		} else {
			return "Invalid state";
		}
	}

	/* (non-Javadoc)
	 * @see org.aksw.sparqlify.restriction.IRestriction#isUnsatisfiable()
	 */
	@Override
	public boolean isUnsatisfiable() {
		return satisfiability == Boolean.FALSE;
	}
}
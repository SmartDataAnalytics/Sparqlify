package org.aksw.sparqlify.restriction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ValueSet<T>
{
	private Set<T> values;
	private boolean isPositive;

	public ValueSet(Set<T> values) {
		this.values = values;
		this.isPositive = true;
	}

	public ValueSet(boolean isPositive, Set<T> values) {
		this.values = values;
		this.isPositive = isPositive;
	}

	/*
	public ValueSet(boolean isPositive, T ...values) {
		this.isPositive = isPositive;
		this.values = new HashSet<T>(Arrays.asList(values));
	}*/
	
	
	public static <T> ValueSet<T> create(boolean isPositive, T ... values) {
		//this.isPositive = isPositive;
		Set<T> v = new HashSet<T>(Arrays.asList(values));
		ValueSet<T> result = new ValueSet<T>(isPositive, v);
		
		return result;
	}
	
	/**
	 * case: positive - positive
	 *     Simply take the intersection
	 *     
	 * case: positive - negative
	 *     {1, 2, 3} intersect {not {2}}: -> {1, 3} (positive.removeAll(negative)) 
	 *     
	 * case: negative - positive
	 *     Same as above     
	 *     
	 * case: negative - negative
	 *     Simply take the union
	 * 
	 * 
	 * @param other
	 * @return
	 */
	public ValueSet<T> intersect(ValueSet<T> that) {
		Set<T> set = new HashSet<T>();
		boolean isPos = true;

		if(isPositive) {
			if(that.isPositive) {
				set.addAll(this.values);
				set.retainAll(that.values);

			} else {

				set.addAll(this.values);
				set.removeAll(that.values);
			}
		} else {
			if(that.isPositive) {

				set.addAll(that.values);
				set.removeAll(this.values);
			} else {
				
				set.addAll(this.values);
				set.addAll(that.values);
				isPos = false;				
			}
		}
		
		ValueSet<T> result = new ValueSet<T>(isPos, set);
		return result;
	}
	
	
	/**
	 * case: positive - positive
	 *     Simply take the union
	 *     
	 * case: positive - negative
	 *     {1, 2, 3} intersect {not {1, 4}}: -> {4} (negative.removeAll(positive)) 
	 *     
	 * case: negative - positive
	 *     Same as above     
	 *     
	 * case: negative - negative
	 *     Simply take the intersection
	 * 
	 * @param that
	 * @return
	 */
	public ValueSet<T> union(ValueSet<T> that) {
		Set<T> set = new HashSet<T>();
		boolean isPos = true;

		if(isPositive) {
			if(that.isPositive) {
				set.addAll(this.values);
				set.addAll(that.values);

			} else {

				set.addAll(that.values);
				set.removeAll(this.values);
				isPos = false;
			}
		} else {
			if(that.isPositive) {

				set.addAll(this.values);
				set.removeAll(that.values);
				isPos = false;
			} else {
				
				set.addAll(this.values);
				set.retainAll(that.values);				
			}
		}
		
		ValueSet<T> result = new ValueSet<T>(isPos, set);
		return result;
	}
	
	public ValueSet<T> negate() {
		ValueSet<T> result = new ValueSet<T>(!isPositive, values);
		return result;
	}
	
	
	public boolean isEmpty() {
		return isPositive && values.isEmpty();
	}

	
	public boolean contains(Object item) {
		boolean isContained = values.contains(item); 
		
		boolean result = isPositive
				? isContained
				: !isContained;

		return result;
	}
	
	
	
	
	@Override
	public String toString() {
		String polarity = (isPositive) ? "+" : "-";
		
		return polarity + values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isPositive ? 1231 : 1237);
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		ValueSet<?> other = (ValueSet<?>) obj;
		if (isPositive != other.isPositive)
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}
}
package sparql;

import java.util.HashSet;
import java.util.Set;

/**
 * A class denoting a positive or negative (intensional) set of values. 
 * 
 * Negative with empty values denotes the set containing all values.
 * 
 * @author raven
 *
 * @param <T>
 */
public class ValueSet<T>
{
	private Set<T> values;
	private Boolean polarity;

	public ValueSet(Set<T> values, Boolean isPositive)
	{
		super();
		this.values = values;
		this.polarity = isPositive;
	}
	
	
	/**
	 * Create a value set without polarity
	 * 
	 * @param <T>
	 * @return
	 */
	public static <T> ValueSet<T> create() {
		return new ValueSet<T>(new HashSet<T>(), null);
	}
	
	public static <T> ValueSet<T> create(Set<T> values, boolean polarity) {
		return new ValueSet<T>(values, polarity);
	}


	public Boolean getPolarity()
	{
		return (isUnknown()) ? null : polarity;
	}

	public boolean isUnknown()
	{
		return values == null;
	}
	
	public void setPolarity(boolean polarity)
	{
		this.polarity = polarity;
	}
	
	public Set<T> getValues()
	{
		return values;
	}
	
	
	public void merge(ValueSet<T> other) {
		if(other.isUnknown()) {
			if(this.isUnknown()) {
				this.values = null;
			} else {
				return;
			}
		}

		if(this.isUnknown()) {			
			return;
		}		
		if(this.polarity != false && other.polarity == true) {
			this.values.addAll(other.values);
			this.polarity = true;
		} else if (this.polarity != true && other.polarity == false) {
			this.values.retainAll(other.getValues());
			this.polarity = false;
		} else {
			this.values = null;
		}
	}


	@Override
	public String toString()
	{
		return "ValueSet [values=" + values + ", polarity=" + polarity + "]";
	}
}


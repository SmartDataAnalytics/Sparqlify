package org.aksw.sparqlify.core.cast;




class CastException
	extends Exception
{
	CastException() {
		super();
	}
	
	CastException(String message) {
		super(message);
	}
}


class IntegerRangeRestrictor {
	
	private long mask;
	
	public IntegerRangeRestrictor(int bits) {
		// 4 -> 1111
		long mask = 0;
		for(int i = 0; i < bits; ++i) {
			mask = mask << 1 | 1;
		}
		
		this.mask = mask;
		
	}
	
	public Integer crop(long value) {
		return null;
		//value
		//BigInteger x;
		//sx.setBit(

	}
}


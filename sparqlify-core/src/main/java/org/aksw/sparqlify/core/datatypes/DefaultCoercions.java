package org.aksw.sparqlify.core.datatypes;



public class DefaultCoercions
{
	
	/*
	public static CustomizableCostFunctionCollection getDefaultCoercions()
		throws Exception
	{

		Method sToB = DefaultCoercions.class.getMethod("toBoolean", String.class);
		Method sToI = DefaultCoercions.class.getMethod("toInteger", String.class);
		Method sToL = DefaultCoercions.class.getMethod("toLong"   , String.class);
		Method sToF = DefaultCoercions.class.getMethod("toFloat"  , String.class);
		Method sToD = DefaultCoercions.class.getMethod("toDouble" , String.class);

		coercions.registerMethod(sToB, null, 5000.0f);
		coercions.registerMethod(sToI, null, 5000.0f);
		coercions.registerMethod(sToL, null, 5000.0f);
		coercions.registerMethod(sToF, null, 5000.0f);
		coercions.registerMethod(sToD, null, 5000.0f);

			
			/ *
			Method fToI = DefaultCoercions.class.getMethod("toInteger", Float.class);
			Method fToD = DefaultCoercions.class.getMethod("toDouble", Float.class);

			Method dToI = DefaultCoercions.class.getMethod("toInteger", Double.class);
			
			Method iToF = DefaultCoercions.class.getMethod("toFloat", Integer.class);
			Method iToD = DefaultCoercions.class.getMethod("toDouble", Integer.class);
	
			
			
			coercions.registerMethod(fToI, null, 1000.0f);
			coercions.registerMethod(dToI, null, 2000.0f);
			coercions.registerMethod(iToF, null, 100.0f);
	
			coercions.registerMethod(iToD, null, 200.0f);
			coercions.registerMethod(fToD, null, 100.0f);
			* /
		}

		return coercions;
	}
	
	
	public static <T> CustomizableFunctionCollection wrap(Class<T> clazz, String pattern)
		throws Exception
	{
		CustomizableCostFunctionCollection coercions = getDefaultCoercions();
		
		CustomizableFunctionCollection mm = new CustomizableFunctionCollection(coercions);
		
		mm.registerAll(clazz, Pattern.compile(pattern));

		return mm;
	}
	
	public static <T> CustomizableFunctionCollection wrap(Object o, String pattern)
		throws Exception
	{
		CustomizableCostFunctionCollection coercions = getDefaultCoercions();
		
		CustomizableFunctionCollection mm = new CustomizableFunctionCollection(coercions);
		
		mm.registerAll(o, Pattern.compile(pattern));
	
		return mm;
	}
*/	

	public static Boolean toBoolean(String a)
	{
		return Boolean.parseBoolean(a);
	}

	
	/*
	 * toInteger
	 */

	public static Integer toInteger(Long a)
	{
		return a.intValue();
	}

	public static Integer toInteger(Float a)
	{
		return a.intValue();
	}

	public static Integer toInteger(Double a)
	{
		return a.intValue();
	}

	public static Integer toInteger(String a)
	{
		return Integer.parseInt(a);
	}


	/*
	 * toLong
	 */

	public static Long toLong(Integer a)
	{
		return a.longValue();
	}
	
	public static Long toLong(Float a)
	{
		return a.longValue();
	}

	public static Long toLong(Double a)
	{
		return a.longValue();
	}

	public static Long toLong(String a)
	{
		return Long.parseLong(a);
	}


	/*
	 * toFloat
	 */
	
	public static Float toFloat(Long a)
	{
		return a.floatValue();
	}

	public static Float toFloat(Integer a)
	{
		return a.floatValue();
	}

	public static Float toFloat(Double a)
	{
		return a.floatValue();
	}

	public static Float toFloat(String s)
	{
		return Float.parseFloat(s);
	}


	/*
	 * toDouble
	 */
	
	
	public static Double toDouble(Integer a)
	{
		return a.doubleValue();
	}

	public static Double toDouble(Long a)
	{
		return a.doubleValue();
	}

	public static Double toDouble(Float a)
	{
		return a.doubleValue();
	}

	public static Double toDouble(String a)
	{
		return Double.parseDouble(a);
	}

}

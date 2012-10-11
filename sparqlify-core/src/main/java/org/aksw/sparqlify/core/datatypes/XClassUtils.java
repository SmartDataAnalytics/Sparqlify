package org.aksw.sparqlify.core.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;


public class XClassUtils {
	
	public static List<XClass> resolve(DatatypeSystem datatypeSystem, Collection<TypeToken> typeNames) {
		
		List<XClass> result = new ArrayList<XClass>(typeNames.size());
		for(TypeToken typeName : typeNames) {
			XClass tmp = datatypeSystem.getByName(typeName);
			if(tmp == null) {
				throw new RuntimeException("Could not resolve: " + typeName);
			}
			
			result.add(tmp);
		}
		
		return result;
	}
	
	public static Integer getDistance(XClass given, XClass there)
	{
		int result = _getDistanceInterface(given, there, 0);

		return result == Integer.MAX_VALUE ? null : result;
	}

	private static int _getDistanceInterface(XClass given, XClass there, int depth)
	{
		if(given == there) {
			return depth;
        }

		++depth;

		int result = Integer.MAX_VALUE;
		for(XClass item : given.getDirectSuperClasses()) {
			result = Math.min(result, _getDistanceInterface(item, there, depth));
        }

		/*
		Class<?> superClass = given.getSuperclass();
		if(superClass != null) {
			result = Math.min(result, _getDistanceInterface(superClass, there, depth));
        }
        */

		return result;
	}

	
    public static Integer[] getDistance(XClass[] a, XClass[] b)
    {
        int n = Math.min(a.length, b.length);

        Integer[] result = new Integer[n];
        for(int i = 0; i < n; ++i) {
            XClass given = a[i];


            // Don't try to abbreviate with (given == null) ? 0 : getDistance(given, b[i]);
            // It will break because getDistance may return null
            if(given == null) {
                result[i] = 0;
            } else {
                result[i] = getDistance(given, b[i]);
            }
        }

        return  result;
    }

    /**
     * Including return types
     *
     * @param ra
     * @param rb
     * @param a
     * @param b
     * @return
     */
    public static Integer[] getDistance(XClass ra, XClass rb, XClass[] a, XClass[] b)
    {
        int n = Math.min(a.length, b.length);

        Integer[] result = new Integer[n + 1];
        result[0] = getDistance(rb, ra);

        for(int i = 0; i < n; ++i) {
            Integer d = getDistance(a[i], b[i]);
            result[i + 1] = d;
        }

        return  result;
    }

}

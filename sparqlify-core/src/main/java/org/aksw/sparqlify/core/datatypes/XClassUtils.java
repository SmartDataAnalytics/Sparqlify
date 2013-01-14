package org.aksw.sparqlify.core.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;


public class XClassUtils {
	
	
	public static Integer getRelation(TypeDistance a, TypeDistance b) {
		
		int result;
		if(a.getCoercion() == null) {
			
			if(b.getCoercion() == null) {
				result = a.getInheritanceDepth() - b.getInheritanceDepth();
			} else {
				result = -1;
			}
			
		} else {
			
			if(b.getCoercion() == null) {				
				result = 1;
			} else {
				result = b.getInheritanceDepth() - a.getInheritanceDepth();
			}			
		
		}
		
		return result;
	}
	
    public static Integer getRelation(TypeDistance[] a, TypeDistance[] b)
    {
        boolean hasGreater = false;
        boolean hasLess = false;

        for(int i = 0; i < a.length; ++i) {
        	
        	TypeDistance x = a[i];
        	TypeDistance y = a[i];
        	
            if(x == null || y == null) {
                //return null;
                throw new NullPointerException();
                // TODO Throw an exception or return null?
            }
            
            int d = getRelation(x, y);

            if (d > 0) {
                hasGreater = true;
            } else if (d < 0) {
                hasLess = true;
            }
        }

        if(hasGreater && hasLess) {
            return null;
        } else if(hasGreater) {
            return 1;
        } else if(hasLess) {
            return -1;
        }

        return 0;
    }
	
	public static TypeDistance[] getTypeDistance(XClass[] a, XClass[] b, CoercionSystemOld coercions) {
        int n = Math.min(a.length, b.length);

        TypeDistance[] result = new TypeDistance[n];
        for(int i = 0; i < n; ++i) {
            XClass given = a[i];


            // Don't try to abbreviate with (given == null) ? 0 : getDistance(given, b[i]);
            // It will break because getDistance may return null
            if(given == null) {
                result[i] = new TypeDistance(0, null);
            } else {
                result[i] = getTypeDistance(given, b[i], coercions);
            }
        }

        return  result;
	}

	
	public static TypeDistance getTypeDistance(XClass source, XClass target, CoercionSystemOld coercions) {
		Integer depth = getDistance(source, target);
		
		TypeDistance result;
		
		if(depth != null) {
			result = new TypeDistance(depth, null);
		} else {
			result = findCoercion(source, target, coercions);
		}
		
		return result;
	}
	
	
	public static List<XClass> resolve(TypeSystem datatypeSystem, Collection<TypeToken> typeNames) {
		
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
		Integer result = _getDistanceInterface(given, there, 0);

		return result == Integer.MAX_VALUE ? null : result;
	}

	public static TypeDistance findCoercion(XClass source, XClass target, CoercionSystemOld coercions) {
		
		List<XClass> open = new ArrayList<XClass>();
		List<XClass> next = null;
		open.add(source);
		
		int depth = 0;
		while(!open.isEmpty()) {
			
			XMethod method = null;
			for(XClass item : open) {
				XMethod tmp = coercions.lookup(source, target);
				
				if(tmp != null && method != null) {
					throw new RuntimeException("Multiple candidates: " + tmp + ", " + item);
				}
				
				method = tmp;
			}
			
			if(method != null) {
				TypeDistance result = new TypeDistance(depth, method);
				return result;
			}

			++depth;

			if(next == null) {
				next = new ArrayList<XClass>();
			} else {
				next.clear();
			}
			
			for(XClass item : open) {
				next.addAll(item.getDirectSuperClasses());
			}
			
			List<XClass> swap = open;
			open = next;
			next = swap;
		}
		
		return null;
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

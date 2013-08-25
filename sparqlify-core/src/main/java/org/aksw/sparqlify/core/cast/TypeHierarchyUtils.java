package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.TypeSystem;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface CoercionSystemOld<T, M> {
	M lookup(T source, T target);
}


public class TypeHierarchyUtils {

	private static final Logger logger = LoggerFactory.getLogger(TypeHierarchyUtils.class);
	
	public static <M> Integer getRelation(TypeDistance<M> a, TypeDistance<M> b) {

		int result;
		if (a.getCoercion() == null) {

			if (b.getCoercion() == null) {
				result = a.getInheritanceDepth() - b.getInheritanceDepth();
			} else {
				result = -1;
			}

		} else {

			if (b.getCoercion() == null) {
				result = 1;
			} else {
				result = b.getInheritanceDepth() - a.getInheritanceDepth();
			}

		}

		return result;
	}

	public static <M> Integer getRelation(TypeDistance<M>[] a, TypeDistance<M>[] b) {
		boolean hasGreater = false;
		boolean hasLess = false;

		for (int i = 0; i < a.length; ++i) {

			TypeDistance<M> x = a[i];
			TypeDistance<M> y = a[i];

			if (x == null || y == null) {
				// return null;
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

		if (hasGreater && hasLess) {
			return null;
		} else if (hasGreater) {
			return 1;
		} else if (hasLess) {
			return -1;
		}

		return 0;
	}

	public static <T, M> TypeDistance<M>[] getTypeDistance(T[] a, T[] b,
			CoercionSystemOld<T, M> coercions, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {
		int n = Math.min(a.length, b.length);

		@SuppressWarnings("unchecked")
		TypeDistance<M>[] result = (TypeDistance<M>[])new Object[n];
		for (int i = 0; i < n; ++i) {
			T given = a[i];

			// Don't try to abbreviate with (given == null) ? 0 :
			// getDistance(given, b[i]);
			// It will break because getDistance may return null
			if (given == null) {
				result[i] = new TypeDistance<M>(0, null);
			} else {
				result[i] = getTypeDistance(given, b[i], coercions, DirectSuperTypeProvider);
			}
		}

		return result;
	}

	public static <T, M> TypeDistance<M> getTypeDistance(T source, T target,
			CoercionSystemOld<T, M> coercions, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {
		Integer depth = getDistance(source, target, DirectSuperTypeProvider);

		TypeDistance<M> result;

		if (depth != null) {
			result = new TypeDistance<M>(depth, null);
		} else {
			result = findCoercion(source, target, coercions, DirectSuperTypeProvider);
		}

		return result;
	}

	public static List<XClass> resolve(TypeSystem datatypeSystem,
			Collection<TypeToken> typeNames) {

		List<XClass> result = new ArrayList<XClass>(typeNames.size());
		for (TypeToken typeName : typeNames) {
			XClass tmp = datatypeSystem.getByName(typeName);
			if (tmp == null) {
				throw new RuntimeException("Could not resolve: " + typeName);
			}

			result.add(tmp);
		}

		return result;
	}

	public static <T> Integer getDistance(T given, T there, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {
		Integer result = _getDistanceInterface(given, there, 0, DirectSuperTypeProvider);

		return result == Integer.MAX_VALUE ? null : result;
	}

	public static <T, M> TypeDistance<M> findCoercion(T source, T target,
			CoercionSystemOld<T, M> coercions, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {

		List<T> open = new ArrayList<T>();
		List<T> next = null;
		open.add(source);

		int depth = 0;
		while (!open.isEmpty()) {

			M method = null;
			for (T item : open) {
				M tmp = coercions.lookup(source, target);

				if (tmp != null && method != null) {
					throw new RuntimeException("Multiple candidates: " + tmp
							+ ", " + item);
				}

				method = tmp;
			}

			if (method != null) {
				TypeDistance<M> result = new TypeDistance<M>(depth, method);
				return result;
			}

			++depth;

			if (next == null) {
				next = new ArrayList<T>();
			} else {
				next.clear();
			}

			for (T item : open) {
				Collection<T> superTypes = DirectSuperTypeProvider.getDirectSuperTypes(item);
				next.addAll(superTypes);
			}

			List<T> swap = open;
			open = next;
			next = swap;
		}

		return null;
	}

	private static <T> int _getDistanceInterface(T given, T there,
			int depth, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {
		if (given == there) {
			return depth;
		}

		++depth;

		int result = Integer.MAX_VALUE;

		Collection<T> superTypes = DirectSuperTypeProvider.getDirectSuperTypes(given);
		for (T item : superTypes) {			
			result = Math
					.min(result, _getDistanceInterface(item, there, depth, DirectSuperTypeProvider));
		}

		/*
		 * Class<?> superClass = given.getSuperclass(); if(superClass != null) {
		 * result = Math.min(result, _getDistanceInterface(superClass, there,
		 * depth)); }
		 */

		return result;
	}

	public static <T> Integer[] getDistance(T[] a, T[] b, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {
		int n = Math.min(a.length, b.length);

		Integer[] result = new Integer[n];
		for (int i = 0; i < n; ++i) {
			T given = a[i];

			// Don't try to abbreviate with (given == null) ? 0 :
			// getDistance(given, b[i]);
			// It will break because getDistance may return null
			if (given == null) {
				result[i] = 0;
			} else {
				result[i] = getDistance(given, b[i], DirectSuperTypeProvider);
			}
		}

		return result;
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
	public static <T> Integer[] getDistance(T ra, T rb, T[] a,
			T[] b, DirectSuperTypeProvider<T> DirectSuperTypeProvider) {
		int n = Math.min(a.length, b.length);

		Integer[] result = new Integer[n + 1];
		result[0] = getDistance(rb, ra, DirectSuperTypeProvider);

		for (int i = 0; i < n; ++i) {
			Integer d = getDistance(a[i], b[i], DirectSuperTypeProvider);
			result[i + 1] = d;
		}

		return result;
	}

}

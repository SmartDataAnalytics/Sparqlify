package org.aksw.sparqlify.core.datatype;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_UserFunc;
import org.aksw.sparqlify.config.syntax.FunctionDeclaration;
import org.aksw.sparqlify.core.algorithms.ExprEvaluatorSql;
import org.aksw.sparqlify.core.datatypes.DatatypeSystemCustom;
import org.aksw.sparqlify.core.datatypes.XMethod;
import org.aksw.sparqlify.core.datatypes.XMethodImpl;
import org.junit.Test;

import cornercases.TestUtils;





class Registry {
	
}

class Ops {
	
	/*
	 * Arithmetic
	 */
	
	// Integer
	
	public static Integer add(Integer a, Integer b) {
		return a + b;
	}

	public static Integer subtract(Integer a, Integer b) {
		return a - b;
	}

	public static Integer multiply(Integer a, Integer b) {
		return a * b;
	}
	
	public static Integer divide(Integer a, Integer b) {
		return a / b;
	}

	// Double
	
	public static Double add(Double a, Double b) {
		return a + b;
	}

	public static Double subtract(Double a, Double b) {
		return a - b;
	}

	public static Double multiply(Double a, Double b) {
		return a * b;
	}
	
	public static Double divide(Double a, Double b) {
		return a / b;
	}
	
	
	/*
	 * In-/Equality
	 */
	

	public static Boolean lessThan(Integer a, Integer b) {
		return a < b;
	}

	public static Boolean lessThanOrEquals(Integer a, Integer b) {
		return a <= b;
	}

	public static Boolean equals(Integer a, Integer b) {
		return a == b;
	}

	public static Boolean greaterThanOrEquals(Integer a, Integer b) {
		return a >= b;
	}

	public static Boolean greaterThan(Integer a, Integer b) {
		return a > b;
	}

	
	public static Boolean lessThan(Double a, Double b) {
		return a < b;
	}

	public static Boolean lessThanOrEquals(Double a, Double b) {
		return a <= b;
	}

	public static Boolean equals(Double a, Double b) {
		return a == b;
	}

	public static Boolean greaterThanOrEquals(Double a, Double b) {
		return a >= b;
	}

	public static Boolean greaterThan(Double a, Double b) {
		return a > b;
	}

	
	public static Boolean lessThan(String a, String b) {
		return a.compareTo(b) < 0;
	}

	public static Boolean lessThanOrEquals(String a, String b) {
		return a.compareTo(b) <= 0;
	}

	public static Boolean equals(String a, String b) {
		return a.equals(b);
	}

	public static Boolean greaterThanOrEquals(String a, String b) {
		return a.compareTo(b) >= 0;
	}

	public static Boolean greaterThan(String a, String b) {
		return a.compareTo(b) > 0;
	}


	public static Boolean logicalAnd(Boolean a, Boolean b) {
		return a && b;
	}

	public static Boolean logicalOr(Boolean a, Boolean b) {
		return a || b;
	}

	public static Boolean logicalNot(Boolean a) {
		return !a;
	}
	
	
	public static String myTestFunc(String str, Integer i) {
		return "yay: " + str + ", " + i;
	}
}




/*
interface XMethod {
	
}
*/

public class DatatypeSystemTests {


	
	@Test
	public void test() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {

		FunctionDeclaration decl;

		DatatypeSystemCustom ds = TestUtils.createDefaultDatatypeSystem();

		S_Constant a = S_Constant.create("http://ex.org/", ds);
		System.out.println(a);

		S_Constant b = S_Constant.create(1, ds);
		System.out.println(b);

		//S_Concat c = S_Concat.create(a, b);
		//System.out.println(c);

		S_UserFunc c = S_UserFunc.create("myTestFunc", a, b);
		System.out.println(c);

		
		// Define a random custom function
		Method m = Ops.class.getMethod("myTestFunc", String.class, Integer.class);
		XMethod x = XMethodImpl.createFromMethod("testFunc", ds, null, m);

		ds.register(x);

		
		
		//S_UserFunc d = S_UserFunc.create("");

		
		
		
		/*
		Method m = Ops.class.getMethod("add", Integer.class, Integer.class);
		XMethod x = XMethodImpl.createFromMethod(ds, null, m);
		*/
		
		
		
//		System.out.println(x);
//
//		Object r = x.getInvocable().invoke(1, 3);
//		System.out.println(r);
		
		
		//ds.add(decl);

				
		/*
		 * What we want to do here:
		 * 
		 * Declare a function
		 *     numeric add(numeric, numeric)
		 *     
		 * For numeric, there is a set of corresponding java classes (Integer, Float, also Byte)
		 * 
		 * And for this declaration, associate a Java method for the type float with it.
		 * 
		 */
		
		//DatatypeSystem.registerMethod(declaration)
		//declaration.addMethod(someMethod)
		
		/*
		for(Method m : Ops.class.getMethods()) {
			System.out.println(m);
		}
		
		//System.out.println();
		
		Method m = Ops.class.getMethod("add", int.class, int.class);
	
		System.out.println(m.invoke(null, 1, 2));
		*/
	}
}


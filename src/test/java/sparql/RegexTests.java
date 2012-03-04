package sparql;

import org.aksw.sparqlify.core.RegexDerivation;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.karneim.util.collection.regex.PAutomaton;
import com.karneim.util.collection.regex.PatternPro;

public class RegexTests {

	@Test
	public void intersection() {
		intersection("^http://linkedgeodata.org/ontology/.*", "^http://linkedgeodata.org/.*");

		intersection("a(aa)+", "(aa)+");
	}

	@Test
	public void alternatives() {
		PatternPro p = new PatternPro("(http://blah.org/.*)|(http://test.org/.*)");
		
		Assert.assertTrue(p.contains("http://test.org/hui"));
		Assert.assertFalse(p.contains("http://evil/xxx"));
		System.out.println("Success");
	}
	
	public void intersection(String a, String b) {
		PatternPro p1 = new PatternPro(a);
		PatternPro p2 = new PatternPro(b);
		
		PAutomaton a1 = p1.getAutomaton();
		PAutomaton a2 = p2.getAutomaton();
		
		a1.retainAll(a2);
		a1.minimize();
		
		System.out.println(p1.getAutomaton().getStates().size());
	}

	@Test
	public void union() {
		PatternPro p1 = new PatternPro("ab1");
		PatternPro p2 = new PatternPro("cd2");
		
		PAutomaton a1 = p1.getAutomaton();
		PAutomaton a2 = p2.getAutomaton();
		
		a1.addAll(a2);
		
		System.out.println("Union");
		System.out.println("    " + p1.contains("ab2"));
		System.out.println("    " + p1.contains("cd2"));
	}
	
	@Test
	public void derivation() {
		Expr expr = ExprUtils.parse("concat('te.*st', ?x)");
		String str = RegexDerivation.deriveRegex(expr);
		System.out.println(str);
		
		PatternPro pattern = new PatternPro(str);
		
		System.out.println("Contains: " + pattern.contains("te.*st"));
		System.out.println(pattern.getRegEx());
		
	}
}


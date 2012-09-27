package cornercases;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.aksw.sparqlify.core.domain.RestrictedExpr;
import org.aksw.sparqlify.core.domain.VarDefinition;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprVar;



public class VarDefinitionTest {
	
	/**
	 * Test for whether renaming works properly
	 * 
	 */
	@Test
	public void testRename() {
		
		Var v1 = Var.alloc("s");
		Var v2 = Var.alloc("p");
		Var v3 = Var.alloc("o");
		Var v4 = Var.alloc("x");
		
		
		
		RestrictedExpr e1 = RestrictedExpr.create(new ExprVar(v1));
		//RestrictedExpr e2 = RestrictedExpr.create(new ExprVar(v2));
		RestrictedExpr e3 = RestrictedExpr.create(new ExprVar(v3));
		RestrictedExpr e4 = RestrictedExpr.create(new ExprVar(v4));
		
		Multimap<Var, RestrictedExpr> map = HashMultimap.create(); 
		map.put(v1, e1);
		map.put(v2, e1);
		map.put(v2, e3);		

		VarDefinition varDef = new VarDefinition(map);
		
		Map<String, String> rename = new HashMap<String, String>();
		rename.put("s", "x");
		
		VarDefinition renamedVarDef = VarDefinition.copyRename(varDef, rename);

		
		Multimap<Var, RestrictedExpr> expectedMap = HashMultimap.create(); 
		expectedMap.put(v1, e4);
		expectedMap.put(v2, e4);
		expectedMap.put(v2, e3);		
		VarDefinition expectedVarDef = new VarDefinition(expectedMap);
			
		
		System.out.println(renamedVarDef);
		System.out.println(expectedVarDef);
		
		Assert.assertEquals(expectedVarDef, renamedVarDef);
	}
}

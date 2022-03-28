package org.aksw.sparqlify.config.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.domain.api.LogicalTable;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewTemplateDefinition {	
	private static final Logger logger = LoggerFactory.getLogger(ViewTemplateDefinition.class);

	
	public static ViewDefinition createViewDefinition(
			String name,
			ViewTemplateDefinition vtd,
			LogicalTable logicalTable,
			Collection<Constraint> constraints) {
		
		Map<Var, Expr> varDefs = new LinkedHashMap<>();
		vtd.getVarExprList().forEachExpr(varDefs::put);

		
        Map<Var, Constraint> varConstraints = new LinkedHashMap<Var, Constraint>();
        if(constraints != null) {
            for(Constraint constraint : constraints) {
                if(constraint instanceof ConstraintPrefix) {
                	ConstraintPrefix c = (ConstraintPrefix)constraint;

                    varConstraints.put(c.getVar(), c);
                } else {
                    logger.warn("Unknown constraint type: " + constraint.getClass() + " - " + constraint);
                }
            }
        }

		
		ViewDefinition result = new ViewDefinition(
				name,
				vtd.getConstructTemplate().getList(),
				varDefs,
				varConstraints,
				logicalTable);
		
		return result;
	}

	
	//private static final Log
	//private String name;
	
	private QuadPattern constructTemplate;
	
	// FIXME: Replace with Map<Var, Expr>
	private List<Expr> varBindings;

	/*
	public ViewTemplateDefinition()
	{
		constructTemplate = new Template(null);
		varBindings =  new ArrayList<Expr>();
	}*/
	
	public ViewTemplateDefinition() {
		this.constructTemplate = new QuadPattern(); //new Template(new BasicPattern());
		this.varBindings = new ArrayList<Expr>();
	}

	public ViewTemplateDefinition(Template template, List<Expr> varBindings) {
		//this.name = name;
		
		this.constructTemplate = QuadPatternUtils.toQuadPattern(template.getBGP());
		this.varBindings = varBindings;
	}
	
	public ViewTemplateDefinition(QuadPattern constructTemplate, List<Expr> varBindings) {
		//this.name = name;
		this.constructTemplate = constructTemplate;
		this.varBindings = varBindings == null ? new ArrayList<Expr>() : varBindings;
	}
	
	/*
	public String getName()
	{
		return name;
	}
	*/
	
	public QuadPattern getConstructTemplate() {
		return constructTemplate;
	}
	public void setConstructTemplate(QuadPattern constructTemplate) {
		this.constructTemplate = constructTemplate;
	}
	public List<Expr> getVarBindings() {
		return varBindings;
	}
	
	public VarExprList getVarExprList() {
		VarExprList result = new VarExprList();
		
		 for(Expr item : this.getVarBindings()) {
//			 if(!(item instanceof E_Equals)) {
//				 throw new RuntimeException("Expected E_Equals");
//			 }
			 
			 E_Equals e = (E_Equals)item;
			 Expr left = e.getArg1();
			 if(!left.isVariable()) {
				 throw new RuntimeException("Variable expected, instead got: " + left);
			 }
			 
			 Var var = left.asVar();
			 Expr expr = e.getArg2();
	
			 Expr previousValue = result.getExpr(var);
			 if(previousValue != null) {				 
				 throw new RuntimeException("Redefinition of variable " + var + " in view '" + "no name" + "' with: " + expr + ", was: " + previousValue);
				 //continue;
			 }
			 
			 
			 result.add(var, expr);
		 }
		
		 return result;
	}
	
	
	public void setVarBindings(List<Expr> varBindings) {
		this.varBindings = varBindings;
	}
	@Override
	public String toString() {
		return "ViewTemplateDefinition [constructTemplate=" + constructTemplate
				+ ", varBindings=" + varBindings + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((constructTemplate == null) ? 0 : constructTemplate
						.hashCode());
		result = prime * result
				+ ((varBindings == null) ? 0 : varBindings.hashCode());
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
		ViewTemplateDefinition other = (ViewTemplateDefinition) obj;
		if (constructTemplate == null) {
			if (other.constructTemplate != null)
				return false;
		} else if (!constructTemplate.equals(other.constructTemplate))
			return false;
		if (varBindings == null) {
			if (other.varBindings != null)
				return false;
		} else if (!varBindings.equals(other.varBindings))
			return false;
		return true;
	}
}

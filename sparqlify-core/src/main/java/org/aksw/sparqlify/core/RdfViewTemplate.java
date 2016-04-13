package org.aksw.sparqlify.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.algebra.sparql.transform.SparqlSubstitute;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;

/**
 * A view template consists of the construct pattern
 * and the binding expressions.
 *
 * @author raven
 *
 */
public class RdfViewTemplate {
    private QuadPattern quadPattern;
    private Map<Node, Expr> binding;

    public RdfViewTemplate(QuadPattern quadPattern, Map<Node, Expr> binding)
    {
        this.quadPattern = quadPattern;
        this.binding = binding;
    }

    public QuadPattern getQuadPattern() {
        return quadPattern;
    }
    public Map<Node, Expr> getBinding() {
        return binding;
    }


    public RdfViewTemplate create(ViewTemplateDefinition definition) {
        return RdfViewTemplate.create(definition.getConstructTemplate(), definition.getVarBindings());
    }

    public static RdfViewTemplate create(QuadPattern quadPattern, List<Expr> bindings)
    {
        // NOTE Macro expansion should occur before the creation of a view template...
        //      ... I think.

//		QuadPattern quadPattern = new QuadPattern();
//
//		for(Triple triple : template.getTriples()) {
//			quadPattern.add(new Quad(Quad.defaultGraphNodeGenerated, triple));
//		}
//
        Map<Node, Expr> bindingMap = new HashMap<Node, Expr>();

        for(Expr expr : bindings) {
            if(!(expr instanceof E_Equals)) {
                throw new RuntimeException("Binding expr must have form ?var = ... --- instead got: " + expr);
            }

            Expr definition = expr.getFunction().getArg(2);
            definition = SparqlSubstitute.substituteExpr(definition);

            Var var = expr.getFunction().getArg(1).asVar();
            bindingMap.put(var, definition);
        }

        return new RdfViewTemplate(quadPattern, bindingMap);
    }

    public Set<Var> getVarsMentioned()
    {
        return QuadPatternUtils.getVarsMentioned(quadPattern);
    }


    public RdfViewTemplate copySubstitute(Map<Node, Node> map)
    {
        BindingMap tmp = new BindingHashMap();
        for(Entry<Node, Node> entry : map.entrySet()) {
            tmp.add((Var)entry.getKey(), entry.getValue());
        }

        RdfViewTemplate result = new RdfViewTemplate(
                QuadUtils.copySubstitute(quadPattern, map),
                QuadUtils.copySubstitute(binding, map));

        return result;
    }


    public Model instanciate(Model result, Map<Var, Object> assignment) {
        return null;
    }
}


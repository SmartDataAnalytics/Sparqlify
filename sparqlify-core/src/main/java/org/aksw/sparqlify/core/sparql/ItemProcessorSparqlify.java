package org.aksw.sparqlify.core.sparql;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.sparqlify.core.MakeExprPermissive;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.process.normalize.CanonicalizeLiteral;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;

public class ItemProcessorSparqlify
    implements Function<Binding, Binding>
{
    private static final Logger logger = LoggerFactory.getLogger(IteratorResultSetSparqlifyBinding.class);

    // Canonicalize values, e.g. 20.0 -> 2.0e1
    private static CanonicalizeLiteral canonicalizer = CanonicalizeLiteral.get();

    //private NodeExprSubstitutor substitutor;// = new NodeExprSubstitutor(sparqlVarMap);
    private Multimap<Var, RestrictedExpr> sparqlVarMap;

    //private long nextRowId;

    public static boolean isCharType(String typeName) {
        String tmp = typeName.toLowerCase();

        Set<String> charNames = new HashSet<String>(Arrays.asList("char"));

        boolean result = charNames.contains(tmp);
        return result;
    }

    public ItemProcessorSparqlify(Multimap<Var, RestrictedExpr> sparqlVarMap)
    {
        this.sparqlVarMap = sparqlVarMap;
     }

    @Override
    public Binding apply(@Nullable Binding binding) {
        Binding result = ItemProcessorSparqlify.process(sparqlVarMap, binding);
        return result;
    }


    public static Binding process(Multimap<Var, RestrictedExpr> sparqlVarMap, Binding binding) {
        boolean debugMode = true;

        BindingMap result = new BindingHashMap();

        for(Entry<Var, Collection<RestrictedExpr>> entry : sparqlVarMap.asMap().entrySet()) {

            Var bindingVar = entry.getKey();
            Collection<RestrictedExpr> candidateExprs = entry.getValue();

//			if(bindingVar.getName().equals("o")) {
//				System.out.println("BindingVar o ");
//			}

            //RDFNode rdfNode = null;
            NodeValue value = null;
            //Node value = Node.NULL;

            // We distinguish on how to create a variable by the columns that are used
            // We use the most specific rdfTerm constructor
            Set<Var> usedVars = new HashSet<Var>();
            for(RestrictedExpr def : candidateExprs) {

                Expr expr = def.getExpr();


                // Check if all variables are bound
                // Null columns may appear on left joins
                boolean allBound = true;
                Set<Var> exprVars = expr.getVarsMentioned();
                for(Var var : exprVars) {
                    if(!binding.contains(var)) {
                        allBound = false;
                        break;
                    }
                }

                if(allBound) {
                    if(value != null) {
                        // If the new rdfTerm constructor makes use of only a subset of the columns
                        // from which the current node was created, we ignore the new bindings
                        if(usedVars.containsAll(expr.getVarsMentioned())) {
                            continue;
                        } else if(usedVars.equals(expr.getVarsMentioned())) {
                            throw new RuntimeException("Multiple expressions binding the variable (ambiguity) " + bindingVar + ": " + entry.getValue());
                        } else if(!expr.getVarsMentioned().containsAll(usedVars)) {
                            throw new RuntimeException("Multiple expressions binding the variable (overlap) " + bindingVar + ": " + entry.getValue());
                        }
                    }


                    expr = MakeExprPermissive.getInstance().deepCopy(expr);

                    value = ExprUtils.eval(expr, binding);
                    //rdfNode = ModelUtils.convertGraphNodeToRDFNode(value.asNode(), null);

                    if(!debugMode) {
                        break;
                    }
                }
            }

            //qs.add(entry.getKey().getName(), rdfNode);
            // TODO Add a switch for this warning/debugging message (also decide on the logging level)
            Node resultValue = value == null ? null : value.asNode();

            if(resultValue == null) {
                logger.trace("Null node for variable " + bindingVar + " - Might be undesired.");
                //throw new RuntimeException("Null node for variable " + entry.getKey() + " - Should not happen.");
            } else {

                //result.add((Var)entry.getKey(), resultValue);

                boolean isDatatypeCanonicalization = false;

                Node canonResultValue = canonicalizer.apply(resultValue);
                //System.out.println("Canonicalization: " + resultValue + " -> " + canonResultValue);
                if(!isDatatypeCanonicalization) {

                    if(canonResultValue.isLiteral()) {
                        String lex = canonResultValue.getLiteralLexicalForm();

                        if(resultValue.isLiteral()) {
                            RDFDatatype originalType = resultValue.getLiteralDatatype();

                            if(originalType != null) {
                            //String typeUri = resultValue.getLiteralDatatypeURI();
                                canonResultValue = NodeFactory.createLiteral(lex, originalType);
                            }
                        } else {
                            throw new RuntimeException("Should not happen: Non-literal canonicalized to literal: " + resultValue + " became " + canonResultValue);
                        }


                    }

                }

                result.add(bindingVar, canonResultValue);
            }
        }

        return result;
    }


}
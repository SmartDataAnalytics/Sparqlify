package org.aksw.sparqlify.core.transformations;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.sparqlify.type_system.CandidateMethod;
import org.aksw.sparqlify.type_system.FunctionModel;
import org.aksw.sparqlify.type_system.MethodDeclaration;
import org.aksw.sparqlify.type_system.TypeSystemUtils;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExprTransformerSparqlFunctionModel
    implements ExprTransformer
{

    private static final Logger logger = LoggerFactory.getLogger(ExprTransformerSparqlFunctionModel.class);


    // TODO Move to appropriate place
    private static E_RdfTerm typeError = E_RdfTerm.createTypedLiteral(SparqlifyConstants.nvTypeError, SparqlifyConstants.nvTypeError);

    private FunctionModel<String> sparqlModel;


    public ExprTransformerSparqlFunctionModel(FunctionModel<String> sparqlModel) {
        this.sparqlModel = sparqlModel;
    }


    public static List<Expr> getTermValues(List<E_RdfTerm> rdfTerms) {
        List<Expr> result = new ArrayList<Expr>(rdfTerms.size());
        for(E_RdfTerm rdfTerm : rdfTerms) {
            Expr item = rdfTerm.getLexicalValue();

            result.add(item);
        }

        return result;
    }

    @Override
    public E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs) {
        ExprFunction fn = orig.getFunction();

        String fnName = ExprUtils.getFunctionId(fn);

        // Get the argument type list
        // All of the arguments must be typed literals (maybe also plain literal???)
        // otherwise its an type error
        List<String> argTypes = new ArrayList<String>(exprs.size());
        for(E_RdfTerm rdfTerm : exprs) {
            Expr termType = rdfTerm.getType();
            Expr datatype = rdfTerm.getDatatype();

            if(!termType.isConstant()) {
                logger.debug("Yielding type error because termType is not a contant in: " + rdfTerm + " from " + orig + " with " + exprs);
                return typeError;
            }


            int termTypeVal = termType.getConstant().getDecimal().intValue();

            if(!datatype.isConstant()) {
                logger.debug("Yielding type error because datatype is not a contant in: " + rdfTerm + " from " + orig + " with " + exprs);
                return typeError;
            }

            NodeValue nodeValue = datatype.getConstant();


            if(nodeValue.equals(SparqlifyConstants.nvTypeError)) { //SqlValue.TYPE_ERROR)) {
                logger.debug("Passing on type error for: " + fnName + " " + exprs);
                return typeError;
            }

            if(!nodeValue.isString()) {
                logger.debug("Yielding type error because datatype is not a string for: " + fnName + " " + exprs);
                return typeError;
            }

            String datatypeStr = nodeValue.asUnquotedString();

            // Convert a plain literal to a typed literal with xsd:string
            if(termTypeVal == 2) {
                if(!datatypeStr.trim().isEmpty()) {
                    logger.debug("Yielding type error because termType has a language tag in : " + rdfTerm + " from " + orig + " with " + exprs);
                    return typeError;
                }

                datatypeStr = XSD.xstring.toString();

            } else {

                if(termTypeVal != 3) {
                    logger.debug("Yielding type error because termType is not 3 (typed literal) in: " + rdfTerm + " from " + orig + " with " + exprs);
                    return typeError;
                }
            }

            argTypes.add(datatypeStr);
        }


        //FunctionModel
        CandidateMethod<String> candidate = TypeSystemUtils.lookupCandidate(sparqlModel, fnName, argTypes);

        E_RdfTerm result;
        if(candidate == null) {
            logger.debug("Yielding type error because no suitable candidate found for " + fnName + " with " + argTypes);
            return typeError;
        }

        MethodDeclaration<String> dec = candidate.getMethod().getDeclaration();

        String name = dec.getName();
        String returnType = dec.getSignature().getReturnType();

        NodeValue nvReturnType = NodeValue.makeString(returnType);

        ExprList newArgs = new ExprList(getTermValues(exprs));
        ExprFunction exprFn = new E_Function(name, newArgs);

        result = E_RdfTerm.createTypedLiteral(exprFn, nvReturnType);

        return result;
    }

}
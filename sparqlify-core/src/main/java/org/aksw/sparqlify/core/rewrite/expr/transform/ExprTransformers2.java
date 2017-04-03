package org.aksw.sparqlify.core.rewrite.expr.transform;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.cast.TypeSystemImpl;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.apache.jena.sparql.expr.E_Add;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.XSD;

public class ExprTransformers2 {

    // TODO Replace with a proper type system
    public static final Expr dummyDatatype = NodeValue.makeString("http://example.org/replacemelater");

    // TODO Ideally we would have access to a proper model of the SPRAQL
    // types and functions
    public static TypeSystem typeSystem = new TypeSystemImpl();


    static {
        //sparqlTypeSystem.

    }

    public static E_RdfTerm _transform(Expr expr) {
        return null;
    }

    // TODO we need again access to some model of the valid combination of datatypes for sparql functions

    /**
     *
     * @param expr
     * @return
     */
    public static E_RdfTerm transform(E_LogicalAnd expr) {
        E_RdfTerm result = null;

        E_RdfTerm a = _transform(expr.getArg1());

        // Short circuit evaluation
        if(a.getLexicalValue().equals(NodeValue.FALSE)) {
            result = E_RdfTerm.createTypedLiteral(NodeValue.FALSE, XSD.xboolean);

            return result;
        }

        E_RdfTerm b = _transform(expr.getArg2());

        Expr newExpr = new E_LogicalAnd(a.getLexicalValue(), b.getLexicalValue());

        result = E_RdfTerm.createTypedLiteral(newExpr, XSD.xboolean);

        return result;
    }


    public static E_RdfTerm transformFunction(ExprFunction fn) {

        List<Expr> args = fn.getArgs();
        List<E_RdfTerm> argTerms = new ArrayList<E_RdfTerm>(args.size());

        for(Expr arg : args) {
            E_RdfTerm argTerm = _transform(arg);
            argTerms.add(argTerm);
        }

        String functionName = ExprUtils.getFunctionId(fn);
        SparqlFunction function = typeSystem.getSparqlFunction(functionName);

        // TODO And what now?
        // What methods does the function object need?

        //function.get

        //E_RdfTerm a = _transform(expr.getArg1());
        //E_RdfTerm b = _transform(expr.getArg2());

        //Expr newExpr = new E_LogicalAnd(a.getLexicalValue(), b.getLexicalValue());

        //E_RdfTerm result = E_RdfTerm.createTypedLiteral(newExpr, dummyDatatype);
        //return result;
        return null;
    }


    public static E_RdfTerm transform(E_Add expr) {

        E_RdfTerm a = _transform(expr.getArg1());
        E_RdfTerm b = _transform(expr.getArg2());

        Expr newExpr = new E_LogicalAnd(a.getLexicalValue(), b.getLexicalValue());

        E_RdfTerm result = E_RdfTerm.createTypedLiteral(newExpr, dummyDatatype);
        return result;
    }


    public static E_RdfTerm transformAdd(E_RdfTerm a, E_RdfTerm b) {
        return null;
    }



}




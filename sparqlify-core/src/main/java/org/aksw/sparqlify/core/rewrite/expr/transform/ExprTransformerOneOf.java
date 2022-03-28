package org.aksw.sparqlify.core.rewrite.expr.transform;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.XSD;

public class ExprTransformerOneOf
    implements ExprTransformer
{
    @Override
    public E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs) {
        E_RdfTerm a = exprs.get(0);
        List<E_RdfTerm> bs = new ArrayList<E_RdfTerm>(exprs.size() - 1);

        for(int i = 1; i < exprs.size(); ++i) {
            E_RdfTerm tmp = exprs.get(i);
            bs.add(tmp);
        }

        Expr termTypeA = a.getType();
        if(!termTypeA.isConstant()) {
            throw new RuntimeException("Term type is expected to be a constant: " + termTypeA + " in expression " + orig + " as " + exprs);
        }

        List<Expr> es = new ArrayList<Expr>(exprs.size());
        for(E_RdfTerm b : bs) {
            Expr termTypeB = b.getType();
            if(!termTypeB.isConstant()) {
                throw new RuntimeException("Term type is expected to be a constant: " + termTypeB + " in expression " + orig + " as " + exprs);
            }

            boolean isEqual = termTypeA.getConstant().equals(termTypeB.getConstant());
            if(isEqual) {
                Expr e = new E_Equals(a.getLexicalValue(), b.getLexicalValue());
                es.add(e);
            }
        }

        Expr or;
        if(es.isEmpty()) {
            or = NodeValue.FALSE;
        } else {
            or = ExprUtils.orifyBalanced(es);
        }

        E_RdfTerm result = E_RdfTerm.createTypedLiteral(or, XSD.xboolean);

        return result;
        //throw new RuntimeException("Implement me");
    }

    /*
    @Override
    public E_RdfTerm transform(Expr orig, E_RdfTerm a) {
        E_OneOf e = (E_OneOf)orig;
        Expr lhs = e.getLHS();
        ExprList rhs = e.getRHS();

        e.getA
        //throw new RuntimeException("Implement me");
    }
    */
}
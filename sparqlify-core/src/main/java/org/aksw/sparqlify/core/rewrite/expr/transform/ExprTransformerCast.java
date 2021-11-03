package org.aksw.sparqlify.core.rewrite.expr.transform;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

public class ExprTransformerCast
    extends ExprTransformerBase1
{

    @Override
    public E_RdfTerm transform(Expr orig, E_RdfTerm a) {

        String typeName = ExprUtils.getFunctionId(orig.getFunction());
        NodeValue typeExpr = NodeValue.makeString(typeName);

        Expr av = a.getLexicalValue();

        //Expr c = new E_Cast(av, bv);
        Expr c = new E_Function(typeName, new ExprList(av));

        E_RdfTerm result = E_RdfTerm.createTypedLiteral(c, typeExpr);

        return result;
    }

}


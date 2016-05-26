package org.aksw.sparqlify.algebra.sparql.domain;


import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.views.EquiMap;
import org.aksw.jena_sparql_api.views.VarsMentioned;
import org.aksw.sparqlify.core.RdfViewConjunction;
import org.aksw.sparqlify.core.RdfViewInstance;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;


public class OpRdfViewPattern
    extends OpExt
    implements VarsMentioned
{
    private RdfViewConjunction conjunction;

    public OpRdfViewPattern(RdfViewConjunction conjunction) {
        super(OpRdfViewPattern.class.getSimpleName());
        this.conjunction = conjunction;
    }

    /*
    public OpRdfViewPattern() {
        super(OpRdfUnionViewPattern.class.getName());
        this.conjunction = new ArrayList<RdfViewConjunction>();
    }*/

    public RdfViewConjunction getConjunction() {
        return conjunction;
    }

    @Override
    public Op effectiveOp() {
        return null;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        /*
         * out.print("'''") ; sqlNode.output(out) ; out.print("'''") ;
         */
        out.print(conjunction.getViewNames() + " " + conjunction.getRestrictions());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((conjunction == null) ? 0 : conjunction.hashCode());
        return result;
    }

    @Override
    public boolean equalTo(Op obj, NodeIsomorphismMap labelMap)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpRdfViewPattern other = (OpRdfViewPattern) obj;
        if (conjunction == null) {
            if (other.conjunction != null)
                return false;
        } else if (!conjunction.equals(other.conjunction))
            return false;
        return true;
    }

    @Override
    public Set<Var> varsMentioned() {
      Set<Var> result = new HashSet<Var>();

      for(RdfViewInstance item : getConjunction().getViewBindings()) {
          EquiMap<Var, Node> equiMap = item.getBinding().getEquiMap();

          result.addAll(equiMap.getEquivalences().asMap().keySet());
          result.addAll(equiMap.getKeyToValue().keySet());

          //result.addAll(item.getQueryToParentBinding().keySet());
      }

      return result;
    }

}


package org.aksw.sparqlify.core.cast;

import org.aksw.commons.util.reflect.Caster;
import org.aksw.sparqlify.expr.util.NodeValueUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;

class CoercionWrapper
    implements NodeValueTransformer
{
    private TypeMapper typeMapper;
    private String targetTypeName;

    public CoercionWrapper(TypeMapper typeMapper, String targetTypeName) {
        this.typeMapper = typeMapper;
        this.targetTypeName = targetTypeName;
    }

    public static CoercionWrapper create(TypeMapper typeMapper, String targetTypeName) {
        CoercionWrapper result = new CoercionWrapper(typeMapper, targetTypeName);
        return result;
    }



    @Override
    public NodeValue transform(NodeValue nodeValue)
            throws CastException
    {
        NodeValue result = CoercionWrapper.cast(typeMapper, nodeValue, targetTypeName);
        return result;
    }


    /**
     * TODO: Somehow we also need to get a warning on upcasts
     *
     * @param source
     * @param targetTypeName
     * @return
     */
    public static NodeValue cast(TypeMapper typeMapper, NodeValue source, String targetTypeName)
        throws CastException
    {
        // Get the corresponding TypeMapper java class of the targetType
        RDFDatatype targetType = typeMapper.getTypeByName(targetTypeName);

        if(targetType == null) {
            throw new CastException();
        }

        Class<?> targetClass = targetType.getJavaClass();
        if(targetClass == null) {
            throw new CastException();
        }

        Object value = NodeValueUtils.getValue(source);

        Object targetValue = Caster.tryCast(value, targetClass);
        String targetForm = targetType.unparse(targetValue);

        Node node = NodeFactory.createLiteral(targetForm, targetType);

        NodeValue result = NodeValue.makeNode(node);
        return result;
    }

}
package org.aksw.sparqlify.core.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MultiMaps;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.factory.Factory1;
import org.aksw.commons.util.reflect.Caster;
import org.aksw.commons.util.reflect.ClassUtils;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.ExprSubstitutorSql;
import org.aksw.sparqlify.expr.util.NodeValueUtilsSparqlify;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/*
class SparqlFunctionMap {
    private FunctionRegistry registry;

    public SparqlFunctionMap() {
        registry.get().get("").create("").build(uri, args).
    }
}
*/

/*
    Expr eval(ExprList args, Object context);
}
*/

public class DatatypeSystemCustom
    implements TypeSystem
{
    private static final Logger logger = LoggerFactory.getLogger(DatatypeSystemCustom.class);

    private Map<TypeToken, XClass> nameToType;
    private IBiSetMultimap<TypeToken, TypeToken> typeHierarchy;

    private transient Map<Class<?>, TypeToken> classToToken;


    private CoercionSystemImpl coercionSystem = new CoercionSystemImpl();


    //private List<XMethod> userFunctions = new ArrayList<>
    /**
     * Sparql function URI to SQL Method
     */
    private Multimap<String, XMethod> sqlFunctions = HashMultimap.create();
    private Map<String, SparqlFunctionImpl> sparqlFunctions = new HashMap<String, SparqlFunctionImpl>();

    //public getSqlFunctions


    public SparqlFunctionImpl createSparqlFunction(String name, SqlExprEvaluator evaluator) {
        SparqlFunctionImpl result = new SparqlFunctionImpl(name, evaluator);
        sparqlFunctions.put(name, result);
        return result;
    }

    public SparqlFunctionImpl getOrCreateSparqlFunction(String name) {
        SparqlFunctionImpl result = sparqlFunctions.get(name);
        if(result == null) {
            result = createSparqlFunction(name, null);
            //sparqlFunctions.put(name, result);
        }

        return result;
    }


    public SparqlFunction getSparqlFunction(String name) {
        //FunctionFactory fnFactory = null;
        //SparqlFunction result = new SparqlFunctionImpl(name, null);
        SparqlFunction result = sparqlFunctions.get(name);

        return result;
    }

    public void registerSqlFunction(String sparqlFunctionName, XMethod method) {
        SparqlFunctionImpl fn = getOrCreateSparqlFunction(sparqlFunctionName);
        //fn.getSqlFunctionMap().put(method.getName(), method);
        ExprSubstitutorSql substitutor = (ExprSubstitutorSql)fn.getSubstitutor();
        //substitutor.

        //throw new RuntimeException("Not implemented");

        sqlFunctions.put(method.getName(), method);
    }


    public static  Map<XMethod, TypeDistance[]> findMethodCandidates(Collection<XMethod> candidates, CoercionSystemOld coercions, XClass ...typeSignature) {
        Map<XMethod, TypeDistance[]> bestMatches = new HashMap<XMethod, TypeDistance[]>();
        for(XMethod m : candidates) {

            TypeDistance[] d = XClassUtils.getTypeDistance(typeSignature, m.getSignature().getParameterTypes().toArray(new XClass[0]), coercions);
            if(d == null || Arrays.asList(d).contains(null)) {
                continue;
            }


            // All matches that are worse than current candidate are removed
            // The candidate is only added, if it is not worse than any of the
            // other candidates
            boolean canBeAdded = true;
            for(Iterator<Entry<XMethod, TypeDistance[]>> it = bestMatches.entrySet().iterator(); it.hasNext();) {
                Entry<XMethod, TypeDistance[]> entry = it.next();

                int rel = XClassUtils.getRelation(d, entry.getValue());

                if(rel == -1) {
                    it.remove();
                } else if(rel > 0) {
                    canBeAdded = false;
                }
            }

            if(canBeAdded) {
                bestMatches.put(m, d);
            }
        }

        return bestMatches;


    }

    public static Map<XMethod, Integer[]> findMethodCandidates(Collection<XMethod> candidates, XClass ...typeSignature)
    {
        Map<XMethod, Integer[]> bestMatches = new HashMap<XMethod, Integer[]>();
        for(XMethod m : candidates) {

            Integer[] d = XClassUtils.getDistance(typeSignature, m.getSignature().getParameterTypes().toArray(new XClass[0]));
            if(d == null || Arrays.asList(d).contains(null)) {
                continue;
            }


            // All matches that are worse than current candidate are removed
            // The candidate is only added, if it is not worse than any of the
            // other candidates
            boolean canBeAdded = true;
            for(Iterator<Entry<XMethod, Integer[]>> it = bestMatches.entrySet().iterator(); it.hasNext();) {
                Entry<XMethod, Integer[]> entry = it.next();

                int rel = ClassUtils.getRelation(d, entry.getValue());

                if(rel == -1) {
                    it.remove();
                } else if(rel > 0) {
                    canBeAdded = false;
                }
            }

            if(canBeAdded) {
                bestMatches.put(m, d);
            }
        }

        return bestMatches;

    }


    public SqlMethodCandidate lookupMethod(String sparqlFunctionName, List<TypeToken> argTypes) {
        SparqlFunction fn = sparqlFunctions.get(sparqlFunctionName);
        if(fn == null) {
            return null;
        }
        Collection<XMethod> candidates = fn.getSqlMethods();

        SqlMethodCandidate result = lookupMethod(candidates, argTypes);

        return result;
    }


    public SqlMethodCandidate lookupMethod(Collection<XMethod> candidates, List<TypeToken> argTypes) {
        //Collection<XMethod> candidates = sqlFunctions.get(name);

        List<XClass> resolved = XClassUtils.resolve(this, argTypes);
        XClass[] tmp = resolved.toArray(new XClass[0]);


        Map<XMethod, TypeDistance[]> bestMatches = findMethodCandidates(candidates, coercionSystem, tmp);

        if(bestMatches.size() == 0) {
            //throw new RuntimeException("No method found: " + name + " " + argTypes);
            //throw new RuntimeException("No method found: " + " " + argTypes);
            logger.debug("No method found: " + " " + argTypes);
            return null;
        } else if(bestMatches.size() > 1) {
            throw new RuntimeException("Multiple matches: " + bestMatches);
        }


        Entry<XMethod, TypeDistance[]> entry = bestMatches.entrySet().iterator().next();
        XMethod method = entry.getKey();

        TypeDistance[] typeDistances = entry.getValue();
        List<XMethod> argCoercions = new ArrayList<XMethod>(typeDistances.length);
        for(TypeDistance item : typeDistances) {
            argCoercions.add(item.getCoercion());
        }


        SqlMethodCandidate result = new SqlMethodCandidate(method, argCoercions);

        return result;
    }


    public List<TypeToken> getDirectSuperClasses(TypeToken typeToken) {
        Collection<TypeToken> superClasses = typeHierarchy.get(typeToken);
        List<TypeToken> result = new ArrayList<TypeToken>(superClasses);

        return result;
    }


    private void initNameToType(Map<String, String> typeToClass, Map<String, String> typeToUri, Map<String, String> typeHierarchy) //Map<String, String> typeToClass, Map<String, String> typeToUri, Map<String, String> typeHierarchy) {
    {
        Set<String> all = new HashSet<String>();
        all.addAll(typeToClass.keySet());
        all.addAll(typeToUri.keySet());
        all.addAll(typeHierarchy.keySet());
        all.addAll(typeHierarchy.values());


        Map<TypeToken, XClass> nameToClass = new HashMap<TypeToken, XClass>();

        for(String typeName : all) {
            TypeToken typeToken = TypeToken.alloc(typeName);

            String className = typeToClass.get(typeName);
            Class<?> clazz = null;

            if(className != null) {
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    logger.error("Class '" + className + "' not found");
                }
            }

            String uri = typeToUri.get(typeName);
            Node node = null;
            if(uri != null) {
                node = NodeFactory.createURI(uri);
            }

            XClass datatype = new XClassImpl(this, typeToken, node, clazz);

            TypeToken token = TypeToken.alloc(typeName);
            nameToClass.put(token, datatype);
        }

        IBiSetMultimap<TypeToken, TypeToken> subToSuperType = new BiHashMultimap<TypeToken, TypeToken>();

        for(Entry<String, String> entry : typeHierarchy.entrySet()) {

            TypeToken subType = TypeToken.alloc(entry.getKey());
            TypeToken superType = TypeToken.alloc(entry.getValue());
            //XClass subType = nameToType.get(entry.getKey());
            //XClass superType = nameToType.get(entry.getValue());

            subToSuperType.put(subType, superType);
        }


        this.nameToType = nameToClass;
        this.typeHierarchy = subToSuperType;
    }

    private void initClassToTypeCache() {
        // TODO Only use the most generic type:
        // usigned short -> integer
        // byte -> integer
        // --> integer -> unsigned short
        //
        this.classToToken = new HashMap<Class<?>, TypeToken>();
        for(Entry<TypeToken, XClass> entry : nameToType.entrySet()) {

            TypeToken typeName = entry.getKey();
            XClass datatype = entry.getValue();
            Class<?> clazz = datatype.getCorrespondingClass();
            if(clazz == null) {
                continue;
            }

            TypeToken oldMapping = classToToken.get(clazz);

            if(oldMapping == null || isSuperClassOf(typeName, oldMapping)) {
                classToToken.put(clazz, typeName);
            }

            /*
            if(oldMapping != null) {
                logger.warn("Remapping " + clazz.getName() + " to " + datatype + " was: " + oldMapping);
            }
            */
        }

        for(Entry<Class<?>, TypeToken> entry : classToToken.entrySet()) {
            logger.debug("[DatatypeSytem] Added type mapping: " + entry);
        }
    }

    /**
     *
     *
     * @param nameToType
     * @param typeHierarchy A mapping from sub to super type.
     */
    //public DatatypeSystemCustom(Map<TypeToken, XClass> nameToType, IBiSetMultimap<TypeToken, TypeToken> typeHierarchy) {
    public DatatypeSystemCustom(Map<String, String> typeToClass, Map<String, String> typeToUri, Map<String, String> typeHierarchy)
    {
        initNameToType(typeToClass, typeToUri, typeHierarchy);
        initClassToTypeCache();

        this.coercionSystem = new CoercionSystemImpl();

//		this.nameToType = nameToType;
//		this.typeHierarchy = typeHierarchy;
    }

    @Override
    public TypeToken getTokenForClass(Class<?> clazz) {
        TypeToken result = classToToken.get(clazz);
        return result;
    }


    /*
    public boolean isMoreSpecific(TypeToken a, TypeToken b) {
        Set<TypeToken> commonParents = MultiMaps.getCommonParent(typeHierarchy.asMap(), a, b);


    }
    */


    @Override
    public XClass getByName(TypeToken name) {
        return nameToType.get(name);
    }


    @Override
    public XClass getByName(String name) {
        return nameToType.get(TypeToken.alloc(name));
    }


    public XClass getByClass(Class<?> clazz) {

        //org.springframework.util.ClassUtils.getAllInterfacesAsSet(instance)

        TypeToken typeName = classToToken.get(clazz);
        XClass result = nameToType.get(typeName);
        return result;
    }


    @Override
    public XClass requireByName(String name) {
        XClass result = nameToType.get(TypeToken.alloc(name));
        if(result == null) {
            throw new RuntimeException("No registered datatype found with name '" + name + "'");
        }

        return result;
    }


    @Override
    public NodeValue cast(NodeValue value, TypeToken to) {
        XClass targetType = getByName(to);


        Class<?> targetClazz = targetType.getCorrespondingClass();
        if(targetClazz == null) {
            //throw new RuntimeException("No class corresponding to '" + to + "' found.");
            logger.warn("No class corresponding to '" + to + "' found.");
            return null;
        }
        NodeValue result = tryCast(value, targetType);
        return result;
    }


    // This is totally hacky!
    public NodeValue tryCast(NodeValue nodeValue, XClass targetType) {
        Class<?> targetClazz = targetType.getClass();

        Object value = NodeValueUtilsSparqlify.getValue(nodeValue);
        Object castedValue = Caster.tryCast(value, targetClazz);

        TypeMapper typeMapper = TypeMapper.getInstance();
        RDFDatatype datatype = typeMapper.getSafeTypeByName(targetType.getName());

        Node tmp = NodeFactory.createLiteral("" + castedValue, datatype);
        NodeValue result = NodeValue.makeNode(tmp);
        return result;
    }



    @Override
    public Factory1<SqlExpr> cast(TypeToken from, TypeToken to) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public TypeToken mostGenericDatatype(TypeToken from, TypeToken to) {
        throw new RuntimeException("This method does not make sense. We coulde always return object");
    }


    @Override
    public Set<TypeToken> supremumDatatypes(TypeToken from, TypeToken to) {
        return MultiMaps.getCommonParent(typeHierarchy.asMap(), from, to);
    }


    @Override
    public Integer compare(TypeToken a, TypeToken b) {
        // TODO Auto-generated method stub
        return null;
    }



    public static DatatypeSystemCustom create(Map<String, String> typeToClass, Map<String, String> typeToUri, Map<String, String> typeHierarchy, Logger logger) {

        DatatypeSystemCustom result = new DatatypeSystemCustom(typeToClass, typeToUri, typeHierarchy); //subToSuperType);
        return result;
    }

    @Override
    public boolean isSuperClassOf(TypeToken a, TypeToken b) {

        Collection<TypeToken> superClasses = MultiMaps.transitiveGet(this.typeHierarchy.asMap(), a);
        boolean result = superClasses.contains(b);

        return result;
    }



    @Override
    public void registerCoercion(XMethod method) {
        coercionSystem.register(method);
    }


}

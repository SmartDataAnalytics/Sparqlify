package org.aksw.sparqlify.core.cast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MultiMaps;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.aksw.sparqlify.core.datatypes.XMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.XSD;


public class TypeSystemImpl
	implements TypeSystem
{
	private static final Logger logger = LoggerFactory.getLogger(TypeSystemImpl.class);
	
	private TypeMapper typeMapper;

	private SqlTypeMapper sqlTypeMapper;
	
	private Map<String, SparqlFunction> nameToSparqlFunction = new HashMap<String, SparqlFunction>();
	private Map<String, SqlLiteralMapper> typeToLiteralMapper = new HashMap<String, SqlLiteralMapper>();

	private Map<String, SqlFunctionCollection> nameToSqlFunctions = new HashMap<String, SqlFunctionCollection>(); 
	
	private IBiSetMultimap<TypeToken, TypeToken> typeHierarchy = new BiHashMultimap<TypeToken, TypeToken>();
	private DirectSuperTypeProvider<TypeToken> typeHierarchyProvider = new TypeHierarchyProviderImpl(typeHierarchy);
	
	//private CoercionSystem<TypeToken, NodeValueTransformer> coercionSystem = new CoercionSystemImpl2(this); 
	private CoercionSystem<TypeToken, SqlValueTransformer> coercionSystem = new CoercionSystemImpl3(this);

	
	public TypeSystemImpl() {
		// By default use Jena's default TypeMapper
		this.typeMapper = TypeMapper.getInstance();
		this.sqlTypeMapper = new SqlTypeMapperImpl();
	}

	public CoercionSystem<TypeToken, SqlValueTransformer> getCoercionSystem() {
		return coercionSystem;
	}
	
	public IBiSetMultimap<TypeToken, TypeToken> getTypeHierarchy() {
		return typeHierarchy;
	}
	
	
	@Override
	public void registerSparqlFunction(SparqlFunction sparqlFunction) {
		this.nameToSparqlFunction.put(sparqlFunction.getName(), sparqlFunction);
	}

	/**
	 * Registering the same name with different signatures (overloading)
	 * is allowed.
	 * 
	 */
	void registerSqlFunction(String name, SqlFunctionCollection sqlFunctions) {
		nameToSqlFunctions.put(name, sqlFunctions);
	}

	
	/**
	 * This mapper converts from NodeValues to SQL Literals and vice versa.
	 * 
	 */
	@Override
	public void registerLiteralMapper(String typeUri, SqlLiteralMapper mapper) {
		SqlLiteralMapper oldValue = typeToLiteralMapper.get(typeUri);
		if(oldValue != null) {
			throw new RuntimeException("Literal Mapper for type " + typeUri + " already registered. Redefinition with " + mapper);
		}
		
		typeToLiteralMapper.put(typeUri, mapper);		
	}

	@Override
	public TypeMapper getTypeMapper() {
		return typeMapper;
	}

	@Override
	public void registerCoercion(XMethod method) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SparqlFunction getSparqlFunction(String name) {
		SparqlFunction result = nameToSparqlFunction.get(name);
		return result;
	}

	
	@Override
	public SqlValue convertSql(NodeValue value) //, TypeToken targetTypeToken)
	{
		if(value.hasNode()) {
			Node node = value.asNode();
			if(node.isURI()) {
				logger.warn("FIXME Replacing URI with string - this should be validated when loading views");
				value = NodeValue.makeString(node.getURI());
			}
		}
		
		if(!value.isLiteral()) {
			throw new RuntimeException("Only literals allowed here, got: " + value);
		}
		
		String datatypeUri = value.asNode().getLiteralDatatypeURI();
		if(datatypeUri == null) {
			datatypeUri = XSD.xstring.toString();
		}

		SqlDatatype sqlType = sqlTypeMapper.getSqlDatatype(datatypeUri);

		if(sqlType == null) {
			
			throw new RuntimeException("No SQL conversion found for NodeValue: " + value + " ( " + datatypeUri + ")");
			
		}
		
		SqlValue result = sqlType.toSqlValue(value);
		return result;
	}

	@Override
	public SqlValue cast(SqlValue value, TypeToken targetTypeToken)
	{
		TypeToken sourceTypeToken = value.getTypeToken();
		//TypeToken targetTypeToken = TypeToken.alloc(targetTypeName);
		
		SqlValueTransformer transformer = coercionSystem.lookup(sourceTypeToken, targetTypeToken);

		if(transformer == null) {
			
			throw new RuntimeException("No cast found for: " + value + " to " + targetTypeToken);
			
		}
		
		//NodeValue result;
		SqlValue result;
		try {
			result = transformer.transform(value);
			//result = transformer.transform(value);
		} catch (CastException e) {
			result = null;
		}

		return result;
	}

//	@Override
	public NodeValue cast(NodeValue value, TypeToken targetTypeToken)
	{
		if(!value.isLiteral()) {
			throw new RuntimeException("Only literals allowed here, got: " + value);
		}
		
		String sourceTypeName = value.asNode().getLiteralDatatypeURI();
		TypeToken sourceTypeToken;
		if(sourceTypeName == null) {
			sourceTypeToken = TypeToken.String;
		} else {
			sourceTypeToken= TypeToken.alloc(sourceTypeName); 
		}


		
		
		//TypeToken targetTypeToken = TypeToken.alloc(targetTypeName);
		NodeValueTransformer transformer = null;
		//NodeValueTransformer transformer = coercionSystem.lookup(sourceTypeToken, targetTypeToken);

		if(transformer == null) {
			
			throw new RuntimeException("No cast found for: " + value + " ( " + sourceTypeToken + ") to " + targetTypeToken);
			
		}
		
		NodeValue result;
		try {
			result = transformer.transform(value);
		} catch (CastException e) {
			result = null;
		}

		return result;
	}

	//@Override
	public SqlValueTransformer lookupCast(TypeToken sourceTypeName, TypeToken targetTypeName) {
		SqlValueTransformer result = coercionSystem.lookup(sourceTypeName, targetTypeName);
		return result;
	}

	@Override
	public boolean isSuperClassOf(TypeToken a, TypeToken b) {
		boolean result = TypeSystemUtils.isSuperClassOf(a, b, typeHierarchyProvider);
		return result;
	}

	@Override
	public XClass resolve(String typeName) {
		XClass result = new XClassImpl2(this, this);
		return result;
	}

	@Override
	public Collection<TypeToken> getDirectSuperTypes(TypeToken name) {
		Collection<TypeToken> result = typeHierarchyProvider.getDirectSuperTypes(name);
		return result;
	}

	@Override
	public Factory1<SqlExpr> cast(TypeToken fromTypeUri, TypeToken toTypeUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerSqlFunction(XMethod sqlFunction) {
		// TODO Auto-generated method stub
		
	}

	
	public static TypeSystemImpl create(Map<String, String> typeHierarchy) {
		
		IBiSetMultimap<TypeToken, TypeToken> subToSuperType = new BiHashMultimap<TypeToken, TypeToken>();
		for(Entry<String, String> entry : typeHierarchy.entrySet()) {
			
			TypeToken subType = TypeToken.alloc(entry.getKey());
			TypeToken superType = TypeToken.alloc(entry.getValue());
			
			
			//XClass subType = nameToType.get(entry.getKey());
			//XClass superType = nameToType.get(entry.getValue());

			subToSuperType.put(subType, superType);
		}

		
		TypeSystemImpl result = new TypeSystemImpl();
		
		result.getTypeHierarchy().putAll(subToSuperType);
		
		return result;
	}

	@Override
	public Set<TypeToken> supremumDatatypes(TypeToken from, TypeToken to) {
		Set<TypeToken> result = MultiMaps.getCommonParent(typeHierarchy.asMap(), from, to);
		return result;
	}

	@Override
	public SqlTypeMapper getSqlTypeMapper() {
		return sqlTypeMapper;
	}
}
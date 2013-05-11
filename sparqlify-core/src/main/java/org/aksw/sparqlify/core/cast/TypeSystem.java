package org.aksw.sparqlify.core.cast;


import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.XMethod;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Purposes of the type system are storing information about the following
 * items:
 * 
 * - Mapping of TypeTokens to actual SQL type names.
 *   e.g.TypeToken.String -> text (postgres)
 *   
 * - SQL Model
 *   - available SQL function signatures
 *     e.g. boolean ST_INTERSECTS(geometry, geometry)
 *   - type hierarchy
 *   - coercions
 * 
 * - Rewrites of SPARQL functions to SQL functions
 *   e.g. ogc:intersects(?a, ?b) -> typedLiteral(ST_INTERSECTS(?a, ?b), xsd:boolean)
 * 
 * - Mapping of SPARQL datatypes to SQL ones
 *   - Thereby generation of appropriate cast expressions
 *     e.g. ogc:WKT -> geometry
 *     "POINT(0 0)"^^ogc:geometry -> "POINT(0 0)"::geometry 
 * 
 * Nodes on converting between SPARQL and SQL datatypes:
 * 
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface TypeSystem
	extends TypeResolver, SparqlFunctionProvider, DirectSuperTypeProvider<TypeToken>
{
	
	CoercionSystem<TypeToken, SqlValueTransformer> getCoercionSystem();

	
	void registerSparqlFunction(SparqlFunction sparqlFunction);
	
	/**
	 * Makes an SQL function known to the system
	 * 
	 * @param sqlFunction
	 */
	@Deprecated
	void registerSqlFunction(XMethod sqlFunction);
	
	/**
	 * Conversion of RDF datatypes to SQL literals
	 * 
	 * @param typeToken
	 * @param mapper
	 */
	void registerLiteralMapper(String typeUri, SqlLiteralMapper mapper);

	/**
	 * An object that can convert between Java Objects and NodeValues
	 * @return
	 */
	TypeMapper getTypeMapper();

	SqlTypeMapper getSqlTypeMapper();
	

	/**
	 * TODO Should this method resolve RDF as well as SQL types?
	 * TODO This XClass name sucks. SqlDatatype or SqlClass wouldn't be that
	 * confusing  
	 * 
	 * @param typeName
	 * @return
	 */
	//XClass resolve(String typeName);
	//XClass
	
	
	
	/**
	 *  
	 * @param method
	 */
	@Deprecated
    void registerCoercion(XMethod method);
	
    
    //SparqlFunction getSparqlFunction(String name);	
	//NodeValue cast(NodeValue value, TypeToken targetType);
    //NodeValue cast(NodeValue value, TypeToken targetType);
    SqlValue cast(SqlValue value, TypeToken targetType);
    
    //SqlValue castSql(NodeValue value, TypeToken targetType);
    SqlValue convertSql(NodeValue value);
    
	/**
	 *Return a factory for creating cast-expressions between the given datatypes
	 * Null if no such cast exists. 
	 * 
	 * Constant folding may be performed, but do not rely on it.
	 * So cast(string, int).create(NodeValue.makeString('666')) may return
	 * Cast((string, int), NodeValue('666')) rather than NodeValue.makeInteger(666)
	 */
	Factory1<SqlExpr> cast(TypeToken fromTypeUri, TypeToken toTypeUri);

	@Deprecated
	boolean isSuperClassOf(TypeToken a, TypeToken b);
	
	@Deprecated
	Set<TypeToken> supremumDatatypes(TypeToken from, TypeToken to);
	
	
	Multimap<String, String> getSparqlSqlDecls();
	Map<String, SqlExprEvaluator> getSqlImpls();
	

	FunctionModel<TypeToken> getSqlFunctionModel();

	IBiSetMultimap<TypeToken, TypeToken> getPhysicalTypeMap();
}



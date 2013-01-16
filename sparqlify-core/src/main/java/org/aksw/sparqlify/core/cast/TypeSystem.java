package org.aksw.sparqlify.core.cast;


import java.util.Set;

import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.XMethod;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public interface TypeSystem
	extends TypeResolver, SparqlFunctionProvider, DirectSuperTypeProvider<TypeToken>
{
	
	CoercionSystem<TypeToken, NodeValueTransformer> getCoercionSystem();

	
	void registerSparqlFunction(SparqlFunction sparqlFunction);
	
	/**
	 * Makes an SQL function known to the system
	 * 
	 * @param sqlFunction
	 */
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
	

	/**
	 * TODO Should this method resolve RDF as well as SQL types? 
	 * 
	 * @param typeName
	 * @return
	 */
	//XClass resolve(String typeName);
	
	/**
	 *  
	 * @param method
	 */
    void registerCoercion(XMethod method);
	
    
    //SparqlFunction getSparqlFunction(String name);	
	NodeValue cast(NodeValue value, TypeToken targetType);
	
	/**
	 *Return a factory for creating cast-expressions between the given datatypes
	 * Null if no such cast exists. 
	 * 
	 * Constant folding may be performed, but do not rely on it.
	 * So cast(string, int).create(NodeValue.makeString('666')) may return
	 * Cast((string, int), NodeValue('666')) rather than NodeValue.makeInteger(666)
	 */
	Factory1<SqlExpr> cast(TypeToken fromTypeUri, TypeToken toTypeUri);

	
	boolean isSuperClassOf(TypeToken a, TypeToken b);
	
	Set<TypeToken> supremumDatatypes(TypeToken from, TypeToken to);
}



package org.aksw.sparqlify.automapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.jdbc.Column;
import org.aksw.commons.util.jdbc.ColumnsReference;
import org.aksw.commons.util.jdbc.ForeignKey;
import org.aksw.commons.util.jdbc.JdbcUtils;
import org.aksw.commons.util.jdbc.PrimaryKey;
import org.aksw.commons.util.jdbc.Relation;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.nodes.VarDef;
import org.aksw.sparqlify.config.syntax.RelationRef;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.DatatypeSystemOld;
import org.aksw.sparqlify.core.SqlDatatype;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;


interface UriSchema {
	String getClassUri(String relationName);
}

class UriSchemaDefault
	implements UriSchema 
{
	private String hostName;
	
	public UriSchemaDefault(){
		hostName = "http://localhost";
	}
	
	public String getNamespace()
	{
		return hostName;
	}
	
	@Override
	public String getClassUri(String relationName) {
		return getNamespace() + "/" + relationName;
	}
	
}

public class AutoMapper {
	public static final Logger logger = LoggerFactory.getLogger(AutoMapper.class);
	

	
	public static String getBlockKey(String key) {
		return key.replace("_", "").toLowerCase();
	}
	
	
	/**
	 * Foreign keys by convention
	 * 
	 * @return
	 */
	/*
	public static Multimap<String, ForeignKey> getConventionalForeignKeys(Collection<RelationMetadata> relations) {
		
		Map<String, RelationMetadata> relationBlockKeys = new HashMap<String, RelationMetadata>();
		
		for(RelationMetadata relation : relations) {
			relationBlockKeys.put(getBlockKey(relation.getName()), relation);
		}
		
		
		for(RelationMetadata relation : relations) {
			
			String relationNameBlockKey = getBlockKey(relation.getName());
			
				
			for(ColumnMetadata column : relation.getColumns().values()) {
				// TODO person_id ->
				
				if(!column.getName().endsWith("id")) {
					continue;
				}
				
				String columnNamePrefix = column.getName().substring(0, column.getName().length() - 3);
				
				String blockName = getBlockKey(columnNamePrefix);
				
				RelationMetadata target = relationBlockKeys.get(columnNamePrefix);
				if(target == null) {
					continue;
				}
				
				
				
				// check if 
			}
				
		}
		
	}*/
	
	public static List<Column> getNonKeyColumns(Relation relation, PrimaryKey primaryKey, Collection<ForeignKey> fks) {
		List<Column> result = new ArrayList<Column>();
		for(Column column : relation.getColumns().values()) {
			String name = column.getName();
			
			if(primaryKey != null) {
				if(primaryKey.getSource().getColumnNames().contains(name)) {
					continue;
				}
			}
			
			for(ForeignKey fk : fks) {
				if(fk.getSource().getColumnNames().contains(name)) {
					continue;
				}
			}
			
			result.add(column);
		}

		return result;
	}
	
	public static VarDef columnReferenceToUriTermDef(String prefix, ColumnsReference columnsReference) {
		return new VarDef(columnReferenceToUriExpr(prefix, columnsReference));
	}
	
	public static Expr columnReferenceToUriExpr(String prefix, ColumnsReference columnsReference) {
		return E_RdfTerm.createUri(columnReferenceToExpr(prefix, columnsReference));
	}
	
	public static Expr columnReferenceToExpr(String prefix, ColumnsReference columnsReference) {
		ExprList exprs = new ExprList();

		prefix += columnsReference.getTableName();
		Expr prefixExpr = NodeValue.makeString(prefix);

		exprs.add(prefixExpr);
		
		List<String> columnNames = columnsReference.getColumnNames();
		for(int i = 0; i < columnNames.size(); ++i) {
			if(i != 0) {
				exprs.add(NodeValue.makeString("-"));
			}
			exprs.add(new ExprVar(columnNames.get(i)));
		}
		
		E_StrConcatPermissive result = new E_StrConcatPermissive(exprs);

		return result;
	}
	
	public static void main(String[] args) throws SQLException {
		// Read commandline args (database connection + optional file with prefix definitions)
		
		/*
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(RDF.type.asNode());
		nodes.add(RDF.first.asNode());
		nodes.add(RDF.rest.asNode());
		
		Query q = CannedQueryUtils.selectBySubjects(nodes);
		System.out.println(q);
		if(true) {
			return;
		}
		*/
		
		
		
		AutoMapper mapper = new AutoMapper();
		//mapper.generate(getConnection());
		
		
		DatatypeSystemOld system = new DatatypeSystemDefault();

		Map<String, SqlDatatype> datatypeMap = new HashMap<String, SqlDatatype>();
		datatypeMap.put("int4", DatatypeSystemDefault._INT);
		datatypeMap.put("varchar", DatatypeSystemDefault._STRING);
		datatypeMap.put("bpchar", DatatypeSystemDefault._STRING);
		datatypeMap.put("text", DatatypeSystemDefault._STRING);
		datatypeMap.put("date", DatatypeSystemDefault._DATE);
		datatypeMap.put("float8", DatatypeSystemDefault._FLOAT);
		datatypeMap.put("timestamp", DatatypeSystemDefault._DATE_TIME);
		
		
		Schema schema = Schema.create(getConnection());
		
		
		
		Set<String> relationNames = JdbcUtils.fetchRelationNames(getConnection());
		System.out.println(relationNames);

		
		Map<String, Relation> relations = JdbcUtils.fetchColumns(getConnection());
		
		Multimap<String, ForeignKey> foreignKeys = schema.getForeignKeys();
		System.out.println(foreignKeys);

		Map<String, PrimaryKey> primaryKeys = schema.getPrimaryKeys();
		System.out.println(primaryKeys);
		
		
		// Infer Foreign keys
		// This method is good enough for BSBM, but could be improved for other use-cases
		Generator genFk = Gensym.create("fk_autogen");
		for(Relation relation : relations.values()) {
			Collection<ForeignKey> fks = foreignKeys.get(relation.getName());
			// If fks exists, we assume that all have been stated
			if(!fks.isEmpty()) {
				continue;
			}
			
			for(Column column : relation.getColumns().values()) {
				
				// Check if a table has the same name as the column
				Relation r = relations.get(column.getName());
				if(r == null) {
					continue;
				}
			
				// Without primary key we have no link target
				PrimaryKey pk = primaryKeys.get(r.getName());
				if(pk == null) {
					continue;
				}

				
				ColumnsReference source = new ColumnsReference(relation.getName());
				source.getColumnNames().add(column.getName());
				
				
				String fkName = genFk.next();
				ForeignKey fk = new ForeignKey(fkName, source, pk.getSource());
				foreignKeys.put(relation.getName(), fk);
				
				System.out.println("Auto FK: " + fk);
			}
		}
		
		
		
		
		
		//UriSchema uriSchema = new UriSchemaDefault();
		String prefix = "http://localhost/am/";
		
		/**
		 *  TODO:
		 *  - N-M Mappings
		 */

		for(Relation relation : relations.values()) {
			
			ViewDefinition viewDefinition = new ViewDefinition();
			
			viewDefinition.setName("view_" + relation.getName());
			
			//Generator genFkSourceVar = Gensym.create("fk_src");
			Generator genFkTargetVar = Gensym.create("fk");
			Generator genColumnVar = Gensym.create("o");
			
			if(!relationNames.contains(relation.getName())) {
				continue;
			}
			
			PrimaryKey primaryKey = primaryKeys.get(relation.getName());
			if(primaryKey == null) {
				//logger.warn(relation.getName() + ": Neither primary nor foreign keys found. Skipping.");
				logger.warn(relation.getName() + ": No primary key found. Skipping.");
				continue;
			}


			Collection<ForeignKey> fks = foreignKeys.get(relation.getName());
			// FIXME Replace with safeGet
			if(fks == null) {
				fks = Collections.emptySet();
			}
			

			Var mainVar = Var.alloc("s");


			VarDef mainVarDef = columnReferenceToUriTermDef(prefix, primaryKey.getSource());
			viewDefinition.addVarDef(mainVar, mainVarDef);
			
			Node classUri = Node.createURI(prefix + StringUtils.toUpperCamelCase(relation.getName()));
			viewDefinition.getConstructPattern().add(new Quad(null, new Triple(mainVar, RDF.type.asNode(), classUri)));

		
			// Create Uris that join according to the FK
			for(ForeignKey fk : fks) {
				// Product(id) Order(id, product_id) -> uri(?order_id) am:product (?product_id) 

				/*
				Var sourceVar = Var.alloc(genFkSourceVar.next());				
				TermDef sourceVarDef = columnReferenceToUriTermDef(prefixExpr, fk.getSource());
				viewDefinition.addVarDef(sourceVar, mainVarDef);
				*/

				Var targetVar = Var.alloc(genFkTargetVar.next());
				//StringUtils.toUpperCamelCase(fk.getSource().getT)
				VarDef targetVarDef = columnReferenceToUriTermDef(prefix, fk.getSource());
				viewDefinition.addVarDef(targetVar, targetVarDef);

				Node property = Node.createURI(prefix + StringUtils.toLowerCamelCase(fk.getTarget().getTableName())); 
				
				Triple triple = new Triple(mainVar, property, targetVar);
				Quad quad = new Quad(Quad.defaultGraphNodeGenerated, triple);
				viewDefinition.getConstructPattern().add(quad);
				
			}
			
			List<Column> nonKeyColumns = getNonKeyColumns(relation, primaryKey, fks);
			
			for(Column column : nonKeyColumns) {
				Var var = Var.alloc(genColumnVar.next());
				
				SqlDatatype dt = datatypeMap.get(column.getType()); //system.getByName(column.getType());
				if(dt == null) {
					logger.warn(column.getType() + ": no datatype mapping found");
				}

				Node datatype = Node.createURI("http://aksw.org/sparqlify/unknown-datatype");
				if(dt != null) {
					Node xsd = dt.getXsd();
					if(xsd != null) {						
						datatype = xsd;
					}
				}
				
				if(datatype.equals(XSD.xstring.asNode())) {
					viewDefinition.addVarDef(var, E_RdfTerm.createPlainLiteral(new ExprVar(column.getName())));					
				} else {
					Expr dte = NodeValue.makeString(datatype.toString());
					viewDefinition.addVarDef(var, E_RdfTerm.createTypedLiteral(new ExprVar(column.getName()), dte));					
				}


				// FIXME Encode name
				Node property = Node.createURI(prefix + column.getName());
				
				Triple triple = new Triple(mainVar, property, var);
				Quad quad = new Quad(Quad.defaultGraphNodeGenerated, triple);
				viewDefinition.getConstructPattern().add(quad);
			}
						
			
			//String relationClass = uriSchema.getClassUri(relationName);			
			//SqlNode relationNode = new SqlTable(relation.getName());
			viewDefinition.setRelation(new RelationRef(relation.getName()));
			
			System.out.println(viewDefinition.getDefinitionString());
			
		}
		
		
		/*
		for(String relationName : relationNames) {
			
		}*/
	}

	public AutoMapper() {

	}

	public static Connection getConnection() throws SQLException {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();

		dataSource.setDatabaseName("bsbm_default");
		dataSource.setUser("postgres");
		dataSource.setPassword("postgres");
		dataSource.setServerName("localhost");
		dataSource.setPortNumber(5432);

		Connection conn = dataSource.getConnection();

		return conn;
	}
	
	public static List<String> getColumnNames(ResultSet rs)
			throws SQLException
	{
		ResultSetMetaData meta = rs.getMetaData();
		List<String> result = new ArrayList<String>(meta.getColumnCount());
		for(int i = 0; i < meta.getColumnCount(); ++i) {
			result.add(meta.getColumnName(i + 1));
		}
		return result;
	}
	
	public static List<Object> getRow(ResultSet rs)
			throws SQLException
	{
		ResultSetMetaData meta = rs.getMetaData();
		List<Object> result = new ArrayList<Object>(meta.getColumnCount());
		
		for(int i = 0; i < meta.getColumnCount(); ++i) {
			result.add(rs.getObject(i + 1));
		}
		
		return result;
	}


}

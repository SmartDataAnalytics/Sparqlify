package org.aksw.sparqlify.core.sparql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.sparqlify.core.MakeNodeValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class RowMapperSparqlifyBinding
    implements RowMapper<Binding>
{
    private static final Logger logger = LoggerFactory.getLogger(RowMapperSparqlifyBinding.class);

    //private long nextRowId;
    private Var rowIdVar;

    public RowMapperSparqlifyBinding()
    {
        this("rowId");
    }


    public RowMapperSparqlifyBinding(String rowIdName)
    {
        this.rowIdVar = rowIdName == null ? null : Var.alloc(rowIdName);
     }


    @Override
    public Binding mapRow(ResultSet rs, int rowId) {
        Binding result = _map(rs, rowId, rowIdVar);
        return result;
    }

    public static Binding _map(ResultSet rs, long rowId, Var rowIdVar) {
        Binding result;
        try {
            result = map(rs, rowId, rowIdVar);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static boolean addAttr(BindingBuilder binding, int i, String colName, Object colValue) {
        NodeValue nodeValue;

        // NOTE Char right padding is handled as a special expression (similar to urlEncode)
//		String colType = meta.getColumnTypeName(i);
//
//		//System.out.println(colValue == null ? "null" : colValue.getClass());
//
//		// TODO: Make datatype serialization configurable
//		if(isCharType(colType)) {
//			if(colValue == null) {
//				nodeValue = null;
//			} else {
//				int displaySize = meta.getPrecision(i);
//				int scale = meta.getScale(i);
//				String tmp = "" + colValue;
//				String v = StringUtils.rightPad(tmp, displaySize);
//				nodeValue = NodeValue.makeString(v);
//			}
//		}
//		else
        if(colValue instanceof Date) {
            String tmp = colValue.toString();
            nodeValue = NodeValue.makeDate(tmp);
        }
        else if(colValue instanceof Timestamp) {
            String tmp = colValue.toString();
            String val = tmp.replace(' ', 'T');
            nodeValue = NodeValue.makeDateTime(val);
        } else if(colValue instanceof UUID) {
            nodeValue = NodeValue.makeString(colValue.toString());
        } else {
            try {
                nodeValue = MakeNodeValue.makeNodeValue(colValue);
            } catch (Exception e) {
                logger.error("TODO: Handle unknown column type for " + colValue + " type: " + colValue.getClass());
                nodeValue = null;
                //throw new RuntimeException(e);
            }
        }

        if(nodeValue == null) {
            return true;
            //continue;
        }

        if(nodeValue.equals(E_RdfTerm.TYPE_ERROR)) {
            return true;
            //continue;
        }

//		if(nodeValue.isDateTime()) {
//			XSDDateTime val = nodeValue.getDateTime();
//			String str = val.timeLexicalForm();
//			String b = val.toString();
//
//			System.out.println("foo");
//		}

        Node node = nodeValue.asNode();


        // FIXME We also add bindings that enable us to reference the columns by their index
        // However, indexes and column-names are in the same namespace here, so there might be clashes
        Var indexVar = Var.alloc("" + i);
        binding.add(indexVar, node);

        Var colVar = Var.alloc(colName);
        if(!binding.contains(colVar)) {
            binding.add(colVar, node);
        }

        return false;
    }

    public static Binding map(ResultSet rs, long rowId, Var rowIdVar) throws SQLException {

        // OPTIMIZE refactor these to attributes
        //NodeExprSubstitutor substitutor = new NodeExprSubstitutor(sparqlVarMap);
        BindingBuilder binding = BindingFactory.builder();


        ResultSetMetaData meta = rs.getMetaData();

        /*
        for(int i = 1; i <= meta.getColumnCount(); ++i) {
            binding.add(Var.alloc("" + i), node)
        }*/


        // Substitute the variables in the expressions
        for(int i = 1; i <= meta.getColumnCount(); ++i) {
            String colName = meta.getColumnLabel(i);
            Object colValue = rs.getObject(i);

            boolean skip = addAttr(binding, i, colName, colValue);
            if(skip) {
                continue;
            }
        }



        // Additional "virtual" columns
        // FIXME Ideally this should be part of a class "ResultSetExtend" that extends a result set with additional columns
        if(rowIdVar != null) {
            Node node = NodeValue.makeInteger(rowId).asNode();

            binding.add(rowIdVar, node);
        }

        return binding.build();
    }
}
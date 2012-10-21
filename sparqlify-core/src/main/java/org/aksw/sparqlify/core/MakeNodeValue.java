package org.aksw.sparqlify.core;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.old.NodeValueGeom;
import org.apache.commons.lang.NotImplementedException;
import org.h2.jdbc.JdbcClob;
import org.postgis.PGgeometry;
import org.postgresql.util.PGobject;

import com.hp.hpl.jena.sparql.expr.NodeValue;


public class MakeNodeValue
{
	public static NodeValue makeNodeValue(Object o)
	{
		if(o == null) {
			return null;
		}
		
		return (NodeValue)MultiMethod.invokeStatic(MakeNodeValue.class, "_makeNodeValue", o);
	}

	public static NodeValue _makeNodeValue(BigDecimal o) {
		return NodeValue.makeDecimal(o);
	}

	public static NodeValue _makeNodeValue(Float o) {
		return NodeValue.makeFloat(o);
	}

	public static NodeValue _makeNodeValue(Boolean o) {
		return NodeValue.makeBoolean(o);
	}
	
	public static NodeValue _makeNodeValue(Integer o) {
		return NodeValue.makeNodeInteger(o);
	}

	public static NodeValue _makeNodeValue(Double o) {
		return NodeValue.makeNodeDouble(o);
	}

	public static NodeValue _makeNodeValue(String o) {
		return NodeValue.makeNodeString(o);
	}
	
	public static NodeValue _makeNodeValue(Long o) {
		return NodeValue.makeNodeInteger(o);
	}
	
	public static NodeValue _makeNodeValue(Calendar o)
	{
		return NodeValue.makeDate(o);
	}

	public static NodeValue _makeNodeValue(Timestamp o) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(o);
		return NodeValue.makeNodeDateTime(cal);
	}

	public static NodeValue _makeNodeValue(Date o) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(o);
		return NodeValue.makeNodeDate(cal);		
	}
	
	public static NodeValue _makeNodeValue(JdbcClob o) {
		String str;
		try {
			str = StreamUtils.toString(o.getAsciiStream());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		NodeValue result = NodeValue.makeString(str);
		return result;
	}
	
	public static NodeValue _makeNodeValue(PGobject o) {
		String type = o.getType();
		if(type.equals("geography") || type.equals("geometry")) {
			try {
				PGgeometry geom = new PGgeometry(o.getValue());
				return new NodeValueGeom(geom);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new NotImplementedException();
		}
		
		
		return null;
	}
}
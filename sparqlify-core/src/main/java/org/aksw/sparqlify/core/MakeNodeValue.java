package org.aksw.sparqlify.core;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.sparqlify.algebra.sparql.expr.old.NodeValueGeom;
import org.apache.commons.lang.NotImplementedException;
import org.h2.jdbc.JdbcClob;
import org.postgis.PGgeometry;
import org.postgresql.util.PGobject;

import org.apache.jena.sparql.expr.NodeValue;


public class MakeNodeValue
{
	public static NodeValue makeNodeValueWorkingButSlow(Object o)
	{
		if(o == null) {
			return null;
		}
		
		return (NodeValue)MultiMethod.invokeStatic(MakeNodeValue.class, "_makeNodeValue", o);
	}

	public static NodeValue makeNodeValue(Object o)
	{
		NodeValue result;
		if(o == null) {
			result = null;
		}
		else if(o instanceof Integer) {
			result = _makeNodeValue((Integer)o);
		}
		else if(o instanceof String) {
			result = _makeNodeValue((String)o);
		}
		else if(o instanceof Boolean) {
			result = _makeNodeValue((Boolean)o);
		}
		else if(o instanceof Long) {
			result = _makeNodeValue((Long)o);
		}
		else if(o instanceof Float) {
			result = _makeNodeValue((Float)o);
		}
		else if(o instanceof Double) {
			result = _makeNodeValue((Double)o);
		}
		else if(o instanceof Calendar) {
			result = _makeNodeValue((Calendar)o);
		}
		else if(o instanceof Timestamp) {
			result = _makeNodeValue((Timestamp)o);
		}
		else if(o instanceof Date) {
			result = _makeNodeValue((Date)o);
		}
		else if(o instanceof BigDecimal) {
			result = _makeNodeValue((BigDecimal)o);
		}
		else if(o instanceof byte[]) {
			result = _makeNodeValue((byte[])o);
		}
		else if(o instanceof JdbcClob) {
			result = _makeNodeValue((JdbcClob)o);
		}
		else if(o instanceof PGobject) {
			result = _makeNodeValue((PGobject)o);
		} 
		else {
			throw new RuntimeException("Unknown argument type " + o.getClass() + " of object " + o);
		}

		return result;
		
		//return (NodeValue)MultiMethod.invokeStatic(MakeNodeValue.class, "_makeNodeValue", o);
	}

	public static NodeValue _makeNodeValue(byte[] bytes) {
		String str = StringUtils.bytesToHexString(bytes);
		String uc = str.toUpperCase();
		NodeValue result = NodeValue.makeString(uc);
		return result;
	}
	

	public static NodeValue _makeNodeValue(Double o) {
		return NodeValue.makeNodeDouble(o);
	}

	public static NodeValue _makeNodeValue(Float o) {
		return NodeValue.makeFloat(o);
	}

	public static NodeValue _makeNodeValue(BigDecimal o) {
		return NodeValue.makeDecimal(o);
	}

	public static NodeValue _makeNodeValue(Boolean o) {
		return NodeValue.makeBoolean(o);
	}

	public static NodeValue _makeNodeValue(Long o) {
		return NodeValue.makeNodeInteger(o);
	}
	
	public static NodeValue _makeNodeValue(Integer o) {
		return NodeValue.makeNodeInteger(o);
	}

	public static NodeValue _makeNodeValue(String o) {
		return NodeValue.makeNodeString(o);
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
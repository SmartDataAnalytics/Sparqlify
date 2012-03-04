package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatypeGeography;

/**
 *
 * http://postgis.refractions.net/documentation/manual-1.3/ch06.html
 * 
 * ST_Intersects(geometry, geometry)
 *
 * Returns 1 (TRUE) if the Geometries "spatially intersect".
 */
public class S_Intersects
	extends SqlExpr2
{
	public S_Intersects(SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super(left, right, datatype);
	}

	public S_Intersects(SqlExpr left, SqlExpr right) {
		super(left, right, DatatypeSystemDefault._BOOLEAN);
	}
	
	
	/**
	 * Only returns a new expression, if the datatypes are correct, othewise null
	 * FIXME: Maybe rather than returning null, it should be false
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static SqlExpr create(SqlExpr left, SqlExpr right) {
		//if(!(left.getDatatype() instanceof SqlDatatypeGeography && right.getDatatype() instanceof SqlDatatypeGeography)) {
		if(!((left.getDatatype().equals(DatatypeSystemDefault._GEOGRAPHY) && right.getDatatype().equals(DatatypeSystemDefault._GEOGRAPHY)) ||
				(left.getDatatype().equals(DatatypeSystemDefault._GEOMETRY) && right.getDatatype().equals(DatatypeSystemDefault._GEOMETRY)))) {

			return new SqlExprValue(false);
		}

		return new S_Intersects(left, right);

	}
}

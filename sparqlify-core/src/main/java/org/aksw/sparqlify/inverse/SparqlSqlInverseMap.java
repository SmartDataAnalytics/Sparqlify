package org.aksw.sparqlify.inverse;

import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * 
 * 
 * @author raven
 *
 */
public class SparqlSqlInverseMap
{
	private Quad candidateQuad;
	private ViewDefinition viewDefinition;
	private Quad viewQuad;	
	private Map<S_ColumnRef, SqlValue> columnToValue;
	
	public SparqlSqlInverseMap(Quad candidateQuad, ViewDefinition viewDefinition, Quad viewQuad, Map<S_ColumnRef, SqlValue> columnToValue) {
		this.candidateQuad = candidateQuad;
		this.viewDefinition = viewDefinition;
		this.viewQuad = viewQuad;
		this.columnToValue = columnToValue;
	}

	public Quad getCandidateQuad() {
		return candidateQuad;
	}

	public ViewDefinition getViewDefinition() {
		return viewDefinition;
	}

	public Quad getViewQuad() {
		return viewQuad;
	}

	public Map<S_ColumnRef, SqlValue> getColumnToValue() {
		return columnToValue;
	}
	
	
	@Override
	public String toString() {
		return "SparqlSqlInverseMap [candidateQuad=" + candidateQuad
				+ ", viewDefinition=" + viewDefinition + ", viewQuad="
				+ viewQuad + ", columnToValue=" + columnToValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((candidateQuad == null) ? 0 : candidateQuad.hashCode());
		result = prime * result
				+ ((columnToValue == null) ? 0 : columnToValue.hashCode());
		result = prime * result
				+ ((viewDefinition == null) ? 0 : viewDefinition.hashCode());
		result = prime * result
				+ ((viewQuad == null) ? 0 : viewQuad.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SparqlSqlInverseMap other = (SparqlSqlInverseMap) obj;
		if (candidateQuad == null) {
			if (other.candidateQuad != null)
				return false;
		} else if (!candidateQuad.equals(other.candidateQuad))
			return false;
		if (columnToValue == null) {
			if (other.columnToValue != null)
				return false;
		} else if (!columnToValue.equals(other.columnToValue))
			return false;
		if (viewDefinition == null) {
			if (other.viewDefinition != null)
				return false;
		} else if (!viewDefinition.equals(other.viewDefinition))
			return false;
		if (viewQuad == null) {
			if (other.viewQuad != null)
				return false;
		} else if (!viewQuad.equals(other.viewQuad))
			return false;
		return true;
	}
	
	
}
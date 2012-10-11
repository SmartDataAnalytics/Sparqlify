package org.aksw.sparqlify.core.domain.input;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

/**
 * A reference to another view definition's logical table.
 * 
 * Roughly corresponds to a foreign key reference.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ViewReference {
	private String targetViewName;
	private SqlExpr joinCondition;
	
	public ViewReference(String targetViewName, SqlExpr joinCondition) {
		super();
		this.targetViewName = targetViewName;
		this.joinCondition = joinCondition;
	}

	public String getTargetViewName() {
		return targetViewName;
	}
	
	public SqlExpr getJoinCondition() {
		return joinCondition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((joinCondition == null) ? 0 : joinCondition.hashCode());
		result = prime * result
				+ ((targetViewName == null) ? 0 : targetViewName.hashCode());
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
		ViewReference other = (ViewReference) obj;
		if (joinCondition == null) {
			if (other.joinCondition != null)
				return false;
		} else if (!joinCondition.equals(other.joinCondition))
			return false;
		if (targetViewName == null) {
			if (other.targetViewName != null)
				return false;
		} else if (!targetViewName.equals(other.targetViewName))
			return false;
		return true;
	}
}

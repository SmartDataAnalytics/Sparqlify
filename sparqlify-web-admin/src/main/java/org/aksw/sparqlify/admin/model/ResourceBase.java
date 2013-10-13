package org.aksw.sparqlify.admin.model;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ResourceBase {
	private String primaryLabel;
	private String primaryComment;

	public String getPrimaryLabel() {
		return primaryLabel;
	}
	public void setPrimaryLabel(String primaryLabel) {
		this.primaryLabel = primaryLabel;
	}
	public String getPrimaryComment() {
		return primaryComment;
	}
	public void setPrimaryComment(String primaryComment) {
		this.primaryComment = primaryComment;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((primaryComment == null) ? 0 : primaryComment.hashCode());
		result = prime * result
				+ ((primaryLabel == null) ? 0 : primaryLabel.hashCode());
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
		ResourceBase other = (ResourceBase) obj;
		if (primaryComment == null) {
			if (other.primaryComment != null)
				return false;
		} else if (!primaryComment.equals(other.primaryComment))
			return false;
		if (primaryLabel == null) {
			if (other.primaryLabel != null)
				return false;
		} else if (!primaryLabel.equals(other.primaryLabel))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResourceBase [primaryLabel=" + primaryLabel
				+ ", primaryComment=" + primaryComment + "]";
	}
	
}

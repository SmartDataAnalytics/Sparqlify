package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.sparql.core.Quad;

public class ViewQuad {
	private ViewDefinition view;
	private Quad quad;
	
	// TODO Maybe another field for some constraints
	
	public ViewQuad(ViewDefinition view, Quad quad) {
		this.view = view;
		this.quad = quad;
	}

	public ViewDefinition getView() {
		return view;
	}

	public Quad getQuad() {
		return quad;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((quad == null) ? 0 : quad.hashCode());
		result = prime * result + ((view == null) ? 0 : view.hashCode());
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
		ViewQuad other = (ViewQuad) obj;
		if (quad == null) {
			if (other.quad != null)
				return false;
		} else if (!quad.equals(other.quad))
			return false;
		if (view == null) {
			if (other.view != null)
				return false;
		} else if (!view.equals(other.view))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return view.getName() + ":" + quad.toString();
	}
}
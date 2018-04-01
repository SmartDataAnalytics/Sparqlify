package org.aksw.obda.domain.api;

import java.util.Optional;

public interface Polymorphic {
	@SuppressWarnings("unchecked")
	default <T> Optional<T> tryAs(Class<T> clazz) {
		return Optional.ofNullable(this.getClass().isAssignableFrom(clazz) ? (T)this : null);
	}
}

package org.aksw.commons.factory;

import java.util.List;

public interface FactoryN<T> {
	T create(List<T> args);
}

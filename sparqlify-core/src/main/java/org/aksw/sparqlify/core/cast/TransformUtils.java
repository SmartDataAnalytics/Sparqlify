package org.aksw.sparqlify.core.cast;

import javax.annotation.Nullable;

import org.aksw.sparqlify.core.TypeToken;

import com.google.common.base.Function;

public class TransformUtils {
	public static final Function<String,TypeToken> toTypeToken = new Function<String, TypeToken>() {

		@Override
		public TypeToken apply(@Nullable String typeName) {
			return TypeToken.alloc(typeName);
		}
		
	};
}
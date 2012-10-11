package org.aksw.sparqlify.config.syntax;

public interface FunctionDeclaration {
	FunctionSignature getSignature();

	/**
	 * TODO What do we want here? Either:
	 * -) A method that returns a factory for creating function expressions, such as create(a, b) -> S_And.create(a, b)
	 * -) A method that can actually evaluate functions.
	 * 
	 * Actually, the former case is more general, as it enables the construction of expressions.
	 * The expression object itself could have a method for the evaluation.
	 * 
	 * 
	 * 
	 * 
	 * @return The definition (which can actually evaluate the function), null if not set.
	 */
	//FunctionDefinition getDefinition();
}

package org.aksw.sparqlify.rest;

import java.io.OutputStream;

interface Writer<T> {
	void write(OutputStream out, T obj);
}
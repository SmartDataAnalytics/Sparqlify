package org.aksw.sparqlify.web;

import java.io.OutputStream;

interface Writer<T> {
	void write(OutputStream out, T obj);
}
package org.aksw.sparqlify.core.datatypes;


interface CoercionSystem {
	XMethod lookup(XClass source, XClass target);
}
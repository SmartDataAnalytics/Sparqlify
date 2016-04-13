package org.aksw.sparqlify.update;

import org.apache.jena.sparql.core.Quad;

interface GraphListener {
	void onPreBatchStart();
	
	void onPreInsert(Quad quad);
	void onPreDelete(Quad quad);

	void onPreBatchEnd();


	void onPostBatchStart();
	
	void onPostInsert(Quad quad);
	void onPostDelete(Quad quad);
	
	void onPostBatchEnd();
}
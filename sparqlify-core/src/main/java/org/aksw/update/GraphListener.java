package org.aksw.update;

import com.hp.hpl.jena.sparql.core.Quad;

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
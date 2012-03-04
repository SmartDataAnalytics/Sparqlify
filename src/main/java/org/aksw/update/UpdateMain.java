package org.aksw.update;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import org.aksw.commons.collections.diff.HashSetDiff;
import org.aksw.commons.collections.diff.IDiff;
import org.aksw.commons.sparql.core.impl.ModelSparqlEndpoint;
import org.aksw.commons.util.Files;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class UpdateMain {
	public static void main(String[] args)
		throws Exception
	{
		Model testModel = ModelFactory.createDefaultModel();
		testModel.read(new FileInputStream(new File("data/base.nt")), null,
				"N-TRIPLE");

		ModelSparqlEndpoint endpoint = new ModelSparqlEndpoint(testModel);

		// Create a graph for which we can listen for updates
		UpdateGraph graph = new UpdateGraph(endpoint);

		// Attach a cleaner to the graph, which makes sure that we get actual
		// changes to the graph, rather than the raw change request which might have
		// no effect, as the change is redundant.
		// TODO Integrate this into the graph class
		//		This cleaning causes severe overhead, however, as long as there are no
		//		listeners, there is nothing to do
		CleanListener cleaner = new CleanListener(endpoint);
		graph.getPreUpdateListeners().add(cleaner);

		
		
		String queryString = "Select * { ?s a <http://dbpedia.org/ontology/Person> . ?s ?p ?o . }";

		//Model insertModel = ModelFactory.createDefaultModel();
		//insertModel.read(new FileInputStream(new File("data/inserts.nt")),
		//		null, "N-TRIPLE");

		IncrementalQueryUpdateManager queryUpdates = new IncrementalQueryUpdateManager(
				queryString, endpoint);

		// Attach the update manager to the cleaner
		cleaner.getGraphListeners().add(queryUpdates);
		
		String inserts = Files.readContent(new File("data/inserts.nt"));
		
		String stmt = "Insert Data {\n" + inserts + "\n}";
		graph.executeUpdate(stmt);
		processChanges(queryUpdates.getChanges());


		stmt = "Delete Data {\n" + inserts + "\n}";		
		graph.executeUpdate(stmt);
		processChanges(queryUpdates.getChanges());
		
		
		//stmt = "Insert Data { <http://dbpedia.org/resource/Saldria> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Person> .}";
		stmt = "Insert { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Person> .} Where { ?s ?p ?o . }";
		graph.executeUpdate(stmt);
		processChanges(queryUpdates.getChanges());

		/*
		stmt = "Delete Data { <http://dbpedia.org/resource/Saldria> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Person> .}";
		graph.executeUpdate(stmt);
		processChanges(queryUpdates.getChanges());
		*/

	}


	public static void print(Collection<Binding> bindings) {
		for(Binding item : bindings) {
			System.out.println(item);
		}
	}
	
	public static void processChanges(HashSetDiff<Binding> diff) {
		System.out.println("Added:");
		System.out.println("-----------------");
		print(diff.getAdded());
		
		System.out.println();
		System.out.println("Removed");
		System.out.println("-----------------");
		print(diff.getRemoved());
		//System.out.println(ResultSetFormatter.asText(diff.getRemoved()));
	}
}

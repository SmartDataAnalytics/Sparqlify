package org.aksw.sparqlify.update;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 */
public class FilterCompiler
{
	/*
		@Override
		public List<String> compileFilter(Collection<List<Object>> keys,
				List<String> columnNames)
		{

			List<String> result = new ArrayList<String>();

			// Special handling for a set of resources
			if (columnNames.size() == 1) {

				String columnName = columnNames.get(0);
				Set<Object> remaining = new HashSet<Object>(
						new FlatMapView<Object>(keys));

				Set<Resource> resources = new HashSet<Resource>();

				Iterator<Object> it = remaining.iterator();
				while (it.hasNext()) {
					Object current = it.next();
					if (current instanceof Resource) {
						resources.add((Resource) current);
					}

					it.remove();
				}

				String inPart = Joiner.on(">,<").join(resources);

				if (!inPart.isEmpty()) {
					result.add(columnName + " In (<" + inPart + ">)");
				}

				for (Resource resource : resources)
					remaining.remove(resource);

				for (Object o : remaining) {
					result.add(compileFilter(o, columnName));
				}

			} else {

				for (List<Object> key : keys) {
					String part = "";
					for (int i = 0; i < key.size(); ++i) {
						if (!part.isEmpty())
							part += " &&";

						part += compileFilter(key.get(i), columnNames.get(i));
					}

					result.add(part);
				}
			}

			return result;
		}
		*/

		public static String askForQuad(Quad quad)
		{
			return
				"Ask { ?s ?p ?o . " +
				wrapFilter(compileFilter(quad.getSubject(), "?s"))+
				wrapFilter(compileFilter(quad.getPredicate(), "?p")) +
				wrapFilter(compileFilter(quad.getObject(), "?o")) +
				"}";
		}
	
		public static String wrapFilter(String expr)
		{
			return (expr != null && !expr.isEmpty()) ? "Filter(" + expr + ") ." : "";
		}
		
		
		public static String compileFilter(Node node, String columnName)
		{
			if (node.isURI()) {
				return columnName + " = <" + node.getURI() + ">";
			} else if (node.isLiteral()) {
				String result = "str(" + columnName + ") = '" + node.getLiteralLexicalForm()
						+ "'";

				if (!node.getLiteralLanguage().isEmpty()) {
					result += " && langMatches(lang(" + columnName + "), '"
							+ node.getLiteralLanguage() + "')";
				} else if (node.getLiteralDatatypeURI() != null) {
					result += " && datatype(" + columnName + ") = <"
							+ node.getLiteralDatatypeURI() + ">";
				}

				return result;
			} else {
				throw new RuntimeException(
						"Should never come here - maybe a blank node of evilness?");
			}
		}


}

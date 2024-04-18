package exp.org.aksw.sparqlify.core;

/**
 * Converts NQuads to a SPARQL result set.
 *
 *
 *
 * @author raven
 *
 */
//public class NQuadsToResultSet {
//    public static ResultSet convert(InputStream in) {
//        Set<Quad> quads = NQuadUtils.readNQuads(in);
//
//        ResultSet result = createResultSet(quads);
//        return result;
//    }
//
//
//    public static ResultSet createResultSet(Iterable<Quad> quads) {
//        ResultSet result = createResultSet(quads.iterator());
//
//        return result;
//    }
//
//    public static ResultSet createResultSet(Iterator<Quad> itQuads) {
//        Function<Quad, Binding> q2b = new FunctionQuadToBinding();
//
//
//        Iterator<Binding> itBinding = Iterators.transform(itQuads, q2b);
//        QueryIterator itQuery = QueryIterPlainWrapper.create(itBinding);
//        ResultSet result = new ResultSetStream(QuadUtils.quadVarNames, null, itQuery);
//
//        return result;
//    }
//}

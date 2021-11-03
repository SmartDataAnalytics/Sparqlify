package org.aksw.sparqlify.core.domain.input;


import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.views.IViewDef;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpTable;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.serializer.FmtTemplate;
import org.apache.jena.sparql.syntax.Template;


/**
 * A view definition is comprised of
 * - A name
 * - A template (a set of quad patterns)
 * - A mapping
 * - A set of references to variables declared in other view definitions.
 *
 *
 * Here are some notes on the references:
 *
 * Create View view_person {
 *     ?s a Person
 * }
 * With
 *     ?s = uri(name) [prefixes = {ns1, ns2}]
 * From
 *     people_table;
 *
 *
 * Create View employee_to_dept As Construct {
 *    ?p :worksIn ?d
 * }
 * References
 *     ppl: view_person On this.person_id = that.id
 *     depts: ... // Reference to the dept view on some join condition
 * With
 *    ?p  = ref(ppl, ?s) // Syntactic sugar for the following line:
 *    ?p_resolved = uri(ppl.name) [prefixes=...] // We now have a qualified column reference and the constraints carry over
 *    ?d  = ref(depts, ?s)
 * From
 *      p2d_table;
 *
 * Issue: (Q = Question, T = thought, R = resolution)
 * - Q: Nested refs: How to treat cases where a view V hase a ref(refName, ?var) which refers to another ref, e.g. ?x = ref(someRef.someNestedRef, ?x)
 *   T: Essentielly it should work somehow like this: for any view instance of V, we would keep track of a (list of) unresolved references; the nested ones would be
 *      simply added.
 *
 *
 * When creating view instances, we now have to keep track of which refereences have been resolved.
 * If a variable is not bound in a varbinding, then its references do not need to be resolved.
 * Conversely: Each bound view variable's references are added to the view instance's list of unresolved references.
 * Also, for each view instance variable we need to deal with the qualified column names.
 * Not sure where to deal with them best.
 *
 * Initially unresolved refs for the employee_to_dept view are ppl and depts.
 *
 *
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ViewDefinition
    implements IViewDef
{
    public static final ViewDefinition emptyViewDefinition = new ViewDefinition("emptyView", new QuadPattern(), null, MappingOpsImpl.createEmptyMapping(), null);


    private String name;

    // Note: all quads in the template must (should?) be composed of variables only
    // Constants and expressions are associated to a variable in the mapping
    // object.
    private QuadPattern template;
    private Mapping mapping;


    @Override
    public Set<Var> getVarsMentioned() {
        Set<Var> result = QuadPatternUtils.getVarsMentioned(template);
        return result;
    }


    /**
     * Mapping from reference names to other's views logical table on a given join condition
     * Usually used for mapping foreign-key relations.
     *
     * Note: ColumnReferences can be qualified with the name of the reference.
     */
    private Map<String, ViewReference> viewReferences;

    // References to variables declaced in other views. Useful for efficient
    // mapping table handling, as self join elimination can be applied.
    // Corresponds to R2RML's rr:join.
    //private Map<Var, VarReference> viewReferences = new HashMap<Var, VarReference>();


    // Restrictions on the variables (rather than on their defining expressions)
    // TODO Implement this again
    private RestrictionManagerImpl varRestrictions;

    // The source can point to an arbitrary object from
    // which this view definition was derived.
    // Mainly intended for pointing back to to the syntactic
    // construct this view definition was created from in order to be
    // able to provide better feedback to the user if problems are
    // encountered.
    //
    // (source can e.g. be an object representing a Sparqlify-ML or R2R-ML
    // definition).
    private Object source;

    public ViewDefinition(String name, QuadPattern template, Map<String, ViewReference> viewReferences, Mapping mapping, Object source)
    {
        this(name, template, viewReferences, mapping, null, source);
        /*
        this.name = name;
        this.template = template;
        this.mapping = mapping;
        this.viewReferences = viewReferences;
        this.source = source;
        */
    }

    public ViewDefinition(String name, QuadPattern template, Map<String, ViewReference> viewReferences, Mapping mapping, RestrictionManagerImpl varRestrictions, Object source)
    {
        this.name = name;
        this.template = template;
        this.mapping = mapping;
        this.viewReferences = viewReferences;
        this.varRestrictions = varRestrictions;
        this.source = source;
    }


    public String getName() {
        return name;
    }


    public QuadPattern getTemplate() {
        return template;
    }


    public Mapping getMapping() {
        return mapping;
    }


    public Map<String, ViewReference> getViewReferences() {
        return viewReferences;
    }


    public Object getSource() {
        return source;
    }


    public ViewDefinition copyRenameVars(Map<Var, Var> oldToNew) {
        QuadPattern newTemplate = NodeTransformLib.transform(NodeTransformRenameMap.create(oldToNew), this.template);

        VarDefinition varDef = mapping.getVarDefinition().copyRenameVars(oldToNew);

        Mapping m = new Mapping(varDef, mapping.getSqlOp());
        ViewDefinition result = new ViewDefinition(name, newTemplate, viewReferences, m, this);


        return result;
    }

    public RestrictionManagerImpl getVarRestrictions() {
        return varRestrictions;
    }

    public void write(IndentedWriter writer) {
        //Head
        writer.println("Create View " + name + " As");
        writer.incIndent();
        writer.println("Construct");
        //writer.incIndent();

        // SinkQuadBracedOutput sink = new SinkQuadBracedOutput(writer, null);
        // sink.open();

        //sink.send(template);


        //Map<Node, Set<Quad>> nodeToQuads = QuadUtils.partitionByGraph(template);

        Template t = new Template(new QuadAcc(template.getList()));

        FmtTemplate.format(writer, null, t);

        // Template
//        for(Quad quad : template) {
//            sink.send(quad);
//            //writer.println("" + quad);
//        }
//        sink.close();
        writer.println(" ");
        //writer.decIndent();
//		writer.println("}");

        // With
        if(!mapping.getVarDefinition().isEmpty()) {
            writer.println("With");

            writer.incIndent();
            for(Entry<Var, RestrictedExpr> entry : mapping.getVarDefinition().getMap().entries()) {
                Var var = entry.getKey();
                RestrictedExpr rexpr = entry.getValue();

                //writer.println(var + " = " + rexpr.getExpr() + "; Constraints " + rexpr.getRestrictions());
                writer.println(var + " = " + rexpr.getExpr());

                //if(rexpr.getRestrictions().)
                //+ " Constraints " + rexpr.getRestrictions());
            }
            writer.decIndent();
        }

        // From
        SqlOp op = mapping.getSqlOp();
        if(op != null) {
            writer.println("From");
            writer.incIndent();

            if(op instanceof SqlOpTable) {
                SqlOpTable tmp = (SqlOpTable)op;
                writer.println(tmp.getTableName());
            } else if (op instanceof SqlOpQuery) {
                SqlOpQuery tmp = (SqlOpQuery)op;
                String queryStr = tmp.getQueryString();

                // Cut off trailing ';'
                queryStr = queryStr.replaceAll("(;|\\s)+$", "");

                writer.println("[[" + queryStr + "]]");
            } else {
                writer.println("" + op);
            }
            writer.decIndent();
        }

    }


    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IndentedWriter writer = new IndentedWriter(out);

        write(writer);
        writer.flush();
        writer.close();

        String result = out.toString();
        return result;
    }

    @Override
    public VarDefinition getVarDefinition() {
        return mapping.getVarDefinition();
    }

    // HACK: We only use the name for equality testing - take care!!!
    @Override
    public int hashCode() {
        throw new RuntimeException("Don't compare views with equal - use their name instead");
        /*
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;*/
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Don't compare views with equal - use their name instead");
        /*
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ViewDefinition other = (ViewDefinition) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
        */
    }



}


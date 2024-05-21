package org.aksw.obda.jena.r2rml.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.aksw.obda.domain.api.Constraint;
import org.aksw.obda.domain.api.LogicalTable;
import org.aksw.obda.domain.impl.LogicalTableQueryString;
import org.aksw.obda.domain.impl.LogicalTableTableName;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.r2rml.common.vocab.R2rmlTerms;
import org.aksw.r2rml.jena.arq.impl.R2rmlTemplateLib;
import org.aksw.r2rml.jena.vocab.RR;
import org.aksw.rmltk.model.r2rml.GraphMap;
import org.aksw.rmltk.model.r2rml.ObjectMap;
import org.aksw.rmltk.model.r2rml.ObjectMapType;
import org.aksw.rmltk.model.r2rml.PredicateMap;
import org.aksw.rmltk.model.r2rml.PredicateObjectMap;
import org.aksw.rmltk.model.r2rml.SubjectMap;
import org.aksw.rmltk.model.r2rml.TermMap;
import org.aksw.rmltk.model.r2rml.TriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

public class R2rmlImporterOld {
//	public static SqlOp logicalTableToSqlOp(LogicalTable logicalTable) {
//		SqlOp result;
//
//		String str;
//		if((str = logicalTable.getTableName()) != null) {
//			result = new SqlOpTable(new SchemaImpl(), str);
//		} else if((str = logicalTable.getQueryString()) != null) {
//			result = new SqlOpQuery(new SchemaImpl(), str);
//		} else {
//			throw new RuntimeException("Unsupported logical table type: " + logicalTable);
//		}
//
//		return result;
//	}

    public static void validateR2rml(Model dataModel) {
        Model shaclModel = RDFDataMgr.loadModel("r2rml.core.shacl.ttl");

        // Perform the validation of everything, using the data model
        // also as the shapes model - you may have them separated
        Resource result = ValidationUtil.validateModel(dataModel, shaclModel, true);

        boolean conforms = result.getProperty(SH.conforms).getBoolean();

        if(!conforms) {
            // Print violations
            RDFDataMgr.write(System.err, result.getModel(), RDFFormat.TURTLE_PRETTY);
            throw new RuntimeException("Shacl validation failed; see report above");
        }
    }

    // It makes sense to have the validation method part of the object - instead of just using a static method
    public void validate(Model dataModel) {
        validateR2rml(dataModel);
    }

    public Collection<ViewDefinition> read(Model model) {
        List<TriplesMap> triplesMaps = model.listSubjectsWithProperty(RR.logicalTable).mapWith(r -> r.as(TriplesMap.class)).toList();

        SparqlStmtMgr.execSparql(model, "r2rml-inferences.sparql");

//		for(TriplesMap tm : triplesMaps) {
            // TODO Integrate validation with shacl, as this gives us free reports of violations
//		}

        List<ViewDefinition> result = triplesMaps.stream()
                .map(tm -> read(tm))
                .collect(Collectors.toList());

        return result;
    }


    public Node allocateVar(TermMap tm, BiMap<Node, Expr> nodeToExpr, Generator<Var> varGen) {
        Node result;
        //VarGeneratorBlacklist.
        BiMap<Expr, Node> exprToNode = nodeToExpr.inverse();

        Expr expr = termMapToExpr(tm);
        result = exprToNode.get(expr);

        if(result == null) {
            // If the expr is a constant, just use its node as the result; no need to track this in the map
            if(expr.isConstant()) {
                result = expr.getConstant().asNode();
            } else {
                // Allocate a new variable
                result = varGen.next();
                nodeToExpr.put(result, expr);
            }
        }

        return result;
    }

    // https://www.w3.org/TR/r2rml/#generated-triples
    // Note on graphs: the spec states: "If sgm and pogm are empty: rr:defaultGraph; otherwise: union of subject_graphs and predicate-object_graphs"
    public ViewDefinition read(TriplesMap tm) {
        // Construct triples by creating the cartesian product between g, s, p, and o term maps

        org.aksw.rmltk.model.r2rml.LogicalTable logicalTable = tm.getLogicalTable();
//		System.out.println("Processing " + tm.getURI());
//		System.out.println("  with table " + logicalTable);

        SubjectMap sm = tm.getSubjectMap();
        Set<GraphMap> sgms = sm.getGraphMaps();

        // Mapping of expressions to allocated variables
        BiMap<Node, Expr> nodeToExpr = HashBiMap.create();

        //ViewDefinition result = new ViewDefinition(name, template, viewReferences, mapping, source)
        List<Quad> template = new ArrayList<>();

        Generator<Var> varGen = new VarGeneratorImpl2("v", 1);

        for(PredicateObjectMap pom : tm.getPredicateObjectMaps()) {
            Set<GraphMap> pogms = pom.getGraphMaps();

            // egms = effective graph maps
            Set<GraphMap> egms = Sets.union(sgms, pogms);

            if(egms.isEmpty()) {
                egms = Collections.singleton(null);
            }

            Set<PredicateMap> pms = pom.getPredicateMaps();
            Set<ObjectMapType> omts = pom.getObjectMaps();

            for(GraphMap gm : egms) {
                for(PredicateMap pm : pms) {
                    for(ObjectMapType omt : omts) {
                        // TODO Add support for RefObjectMaps
                        ObjectMap om = omt.asTermMap();

                        Node g = gm == null ? Quad.defaultGraphNodeGenerated : allocateVar(gm, nodeToExpr, varGen);
                        Node s = allocateVar(sm, nodeToExpr, varGen);
                        Node p = allocateVar(pm, nodeToExpr, varGen);
                        Node o = allocateVar(om, nodeToExpr, varGen);

                        Quad quad = new Quad(g, s, p, o);
                        template.add(quad);
                    }
                }
            }
        }

        //ViewDefinition x;
        //x.getVarDefinition().
        //VarDefinition vd = new VarDefinition();
        Map<Var, Expr> varDefs = new LinkedHashMap<>();

        // Add the var definitions
        for(Entry<Node, Expr> entry : nodeToExpr.entrySet()) {
            Node n = entry.getKey();

            if(n.isVariable()) {
                Var v = (Var)n;
                Expr e = entry.getValue();

                varDefs.put(v, e);
            }
        }


//		List<Expr> varBindings = varDefs.entrySet().stream()
//				.map(e -> new E_Equals(new ExprVar(e.getKey()), e.getValue()))
//				.collect(Collectors.toList());

        //SqlOp relation = logicalTableToSqlOp(tm.getLogicalTable());

//		ViewTemplateDefinition vtd = new ViewTemplateDefinition();
//		vtd.setConstructTemplate(template);
//
//		vtd.setVarBindings(varBindings);

        Map<Var, Constraint> varConstraints = new LinkedHashMap<>();

        // Derive name
        String name = Optional.ofNullable(tm.getProperty(RDFS.label))
                .map(Statement::getString)
                .orElseGet(() -> tm.isURIResource() ? tm.getURI() : "" + tm);


        LogicalTable lt;

        if (logicalTable.qualifiesAsBaseTableOrView()) {
            lt = new LogicalTableTableName(logicalTable.asBaseTableOrView().getTableName());
        } else if (logicalTable.qualifiesAsR2rmlView()) {
            lt = new LogicalTableQueryString(logicalTable.asBaseTableOrView().getTableName());
        } else {
            throw new RuntimeException("Unknown logical table type: " + logicalTable);
        }

        ViewDefinition result = new ViewDefinition(name, template, varDefs, varConstraints, lt);

        return result;
    }


    public static Expr parseTemplate(String str) {
        List<Expr> exprs = parseTemplateCore(str);

        Expr result = exprs.size() == 1
                ? exprs.get(0)
                : new E_StrConcat(new ExprList(exprs))
                ;

        return result;
    }

    public static List<Expr> parseTemplateCore(String str) {
        List<Expr> result = new ArrayList<>();

        char cs[] = str.toCharArray();

        boolean isInVarName = false;
        boolean escaped = false;

        int i = 0;

        StringBuilder builder = new StringBuilder();

        boolean repeat = true;
        while(repeat) {
            char c = i < cs.length ? cs[i++] : (char)-1;
            if(escaped) {
                builder.append(c);
                escaped = false;
            } else {

                switch(c) {
                case '\\':
                    escaped = true;
                    continue;
                case '{':
                    if(isInVarName) {
                        throw new RuntimeException("Unescaped '{' in var name not allowed");
                    } else {
                        result.add(NodeValue.makeString(builder.toString()));
                        builder  = new StringBuilder();
                        isInVarName = true;
                    }
                    break;

                case '}':
                    if(isInVarName) {
                        String varName = builder.toString();
                        result.add(new ExprVar(varName));
                        builder  = new StringBuilder();
                        isInVarName = false;
                    } else {
                        throw new RuntimeException("Unescaped '}' not allowed");
                    }
                    break;

                case (char)-1:
                    if(isInVarName) {
                        throw new RuntimeException("End of string reached, but '}' missing");
                    } else {
                        if(builder.length() > 0) {
                            result.add(NodeValue.makeString(builder.toString()));
                        }
                    }
                    repeat = false;
                    break;

                default:
                    builder.append(c);
                }
            }
        }

        return result;
    }




    /**
     * Convert a term map to a corresponing SPARQL expression
     *
     * @param tm
     * @return
     */
    public static Expr termMapToExpr(TermMap tm) {
        Expr result;

        String template;

        RDFNode constant;

        // If a datatype has been specified then get its node
        // and validate that its an IRI
        Node datatypeNode = getIriNodeOrNull(tm.getDatatype());
        Node termTypeNode = getIriNodeOrNull(tm.getTermType());

        if((template = tm.getTemplate()) != null) {
            Expr arg = R2rmlTemplateLib.parse(template);

            Node effectiveTermType = termTypeNode == null ? RR.IRI.asNode() : termTypeNode;
            result = applyTermType(arg, effectiveTermType, XSD.xstring.asNode());

        } else if((constant = tm.getConstant()) != null) {
             result = NodeValue.makeNode(constant.asNode());
             //	result = constantToExpr(constant.asNode());

        } else {
            String colName;
            if((colName = tm.getColumn()) != null) {

                // TODO Hack: We remove quotes because qualified column names are not yet supported
                //   This goes wrong for an argument such as '"tableAlias"."columnName"'
                SqlCodec sqlCodec = SqlCodecUtils.createSqlCodecDoubleQuotes();
                colName = sqlCodec.forColumnName().decodeOrGetAsGiven(colName);

                ExprVar column = new ExprVar(colName);
                String langValue = Optional.ofNullable(tm.getLanguage()).map(String::trim).orElse(null);

                if (termTypeNode == null) {
                    termTypeNode = RR.Literal.asNode();
                }

                if(langValue == null) { // && !termTypeNode.equals(RR.Literal.asNode()) ) { //|| XSD.xstring.asNode().equals(datatypeNode)) {

                    result = applyTermType(column, termTypeNode, datatypeNode);

                } else {
                    String language = langValue == null ? "" : langValue;
                    // If there is no indication about the datatype just use the column directly
                    // This will later directly allow evaluation w.r.t. a column's natural RDF datatype

                    // FIXME Sparqlify relies on StrLang with empty language tag as a
                    // synonym for plain literal - but actually E_StrLang would raise an exception with empty lang
//					result = language.isEmpty()
//							? column // new E_StrDatatype(column, NodeValue.makeNode(XSD.xstring.asNode()))
//							: new E_StrLang(column, NodeValue.makeString(language));
                    result = new E_StrLang(column, NodeValue.makeString(language));
                }

            } else {
                throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
            }

        }

        return result;
    }


    public static Expr constantToExpr(Node node) {
        Expr result = node.isURI()
            ? new E_URI(NodeValue.makeString(node.getURI()))
            : node.isBlank()
                ? E_BNode.create(NodeValue.makeString(node.getBlankNodeLabel()))
                : node.isLiteral()
                    ? XSD.xstring.getURI().equals(node.getLiteralDatatypeURI())
                        ? new E_StrLang(NodeValue.makeString(node.getLiteralLexicalForm()), NodeValue.makeString(""))
                        : Strings.isNullOrEmpty(node.getLiteralLanguage())
                            ? new E_StrDatatype(NodeValue.makeString(node.getLiteralLexicalForm()), NodeValue.makeString(node.getLiteralDatatypeURI()))
                            : new E_StrLang(NodeValue.makeString(node.getLiteralLexicalForm()), NodeValue.makeString(node.getLiteralLanguage()))
                : null; // <- unknown node type

        Objects.requireNonNull(result, "Unknown node type encountered: " + node);
        return result;
    }

    public static Node getIriNodeOrNull(RDFNode rdfNode) {
        Node result = null;
        if (rdfNode != null) {
            result = rdfNode.asNode();
            if (!result.isURI()) {
                throw new RuntimeException(result + " is not an IRI");
            }
        }

        return result;
    }

    public static Expr applyTermType(Expr column, Node termType, Node knownDatatype) {
        String termTypeIri = termType.getURI();

        Expr result;
        result = termTypeIri.equals(R2rmlTerms.IRI)
            ? new E_URI(column)
            : termTypeIri.equals(R2rmlTerms.BlankNode)
                ? E_BNode.create(column)
                : termTypeIri.equals(R2rmlTerms.Literal)
                    ? (knownDatatype == null || XSD.xstring.asNode().equals(knownDatatype)
                        ? new E_StrLang(column, NodeValue.makeString("")) // FIXME StrLang with empty lang tag wouldn't evaluate
                        : new E_StrDatatype(column, NodeValue.makeNode(knownDatatype)))
                    : null;

        Objects.requireNonNull("Unknown term type: " + column + " - " + termType + " - " + knownDatatype);
        return result;
    }

    public static Expr applyDatatype(Expr column, Node expectedDatatype, Node knownDatatype) {
        Objects.requireNonNull(expectedDatatype, "Need an expected datatype");

        Expr result = expectedDatatype.equals(knownDatatype)
                ? column
                : expectedDatatype.equals(XSD.xstring.asNode())
                    ? new E_Str(column)
                    : new E_Function(knownDatatype.getURI(), new ExprList(column));

        return result;
    }



    public static Expr termMapToExprBugged(TermMap tm) {
        Expr result;

        String template;
        RDFNode constant;
        if((template = tm.getTemplate()) != null) {
            Expr arg = parseTemplate(template);

            // TODO Support rr:termType rr:BlankNode
            result = new E_URI(arg);

        } else if((constant = tm.getConstant()) != null) {
            result = NodeValue.makeNode(constant.asNode());
        } else {
            String colName;
            if((colName = tm.getColumn()) != null) {

                ExprVar column = new ExprVar(colName);
                Resource dtype = tm.getDatatype();
                if(dtype != null || XSD.xstring.equals(dtype)) {
                    Node dn = dtype.asNode();
                    if (!dn.isURI()) {
                        throw new RuntimeException("Datatype " + dtype + " is not an IRI");
                    }
                    String du = dn.getURI();

                    result = du.equals(NodeUtils.R2RML_IRI)
                                ? new E_URI(column)
                                : du.equals(NodeUtils.R2RML_BlankNode)
                                    ? new E_IsBlank(column)
                                    : new E_StrDatatype(column, NodeValue.makeNode(dn));

                } else {
                    String language = Optional.ofNullable(tm.getLanguage()).orElse("");
                    result = new E_StrLang(column, NodeValue.makeString(language));
                }
            } else {
                throw new RuntimeException("TermMap does neither define rr:template, rr:constant nor rr:column " + tm);
            }
        }

        return result;
    }
}

package org.aksw.sparqlify.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.XmlUtils;
import org.aksw.jena_sparql_api.views.RdfTerm;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Add;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs2.S_GreaterThan;
import org.aksw.sparqlify.algebra.sql.exprs2.S_GreaterThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LessThan;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LessThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Multiply;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Substract;
import org.aksw.sparqlify.backend.postgres.SqlLiteralMapperPostgres;
import org.aksw.sparqlify.config.xml.Mapping;
import org.aksw.sparqlify.config.xml.SimpleFunction;
import org.aksw.sparqlify.config.xml.SparqlifyConfig;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.cast.CoercionSystemImpl3;
import org.aksw.sparqlify.core.cast.MethodDeclarationParserSimple;
import org.aksw.sparqlify.core.cast.NodeValueToObjectDefault;
import org.aksw.sparqlify.core.cast.SqlDatatypeConstant;
import org.aksw.sparqlify.core.cast.SqlDatatypeDefault;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystemImpl;
import org.aksw.sparqlify.core.cast.SqlFunctionSerializerStringTemplate;
import org.aksw.sparqlify.core.cast.SqlLiteralMapper;
import org.aksw.sparqlify.core.cast.SqlTypeMapper;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.cast.SqlValueTransformerFloat;
import org.aksw.sparqlify.core.cast.SqlValueTransformerInteger;
import org.aksw.sparqlify.core.cast.TransformUtils;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.cast.TypeSystemImpl;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.SparqlFunctionImpl;
import org.aksw.sparqlify.core.jena.functions.BNode;
import org.aksw.sparqlify.core.jena.functions.PlainLiteral;
import org.aksw.sparqlify.core.jena.functions.RightPad;
import org.aksw.sparqlify.core.jena.functions.TypedLiteral;
import org.aksw.sparqlify.core.jena.functions.Uri;
import org.aksw.sparqlify.core.jena.functions.UrlDecode;
import org.aksw.sparqlify.core.jena.functions.UrlEncode;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformer;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerArithmetic;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerCast;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerConcat;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerFunction;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerHasRdfTermType;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerIsNumeric;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerLang;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerLangMatches;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerLogicalConjunction;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerOneOf;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerPassAsTypedLiteral;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerRdfTermComparator;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerRdfTermCtor;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerSparqlFunctionModel;
import org.aksw.sparqlify.core.rewrite.expr.transform.ExprTransformerStr;
import org.aksw.sparqlify.core.rewrite.expr.transform.RdfTermEliminatorImpl;
import org.aksw.sparqlify.core.rewrite.expr.transform.RdfTermEliminatorWriteable;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_Arithmetic;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_Compare;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_LogicalAnd;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_LogicalNot;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_LogicalOr;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_ParseDate;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_ParseInt;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_PassThrough;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_UrlDecode;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator_UrlEncode;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerCase;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerDefault;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerElse;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerOp1;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerOp1Prefix;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerOp2;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerPassThrough;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializerWhen;
import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer_Join;
import org.aksw.sparqlify.type_system.FunctionModel;
import org.aksw.sparqlify.type_system.FunctionModelAliased;
import org.aksw.sparqlify.type_system.FunctionModelMeta;
import org.aksw.sparqlify.type_system.MethodDeclaration;
import org.aksw.sparqlify.type_system.MethodSignature;
import org.aksw.sparqlify.type_system.TypeModel;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggGroupConcat;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.vocabulary.XSD;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SparqlifyCoreInit {
    public static void initSparqlifyFunctions() {
        FunctionRegistry.get().put(SparqlifyConstants.rdfTermLabel, RdfTerm.class);

        FunctionRegistry.get().put(SparqlifyConstants.blankNodeLabel, BNode.class);
        FunctionRegistry.get().put(SparqlifyConstants.uriLabel, Uri.class);
        FunctionRegistry.get().put(SparqlifyConstants.plainLiteralLabel, PlainLiteral.class);
        FunctionRegistry.get().put(SparqlifyConstants.typedLiteralLabel, TypedLiteral.class);

        FunctionRegistry.get().put("http://aksw.org/sparqlify/urlDecode", UrlDecode.class);

        // Jena does not yet seem to have this strangely named encode_for_uri function
        FunctionRegistry.get().put("http://aksw.org/sparqlify/urlEncode", UrlEncode.class);

        FunctionRegistry.get().put(SparqlifyConstants.rightPadLabel, RightPad.class);
    }

    public static SqlExprSerializerSystem createSerializerSystem(TypeSystem typeSystem, DatatypeToString typeSerializer, SqlEscaper sqlEscaper) {

        //DatatypeToString typeSerializer = new DatatypeToStringCast();//new DatatypeToStringPostgres();
        //DatatypeToString typeSerializer = new DatatypeToStringPostgres();

        SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperPostgres(
                typeSerializer, sqlEscaper);
        SqlExprSerializerSystem result = new SqlExprSerializerSystemImpl(
                typeSerializer, sqlEscaper, sqlLiteralMapper);

        FunctionModel<TypeToken> sqlModel = typeSystem.getSqlFunctionModel();

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("+");
            result.addSerializer(sqlModel.getIdsByName("numericPlus"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("-");
            result.addSerializer(sqlModel.getIdsByName("numericMinus"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("*");
            result.addSerializer(sqlModel.getIdsByName("numericMultiply"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("/");
            result.addSerializer(sqlModel.getIdsByName("numericDivide"), serializer);
        }



        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("=");
            result.addSerializer(sqlModel.getIdsByName("equal"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializer_Join(" || ");
            result.addSerializer("concat@str", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("COALESCE");
            result.addSerializer("coalesce", serializer);
        }


        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(">");
            result.addSerializer(sqlModel.getIdsByName("greaterThan"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(">=");
            result.addSerializer(sqlModel.getIdsByName("greaterThanOrEqual"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("<");
            result.addSerializer(sqlModel.getIdsByName("lessThan"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("<=");
            result.addSerializer(sqlModel.getIdsByName("lessThanOrEqual"), serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("AND");
            result.addSerializer(sqlModel.getIdsByName("logicalAnd"), serializer);
            result.addSerializer("logicalAnd", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("OR");
            result.addSerializer(sqlModel.getIdsByName("logicalOr"), serializer);
            result.addSerializer("logicalOr", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1("NOT");
            result.addSerializer(sqlModel.getIdsByName("logicalNot"), serializer);
            result.addSerializer("logicalNot", serializer);
        }


        // HACK: When isNotNull contraints are added based on the schema,
        // these expressions are not passed through the SQL rewriting process
        // Therefore we need this entry
        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix(" IS NOT NULL");
            result.addSerializer("isNotNull", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix(" IS NOT NULL");
            result.addSerializer(sqlModel.getIdsByName("isNotNull"), serializer);
        }


        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::float8");
            result.addSerializer("double@str", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
            result.addSerializer("str@float", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
            result.addSerializer("str@double", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerPassThrough();
            result.addSerializer("str@str", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
            result.addSerializer("str@int", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
            result.addSerializer("str@char", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
            result.addSerializer("str@date", serializer);
        }


        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::float");
            result.addSerializer("float toFloat(int)", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::float");
            result.addSerializer("double toDouble(int)", serializer);
        }


// Geo stuff have been converted to extension functions
//		{
//			//SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("ST_GeomFromPoint");
//			result.addSerializer("geometry ST_GeomFromPoint(float, float)", new SqlFunctionSerializer() {
//				@Override
//				public String serialize(List<String> args) {
//					return "ST_SetSRID(ST_Point(" + args.get(0) + ", " + args.get(1) + "), 4326)";
//				}
//			});
//		}
//
//		{
//			MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.Geometry, "ST_GeomFromText", false, TypeToken.String);
//			SqlFunctionSerializer serializer = new SqlFunctionSerializer() {
//				@Override
//				public String serialize(List<String> args) {
//					return "ST_SetSRID(ST_GeomFromText(" + args.get(0) + "), 4326)";
//				}
//			};
//
//			result.addSerializer(decl.toString(), serializer);
//		}
//


        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.Boolean, "ST_DWithin", false, TypeToken.Geometry, TypeToken.Geometry, TypeToken.Float);

            SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("ST_DWithin");
            result.addSerializer(decl.toString(), serializer);
        }

        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.Boolean, "ST_Intersects", false, TypeToken.Geometry, TypeToken.Geometry);

            SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("ST_Intersects");
            result.addSerializer(decl.toString(), serializer);
        }


        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.Long, "Count", false);
            //SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("Count");
            SqlFunctionSerializer serializer = new SqlFunctionSerializer() {
                @Override
                public String serialize(List<String> args) {
                    return "Count(*)";
                }
            };
            result.addSerializer(decl.toString(), serializer);
        }


        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.String, SparqlifyConstants.urlEncode, false, TypeToken.String);

            //SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("ST_");
            result.addSerializer(decl.toString(), new SqlFunctionSerializerPassThrough());
        }


        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.Int, "Sum", false, TypeToken.Int);
            SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("Sum");
            result.addSerializer(decl.toString(), serializer);
        }

        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.String, "GroupConcat", false, TypeToken.String, TypeToken.String);
            SqlFunctionSerializer serializer = new SqlFunctionSerializer() {
                @Override
                public String serialize(List<String> args) {
                    return "string_agg(" + args.get(0) + ", " + args.get(1) + ")";
                }
            };
            result.addSerializer(decl.toString(), serializer);
        }

        {
            MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.Double, "Sum", false, TypeToken.Double);
            SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("Sum");
            result.addSerializer(decl.toString(), serializer);
        }



        {
            //SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("~*");
            result.addSerializer(sqlModel.getIdsByName("regex"), new SqlFunctionSerializer() {
                @Override
                public String serialize(List<String> args) {
                    return "(" + args.get(0) + " ~ " + args.get(1) + ")";
                }
            });
            //result.addSerializer(sqlModel.getIdsByName("regex"), serializer);
        }



        // Cast is built in
//		{
//			SqlFunctionSerializer serializer = new SqlFunctionSerializerCast();
//			result.addSerializer("cast", serializer);
//		}

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerWhen();
            //result.addSerializer(sqlModel.getIdsByName("when"), serializer);
            result.addSerializer("when", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerCase();
            //result.addSerializer(sqlModel.getIdsByName("case"), serializer);
            result.addSerializer("case", serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerElse();
            //result.addSerializer(sqlModel.getIdsByName("else"), serializer);
            result.addSerializer("else", serializer);
        }


        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializerPassThrough();
            result.addSerializer(SparqlifyConstants.urlEncode, serializer);
        }

        {
            SqlFunctionSerializer serializer = new SqlFunctionSerializer() {
                @Override
                public String serialize(List<String> args) {
                    return "COUNT(*)";
                }
            };

            result.addSerializer("org.aksw.sparqlify.algebra.sql.exprs2.S_AggCount", serializer);
        }



        return result;
    }

    public static RdfTermEliminatorImpl createDefaultTransformer(TypeSystem typeSystem) {

            RdfTermEliminatorImpl exprTransformer = new RdfTermEliminatorImpl();

            Map<String, ExprTransformer> transMap = exprTransformer.getTransformerMap();

            transMap.put("concat", new ExprTransformerConcat());
            transMap.put("lang", new ExprTransformerLang());

            transMap.put("langMatches", new ExprTransformerLangMatches());


            transMap.put("=", new ExprTransformerRdfTermComparator(XSD.xboolean));
            transMap.put(">", new ExprTransformerRdfTermComparator(XSD.xboolean));
            transMap.put(">=", new ExprTransformerRdfTermComparator(XSD.xboolean));
            transMap.put("<", new ExprTransformerRdfTermComparator(XSD.xboolean));
            transMap.put("<=", new ExprTransformerRdfTermComparator(XSD.xboolean));
            //transMap.put("+", new ExprTransformerArithmetic(XSD.decimal));

            FunctionModel<String> sparqlModel = typeSystem.getSparqlFunctionModel();

            transMap.put("+", new ExprTransformerSparqlFunctionModel(sparqlModel));

            transMap.put("-", new ExprTransformerArithmetic(XSD.decimal));
            transMap.put("*", new ExprTransformerArithmetic(XSD.decimal));
            transMap.put("/", new ExprTransformerArithmetic(XSD.decimal));

            transMap.put("bound", new ExprTransformerPassAsTypedLiteral(XSD.xboolean));
            transMap.put("cast", new ExprTransformerCast());
            transMap.put("str", new ExprTransformerStr());
            transMap.put("regex", new ExprTransformerFunction(XSD.xboolean));

            transMap.put(SparqlifyConstants.blankNodeLabel, new ExprTransformerRdfTermCtor());
            transMap.put(SparqlifyConstants.uriLabel, new ExprTransformerRdfTermCtor());
            transMap.put(SparqlifyConstants.plainLiteralLabel, new ExprTransformerRdfTermCtor());
            transMap.put(SparqlifyConstants.typedLiteralLabel, new ExprTransformerRdfTermCtor());
            transMap.put(SparqlifyConstants.rdfTermLabel, new ExprTransformerRdfTermCtor());

            transMap.put("&&", new ExprTransformerLogicalConjunction());
            transMap.put("||", new ExprTransformerLogicalConjunction());
            transMap.put("!", new ExprTransformerPassAsTypedLiteral(XSD.xboolean));

            transMap.put("in", new ExprTransformerOneOf());
            //transMap.put("||", new ExprTransformerLogicalAn());

            transMap.put(XSD.xdouble.getURI(), new ExprTransformerCast());


            transMap.put(SparqlifyConstants.urlEncode, new ExprTransformerFunction(XSD.xstring));
            transMap.put(SparqlifyConstants.urlDecode, new ExprTransformerFunction(XSD.xstring));

            // Geometry
            String bif = "http://www.openlinksw.com/schemas/bif#";

            Resource virtGeometry = ResourceFactory.createResource("http://www.openlinksw.com/schemas/virtrdf#Geometry");


    //		transMap.put(bif + "st_intersects", new ExprTransformerFunction(XSD.xboolean));
    //		transMap.put(bif + "st_geomFromText", new ExprTransformerFunction(virtGeometry));
    //		transMap.put(bif + "st_point", new ExprTransformerFunction(ResourceFactory.createResource("http://www.opengis.net/ont/geosparql#wktLiteral"))); //));



            //typeSystem.get
            TypeModel<String> sparqlTypeModel = typeSystem.getSparqlTypeModel();
            transMap.put("isNumeric", new ExprTransformerIsNumeric(sparqlTypeModel));

            transMap.put("isURI", new ExprTransformerHasRdfTermType(1));
            transMap.put("isBlank", new ExprTransformerHasRdfTermType(0));

            //transMap.put("isDecimal", new ET_IsDecimal(sparqlTypeModel));


            // TODO: The return type of this function depends on which signature is used
            // So i more sophicsticated transformer is needed
            transMap.put(AggCount.class.getSimpleName(), new ExprTransformerFunction(XSD.xlong));
            transMap.put(AggSum.class.getSimpleName(), new ExprTransformerFunction(XSD.xdouble));
            transMap.put(AggGroupConcat.class.getSimpleName(), new ExprTransformerSparqlFunctionModel(sparqlModel));

            return exprTransformer;
        }

    public static void registerSqlOperatorBatchNumeric(FunctionModel<TypeToken> sqlModel, String name) {
        sqlModel.registerFunction(name + "@boolean", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Boolean, TypeToken.Boolean));
        sqlModel.registerFunction(name + "@int", name, MethodSignature.create(false, TypeToken.Int, TypeToken.Int, TypeToken.Int));
        sqlModel.registerFunction(name + "@float", name, MethodSignature.create(false, TypeToken.Float, TypeToken.Float, TypeToken.Float));
        sqlModel.registerFunction(name + "@double", name, MethodSignature.create(false, TypeToken.Double, TypeToken.Double, TypeToken.Double));
        //sqlModel.registerFunction(name + "@string", name, MethodSignature.create(false, TypeToken.String, TypeToken.String, TypeToken.String));
        //sqlModel.registerFunction(name + "@dateTime", name, MethodSignature.create(false, TypeToken.Date, TypeToken.Date, TypeToken.Date));
    }

    public static void registerSqlOperatorBatchCompare(FunctionModel<TypeToken> sqlModel, String name) {
        sqlModel.registerFunction(name + "@boolean", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Boolean, TypeToken.Boolean));
        sqlModel.registerFunction(name + "@int", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Int, TypeToken.Int));
        sqlModel.registerFunction(name + "@float", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Float, TypeToken.Float));
        sqlModel.registerFunction(name + "@double", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Double, TypeToken.Double));
        sqlModel.registerFunction(name + "@string", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.String, TypeToken.String));
        sqlModel.registerFunction(name + "@dateTimeStamp", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.TimeStamp, TypeToken.TimeStamp));
        //sqlModel.registerFunction(name + "@dateTime", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Date, TypeToken.TimeStamp));
        sqlModel.registerFunction(name + "@date", name, MethodSignature.create(false, TypeToken.Boolean, TypeToken.Date, TypeToken.Date));
    }

    public static TypeSystem createDefaultDatatypeSystem() {

            // String basePath = "src/main/resources";
            try {
                /*
                Map<String, String> typeNameToClass = MapReader.read(SparqlifyCoreInit.class.getResourceAsStream("/type-class.tsv"));

                Map<String, String> typeNameToUri = MapReader.read(SparqlifyCoreInit.class.getResourceAsStream("/type-uri.tsv"));

                Map<String, String> typeHierarchy = MapReader.read(SparqlifyCoreInit.class.getResourceAsStream("/type-hierarchy.default.tsv"));

                Map<String, String> physicalTypeMap = MapReader.read(SparqlifyCoreInit.class.getResourceAsStream("/type-map.h2.tsv"));

                Map<String, String> rdfTypeHierarchyRaw = MapReader.read(SparqlifyCoreInit.class.getResourceAsStream("/rdf-type-hierarchy.tsv"));
                */

                Map<String, String> typeNameToClass = MapReader
                        .readFromResource("/type-class.tsv");
                Map<String, String> typeNameToUri = MapReader
                        .readFromResource("/type-uri.tsv");

                Map<String, String> typeHierarchy = MapReader
                        .readFromResource("/type-hierarchy.default.tsv");

                Map<String, String> physicalTypeMap = MapReader
                        .readFromResource("/type-map.h2.tsv");

                Map<String, String> rdfTypeHierarchyRaw =
                        MapReader.readFromResource("/rdf-type-hierarchy.tsv");
                IBiSetMultimap<String, String> rdfTypeHierarchy = TypeSystemImpl.toBidiMap(rdfTypeHierarchyRaw);



                // TODO HACK Do not add types programmatically
                physicalTypeMap.put("INTEGER", "int");
                physicalTypeMap.put("FLOAT", "float");
                physicalTypeMap.put("DOUBLE", "double");
                physicalTypeMap.put("DATE", "date");

                //typeHierarchy.putAll(physicalTypeMap);


    //			Map<String, String> typeNameToClass = MapReader
    //					.readFromResource("/type-class.tsv");





                TypeSystemImpl result = TypeSystemImpl.create(typeHierarchy, physicalTypeMap);

                result.getSparqlTypeHierarchy().putAll(rdfTypeHierarchy);

                result.getNormSqlTypeToUri().putAll(typeNameToUri);


                SparqlifyCoreInit.initSparqlModel(result);

                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    /**
         * Create the SPARQL and SQL models
         *
         *
         *
         */
        public static void initSparqlModel(TypeSystem typeSystem) {


            // NodeValue xxx = NodeValue.makeInteger(1);
            // RDFDatatype yyy = xxx.asNode().getLiteral().getDatatype();
            // System.out.println(yyy);

            S_Constant x;

            // SIGH, Jena does not support assigning custom URIs to its default
            // datatypes.
            // Register type token datatypes
            TypeMapper tm = TypeMapper.getInstance();
            String xxx = "http://mytype.org/foo/bar";

    //		RDFDatatype inner = new XSDBaseNumericType("int", BigInteger.class);
    //		RDFDatatype i = new RDFDatatypeCustomUri("int", inner);


            SqlTypeMapper stm = typeSystem.getSqlTypeMapper();
            stm.register(XSD.xstring.getURI(), new SqlDatatypeDefault(TypeToken.String, new NodeValueToObjectDefault()));
            stm.register(XSD.xboolean.getURI(), new SqlDatatypeDefault(TypeToken.Boolean, new NodeValueToObjectDefault()));

            stm.register(XSD.integer.getURI(),  new SqlDatatypeDefault(TypeToken.Int, new NodeValueToObjectDefault()));
            stm.register(XSD.xfloat.getURI(),  new SqlDatatypeDefault(TypeToken.Float, new NodeValueToObjectDefault()));
            stm.register(XSD.xdouble.getURI(),  new SqlDatatypeDefault(TypeToken.Double, new NodeValueToObjectDefault()));
            stm.register(XSD.decimal.getURI(),  new SqlDatatypeDefault(TypeToken.Int, new NodeValueToObjectDefault()));


            stm.register(XSD.date.getURI(),  new SqlDatatypeDefault(TypeToken.Date, new NodeValueToObjectDefault()));
            stm.register(XSD.dateTime.getURI(),  new SqlDatatypeDefault(TypeToken.TimeStamp, new NodeValueToObjectDefault()));
            stm.register(XSD.dateTimeStamp.getURI(),  new SqlDatatypeDefault(TypeToken.TimeStamp, new NodeValueToObjectDefault()));


            stm.register(SparqlifyConstants.nvTypeError.asNode().getLiteralDatatypeURI(), new SqlDatatypeConstant(SqlValue.TYPE_ERROR));





            // RDFDatatype i = new XSDBaseNumericType(TypeToken.Int.toString(),
            // BigInteger.class);

    //		tm.registerDatatype(i);

            CoercionSystemImpl3 cs = (CoercionSystemImpl3) typeSystem
                    .getCoercionSystem();


            cs.registerCoercion(TypeToken.alloc(XSD.integer.toString()),
                    TypeToken.Int, new SqlValueTransformerInteger());

            cs.registerCoercion(TypeToken.String, TypeToken.alloc("int8"),
                    new SqlValueTransformerInteger());

            cs.registerCoercion(TypeToken.String, TypeToken.alloc("int4"),
                    new SqlValueTransformerInteger());

            cs.registerCoercion(TypeToken.String, TypeToken.alloc("int"),
                    new SqlValueTransformerInteger());


            cs.registerCoercion(TypeToken.Int, TypeToken.Float,
                    new SqlValueTransformerFloat());


            // TODO Finally clean up the TypeSystem
            // The coercion system is still a hack...
            cs.registerCoercion(TypeToken.String, TypeToken.alloc("INTEGER"),
                    new SqlValueTransformerInteger());


    //		cs.registerCoercion(TypeToken.Date, TypeToken.,
    //				new SqlValueTransformer());

            // FunctionRegistry functionRegistry = new FunctionRegistry();

//            ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();

            // Eliminates rdf terms from Expr (this is datatype independent)
//            ExprEvaluator exprEvaluator = SqlTranslationUtils
//                    .createDefaultEvaluator();

            // Computes types for Expr, thereby yielding SqlExpr
//            TypedExprTransformer typedExprTransformer = new TypedExprTransformerImpl(
//                    typeSystem);

            // Obtain DBMS specific string representation for SqlExpr

//            DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres();

//            SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(
//                    typeSerializer);
//            SqlExprSerializerSystem serializerSystem = new SqlExprSerializerSystemImpl(
//                    typeSerializer, sqlEscaper, sqlLiteralMapper);

            // ExprEvaluator exprEvaluator = new
            // ExprEvaluatorPartial(functionRegistry, typedExprTransformer)

            // {
            // Method m = DefaultCoercions.class.getMethod("toDouble",
            // Integer.class);
            // XMethod x = XMethodImpl.createFromMethod("toDouble", ds, null, m);
            // ds.registerCoercion(x);
            // }
            //
            // /*
            // * Methods that can only be rewritten
            // */
            //
            //
            // // For most of the following functions, we can rely on Jena for their
            // // evaluation
            // ExprEvaluator evaluator = new ExprEvaluatorJena();
            //
            //

            FunctionModel<TypeToken> sqlModel = typeSystem.getSqlFunctionModel();


            Multimap<String, String> sparqlSqlDecls = typeSystem.getSparqlSqlDecls();
            Map<String, SqlExprEvaluator> sqlImpls = typeSystem.getSqlImpls();


            registerSqlOperatorBatchCompare(sqlModel, "lessThan");
            registerSqlOperatorBatchCompare(sqlModel, "lessThanOrEqual");
            registerSqlOperatorBatchCompare(sqlModel, "equal");
            registerSqlOperatorBatchCompare(sqlModel, "greaterThan");
            registerSqlOperatorBatchCompare(sqlModel, "greaterThanOrEqual");

            registerSqlOperatorBatchNumeric(sqlModel, "numericPlus");
            registerSqlOperatorBatchNumeric(sqlModel, "numericMinus");
            registerSqlOperatorBatchNumeric(sqlModel, "numericMultiply");
            registerSqlOperatorBatchNumeric(sqlModel, "numericDivide");

            //sqlModel.registerFunction("str@char", "str", MethodSignature.create(false, TypeToken., TypeToken.String));
            sqlModel.registerFunction("str@str", "str", MethodSignature.create(false, TypeToken.String, TypeToken.String));
            sqlModel.registerFunction("str@double", "str", MethodSignature.create(false, TypeToken.String, TypeToken.Double));
            sqlModel.registerFunction("str@float", "str", MethodSignature.create(false, TypeToken.String, TypeToken.Float));
            sqlModel.registerFunction("str@int", "str", MethodSignature.create(false, TypeToken.String, TypeToken.Int));

            sqlModel.registerFunction("str@date", "str", MethodSignature.create(false, TypeToken.String, TypeToken.Date));
            sqlModel.registerFunction("str@dateTime", "str", MethodSignature.create(false, TypeToken.String, TypeToken.DateTime));
            sqlModel.registerFunction("str@dateTimeStamp", "str", MethodSignature.create(false, TypeToken.String, TypeToken.TimeStamp));
            //sqlModel.registerFunction("str@time", "str", MethodSignature.create(false, TypeToken.String, TypeToken.Date));

            sqlModel.registerFunction("double@str", "double", MethodSignature.create(false, TypeToken.Double, TypeToken.String));

            sqlModel.registerFunction("isNotNull@object", "isNotNull", MethodSignature.create(false, TypeToken.Boolean, TypeToken.Object));


            sparqlSqlDecls.putAll("<", sqlModel.getIdsByName("lessThan"));
            sparqlSqlDecls.putAll("<=", sqlModel.getIdsByName("lessThanOrEqual"));
            sparqlSqlDecls.putAll("=", sqlModel.getIdsByName("equal"));
            sparqlSqlDecls.putAll(">", sqlModel.getIdsByName("greaterThan"));
            sparqlSqlDecls.putAll(">=", sqlModel.getIdsByName("greaterThanOrEqual"));

            sparqlSqlDecls.putAll("+", sqlModel.getIdsByName("numericPlus"));
            sparqlSqlDecls.putAll("-", sqlModel.getIdsByName("numericMinus"));
            sparqlSqlDecls.putAll("/", sqlModel.getIdsByName("numericMultiply"));
            sparqlSqlDecls.putAll("*", sqlModel.getIdsByName("numericDivide"));

            sparqlSqlDecls.put("str", "str@str");
            sparqlSqlDecls.put("str", "str@double");
            sparqlSqlDecls.put("str", "str@float");
            sparqlSqlDecls.put("str", "str@int");
            sparqlSqlDecls.put(XSD.xdouble.getURI(), "double@str");
            sparqlSqlDecls.put("str", "str@date");
            sparqlSqlDecls.put("str", "str@dateTime");
            sparqlSqlDecls.put("str", "str@dateTimeStamp");

            sparqlSqlDecls.put("bound", "isNotNull@object");

            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("lessThan"), new SqlExprEvaluator_Compare(typeSystem, S_LessThan::new));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("lessThanOrEqual"), new SqlExprEvaluator_Compare(typeSystem, S_LessThanOrEqual::new));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("equal"), new SqlExprEvaluator_Compare(typeSystem, S_Equals::new));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("greaterThan"), new SqlExprEvaluator_Compare(typeSystem, S_GreaterThan::new));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("greaterThanOrEqual"), new SqlExprEvaluator_Compare(typeSystem, S_GreaterThanOrEqual::new));

            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("numericPlus"), new SqlExprEvaluator_Arithmetic());

            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("+"), new SqlExprEvaluator_Compare(typeSystem, S_Add::new));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("-"), new SqlExprEvaluator_Compare(typeSystem, S_Substract::new));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("*"), new SqlExprEvaluator_Compare(typeSystem, S_Multiply::new));
    //		putForAll(sqlImpls, sqlModel.getIdsByName("/"), new SqlExprEvaluator_Compare(typeSystem, SqlExprFactoryUtils.factoryNumericDivide));



            sqlModel.registerFunction("logicalAnd@boolean", "logicalAnd", MethodSignature.create(false, TypeToken.Boolean, TypeToken.Boolean, TypeToken.Boolean));
            sparqlSqlDecls.putAll("&&", sqlModel.getIdsByName("logicalAnd"));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("logicalAnd"), new SqlExprEvaluator_LogicalAnd());

            sqlModel.registerFunction("logicalOr@boolean", "logicalOr", MethodSignature.create(false, TypeToken.Boolean, TypeToken.Boolean, TypeToken.Boolean));
            sqlModel.registerFunction("logicalOr@booleanError", "logicalOr", MethodSignature.create(false, TypeToken.Boolean, TypeToken.Boolean, TypeToken.TypeError));
            sqlModel.registerFunction("logicalOr@errorBoolean", "logicalOr", MethodSignature.create(false, TypeToken.Boolean, TypeToken.TypeError, TypeToken.Boolean));

            sparqlSqlDecls.putAll("||", sqlModel.getIdsByName("logicalOr"));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("logicalOr"), new SqlExprEvaluator_LogicalOr());



            sqlModel.registerFunction("logicalNot@boolean", "logicalNot", MethodSignature.create(false, TypeToken.Boolean, TypeToken.Boolean));
            sparqlSqlDecls.putAll("!", sqlModel.getIdsByName("logicalNot"));
            MapUtils.putForAll(sqlImpls, sqlModel.getIdsByName("logicalNot"), new SqlExprEvaluator_LogicalNot());


            sqlModel.registerFunction("concat@str", "concat", MethodSignature.create(true, TypeToken.String, TypeToken.String));
            sparqlSqlDecls.put("concat", "concat@str");


            // register a parse int function
            sqlModel.registerFunction("parseInt@str", "parseInt", MethodSignature.create(false, TypeToken.Int, TypeToken.String));
            sqlModel.registerFunction("parseDate@str", "parseDate", MethodSignature.create(false, TypeToken.Date, TypeToken.String));
            sqlModel.registerFunction("parseDateTime@str", "parseDateTime", MethodSignature.create(false, TypeToken.DateTime, TypeToken.String));


            //sparqlSqlDecls.put("concat", "concat@object");

            FunctionModelMeta sqlMetaModel = typeSystem.getSqlFunctionMetaModel();

            sqlMetaModel.getInverses().put("str@int", "parseInt@str");
            sqlMetaModel.getInverses().put("str@date", "parseDate@str");
            sqlMetaModel.getInverses().put("str@dateTime", "parseDateTime@str");


            sqlMetaModel.getComparators().addAll(sqlModel.getIdsByName("lessThan"));
            sqlMetaModel.getComparators().addAll(sqlModel.getIdsByName("lessThanOrEqual"));
            sqlMetaModel.getComparators().addAll(sqlModel.getIdsByName("equal"));
            sqlMetaModel.getComparators().addAll(sqlModel.getIdsByName("greaterThanOrEqual"));
            sqlMetaModel.getComparators().addAll(sqlModel.getIdsByName("greaterThan"));

            sqlMetaModel.getLogicalAnds().addAll(sqlModel.getIdsByName("logicalAnd"));
            sqlMetaModel.getLogicalOrs().addAll(sqlModel.getIdsByName("logicalOr"));
            sqlMetaModel.getLogicalNots().addAll(sqlModel.getIdsByName("logicalNot"));

            //sqlModel.getInverses().put();
            sqlImpls.put("parseInt@str", new SqlExprEvaluator_ParseInt());
            sqlImpls.put("parseDate@str", SqlExprEvaluator_ParseDate.DATE);
            sqlImpls.put("parseDateTime@str", SqlExprEvaluator_ParseDate.DATETIMESTAMP);

            //sqlMetaModel.getInverses().put(key, value)



            // Geographic
            TypeToken typeGeometry = TypeToken.alloc("geometry");
            String bif = "http://www.openlinksw.com/schemas/bif#";


    //		sqlModel.registerFunction("geometry ST_GeomFromPoint(float, float)", "ST_GeomFromPoint", MethodSignature.create(false, typeGeometry, TypeToken.Float, TypeToken.Float));
    //		sparqlSqlDecls.putAll(bif + "st_point", sqlModel.getIdsByName("ST_GeomFromPoint"));
    //		sqlImpls.put("geometry ST_GeomFromPoint(float, float)", new SqlExprEvaluator_PassThrough(typeGeometry, "ST_GeomFromPoint"));
    //


            //String stIntersectsName = bif +7/
    //		MethodDeclaration<TypeToken> stIntersectsDecl1 = MethodDeclaration.create(TypeToken.Boolean, "ST_Intersects", false, typeGeometry, typeGeometry);
    //		MethodDeclaration<TypeToken> stIntersectsDecl2 = MethodDeclaration.create(TypeToken.Boolean, "ST_DWithin", false, typeGeometry, typeGeometry, TypeToken.Float);
    //		sqlModel.registerFunction(stIntersectsDecl1);
    //		sqlModel.registerFunction(stIntersectsDecl2);
    //
    //		sparqlSqlDecls.put(bif + "st_intersects", stIntersectsDecl1.toString());
    //		sparqlSqlDecls.put(bif + "st_intersects", stIntersectsDecl2.toString());
            //sqlImpls.put(stIntersectsDecl1.toString(), new SqlExprEvaluator_PassThrough(TypeToken.Boolean, "ST_Intersects"));
            //sqlImpls.put(stIntersectsDecl2.toString(), new SqlExprEvaluator_PassThrough(TypeToken.Boolean, "ST_Intersects"));


            //sqlImpls.put(urlEncodeDecl.toString(), new SqlExprEvaluator_UrlEncode());


            //sqlModel.registerFunction("boolean ST_Intersects(geometry, geometry, float)", "ST_Intersects", MethodSignature.create(false, TypeToken.Boolean, typeGeometry, typeGeometry, TypeToken.Float));
            //sparqlSqlDecls.putAll(bif + "st_intersects", sqlModel.getIdsByName("ST_Intersects"));
            //sqlImpls.put("boolean ST_Intersects(geometry, geometry, float)", new SqlExprEvaluator_PassThrough(TypeToken.Boolean, "ST_Intersects"));


            // TODO: This coercion seems to get applied too often - why?
            sqlModel.registerCoercion("float toFloat(int)", "toFloat", MethodSignature.create(false, TypeToken.Float, TypeToken.Int));
            sqlModel.registerCoercion("double toDouble(int)", "toDouble", MethodSignature.create(false, TypeToken.Double, TypeToken.Int));
            //sqlImpls.put("float toFloat(int)", new SqlE);


            // Regex: Which function to use depends on the flags given the SPARQL function
            // Postgres:
            //   ~ -> case sensitive
            //   ~* -> case insensitive
            // However, we could also create a virtual SQL function, and process the regex flags in the SQL impl, or even the serializer
            // Put differently: Where is the best place to handle this?
            // - The SQL model should actually model what's there, so a fake SQL model doesn't really make sense.
            sqlModel.registerFunction("boolean regex(string, string)", "regex", MethodSignature.create(false, TypeToken.Boolean, TypeToken.String, TypeToken.String));
            sqlModel.registerFunction("boolean regex(string, string, string)", "regex", MethodSignature.create(false, TypeToken.Boolean, TypeToken.String, TypeToken.String, TypeToken.String));
            sparqlSqlDecls.putAll("regex", sqlModel.getIdsByName("regex"));
            sqlImpls.put("boolean regex(string, string)", new SqlExprEvaluator_PassThrough(TypeToken.Boolean, "regex"));
            sqlImpls.put("boolean regex(string, string, string)", new SqlExprEvaluator_PassThrough(TypeToken.Boolean, "regex"));



    //		MethodDeclaration<TypeToken> stGeomFromTextDecl = MethodDeclaration.create(typeGeometry, "ST_GeomFromText", false, TypeToken.String);
    //		sqlModel.registerFunction(stGeomFromTextDecl);
    //		sparqlSqlDecls.put(bif + "st_geomFromText", stGeomFromTextDecl.toString());
            //sqlImpls.put(stGeomFromTextDecl.toString(), new SqlExprEvaluator_PassThrough(TypeToken.Boolean, "ST_GeomFromText"));

            MethodDeclaration<TypeToken> urlEncodeDecl = MethodDeclaration.create(TypeToken.String, SparqlifyConstants.urlEncode, false, TypeToken.String);
            sqlModel.registerFunction(urlEncodeDecl);
            sparqlSqlDecls.put(SparqlifyConstants.urlEncode, urlEncodeDecl.toString());
            sqlImpls.put(urlEncodeDecl.toString(), new SqlExprEvaluator_UrlEncode());


            MethodDeclaration<TypeToken> urlDecodeDecl = MethodDeclaration.create(TypeToken.String, SparqlifyConstants.urlDecode, false, TypeToken.String);
            sqlModel.registerFunction(urlDecodeDecl);
            sparqlSqlDecls.put(SparqlifyConstants.urlDecode, urlDecodeDecl.toString());
            sqlImpls.put(urlDecodeDecl.toString(), new SqlExprEvaluator_UrlDecode());


            sqlMetaModel.getInverses().put(urlEncodeDecl.toString(), urlDecodeDecl.toString());
            sqlMetaModel.getInverses().put(urlDecodeDecl.toString(), urlEncodeDecl.toString());


            // Maps a sparql symbol to a set of implementing method declarations
            // This mapping allows the following:
            // the + symbol maps to { int op:plus_int (int, int), double op:plus_double (double, double) ,... }
            // Upon transformation, '+' is replaced with the name of a matching declaration
            // i.e. the name may change
            // afterwards, each sparql function name may have a set of backing sql declarations.
            // Note that the set of backing sql declarations is independent of the existing
            // overload signatures for that name - i.e. it only depends on the name!
            //
            // It seems to me, that we should somehow bundle up this MultiMap<FunctionSymbol, FunctionName> map.
            // Yet again, on the SQL level, we assign a unique ID to each overload
            //
            //
            //
            //
            Multimap<String, String> symbolSparqlDecls = HashMultimap.create();


            // Aggregate function declarations
            // Note: These function have a hidden 'flags' field (string), e.g. for distinct
    //		MethodDeclaration<TypeToken> aggCountDecl1 = MethodDeclaration.create(TypeToken.Long, "Count");
    //		MethodDeclaration<TypeToken> aggCountDecl2 = MethodDeclaration.create(TypeToken.Long, "Count", false, TypeToken.Object);
    //		sqlModel.registerFunction(aggCountDecl1);
    //		sqlModel.registerFunction(aggCountDecl2);
    //		sparqlSqlDecls.put("count(*)", aggCountDecl1.toString());
    //		sparqlSqlDecls.put("count(*)", aggCountDecl2.toString());


            /*
             * TODO Think of some builder pattern...
             * but maybe a declarative approach would be even better...
             * yet the problem is that we need beans in the xml, which means we need to mix it
             * with spring
             *
             * builder.getSparqlDecl(bif + "foo").addSqlDecl(..).setImpl().addSqlDecl(...)
             *
             */

    //		try {
    //			InputStream in = NewWorldTest.class.getClassLoader().getResourceAsStream("functions.xml");
    //			SparqlifyConfig config = XmlUtils.unmarshallXml(SparqlifyConfig.class, in);
    //
    //			//System.out.println(config);
    //
    //
    //			for(SimpleFunction simpleFunction : config.getSimpleFunctions().getSimpleFunction()) {
    //				String functionName = simpleFunction.getName();
    //
    //				for(Mapping mapping : simpleFunction.getMappings().getMapping()) {
    //					String decStr = mapping.getSignature();
    //					String patternStr = mapping.getPattern();
    //
    //					MethodDeclaration<String> dec = MethodDeclarationParserSimple.parse(decStr);
    //
    //					SqlFunctionSerializer serializer;
    //					if(patternStr == null) {
    //						serializer = new SqlFunctionSerializerDefault(functionName);
    //					} else {
    //						serializer = SqlFunctionSerializerStringTemplate.create(patternStr, dec);
    //					}
    //
    //					// TODO Add to serializer system
    //					// TODO Auto create SPARQL declaration
    //				}
    //			}
    //
    //		} catch(Exception e) {
    //			throw new RuntimeException(e);
    //		}

            // TODO We need to find the best overload based on a set of types:

            // http://www.postgresql.org/docs/9.1/static/functions-aggregate.html
            // Expample: sum is declared for: smallint, int, bigint, real, double precision, numeric, or interval

            // So given: [smallint, text, decimal, double, geometry] we need to figure out that there is no match
            // for geometry and text, but there are matches for smallint, decimal and double.
            //
            // Well, I guess decimal and double only have numeric as a common base
            //
            /*
             * Algo:
             * For each expression type, find all candidates
             *   By this we rule out the types for which no candidates exist
             *
             * For the remaining ones, we need to find the best matching candidate out of those that we already found.
             *
             * So we compute the distance for each type and for each candidate, and take the best one... seems easy.
             *
             *
             * For group by:
             *
             * Group by is being done by expressions, so we can use the same expression as
             * generated by order by for the grouping, rather than duplicating the views and
             * increasing the joins.
             *
             *
             */


            MethodDeclaration<TypeToken> aggCountDecl = MethodDeclaration.create(TypeToken.Long, "Count");
            sqlModel.registerFunction(aggCountDecl);
            sparqlSqlDecls.put(AggCount.class.getSimpleName(), aggCountDecl.toString());


            MethodDeclaration<TypeToken> aggSumDecl1 = MethodDeclaration.create(TypeToken.Long, "Sum", false, TypeToken.Long);
            MethodDeclaration<TypeToken> aggSumDecl2 = MethodDeclaration.create(TypeToken.Double, "Sum", false, TypeToken.Double);
            sqlModel.registerFunction(aggSumDecl1);
            sqlModel.registerFunction(aggSumDecl2);
            sparqlSqlDecls.put(AggSum.class.getSimpleName(), aggSumDecl1.toString());
            sparqlSqlDecls.put(AggSum.class.getSimpleName(), aggSumDecl2.toString());



            MethodDeclaration<TypeToken> aggGroupConcatDecl = MethodDeclaration.create(TypeToken.String, "GroupConcat", false, TypeToken.String);
            sqlModel.registerFunction(aggGroupConcatDecl);
            sparqlSqlDecls.put(AggGroupConcat.class.getSimpleName(), aggGroupConcatDecl.toString());



            //sqlImpls.put(urlEncodeDecl.toString(), new SqlExprEvaluator_UrlEncode());


            FunctionModelAliased<String> sparqlModel = typeSystem.getSparqlFunctionModel();
            String fn = "http://www.w3.org/2005/xpath-functions#";
            String op = "http://www.w3.org/2005/xpath-functions#";

            String xsdInt = XSD.xint.toString();
            String xsdString = XSD.xstring.toString();
            String xsdFloat = XSD.xfloat.toString();
            String xsdDouble = XSD.xdouble.toString();
            String xsdDecimal = XSD.decimal.toString();

            MethodDeclaration<String> numericAddInt = MethodDeclaration.create("+", MethodSignature.create(false, xsdInt, xsdInt, xsdInt));
            sparqlModel.registerFunction("+", numericAddInt);

            MethodDeclaration<String> numericAddDouble = MethodDeclaration.create("+", MethodSignature.create(false, xsdDouble, xsdDouble, xsdDouble));
            sparqlModel.registerFunction("+", numericAddDouble);

            sparqlModel.registerCoercion(MethodDeclaration.create(xsdDouble, MethodSignature.create(false, xsdDouble, xsdInt)));

            // Decimal -> Float
            // Decimal -> Double
            sparqlModel.registerCoercion(MethodDeclaration.create(xsdFloat, MethodSignature.create(false, xsdFloat, xsdDecimal)));
            sparqlModel.registerCoercion(MethodDeclaration.create(xsdDouble, MethodSignature.create(false, xsdDouble, xsdDecimal)));


            MethodDeclaration<String> groupConcat = MethodDeclaration.create(AggGroupConcat.class.getSimpleName(), MethodSignature.create(false, xsdString));
            sparqlModel.registerFunction(AggGroupConcat.class.getSimpleName(), groupConcat);

            //sparqlModel.registerCoercion(MethodDeclaration.create(xsdDouble, MethodSignature.create(false, xsdDouble, xsdInt)));


                //sqlImpls.put()

    //			SparqlFunction f = new SparqlFunctionImpl(decl, null, null);
    //			typeSystem.registerSparqlFunction(f);


                /**
                 * Ok, seems like we need one more iteration to get the type system right:
                 * - Initially, we start with a SPARQL expression, such as typedLit(?foo, xsd:int) + typedLit(?bar, xsd:float)
                 * The xsd types must be compatible with the underlying sql type.
                 * What compatible means needs to be formalized, but essentially it means
                 * we are mapping to the closest semantic type - and that we are not mapping e.g. strings to integers.
                 *
                 * - Now the TypedExprTransformer does a bottom up evaluation of the expression, and turns each node into an RdfTerm expression
                 *   typed literal constants and column references are automatically converted to typed literals
                 *
                 * - When the TypedExprTransformer hits an operator, such as '+', '||', '&&' and so on, it invokes any registered
                 *   ExprTransformer*  --- its signature is:  E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs);
                 *
                 *   The expr transformer can now yield a now RdfTerm expression
                 *   and has now the chance to detect type errors, or compute an appropriate datatype language tag, etc.
                 *
                 *   This step is independent of the SQL datatypes - its purpose is to fulfill the functions' and operators' contracts
                 *   that are set forth by the SPARQL standard.
                 *   Note that for simplicity we do not allow xsd:datatypes to be dynamic.
                 *      In theory, we could push down the conditions into the SQL, but this is
                 *      rather cumbersome and there is no probably no use case that can't be solved in a better way.
                 *
                 *  The resulting expression for a function may be a constant or an arbitray new expression with its own function name.
                 *  However, in practice, the function name should stay the same as the original one, as the main purpose is to
                 *  create the appropriate E_RdfTerm object for the encountered symbol.
                 *
                 *
                 * After the TypedExprTransformer is done, we end up with a new expression which does not contain any E_RdfTer
                 * objects anymore except for the root of the expression.
                 * This means, that we eleminated the RDF specific RDF terms and instead have expressions that only make use of
                 * plain old SQL datatypes.
                 *
                 * - The function symbols now no longer refer to the original SPARQL functions, which have to cope with RdfTerm semantics,
                 *   but rather, their arguments are now SQL types.
                 *
                 *   We can now declare for each function symbol which combinations of SQL typed parameters are valid,
                 *   thereby *overloading* the symbol with SQL symbols.
                 *   The plus operator could for instance have the overloads int + (int, int), float + (float, float), etc...
                 *
                 *   [TODO] We already store literals as java objects, can we just map them to corresponding java methods using reflection?
                 *   On the other hand, reflection is so fucking expensive which is bad for benchmarks.
                 *
                 *
                 * - We could now provide java implementations the evaluate these functions
                 *
                 * - Finally, for each SQL function symbol, the appropriate serializer needs to be used
                 *   e.g. double(foo) -&gt; foo::double for postgres, float(bar) -&gt; (cast bar as float) for mysql, ...
                 *
                 *
                 *
                 */



            // tag the comparators as comparators...


            // urlEncode
            {
                MethodSignature<TypeToken> sig = MethodSignature.create(false, TypeToken.String, TypeToken.String);

                SparqlFunction f = new SparqlFunctionImpl(SparqlifyConstants.urlEncode, sig, null, null);
                typeSystem.registerSparqlFunction(f);
            }

            {
                MethodDeclaration<TypeToken> decl = MethodDeclaration.create(TypeToken.String, SparqlifyConstants.urlEncode, false, TypeToken.String);

                SparqlFunction f = new SparqlFunctionImpl(decl, null, null);
                typeSystem.registerSparqlFunction(f);
            }







            //

            // {
            // MethodSignature<String> sig = MethodSignature.create(false,
            // SparqlifyConstants.numericTypeLabel,
            // SparqlifyConstants.numericTypeLabel,
            // SparqlifyConstants.numericTypeLabel);
            // SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();
            //
            // SparqlFunction f = new SparqlFunctionImpl("or", sig, evaluator,
            // null);
            // typeSystem.registerSparqlFunction(f);
            // SqlFunctionSerializer serializer = new
            // SqlFunctionSerializerOp2("OR");
            // serializerSystem.addSerializer("or", serializer);
            //
            // }

            {
                // MethodSignature<String> sig = MethodSignature.create(false,
                // SparqlifyConstants.numericTypeLabel,
                // SparqlifyConstants.numericTypeLabel,
                // SparqlifyConstants.numericTypeLabel);
                // SqlExprEvaluator evaluator = new
                // SqlExprEvaluator_Arithmetic(typeSystem);
                //
                // XSDFuncOp.add(nv1, nv2);
                // XSDFuncOp.classifyNumeric(fName, nv);
                //
                // // As a fallback where Jena can't evaluate it, register a
                // transformation to an SQL expression.
                // SparqlFunction f = new SparqlFunctionImpl("+", sig, evaluator,
                // null);
                // typeSystem.registerSparqlFunction(f);
                // SqlFunctionSerializer serializer = new
                // SqlFunctionSerializerOp2("+");
                // serializerSystem.addSerializer("+", serializer);
            }

        }

        public static SparqlifyConfig loadSqlFunctionDefinitions(String resourceName) {
            InputStream in = SparqlifyCoreInit.class.getClassLoader().getResourceAsStream(resourceName);
            SparqlifyConfig result;
			try {
				result = XmlUtils.unmarshallXml(SparqlifyConfig.class, in);
			} catch (UnsupportedEncodingException | JAXBException e) {
				throw new RuntimeException(e);
			}
            return result;
        }

    public static void loadExtensionFunctions(TypeSystem typeSystem, RdfTermEliminatorWriteable exprTransformer, SqlExprSerializerSystem serializerSystem, SparqlifyConfig sqlFunctionMapping) {



    //		transMap.put(bif + "st_intersects", new ExprTransformerFunction(XSD.xboolean));
    //		transMap.put(bif + "st_geomFromText", new ExprTransformerFunction(virtGeometry));
    //		transMap.put(bif + "st_point", new ExprTransformerFunction(ResourceFactory.createResource("http://www.opengis.net/ont/geosparql#wktLiteral"))); //));

            try {



                Map<String, String> typeNameToUri = MapReader.readFromResource("/type-uri.tsv");

                Function<String, String> fnTypeToUri = Functions.forMap(typeNameToUri);


                FunctionModelAliased<String> sparqlModel = typeSystem.getSparqlFunctionModel();



                Multimap<String, String> sparqlSqlDecls = typeSystem.getSparqlSqlDecls();
                FunctionModel<TypeToken> sqlModel = typeSystem.getSqlFunctionModel();


                //System.out.println(config);


                for(SimpleFunction simpleFunction : sqlFunctionMapping.getSimpleFunctions().getSimpleFunction()) {
                    String sparqlName = simpleFunction.getName();

                    for(Mapping mapping : simpleFunction.getMappings().getMapping()) {
                        String decStr = mapping.getSignature();
                        String patternStr = mapping.getPattern();

                        MethodDeclaration<String> dec = MethodDeclarationParserSimple.parse(decStr);

                        SqlFunctionSerializer serializer;
                        if(patternStr == null) {
                            serializer = new SqlFunctionSerializerDefault(dec.getName());
                        } else {
                            serializer = SqlFunctionSerializerStringTemplate.create(patternStr, dec);
                        }

                        MethodDeclaration<TypeToken> sqlDec = transform(dec, TransformUtils.toTypeToken);

                        String translationName = sparqlName + "@" + sqlDec.getSignature().getReturnType();
                        MethodDeclaration<String> sparqlDec = MethodDeclaration.create(translationName, transform(dec.getSignature(), fnTypeToUri));


                        sparqlModel.registerFunction(sparqlName, sparqlDec);

                        //Resource resReturnType = ResourceFactory.createResource(sparqlDec.getSignature().getReturnType());
                        //ExprTransformer et = new ExprTransformerFunction(resReturnType);
                        //transMap.put(bif + "st_intersects", );


                        // TODO Add to serializer system
                        // TODO Auto create SPARQL declaration
                        String sqlDescriptor = sqlDec.toString();
                        sparqlSqlDecls.put(translationName, sqlDescriptor);
                        sqlModel.registerFunction(sqlDec);
                        serializerSystem.addSerializer(sqlDescriptor, serializer);

                    }

                    ExprTransformer et = new ExprTransformerSparqlFunctionModel(sparqlModel);
                    exprTransformer.register(sparqlName, et);
                }

            } catch(Exception e) {
                throw new RuntimeException(e);
            }


        }

    // TODO Move these transform methods to an appropriate place

    public static <I, O> MethodDeclaration<O> transform(MethodDeclaration<I> dec, Function<I, O> fn) {
        MethodSignature<O> s = transform(dec.getSignature(), fn);
        MethodDeclaration<O> result = MethodDeclaration.create(dec.getName(), s);

        return result;
    }


    public static <I, O> MethodSignature<O> transform(MethodSignature<I> sig, Function<I, O> fn) {
        O returnType = fn.apply(sig.getReturnType());

        List<I> items = sig.getParameterTypes();
        List<O> paramTypes = new ArrayList<O>(items.size());
        for(I item : items) {
            O paramType = fn.apply(item);
            paramTypes.add(paramType);
        }

        I vat = sig.getVarArgType();
        O varArgType = vat == null ? null : fn.apply(vat);

        MethodSignature<O> result = MethodSignature.create(returnType, paramTypes, varArgType);

        return result;
    }
}
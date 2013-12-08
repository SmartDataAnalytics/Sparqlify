/*
 *    Copyright 2007-2010 The sparkle-g Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * @author Simone Tripodi   (simone.tripodi)
 * @author Michele Mostarda (michele.mostarda)
 * @author Juergen Pfundt   (Juergen.Pfundt)
 * @version $Id: Sparql.g 161 2011-05-23 22:14:39Z Juergen.Pfundt@web.de $
 */
tree grammar SparqlifyConfigTree;

options {
tokenVocab=SparqlifyConfig; // reuse token types
ASTLabelType=CommonTree; // $label will have type CommonTree
//output=template;
}

@header {
    package org.aksw.sparqlify.config.lang;
    
    import com.hp.hpl.jena.sparql.expr.*;
    import org.apache.commons.lang.NotImplementedException;
    import com.hp.hpl.jena.graph.*;
    import com.hp.hpl.jena.vocabulary.*;
    import com.hp.hpl.jena.sparql.syntax.*;
    import com.hp.hpl.jena.shared.*;
    import org.aksw.sparqlify.config.syntax.*;
    import com.hp.hpl.jena.shared.impl.*;
    import com.hp.hpl.jena.sparql.core.*;
    import com.hp.hpl.jena.datatypes.*;
    import org.aksw.sparqlify.algebra.sparql.expr.*;
    import com.hp.hpl.jena.rdf.model.AnonId;    
    import org.aksw.sparqlify.util.*;
    import org.aksw.jena_sparql_api.utils.*;

    import org.aksw.sparqlify.algebra.sql.nodes.*;
    import com.hp.hpl.jena.sdb.core.JoinType;

    import java.util.Collection;
    import java.util.List;
    import java.util.ArrayList;
    import java.util.Map;
    import java.util.AbstractMap;
    import java.util.HashMap;


    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import com.hp.hpl.jena.shared.PrefixMapping;
    import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;


    import org.aksw.sparqlify.core.SparqlifyConstants;
}


@members {
	private static final Logger logger = LoggerFactory.getLogger("Parser");



	private PrefixMapping prefixMapping = new PrefixMappingImpl();

    private Map<String, String> options = new HashMap<String, String>();
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}
	
	public Map<String, String> getOptions() {
        return options;
	}
	
	
	public Node getDefaultGraph() {
	    String value = options.get("defaultGraph");
	    if(value == null) {
	    	value = "";
	    } else {
	    	value = value.trim();
	    }


	    Node result;
	    if(value.isEmpty()) {
	    	result = Quad.defaultGraphNodeGenerated;
	   	}
	   	else {
	   		result = Node.createURI(value);
	   	}
	    
	    return result;
	}
	

	void registerPrefix(String prefix, String uri) {
		registerPrefix(prefixMapping, new PrefixDecl(prefix, uri));
	}

/*
	void registerPrefix(Config config, String prefix, String uri) {
		registerPrefix(config.getPrefixMapping(), new PrefixDecl(prefix, uri));
	}

	void registerPrefix(TemplateConfig config, String prefix, String uri) {
		registerPrefix(config.getPrefixMapping(), new PrefixDecl(prefix, uri));
	}

	void registerPrefix(ConstructConfig config, String prefix, String uri) {
		registerPrefix(config.getPrefixMapping(), new PrefixDecl(prefix, uri));
	}
*/
	
	void registerPrefix(PrefixMapping prefixMapping, PrefixDecl prefixDecl) {
	    // Warn if prefix gets re-registered
	    String uri = prefixMapping.getNsPrefixURI(prefixDecl.getPrefix());
	    
	    if(uri != null) {
			logger.warn("Prefix '" + prefixDecl.getPrefix() + "' remapped from <" + uri + "> to <" + prefixDecl.getUri() + ">");
	    }
	    
	    prefixMapping.setNsPrefix(prefixDecl.getPrefix(), prefixDecl.getUri());
	}


    public String expandPrefix(String prefix) {
    	String result = prefixMapping.getNsPrefixMap().get(prefix);

        if(result == null) {
            logger.error("Namespace for prefix '" + prefix + "' not declared");
            result = "http://sparqlify.org/resource/unresolvedNamespace/";
        }
        
        return result;
    }
    
    
    public String tryExpandUri(String uri) {
    	String result;
    
        int index = uri.indexOf(':');
        if(index < 0) {
			result = uri;
		} else {
			result = expandUri(uri);
		}
		
		return result;
	}
    
    public String expandUri(String uri) {
        int index = uri.indexOf(':');
        if(index < 0) {
            logger.error("Internal error: Expansion of '" + uri + "' requested although no symbol ':' found.");
            return uri;
        }
        
        String prefix = uri.substring(0, index);
        String namespace = expandPrefix(prefix);

        String result = namespace + uri.substring(index + 1);
        
        return result;
    }

    public String getErrorMessage(RecognitionException e,
                                  String[] tokenNames)
    {
        List stack = getRuleInvocationStack(e, this.getClass().getName());
        String msg = null;
        String inputContext =
            input.LT(-3) == null ? "" : ((Tree)input.LT(-3)).getText()+" "+
            input.LT(-2) == null ? "" : ((Tree)input.LT(-2)).getText()+" "+
            input.LT(-1) == null ? "" : ((Tree)input.LT(-1)).getText()+" >>>"+
            ((Tree)input.LT(1)).getText()+"<<< "+
            ((Tree)input.LT(2)).getText()+" "+
            ((Tree)input.LT(3)).getText();
        if ( e instanceof NoViableAltException ) {
           NoViableAltException nvae = (NoViableAltException)e;
           msg = " no viable alt; token="+e.token+
              " (decision="+nvae.decisionNumber+
              " state "+nvae.stateNumber+")"+
              " decision=<<"+nvae.grammarDecisionDescription+">>";
        }
        else {
           msg = super.getErrorMessage(e, tokenNames);
        }
        return stack+" "+msg+" context=..."+inputContext+"...";
    }
    public String getTokenErrorDisplay(Token t) {
        return t.toString();
    }

/*
    void addAsQuad(QuadPattern qp, Template template, Node g) {
        if(g == null) {
            g = Quad.defaultGraphNodeGenerated;
        }
        
        for(Triple t : template.getTriples()) {
            Quad quad = new Quad(g, t);
            qp.add(quad);
        }
    }
*/

    public E_Function createUriFunction(ExprList args) {
        Expr arg;
       
        if(args.size() == 1) {
			arg = args.get(0);
        } else {
			arg = new E_StrConcatPermissive(args);
        }

        E_Function result = createFunction(SparqlifyConstants.uriLabel, arg);
        return result; 
    }


    public E_Function createFunction(String label, Expr ... args) {
        ExprList exprs = new ExprList();
        for(Expr arg : args) {
            if(arg != null) {
                exprs.add(arg);
            }
        }
        
        return new E_Function(label, exprs);        
    }

}

// $<Parser

templateConfig returns[TemplateConfig config]
	@init { $config = new TemplateConfig(); prefixMapping = $config.getPrefixMapping(); }
	: templateConfigItem[config]+
	;

templateConfigItem[TemplateConfig config]
	: x=namedViewTemplateDefinition {config.getDefinitions().add($x.value);}
	| a=prefixDecl {registerPrefix($a.prefix, $a.uri);}
	| b=setStmt {options.put($b.value.getKey(), $b.value.getValue());}
	;


setStmt returns[Map.Entry<String, String> value]
    : ^(SET a=NAME b=string) {$value = new AbstractMap.SimpleEntry<String, String>($a.text, $b.value);}
    ;

constructConfig returns[ConstructConfig config]
	@init { $config = new ConstructConfig(); prefixMapping = $config.getPrefixMapping(); }
	: constructConfigItem[config]+
	;

constructConfigItem[ConstructConfig config]
	: x=constructViewDefinition[config] { config.getViewDefinitions().add($x.value); }
	| a=prefixDecl { registerPrefix($a.prefix, $a.uri); }
	| b=setStmt {options.put($b.value.getKey(), $b.value.getValue());}
	;

constructViewDefinition[ConstructConfig config] returns [ ConstructViewDefinition value ]
	: ^(CONSTRUCT_VIEW_DEFINITION a=NAME b=SQL_QUERY) { $value = new ConstructViewDefinition($a.text, $b.text, prefixMapping); }
	;




sparqlifyConfig returns[Config config]
	@init { $config = new Config(); prefixMapping = $config.getPrefixMapping(); }
	: sparqlifyConfigItem[config]+
	;
	
sparqlifyConfigItem[Config config]
	: x=viewDefinition      {$config.getViewDefinitions().add($x.value);}
	| a=prefixDecl          {registerPrefix($a.prefix, $a.uri);}
	| b=functionDeclaration {$config.getFunctionDeclarations().add($b.value);}
	| c=setStmt {options.put($c.value.getKey(), $c.value.getValue());}
	;



functionDeclaration returns [FunctionDeclarationTemplate value]
    : ^(FUNCTION_DECLARATION a=functionSignature b=functionTemplate) {$value = new FunctionDeclarationTemplate($a.value, $b.value);}
    ;

functionSignature returns [FunctionSignature value]
    : ^(FUNCTION_SIGNATURE a=NAME b=iriRef c=paramTypeList) {$value = new FunctionSignature($b.value.toString(), $a.text, $c.value);}
    ;

paramTypeList returns [ParamTypeList value]
	@init { $value = new ParamTypeList(); }
    : ^(PARAM_TYPE_LIST (a=paramType {$value.add($a.value);})+)
    | ^(PARAM_TYPE_LIST nil)	
    ;


paramType returns [ParamType value]
    : ^(PARAM_TYPE a=NAME b=var) {$value = new ParamType($a.text, $b.value);}
    ;


functionTemplate returns [FunctionTemplate value]
    : ^(FUNCTION_TEMPLATE a=NAME b=expressionList) {$value = new FunctionTemplate($a.text, $b.value);}
    ;




namedViewTemplateDefinition returns [NamedViewTemplateDefinition value]
	: ^(NAMED_VIEW_TEMPLATE_DEFINITION a=NAME b=viewTemplateDefinition) {$value = new NamedViewTemplateDefinition($a.text, $b.value);}
	;
	
viewTemplateDefinition returns [ViewTemplateDefinition value]
	: ^(VIEW_TEMPLATE_DEFINITION a=constructTemplateQuads b=varBindings?) {$value = new ViewTemplateDefinition($a.value, $b.value);}
	;

viewDefinition returns [ ViewDefinition value ]
	: ^(VIEW_DEFINITION a=NAME b=viewTemplateDefinition d=varConstraints? c=sqlRelation?) { $value = new ViewDefinition($a.text, $b.value, $c.value, $d.value); }
	;
	
varBindings returns [List<Expr> value]
	@init {$value = new ArrayList<Expr>();}
	: ^(VAR_BINDINGS (a=varBinding {$value.add($a.value);})+)
	;
	

varBinding returns [Expr value]
	: ^(VAR_BINDING	a=var b=typeCtorExpression) {$value = new E_Equals(new ExprVar($a.value), $b.value);}
	;
	
typeCtorExpression returns [Expr value]
    : ^(BNODE a=expression) {$value = createFunction(SparqlifyConstants.blankNodeLabel, $a.value); } 
    | ^(URI c=expressionList) {$value = createUriFunction($c.value); }
    | ^(PLAIN_LITERAL a=expression b=expression?) {$value = createFunction(SparqlifyConstants.plainLiteralLabel, $a.value, $b.value); }
    | ^(TYPED_LITERAL a=expression b=expression) {$value = createFunction(SparqlifyConstants.typedLiteralLabel, $a.value, $b.value); }
    ;
	
sqlRelationOld returns [Relation value]
	: ^(SQL_RELATION a=SQL_QUERY) {$value = new QueryString($a.text);}
	| ^(SQL_RELATION a=SQL_TABLE) {$value = new RelationRef($a.text);}
	;


// Expressions are SPARQL expressions, so its a bit hacky right now
sqlRelation returns [SqlOp value]
    : ^(RELATION_REF a=joinClause b=expression?) {$value=($b.value == null) ? a : new SqlOpFilterExpr(null, $a.value, $b.value);}
    ;

joinClause returns [SqlOp value]
    : a=joinClauseMember {$value=$a.value;}
    | ^(FULL_JOIN a=joinClauseMember b=joinClauseMember) {$value=new SqlOpJoin(null, JoinType.INNER, $a.value, $b.value);} 
    ;
    
joinClauseMember returns [SqlOp value]
    : ^(JOIN_MEMBER a=sqlLogicalTable b=NAME?) {$value = $a.value; ((SqlOpLeaf)$value).setAliasName($b.text);}
    ;

sqlLogicalTable returns [SqlOp value]
	: ^(SQL_RELATION a=SQL_QUERY) {$value = new SqlOpQuery(null, $a.text);}
	| ^(SQL_RELATION a=SQL_TABLE) {$value = new SqlOpTable(null, $a.text);}
	;


varConstraints returns [List<Constraint> value]
	@init {$value = new ArrayList<Constraint>();}
    : ^(CONSTRAINTS (a=varConstraint{$value.add($a.value);})+)
    ;

varConstraint returns [Constraint value]
    : (a=regexVarConstraint) {$value = $a.value;}
    | (b=prefixVarConstraint) {$value = $b.value;}
    ;

regexVarConstraint returns [RegexConstraint value]
	: ^(REGEX_CONSTRAINT a=var b=string) {$value = new RegexConstraint($a.value, "value", $b.value);}
    ;

prefixVarConstraint returns [PrefixConstraint value]
	: ^(PREFIX_CONSTRAINT a=var b=stringList) {$value = new PrefixConstraint($a.value, "value", $b.value);}
    ;

stringList returns [List<String> value]
	@init {$value = new ArrayList<String>();}
    : ^(STRING_LIST (a=string {value.add($a.value); })+)
    ;


query
    : ^(QUERY prologue selectQuery* constructQuery* describeQuery* askQuery*) bindingsClause*
    | ^(UPDATE update+)
    ;

prologue
    : ^(PROLOGUE baseDecl* prefixDecl*)
    ;

baseDecl
    : ^(BASE IRI_REF)
    ;

prefixDecl returns [String prefix, String uri]
    : ^(PREFIX a=PNAME_NS b=IRI_REF) { $prefix=$a.text; $uri=$b.text; }
    ;

selectQuery
    : ^(SELECT selectClause datasetClause* whereClause* solutionModifier)
    ;

subSelect
    : ^(SUBSELECT whereClause* solutionModifier)
    ;
    	
selectClause
    : ^(SELECT_CLAUSE ASTERISK)
    | ^(SELECT_CLAUSE DISTINCT ASTERISK)
    | ^(SELECT_CLAUSE REDUCED ASTERISK)
    | ^(SELECT_CLAUSE selectVariables*)
    | ^(SELECT_CLAUSE DISTINCT selectVariables*)
    | ^(SELECT_CLAUSE REDUCED selectVariables*)
    ;

selectVariables
    : ^(VAR var) 
    | ^(AS expression var)
    ;
  
constructQuery
    : ^(CONSTRUCT constructTemplate* datasetClause* whereClause* solutionModifier)
    | ^(CONSTRUCT datasetClause* ^(WHERE triplesTemplate*) solutionModifier)
    ;

describeQuery
    : ^(DESCRIBE varOrIRIref* ASTERISK* datasetClause* whereClause? solutionModifier)
    ;

askQuery
    : ^(ASK datasetClause* whereClause)
    ;

datasetClause
    : ^(FROM NAMED? iriRef)
    ;

whereClause
    : ^(WHERE_CLAUSE groupGraphPattern)
    ;
    
solutionModifier
    : groupClause? havingClause? orderClause? limitOffsetClauses?
    ;

groupClause
    : ^(GROUP_BY groupCondition+)
    ;
    		 
groupCondition
    : builtInCall
    | functionCall
    | ^(AS expression var*)
    | var
    ;

havingClause
    : ^(HAVING constraint+)	
    ;
    
orderClause
    : ^(ORDER_BY orderCondition+)
    ;

orderCondition
    : ^(ORDER_CONDITION ASC expression)
    | ^(ORDER_CONDITION DESC expression)
    | ^(ORDER_CONDITION constraint)
    | ^(ORDER_CONDITION var)
    ;
	    
limitOffsetClauses
    : ^(LIMIT INTEGER) (^(OFFSET INTEGER))*
    | ^(OFFSET INTEGER) (^(LIMIT INTEGER))*
    ;


bindingsClause
    : ^(BINDINGS var* bindingValueList*)
    ;
    
bindingValueList
    : ^(BINDING_VALUE bindingValue*)
    ;
    	
bindingValue returns [Node value]
    : a=iriRef {$value=$a.value;}
    | a=rdfLiteral {$value=$a.value;}
    | numericLiteral
    | booleanLiteral
    | UNDEF
    ;
    
update
    : prologue load* clear* drop* add* move* copy* create* insert* delete* modify*
    ;   
    
load 	  
    : ^(LOAD SILENT* iriRef graphRef*)
    ;
    
clear
    : ^(CLEAR SILENT* graphRefAll)
    ;
    
drop
    : ^(DROP SILENT* graphRefAll)
    ; 

create
    : ^(CREATE SILENT* graphRef)
    ;
    
add
    : ^(ADD SILENT* graphOrDefault graphOrDefault)
    ;
    
move
    : ^(MOVE SILENT* graphOrDefault graphOrDefault)
    ;
    
copy
    : ^(COPY SILENT* graphOrDefault graphOrDefault)
    ;

insert
    : ^(INSERT DATA quadPattern)
    ;
   
delete 	  
    : DELETE ( deleteData | deleteWhere )
    ;

deleteData
    : ^(DELETE DATA quadPattern)
    ;

deleteWhere
    : ^(DELETE WHERE quadPattern)
    ;
    
modify
    : ^(MODIFY ^(WITH iriRef) deleteClause* insertClause* usingClause* ^(WHERE groupGraphPattern))
    | ^(MODIFY deleteClause* insertClause* usingClause* ^(WHERE groupGraphPattern))
    ;
  
deleteClause
    : DELETE quadPattern
    ;
      
insertClause
    : INSERT quadPattern
    ;

usingClause
    : ^(USING NAMED? iriRef)
    ;

graphOrDefault	  
    : DEFAULT 
    | GRAPH? iriRef
    ;
    	    	
graphRef
    : GRAPH iriRef
    ;

graphRefAll
    : graphRef | DEFAULT | NAMED | ALL
    ;

quadPattern returns [QuadPattern value]
    : a=quads{$value = $a.value;}
    ;
    
quads returns [QuadPattern value]
    @init{$value = new QuadPattern();}
    : (a=triplesTemplate {$value.addAll(QuadPatternUtils.toQuadPattern(getDefaultGraph(), $a.value));})? ( quadsNotTriples[$value] (c=triplesTemplate {$value.addAll(QuadPatternUtils.toQuadPattern(getDefaultGraph(), $c.value));})? )*
    ;
    
quadsNotTriples [QuadPattern value] //returns [QuadPattern value]
    //@init{$value = new QuadPattern()}
    : ^(GRAPH_TOKEN a=varOrIRIref b=triplesTemplate?) {$value.addAll(QuadPatternUtils.toQuadPattern($a.value, $b.value));}
    ;

/*
triplesTemplate returns [BasicPattern value]
    @init {$value = new BasicPattern();}
    : ^(TRIPLES_TEMPLATE triplesSameSubject[$value]*)
    ;
*/

triplesTemplate returns [BasicPattern value]
    : ^(TRIPLES_TEMPLATE a=triples {$value = $a.value;})
    ;
    	
groupGraphPattern
    : ^(GROUP_GRAPH_PATTERN subSelect)
    | ^(GROUP_GRAPH_PATTERN groupGraphPatternSub)
    ;
    
groupGraphPatternSub
    : triplesBlock? groupGraphPatternSubCache*
    ;
    
groupGraphPatternSubCache
    :  graphPatternNotTriples triplesBlock?
    ;

triplesBlock
    : ^(TRIPLES_BLOCK triplesSameSubjectPath[null]+)
    ;

graphPatternNotTriples
    : groupOrUnionGraphPattern | optionalGraphPattern | minusGraphPattern | graphGraphPattern | serviceGraphPattern | filter | bind
    ;

optionalGraphPattern
    : ^(OPTIONAL groupGraphPattern)
    ;

graphGraphPattern
    : ^(GRAPH varOrIRIref groupGraphPattern)
    ;

serviceGraphPattern
    : ^(SERVICE SILENT? varOrIRIref groupGraphPattern)
    ;
    
bind
    : ^(BIND expression ^(AS var))	
    ;
    	
minusGraphPattern
    : ^(MINUS_KEYWORD groupGraphPattern)
    ;

groupOrUnionGraphPattern
    : ^(UNION groupGraphPattern groupGraphPattern)
    | groupGraphPattern
    ;

filter
    : ^(FILTER constraint)
    ;

constraint
    : expression
    | builtInCall
    | functionCall
    ;

functionCall returns [Expr value]
    : ^(FUNCTION a=iriRef ^(ARG_LIST b=argList)) { value = new E_Function($a.value.toString(), $b.value); }
    ;

// TODO Not sure if the return type is correct
argList returns [ExprList value]
	@init { value = new ExprList(); }
    : nil
    | DISTINCT? (a=expression {$value.add($a.value);})*
    ;

expressionList returns [ExprList value]
	@init { value = new ExprList(); }
    : ^(EXPRESSION_LIST (a=expression {value.add($a.value); })+)
    | ^(EXPRESSION_LIST nil)
    ;	


/*
constructTemplateEx returns[QuadPattern value]
	@init { $value = QuadPattern(); }
	: a=constructTemplate {addAsQuad($value, $a, null);}
    | ^(GRAPH b=varOrIriRef a=constructTemplate {addAsQuad($value, $a, $b);}
    ;
*/  

constructTemplateQuads returns[QuadPattern value]
    : ^(CONSTRUCT_QUADS a=quadPattern) {$value = $a.value;}
    ;


constructTemplate returns[Template value]
    //: ^(CONSTRUCT_TRIPLES a=constructTriples) { $value = new Template(BasicPattern.wrap($a.value)); }
    : ^(CONSTRUCT_TRIPLES a=constructTriples) {$value = new Template($a.value);}
    ;


constructTriples returns [BasicPattern value]
    : a=triples {$value = $a.value;}
    ;
    
//TODO Insert Basic Pattern rule

triples returns [BasicPattern value]
	@init {$value = new BasicPattern();}
	: triple[value]+
    ;

triple[BasicPattern triples]
    : ^(TRIPLE ^(SUBJECT a=varOrTerm) ^(PREDICATE b=verb) ^(OBJECT c=graphNode[triples])) {$triples.add(new Triple($a.value, $b.value, $c.value)); }
	;


// TODO: Do we need this rule?
triplesSameSubject[BasicPattern value]
    : TODO //(^(TRIPLE objectList[value]))+
    ;




// Object list is actually just a a single triple
objectList[BasicPattern triples]
	: ^(SUBJECT a=varOrTerm) ^(PREDICATE b=verb) ^(OBJECT c=graphNode[triples]) {$triples.add(new Triple($a.value, $b.value, $c.value)); System.out.println("Created triple: " + $triples); }
	;



/*
triple returns [Triple value]
    : ^(TRIPLE a=objectList) { $value = $a.value; }
    ; 

constructTemplate
    : ^(CONSTRUCT_TRIPLES constructTriples?)
    ;

constructTriples
    : triplesSameSubject[null]+
    ;

triplesSameSubject[BasicPattern value]
    : ^(TRIPLE $t=objectList { $value.add($t); })
//    | ^(TRIPLE triplesSameSubject[value] triplesSameSubject[value]?)
    ;

objectList[Collection<Triple> triples]
    : ^(TRIPLE ^(SUBJECT a=varOrTerm?) ^(PREDICATE b=verb) ^(OBJECT c=graphNode)  {$triples.add(new Triple($a.value, $b.value, $c.value);})
    ;
*/


// FIXME Result can also be a path
verb returns [ Node value ]
    : a=varOrIRIref { $value = $a.value; } 
    | A           { $value = RDF.type.asNode(); }
    | path		  { if(true) { throw new NotImplementedException(); } }
    ;

triplesSameSubjectPath [BasicPattern value]
    : ^(TRIPLE objectList[value])
    | ^(TRIPLE triplesSameSubjectPath[value])
    ;
      
path
    : PATH pathSequence ( PIPE pathSequence )*
    ; 

pathSequence
    : pathEltOrInverse ( DIVIDE pathEltOrInverse )*
    ;
    	  	
pathElt
    : pathPrimary pathMod?
    ;
    
pathEltOrInverse
    : pathElt | INVERSE pathElt
    ;
    
pathMod
    : ( ASTERISK | QUESTION_MARK | PLUS | OPEN_CURLY_BRACE ( INTEGER ( COMMA ( CLOSE_CURLY_BRACE | INTEGER CLOSE_CURLY_BRACE ) | CLOSE_CURLY_BRACE ) | COMMA INTEGER CLOSE_CURLY_BRACE ) )
    ;

pathPrimary
    : ^(PATH_PRIMARY iriRef)
    | ^(PATH_PRIMARY A)
    | ^(PATH_PRIMARY NEGATION pathNegatedPropertySet)
    | ^(PATH_PRIMARY path)
    ;

pathNegatedPropertySet
    : ^(PATH_NEGATED pathOneInPropertySet+)
    ;  	

pathOneInPropertySet
    : INVERSE? ( iriRef | A )
    ;
	
triplesNode[BasicPattern triples] returns [Node value]
    : ^(COLLECTION graphNode[triples]+)
    | ^(TRIPLE objectList[triples])
    ;

graphNode[BasicPattern triples] returns [Node value]
    : a = varOrTerm {$value = $a.value;}
    | triplesNode[triples]
    ;

varOrTerm returns [Node value]
    : a=var       {$value = $a.value;}
    | a=graphTerm {$value = $a.value;}
    ;

varOrIRIref returns [Node value]
    : a=var    {$value = $a.value;}
    | a=iriRef {$value = $a.value;}
    ;

var returns [Var value]
    : a=VAR {$value = Var.alloc($a.text);} 
    ;

graphTerm returns [Node value]
    : a=iriRef         {$value = $a.value;} 
    | a=rdfLiteral       {$value = $a.value;}
    | b=numericLiteral {$value = $b.value.asNode();}
    | b=booleanLiteral {$value = $b.value.asNode();}
    | a=blankNode      {$value = $a.value;}
    | nil
    ;
    
nil
    : OPEN_BRACE CLOSE_BRACE
    ;

expression returns [ Expr value ]
    : ^(OR a=expression b=expression)            { $value = new E_LogicalOr         ($a.value, $b.value); }
    | ^(AND a=expression b=expression)           { $value = new E_LogicalAnd        ($a.value, $b.value); }
    | ^(EQUAL a=expression b=expression)         { $value = new E_Equals            ($a.value, $b.value); }
    | ^(NOT_EQUAL a=expression b=expression)     { $value = new E_NotEquals         ($a.value, $b.value); }
    | ^(LESS a=expression b=expression)          { $value = new E_LessThan          ($a.value, $b.value); }
    | ^(GREATER a=expression b=expression)       { $value = new E_GreaterThan       ($a.value, $b.value); }
    | ^(LESS_EQUAL a=expression b=expression)    { $value = new E_LessThanOrEqual   ($a.value, $b.value); }
    | ^(GREATER_EQUAL a=expression b=expression) { $value = new E_GreaterThanOrEqual($a.value, $b.value); }
    | ^(IN a=expression b=expression)            { if(true) { throw new NotImplementedException(); } }
    | ^(NOT IN a=expression b=expression)        { if(true) { throw new NotImplementedException(); } }
    | ^(PLUS a=expression b=expression)          { $value = new E_Add               ($a.value, $b.value); }
    | ^(MINUS a=expression b=expression)         { $value = new E_Subtract          ($a.value, $b.value); }
    | ^(ASTERISK a=expression b=expression)      { $value = new E_Multiply          ($a.value, $b.value); }
    | ^(DIVIDE a=expression b=expression)        { $value = new E_Divide            ($a.value, $b.value); }
    | a=unaryExpression                          { $value = $a.value; } 
    ;
    
unaryExpression returns [ Expr value ]
    : ^(UNARY_NOT a=primaryExpression)         { $value = new E_LogicalNot($a.value); }
    | ^(UNARY_PLUS a=primaryExpression)        { $value = new E_UnaryPlus ($a.value); }
    | ^(UNARY_MINUS a=primaryExpression)       { $value = new E_UnaryMinus($a.value); }
    | ^(UNARY a=primaryExpression)             { $value = $a.value; }
    ;

primaryExpression returns [Expr value]
    : e=builtInCall       {$value = $e.value;}
    | e=iriRefOrFunction  {$value = $e.value;}
    | n=rdfLiteral        {$value = NodeValue.makeNode($n.value);}
    | nv=numericLiteral    {$value = $nv.value;}
    | nv=booleanLiteral    {$value = $nv.value;}
    | v=var               {$value = new ExprVar($v.value);}
    | aggregate
    ;

builtInCall returns [Expr value]
    : ^(STR a=expression)             {$value = new E_Str($a.value);} 
    | ^(LANG a=expression)            {$value = new E_Lang($a.value);}
    | ^(LANGMATCHES a=expression b=expression)    {$value = new E_LangMatches($a.value, $b.value);}
    | ^(DATATYPE a=expression)
    | ^(BOUND n=var)                  {$value = new E_Bound(new ExprVar($n.value));}
    | ^(IRI a=expression)
    | ^(URI a=expression)
    | ^(BNODE expression)
    | RAND
    | ^(ABS a=expression)
    | ^(CEIL a=expression)
    | ^(FLOOR a=expression)
    | ^(ROUND expression)
    | ^(CONCAT l=expressionList)      {$value = new E_StrConcatPermissive($l.value);}
    | subStringExpression
    | ^(STRLEN expression)
    | ^(UCASE expression)
    | ^(LCASE expression)
    | ^(ENCODE_FOR_URI expression)
    | ^(CONTAINS expression)
    | ^(STRSTARTS expression)
    | ^(STRENDS expression)
    | ^(YEAR expression)
    | ^(MONTH expression)
    | ^(DAY expression)
    | ^(HOURS expression)
    | ^(MINUTES expression)
    | ^(SECONDS expression)
    | ^(TIMEZONE expression)
    | ^(TZ expression)
    | NOW
    | ^(MD5 expression)
    | ^(SHA1 expression)
    | ^(SHA224 expression)
    | ^(SHA256 expression)
    | ^(SHA384 expression)
    | ^(SHA512 expression)
    | ^(COALESCE expressionList)
    | ^(IF expression expression expression)
    | ^(STRLANG expression expression)
    | ^(STRDT expression expression)
    | ^(SAMETERM expression expression)
    | ^(ISIRI expression)
    | ^(ISURI expression)
    | ^(ISBLANK expression) 
    | ^(ISLITERAL expression)
    | ^(ISNUMERIC expression)
    | regexExpression
    | existsFunction
    | notExistsFunction
    ;

regexExpression
    : ^(REGEX expression+)
    ;
    
subStringExpression
    : ^(SUBSTR expression+)
    ;
    
existsFunction
    : ^(EXISTS groupGraphPattern)
    ;

notExistsFunction
    : ^(NOT_EXISTS groupGraphPattern)
    ;

aggregate
    : ^(COUNT DISTINCT* ASTERISK* expression*)
    | ^(SUM DISTINCT* expression)
    | ^(MIN DISTINCT* expression)
    | ^(MAX DISTINCT* expression)
    | ^(AVG DISTINCT* expression)
    | ^(SAMPLE DISTINCT? expression)
    | ^(GROUP_CONCAT DISTINCT* expression string?)
    ;
    
iriRefOrFunction returns [Expr value]
    : a=iriRef                                   {$value = NodeValue.makeNode($a.value);} 
    | ^(FUNCTION a=iriRef ^(ARG_LIST b=argList)) {$value = new E_Function(a.toString(), b);}
    ;

rdfLiteral returns [Node value]
    : ^(PLAIN_LITERAL a=string b=LANGTAG?) {$value = Node.createLiteral($a.value, $b.text, null);}
    | ^(TYPED_LITERAL a=string c=iriRef) {$value = Node.createLiteral($a.value, null, TypeMapper.getInstance().getSafeTypeByName($c.value == null ? null : $c.value.toString()));}
    
    //: a=string ( b=LANGTAG | ( REFERENCE c=iriRef ) )? { $value = Node.createLiteral($a.value, $b.text, TypeMapper.getInstance().getSafeTypeByName($c.value == null ? null : $c.value.toString()));}
    ;

numericLiteral returns [ NodeValue value ]
    : a=numericLiteralUnsigned { $value = $a.value; } 
    | a=numericLiteralPositive { $value = $a.value; }
    | a=numericLiteralNegative { $value = $a.value; }
    ;

numericLiteralUnsigned returns [ NodeValue value ]
    : a=INTEGER { $value=NodeValue.makeInteger($a.text); }
    | a=DECIMAL { $value=NodeValue.makeDecimal($a.text); }
    | a=DOUBLE  { $value=NodeValue.makeDouble(Double.parseDouble($a.text)); }
    ;

numericLiteralPositive returns [ NodeValue value ]
    : a=INTEGER_POSITIVE { $value=NodeValue.makeInteger($a.text); }
    | a=DECIMAL_POSITIVE { $value=NodeValue.makeDecimal($a.text); }
    | a=DOUBLE_POSITIVE  { $value=NodeValue.makeDouble(Double.parseDouble($a.text)); }
    ;

numericLiteralNegative returns [ NodeValue value ]
    : a=INTEGER_NEGATIVE { $value=NodeValue.makeInteger($a.text); }
    | a=DECIMAL_NEGATIVE { $value=NodeValue.makeDecimal($a.text); }
    | a=DOUBLE_NEGATIVE  { $value=NodeValue.makeDouble(Double.parseDouble($a.text)); }
    ;

booleanLiteral returns [ NodeValue value ]
    : TRUE  { $value = NodeValue.TRUE; }
    | FALSE { $value = NodeValue.FALSE; }
    ;

string returns [String value]
    : a=STRING_LITERAL1      {$value = $a.text;}
    | a=STRING_LITERAL2      {$value = $a.text;}
    | a=STRING_LITERAL_LONG1 {$value = $a.text;}
    | a=STRING_LITERAL_LONG2 {$value = $a.text;}
    ;

iriRef returns [Node value]
    : a=IRI_REF      {$value = Node.createURI($a.text);} 
    | b=prefixedName {$value = $b.value;}
    ;

prefixedName returns [Node value]
    : a=PNAME_LN  {$value = Node.createURI(expandUri($a.text));}
    | a=PNAME_NS  {$value = Node.createURI(expandPrefix($a.text));}
    ;

blankNode returns [Node value]
    : a=BLANK_NODE_LABEL {$value = Node.createAnon(new AnonId($a.text));}
    | anon {$value = Node.createAnon();}
    ;

anon
    : OPEN_SQUARE_BRACKET CLOSE_SQUARE_BRACKET
    ;	
// $>

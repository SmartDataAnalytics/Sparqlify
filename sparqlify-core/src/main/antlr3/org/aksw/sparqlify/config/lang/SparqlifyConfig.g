/*
 *    Copyright 2007-2011 The sparkle-g Team
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
 * @version $Id$
 */
grammar SparqlifyConfig;

options{
output = AST;
ASTLabelType=CommonTree;
}



tokens{
VIEW_DEFINITION;
VAR_BINDING;
VAR_BINDINGS;
SQL_RELATION;
SQL_TABLE;
SQL_QUERY;
PREFIX_DECL;
MACRO_DEF;
REGEX;
CONSTRAINTS;
REGEX_CONSTRAINT;
PREFIX_CONSTRAINT;

STRING_LIST;

TEMPLATE;
VIEW_TEMPLATE_DEFINITION;
NAMED_VIEW_TEMPLATE_DEFINITION;
CONSTRUCT_VIEW_DEFINITION;


QUERY;
UPDATE;
PROLOGUE;
MODIFY;
SUBSELECT;
SELECT_CLAUSE;
WHERE_CLAUSE;
VAR;
GROUP_BY;
ORDER_BY;
ORDER_CONDITION;
BINDING_VALUE;
TRIPLES_TEMPLATE;
TRIPLES_BLOCK;
GROUP_GRAPH_PATTERN;
ARG_LIST;
EXPRESSION_LIST;
CONSTRUCT_TRIPLES;
PROPERTY_LIST;
COLLECTION;
TRIPLE;
SUBJECT;
PREDICATE;
OBJECT;
NOT_EXISTS;
FUNCTION;
PATH;
PATH_PRIMARY;
PATH_NEGATED;
UNARY_NOT;
UNARY_PLUS;
UNARY_MINUS;
UNARY;

FUNCTION_DECLARATION;
FUNCTION_SIGNATURE;
PARAM_TYPE;
PARAM_TYPE_LIST;
FUNCTION_TEMPLATE;


TODO;
}

@header {
    package org.aksw.sparqlify.config.lang;
    
    import org.slf4j.Logger;
}

@lexer::header {
    package org.aksw.sparqlify.config.lang;
}


@members {
    private Logger logger = null;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void emitErrorMessage(String msg) {
        if(this.logger != null) {
        	this.logger.error(msg);
        } else {
        	System.err.println(msg);
        }
    }

	public String getErrorMessage(RecognitionException e, String[] tokenNames)
	{
	    List stack = getRuleInvocationStack(e, this.getClass().getName());
	    String msg = null;
	    if ( e instanceof NoViableAltException ) {
	       NoViableAltException nvae = (NoViableAltException)e;
	       msg = " no viable alt; token="+e.token+
	          " (decision="+nvae.decisionNumber+
	          " state "+nvae.stateNumber+")"+
	          " decision=<<"+nvae.grammarDecisionDescription+">>";
	    }
	    else if(  e instanceof FailedPredicateException  ) {
	       FailedPredicateException fpe = (FailedPredicateException)e;
	       msg = "failed predicate; token="+fpe.token+
	              " (rule="+fpe.ruleName+" predicate="+fpe.predicateText+")";
	    }
	    else {
	       msg = super.getErrorMessage(e, tokenNames);
	    }
	    return stack+" "+msg;
	}
	public String getTokenErrorDisplay(Token t) {
	    return t.toString();
	}   
}


// $<Parser


//     $<SparqlifyGrammar

sparqlifyConfig
    : sparqlifyConfigStmt+ EOF!
    ;


constructViewConfig
    : constructViewConfigStmt+ EOF!
    ;

constructViewConfigStmt 
    : viewDefStmtConstruct
    | prefixDefStmt
//    | macroStmt
//    | importStmt
    ;


sparqlifyConfigStmt 
    : viewDefStmt
    | prefixDefStmt
    | macroStmt
    | functionDeclarationStmt
//    | importStmt
    ;


/*
 * Function declaration extension
 *
 *
 * Example:
 *     DECLARE FUNCTION ogc:intersects(geometry ?a, geometry ?b) AS ST_INTERSECTS(?a, ?b)
 */
functionDeclarationStmt
    : DECLARE FUNCTION functionSignature AS functionTemplate ';'?
        -> ^(FUNCTION_DECLARATION functionSignature functionTemplate)
    ;

functionSignature
    : iriRef '(' parameterTypeList? ')'
        -> ^(FUNCTION_SIGNATURE iriRef parameterTypeList?)
    ;

parameterTypeList
    : parameterType (COMMA parameterType)*
        -> ^(PARAM_TYPE_LIST parameterType+)
    ;

parameterType
    : NAME var
        -> ^(PARAM_TYPE NAME var)
    ;
 
functionTemplate
    : NAME expressionList
        -> ^(FUNCTION_TEMPLATE NAME expressionList)
    ;




templateConfig
	: templateConfigStmt+ EOF!
	;
	
templateConfigStmt
	: viewTemplateDefStmt
	| prefixDefStmt
	;

importStmt
    : IMPORT iriOrFileRef
    ;

iriOrFileRef
    : iriRef
    ;

viewDefStmt
    : CREATE VIEW NAME AS CONSTRUCT viewTemplateDef varConstraintsClause? (FROM relationRef)? ';'?
    	-> ^(VIEW_DEFINITION NAME viewTemplateDef varConstraintsClause? relationRef?)
    ;

viewDefStmtConstruct
    : CREATE CONSTRUCT VIEW NAME AS SQL_QUERY ';'?
    	-> ^(CONSTRUCT_VIEW_DEFINITION NAME SQL_QUERY)
    ;

//a=SQL_QUERY -> ^(SQL_RELATION SQL_QUERY[$a])


varConstraintsClause
    : CONSTRAIN varConstraint+
    	-> ^(CONSTRAINTS varConstraint*)
    ;

varConstraint
    : regexValueConstraint
    | prefixValueConstraint
    ;


/**
 * Example: ?s regex "^http://linkedgeodata.org/ontology/*"
 *
 */    
regexValueConstraint
    //: var (memberAccess)? REGEX string
    : var REGEX string
        -> ^(REGEX_CONSTRAINT var string)
    ;

prefixValueConstraint
    : var PREFIX stringList
        -> ^(PREFIX_CONSTRAINT var stringList)
    ;

stringList
    : string (COMMA? string)*
    	-> ^(STRING_LIST string*)
    ;


memberAccess
    : '.' NAME
    ;

viewTemplateDefStmt
    : CREATE VIEW TEMPLATE NAME AS CONSTRUCT viewTemplateDef ';'?
    	-> ^(NAMED_VIEW_TEMPLATE_DEFINITION NAME viewTemplateDef)
    ;

viewTemplateDef
	: constructTemplate varBindingPart?
		-> ^(VIEW_TEMPLATE_DEFINITION constructTemplate varBindingPart?)
	;


prefixDefStmt
    : prefixDecl
    ;
    
macroStmt
    : DEFINE expression -> ^(MACRO_DEF expression)
    ;

relationRef
    : a=SQL_QUERY -> ^(SQL_RELATION SQL_QUERY[$a])
    | a=NAME -> ^(SQL_RELATION SQL_TABLE[$a])
    ;


varBindingPart
    : WITH varBinding+ -> ^(VAR_BINDINGS varBinding+)
    ;

/*
varBinding
    : expression ';' -> ^(VAR_BINDING expression)
    ;	
*/

varBinding
    : var '=' typeCtorExpression -> ^(VAR_BINDING var typeCtorExpression)
    ;	

typeCtorExpression
    : BNODE '(' expression ')' -> ^(BNODE expression)
    | URI '(' expression ')' -> ^(URI expression)
    | PLAIN_LITERAL '(' expression (',' expression)? ')' -> ^(PLAIN_LITERAL expression expression?)
    | TYPED_LITERAL '(' expression ',' expression ')' -> ^(TYPED_LITERAL expression expression)
    ;



//     $>

query
    : prologue ( selectQuery | constructQuery | describeQuery | askQuery ) bindingsClause EOF -> ^(QUERY prologue selectQuery* constructQuery* describeQuery* askQuery*) bindingsClause*
    | update ( SEMICOLON update)* EOF -> ^(UPDATE update+)
    ;

prologue
    : (baseDecl | prefixDecl)* -> ^(PROLOGUE baseDecl* prefixDecl*)
    ;

baseDecl
    : BASE IRI_REF -> ^(BASE IRI_REF)
    ;

prefixDecl
    : PREFIX PNAME_NS IRI_REF -> ^(PREFIX PNAME_NS IRI_REF)
    ;

selectQuery
    : selectClause datasetClause* whereClause solutionModifier -> ^(SELECT selectClause datasetClause* whereClause* solutionModifier*)
    ;

subSelect
    : selectClause whereClause solutionModifier	-> ^(SUBSELECT whereClause* solutionModifier)
    ;
    	
selectClause
    : SELECT ( DISTINCT | REDUCED )? ASTERISK -> ^(SELECT_CLAUSE DISTINCT* REDUCED* ASTERISK)
    | SELECT ( DISTINCT | REDUCED )? (v+=selectVariables)+ -> ^(SELECT_CLAUSE DISTINCT* REDUCED* $v*)
    ;

selectVariables
    : v=var -> ^(VAR $v) 
    | OPEN_BRACE e=expression AS v=var CLOSE_BRACE -> ^(AS $e $v)
    ;
  
constructQuery
    : CONSTRUCT constructTemplate datasetClause* whereClause solutionModifier -> ^(CONSTRUCT constructTemplate* datasetClause* whereClause* solutionModifier*)
    | CONSTRUCT datasetClause* WHERE OPEN_CURLY_BRACE triplesTemplate? CLOSE_CURLY_BRACE solutionModifier -> ^(CONSTRUCT datasetClause* ^(WHERE triplesTemplate*) solutionModifier*)
    ;

describeQuery
    : DESCRIBE ( (v+=varOrIRIref)+ | ASTERISK ) datasetClause* whereClause? solutionModifier -> ^(DESCRIBE $v* ASTERISK* datasetClause* whereClause? solutionModifier*)
    ;

askQuery
    : ASK datasetClause* whereClause -> ^(ASK datasetClause* whereClause)
    ;

datasetClause
    : FROM NAMED? iriRef -> ^(FROM NAMED? iriRef)
    ;

whereClause
    : WHERE? groupGraphPattern -> ^(WHERE_CLAUSE groupGraphPattern)
    ;

solutionModifier
    : groupClause? havingClause? orderClause? limitOffsetClauses? -> groupClause* havingClause* orderClause* limitOffsetClauses*
    ;

groupClause
    : GROUP BY groupCondition+ -> ^(GROUP_BY groupCondition+)
    ;
    		 
groupCondition
    : builtInCall
    | functionCall
    | OPEN_BRACE expression (AS var)? CLOSE_BRACE -> ^(AS expression var*)
    | var
    ;
    
havingClause
    : HAVING constraint+ -> ^(HAVING constraint+)	
    ;
    
orderClause
    : ORDER BY orderCondition+ -> ^(ORDER_BY orderCondition+)
    ;

orderCondition
    : ASC brackettedExpression -> ^(ORDER_CONDITION ASC brackettedExpression)
    | DESC brackettedExpression -> ^(ORDER_CONDITION DESC brackettedExpression)
    | constraint -> ^(ORDER_CONDITION constraint)
    | var -> ^(ORDER_CONDITION var)
    ;
	    
limitOffsetClauses
    : limitClause offsetClause? -> limitClause offsetClause*
    | offsetClause limitClause? -> offsetClause limitClause*
    ;

limitClause
    : LIMIT INTEGER -> ^(LIMIT INTEGER)
    ;

offsetClause
    : OFFSET INTEGER -> ^(OFFSET INTEGER)
    ;

bindingsClause
    : (BINDINGS var* OPEN_CURLY_BRACE bindingValueList* CLOSE_CURLY_BRACE)? -> ^(BINDINGS var* bindingValueList*)?
    ;
    
bindingValueList
    : OPEN_BRACE bindingValue* CLOSE_BRACE -> ^(BINDING_VALUE bindingValue*)
    ;
    	
bindingValue
    : iriRef | rdfLiteral | numericLiteral | booleanLiteral | UNDEF
    ;
    
update
    : prologue (load | clear | drop | add | move | copy | create | insert | delete | modify) -> prologue load* clear* drop* add* move* copy* create* insert* delete* modify*
    ;   
    
load 	  
    : LOAD SILENT? iriRef ( INTO graphRef )? -> ^(LOAD SILENT* iriRef graphRef*)
    ;
    
clear
    : CLEAR  SILENT? graphRefAll -> ^(CLEAR SILENT* graphRefAll)
    ;
    
drop
    : DROP SILENT? graphRefAll -> ^(DROP SILENT* graphRefAll)
    ; 

create
    : CREATE SILENT? graphRef -> ^(CREATE SILENT* graphRef)
    ;
    
add
    : ADD SILENT? from=graphOrDefault TO to=graphOrDefault -> ^(ADD SILENT* $from $to)
    ;
    
move
    : MOVE SILENT? from=graphOrDefault TO to=graphOrDefault -> ^(MOVE SILENT* $from $to)
    ;
    
copy
    : COPY SILENT? from=graphOrDefault TO to=graphOrDefault -> ^(COPY SILENT* $from $to)
    ;

insert
    : INSERT DATA quadPattern -> ^(INSERT DATA quadPattern)
    ;
   
delete 	  
    : DELETE ( deleteData | deleteWhere )
    ;

deleteData
    : DATA quadPattern -> ^(DELETE DATA quadPattern)
    ;

deleteWhere
    : WHERE quadPattern -> ^(DELETE WHERE quadPattern)
    ;
    
modify
    : ( WITH i=iriRef )? (deleteClause insertClause? | insertClause) usingClause* WHERE groupGraphPattern -> ^(MODIFY ^(WITH $i)? deleteClause* insertClause* usingClause* ^(WHERE groupGraphPattern))
    ;
  
deleteClause
    : DELETE quadPattern -> DELETE quadPattern
    ;
      
insertClause
    : INSERT quadPattern -> INSERT quadPattern
    ;

usingClause
    : USING NAMED? iriRef -> ^(USING NAMED? iriRef)
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

quadPattern
    : OPEN_CURLY_BRACE quads CLOSE_CURLY_BRACE -> quads
    ;
    
quads
    : triplesTemplate? ( quadsNotTriples DOT? triplesTemplate? )* ->  triplesTemplate? ( quadsNotTriples triplesTemplate? )* 
    ;
    
quadsNotTriples
    : GRAPH varOrIRIref OPEN_CURLY_BRACE triplesTemplate? CLOSE_CURLY_BRACE -> ^(GRAPH varOrIRIref triplesTemplate?)
    ;
    
triplesTemplate
    : triplesSameSubject ( DOT triplesSameSubject )* DOT? -> ^(TRIPLES_TEMPLATE triplesSameSubject*)
    ;
    	
groupGraphPattern
    : OPEN_CURLY_BRACE ( subSelect | groupGraphPatternSub ) CLOSE_CURLY_BRACE -> ^(GROUP_GRAPH_PATTERN subSelect* groupGraphPatternSub*)
    ;
    
groupGraphPatternSub
    : triplesBlock? ( groupGraphPatternSubCache )* -> triplesBlock? groupGraphPatternSubCache*
    ;

groupGraphPatternSubCache
    :  graphPatternNotTriples DOT? triplesBlock? -> graphPatternNotTriples triplesBlock?
    ; 	

triplesBlock
    : triplesSameSubjectPath ( DOT triplesSameSubjectPath)* DOT? -> ^(TRIPLES_BLOCK triplesSameSubjectPath+)
    ;

graphPatternNotTriples
    : groupOrUnionGraphPattern | optionalGraphPattern | minusGraphPattern | graphGraphPattern | serviceGraphPattern | filter | bind
    ;

optionalGraphPattern
    : OPTIONAL groupGraphPattern -> ^(OPTIONAL groupGraphPattern)
    ;

graphGraphPattern
    : GRAPH varOrIRIref groupGraphPattern -> ^(GRAPH varOrIRIref groupGraphPattern)
    ;

serviceGraphPattern
    : SERVICE SILENT? varOrIRIref groupGraphPattern -> ^(SERVICE SILENT? varOrIRIref groupGraphPattern)
    ;
    
bind
    : BIND OPEN_BRACE expression AS var CLOSE_BRACE -> ^(BIND expression ^(AS var))	
    ;
    	
minusGraphPattern
    : MINUS_KEYWORD groupGraphPattern -> ^(MINUS_KEYWORD groupGraphPattern)
    ;

groupOrUnionGraphPattern
    : (g1=groupGraphPattern->$g1) ((UNION g2=groupGraphPattern) -> ^(UNION $groupOrUnionGraphPattern $g2))*
    ;

filter
    : FILTER constraint -> ^(FILTER constraint)
    ;

constraint
    : brackettedExpression
    | builtInCall
    | functionCall
    ;

functionCall
    : iriRef argList -> ^(FUNCTION iriRef ^(ARG_LIST argList))
    ;

argList
    : nil -> nil
    | OPEN_BRACE DISTINCT? expression ( COMMA expression )* CLOSE_BRACE -> DISTINCT? expression*
    ;


expressionList
    : ( nil | OPEN_BRACE expression ( COMMA expression )* CLOSE_BRACE ) -> ^(EXPRESSION_LIST nil* expression*)
    ;	

/*
expressionList
    : ( OPEN_BRACE expression CLOSE_BRACE ) -> ^(EXPRESSION_LIST expression)
    ;	
*/

constructTemplate
    : OPEN_CURLY_BRACE constructTriples? CLOSE_CURLY_BRACE -> ^(CONSTRUCT_TRIPLES constructTriples?)
    ;

constructTriples
    : triplesSameSubject ( DOT triplesSameSubject )* DOT? -> triplesSameSubject+
    ;

/**
 * FIXME I have no idea how I figured out that you can use {someTreeNodeObject} to emit custom nodes into the AST
 * Furthermore, I am not sure how safe this dupNode() approach is (the trees need of course to be cloned!)
 */
triplesSameSubject
    : varOrTerm propertyListNotEmpty[(CommonTree) $varOrTerm.tree] -> propertyListNotEmpty
    | (t=triplesNode -> $t) (p=propertyListNotEmpty[(CommonTree) $t.tree]? -> ^(TRIPLE $triplesSameSubject $p?))
    ;

propertyListNotEmpty[CommonTree subject]
    : v=verb objectList[subject, (CommonTree) $v.tree] (SEMICOLON (v=verb objectList[subject, (CommonTree) $v.tree])?)* -> objectList+ // ^(TRIPLE objectList)+
    ;

objectList[CommonTree subject, CommonTree predicate]
    : graphNode ( COMMA graphNode )* -> ^(TRIPLE ^(SUBJECT {subject.dupNode()}) ^(PREDICATE {predicate.dupNode()}) ^(OBJECT graphNode) )+
    ;


/*
propertyListNotEmpty[CommonTree subject]
    : v=verb objectList[subject, (CommonTree) $v.tree] (SEMICOLON (v=verb objectList[subject, (CommonTree) $v.tree])?)* -> ^(TRIPLE objectList)+
    ;

objectList[CommonTree subject, CommonTree predicate]
    : graphNode ( COMMA graphNode )* -> (^(SUBJECT {subject}) ^(PREDICATE {predicate}) ^(OBJECT graphNode))+
    ;
*/

verb
    : varOrIRIref
    | A
    ;

triplesSameSubjectPath
    : varOrTerm propertyListNotEmptyPath[(CommonTree) $varOrTerm.tree] -> ^(TRIPLE propertyListNotEmptyPath)
    | (t=triplesNode -> $t) (p=propertyListNotEmpty[(CommonTree) $t.tree]? -> ^(TRIPLE $triplesSameSubjectPath $p?))
    ;
  
propertyListNotEmptyPath[CommonTree subject]
    : (p=verbPath  objectList[subject, (CommonTree) $p.tree]| v=verbSimple objectList[subject, (CommonTree) $v.tree]) (SEMICOLON (p=verbPath objectList[subject, (CommonTree) $p.tree]  | v=verbSimple  objectList[subject, (CommonTree) $v.tree] )?)* -> objectList+
    ;
    
verbPath
    : path
    ;
    
verbSimple
    : var
    ;
    	
path
    : pathSequence ( PIPE pathSequence )* -> PATH pathSequence ( PIPE pathSequence )*
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
    : iriRef -> ^(PATH_PRIMARY iriRef)
    | A -> ^(PATH_PRIMARY A)
    | NEGATION pathNegatedPropertySet -> ^(PATH_PRIMARY NEGATION pathNegatedPropertySet)
    | OPEN_BRACE path CLOSE_BRACE -> ^(PATH_PRIMARY path)
    ;

pathNegatedPropertySet
    : (pathOneInPropertySet | OPEN_BRACE ( pathOneInPropertySet ( PIPE pathOneInPropertySet )* )? CLOSE_BRACE) -> ^(PATH_NEGATED pathOneInPropertySet+)
    ;  	

pathOneInPropertySet
    : INVERSE? ( iriRef | A )
    ;
	
triplesNode
    : OPEN_BRACE graphNode+ CLOSE_BRACE -> ^(COLLECTION graphNode+)
    | lsb=OPEN_SQUARE_BRACKET propertyListNotEmpty[new CommonTree(new CommonToken(VAR,"[]"))] CLOSE_SQUARE_BRACKET -> ^(TRIPLE propertyListNotEmpty)
    ;

graphNode
    : varOrTerm | triplesNode
    ;

varOrTerm
    : var | graphTerm
    ;

varOrIRIref
    : var | iriRef
    ;

var
    : v=VAR1 -> ^(VAR[$v])
    | v=VAR2 -> ^(VAR[$v])
//    | v=VAR2 -> ^(VAR $v) 
    ;

graphTerm
    : iriRef
    | rdfLiteral
    | numericLiteral
    | booleanLiteral
    | blankNode
    | nil
    ;
    
nil
    : OPEN_BRACE CLOSE_BRACE
    ;

expression
    : conditionalOrExpression
    ;

conditionalOrExpression
    : (c1=conditionalAndExpression -> $c1) (OR c2=conditionalAndExpression -> ^(OR $conditionalOrExpression $c2))*
    ;

conditionalAndExpression
    : (v1=valueLogical -> $v1) (AND v2=valueLogical -> ^(AND $conditionalAndExpression $v2))*
    ;

valueLogical
    : relationalExpression
    ;

relationalExpression
    : (n1=numericExpression -> $n1) ( (EQUAL n2=numericExpression -> ^(EQUAL $relationalExpression $n2))   
                                    | (NOT_EQUAL n3=numericExpression -> ^(NOT_EQUAL $relationalExpression $n3)) 
                                    | (LESS n4=numericExpression -> ^(LESS $relationalExpression $n4)) 
                                    | (GREATER n5=numericExpression -> ^(GREATER $relationalExpression $n5))
                                    | (LESS_EQUAL n6=numericExpression -> ^(LESS_EQUAL $relationalExpression $n6))
                                    | (GREATER_EQUAL n7=numericExpression -> ^(GREATER_EQUAL $relationalExpression $n7))  
                                    | (IN l2=expressionList -> ^(IN $relationalExpression $l2))
                                    | (NOT IN l3=expressionList -> ^(NOT IN $relationalExpression $l3)) )?
    ;

numericExpression
    : additiveExpression
    ;

additiveExpression
    : (m1=multiplicativeExpression -> $m1) ( (additiveOperator m2=multiplicativeExpression -> ^(additiveOperator $additiveExpression $m2))  
                                             | (n1=numericLiteralPositive -> ^(PLUS $additiveExpression $n1) | n2=numericLiteralNegative -> ^(PLUS $additiveExpression $n2) ) 
                                             ( ( (ASTERISK u2=unaryExpression -> ^(ASTERISK $additiveExpression $u2)) ) 
                                             | ( (DIVIDE u2=unaryExpression -> ^(DIVIDE $additiveExpression $u2))) )? )*
    ; 
    
additiveOperator
    : PLUS|MINUS
    ;
    
multiplicativeExpression
    : (u1=unaryExpression -> $u1) (multiplicativeOperator u2=unaryExpression -> ^(multiplicativeOperator $multiplicativeExpression $u2))*
    ;
    
multiplicativeOperator
    : ASTERISK | DIVIDE
    ;

unaryExpression
    : op=NEGATION primaryExpression -> ^(UNARY_NOT[$op] primaryExpression)
    | op=PLUS primaryExpression -> ^(UNARY_PLUS[$op] primaryExpression)
    | op=MINUS primaryExpression -> ^(UNARY_MINUS[$op] primaryExpression)
//    | primaryExpression
    | primaryExpression -> ^(UNARY primaryExpression)
    ;

primaryExpression
    : brackettedExpression | builtInCall | iriRefOrFunction | rdfLiteral | numericLiteral | booleanLiteral | var | aggregate
    ;

brackettedExpression
    : OPEN_BRACE expression CLOSE_BRACE -> expression
    ;

builtInCall
    : STR OPEN_BRACE expression CLOSE_BRACE -> ^(STR expression)
    | LANG OPEN_BRACE expression CLOSE_BRACE -> ^(LANG expression)
    | LANGMATCHES OPEN_BRACE expression COMMA expression CLOSE_BRACE -> ^(LANGMATCHES expression+)
    | DATATYPE OPEN_BRACE expression CLOSE_BRACE -> ^(DATATYPE expression)
    | BOUND OPEN_BRACE var CLOSE_BRACE -> ^(BOUND var)
    | IRI OPEN_BRACE expression CLOSE_BRACE -> ^(IRI expression)
    | URI OPEN_BRACE expression CLOSE_BRACE -> ^(URI expression)
    | BNODE (OPEN_BRACE expression CLOSE_BRACE| nil) -> ^(BNODE expression)
    | RAND nil -> RAND
    | ABS OPEN_BRACE expression CLOSE_BRACE -> ^(ABS expression)
    | CEIL OPEN_BRACE expression CLOSE_BRACE -> ^(CEIL expression)
    | FLOOR OPEN_BRACE expression CLOSE_BRACE -> ^(FLOOR expression)
    | ROUND OPEN_BRACE expression CLOSE_BRACE -> ^(ROUND expression)
    | CONCAT expressionList -> ^(CONCAT expressionList)
    | subStringExpression -> subStringExpression
    | STRLEN OPEN_BRACE expression CLOSE_BRACE -> ^(STRLEN expression)
    | UCASE OPEN_BRACE expression CLOSE_BRACE -> ^(UCASE expression)
    | LCASE OPEN_BRACE expression CLOSE_BRACE -> ^(LCASE expression)
    | ENCODE_FOR_URI OPEN_BRACE expression CLOSE_BRACE -> ^(ENCODE_FOR_URI expression)
    | CONTAINS OPEN_BRACE expression CLOSE_BRACE -> ^(CONTAINS expression)
    | STRSTARTS OPEN_BRACE expression CLOSE_BRACE -> ^(STRSTARTS expression)
    | STRENDS OPEN_BRACE expression CLOSE_BRACE -> ^(STRENDS expression)
    | YEAR OPEN_BRACE expression CLOSE_BRACE -> ^(YEAR expression)
    | MONTH OPEN_BRACE expression CLOSE_BRACE -> ^(MONTH expression)
    | DAY OPEN_BRACE expression CLOSE_BRACE -> ^(DAY expression)
    | HOURS OPEN_BRACE expression CLOSE_BRACE -> ^(HOURS expression)
    | MINUTES OPEN_BRACE expression CLOSE_BRACE -> ^(MINUTES expression)
    | SECONDS OPEN_BRACE expression CLOSE_BRACE -> ^(SECONDS expression)
    | TIMEZONE OPEN_BRACE expression CLOSE_BRACE -> ^(TIMEZONE expression)
    | TZ OPEN_BRACE expression CLOSE_BRACE -> ^(TZ expression)
    | NOW nil -> NOW
    | MD5 OPEN_BRACE expression CLOSE_BRACE -> ^(MD5 expression)
    | SHA1 OPEN_BRACE expression CLOSE_BRACE -> ^(SHA1 expression)
    | SHA224 OPEN_BRACE expression CLOSE_BRACE -> ^(SHA224 expression)
    | SHA256 OPEN_BRACE expression CLOSE_BRACE -> ^(SHA256 expression)
    | SHA384 OPEN_BRACE expression CLOSE_BRACE -> ^(SHA384 expression)
    | SHA512 OPEN_BRACE expression CLOSE_BRACE -> ^(SHA512 expression)
    | COALESCE expressionList -> ^(COALESCE expressionList)
    | IF OPEN_BRACE e1=expression COMMA e2=expression COMMA e3=expression CLOSE_BRACE -> ^(IF $e1 $e2 $e3)
    | STRLANG OPEN_BRACE expression COMMA expression CLOSE_BRACE -> ^(STRLANG expression expression)
    | STRDT OPEN_BRACE expression COMMA expression CLOSE_BRACE -> ^(STRDT expression expression)
    | SAMETERM OPEN_BRACE expression COMMA expression CLOSE_BRACE -> ^(SAMETERM expression expression)
    | ISIRI OPEN_BRACE expression CLOSE_BRACE -> ^(ISIRI expression)
    | ISURI OPEN_BRACE expression CLOSE_BRACE -> ^(ISURI expression)
    | ISBLANK OPEN_BRACE expression CLOSE_BRACE -> ^(ISBLANK expression) 
    | ISLITERAL OPEN_BRACE expression CLOSE_BRACE -> ^(ISLITERAL expression)
    | ISNUMERIC OPEN_BRACE expression CLOSE_BRACE -> ^(ISNUMERIC expression)
    | regexExpression -> regexExpression
    | existsFunction -> existsFunction
    | notExistsFunction -> notExistsFunction
    ;

regexExpression
    : REGEX OPEN_BRACE expression COMMA expression ( COMMA expression )? CLOSE_BRACE -> ^(REGEX expression*)
    ;
    
subStringExpression
    : SUBSTR OPEN_BRACE expression COMMA expression ( COMMA expression )? CLOSE_BRACE -> ^(SUBSTR expression*)
    ;
    
existsFunction
    : EXISTS groupGraphPattern -> ^(EXISTS groupGraphPattern)
    ;

notExistsFunction
    : NOT EXISTS groupGraphPattern -> ^(NOT_EXISTS groupGraphPattern)
    ;

aggregate
    : COUNT OPEN_BRACE DISTINCT? ( ASTERISK | expression ) CLOSE_BRACE -> ^(COUNT DISTINCT* ASTERISK* expression*)
    | SUM OPEN_BRACE DISTINCT? expression CLOSE_BRACE -> ^(SUM DISTINCT* expression)
    | MIN OPEN_BRACE DISTINCT? expression CLOSE_BRACE -> ^(MIN DISTINCT* expression)
    | MAX OPEN_BRACE DISTINCT? expression CLOSE_BRACE -> ^(MAX DISTINCT* expression)
    | AVG OPEN_BRACE DISTINCT? expression CLOSE_BRACE -> ^(AVG DISTINCT* expression)
    | SAMPLE OPEN_BRACE DISTINCT? expression CLOSE_BRACE -> ^(SAMPLE DISTINCT? expression)
    | GROUP_CONCAT OPEN_BRACE DISTINCT? expression ( SEMICOLON SEPARATOR EQUAL string )? CLOSE_BRACE -> ^(GROUP_CONCAT DISTINCT* expression string*)
    ;
    
iriRefOrFunction
    : iriRef 
    | iriRef argList -> ^(FUNCTION iriRef ^(ARG_LIST argList))
    ;

rdfLiteral
    : string LANGTAG? -> ^(PLAIN_LITERAL string LANGTAG?)
    | string REFERENCE iriRef -> ^(TYPED_LITERAL string iriRef)
    ;

numericLiteral
    : numericLiteralUnsigned
    | numericLiteralPositive
    | numericLiteralNegative
    ;

numericLiteralUnsigned
    : INTEGER 
    | DECIMAL
    | DOUBLE
    ;

numericLiteralPositive
    : INTEGER_POSITIVE
    | DECIMAL_POSITIVE
    | DOUBLE_POSITIVE
    ;

numericLiteralNegative
    : INTEGER_NEGATIVE
    | DECIMAL_NEGATIVE
    | DOUBLE_NEGATIVE
    ;

booleanLiteral
    : TRUE
    | FALSE
    ;

string
    : STRING_LITERAL1
    | STRING_LITERAL2
    | STRING_LITERAL_LONG1
    | STRING_LITERAL_LONG2
    ;

iriRef
    : IRI_REF
    | prefixedName
    ;

prefixedName
    : PNAME_LN
    | PNAME_NS
    ;

blankNode
    : BLANK_NODE_LABEL
    | anon
    ;

anon
    : OPEN_SQUARE_BRACKET CLOSE_SQUARE_BRACKET
    ;	
// $>

// $<Lexer

VIEW : ('V'|'v')('I'|'i')('E'|'e')('W'|'w');

DEFINE 	: ('D'|'d')('E'|'e')('F'|'f')('I'|'i')('N'|'n')('E'|'e');

IMPORT 	: ('I'|'i')('M'|'m')('P'|'p')('O'|'o')('R'|'r')('T'|'t');

TEMPLATE : ('T'|'t')('E'|'e')('M'|'m')('P'|'p')('L'|'l')('A'|'a')('T'|'t')('E'|'e');

SQL_QUERY : '[' '[' .* ']' ']' {setText($text.substring(2, $text.length()-2));};

CONSTRAIN 	: ('C'|'c')('O'|'o')('N'|'n')('S'|'s')('T'|'t')('R'|'r')('A'|'a')('I'|'i')('N'|'n');


BLANK_NODE : ('B'|'b')('L'|'l')('A'|'a')('N'|'n')('K'|'k')('N'|'n')('O'|'o')('D'|'d')('E'|'e');

PLAIN_LITERAL : ('P'|'p')('L'|'l')('A'|'a')('I'|'i')('N'|'n')('L'|'l')('I'|'i')('T'|'t')('E'|'e')('R'|'r')('A'|'a')('L'|'l');

TYPED_LITERAL : ('T'|'t')('Y'|'y')('P'|'p')('E'|'e')('D'|'d')('L'|'l')('I'|'i')('T'|'t')('E'|'e')('R'|'r')('A'|'a')('L'|'l');


/* CONSTRAINTS 	: ('C'|'c')('O'|'o')('N'|'n')('S'|'s')('T'|'t')('R'|'r')('A'|'a')('I'|'i')('N'|'n')('T'|'t')('S'|'s'); */


/*
 * Function declaration extension
 */
DECLARE : ('D'|'d')('E'|'e')('C'|'c')('L'|'l')('A'|'a')('R'|'r')('E'|'e');
FUNCTION : ('F'|'f')('U'|'u')('N'|'n')('C'|'c')('T'|'t')('I'|'i')('O'|'o')('N'|'n');




WS : (' '| '\t'| EOL)+ { $channel=HIDDEN; };

BASE : ('B'|'b')('A'|'a')('S'|'s')('E'|'e');

PREFIX : ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');

SELECT : ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');

DISTINCT : ('D'|'d')('I'|'i')('S'|'s')('T'|'t')('I'|'i')('N'|'n')('C'|'c')('T'|'t');

REDUCED : ('R'|'r')('E'|'e')('D'|'d')('U'|'u')('C'|'c')('E'|'e')('D'|'d');

CONSTRUCT : ('C'|'c')('O'|'o')('N'|'n')('S'|'s')('T'|'t')('R'|'r')('U'|'u')('C'|'c')('T'|'t');

DESCRIBE : ('D'|'d')('E'|'e')('S'|'s')('C'|'c')('R'|'r')('I'|'i')('B'|'b')('E'|'e');

ASK : ('A'|'a')('S'|'s')('K'|'k');

FROM : ('F'|'f')('R'|'r')('O'|'o')('M'|'m');

NAMED : ('N'|'n')('A'|'a')('M'|'m')('E'|'e')('D'|'d');   

WHERE : ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

ORDER : ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r');

BY : ('B'|'b')('Y'|'y');

ASC : ('A'|'a')('S'|'s')('C'|'c');

DESC : ('D'|'d')('E'|'e')('S'|'s')('C'|'c');

LIMIT : ('L'|'l')('I'|'i')('M'|'m')('I'|'i')('T'|'t');

OFFSET : ('O'|'o')('F'|'f')('F'|'f')('S'|'s')('E'|'e')('T'|'t');

OPTIONAL : ('O'|'o')('P'|'p')('T'|'t')('I'|'i')('O'|'o')('N'|'n')('A'|'a')('L'|'l');  

GRAPH : ('G'|'g')('R'|'r')('A'|'a')('P'|'p')('H'|'h');   

UNION : ('U'|'u')('N'|'n')('I'|'i')('O'|'o')('N'|'n');

FILTER : ('F'|'f')('I'|'i')('L'|'l')('T'|'t')('E'|'e')('R'|'r');

A : 'a';

STR : ('S'|'s')('T'|'t')('R'|'r');

LANG : ('L'|'l')('A'|'a')('N'|'n')('G'|'g');

LANGMATCHES : ('L'|'l')('A'|'a')('N'|'n')('G'|'g')('M'|'m')('A'|'a')('T'|'t')('C'|'c')('H'|'h')('E'|'e')('S'|'s');

DATATYPE : ('D'|'d')('A'|'a')('T'|'t')('A'|'a')('T'|'t')('Y'|'y')('P'|'p')('E'|'e');

BOUND : ('B'|'b')('O'|'o')('U'|'u')('N'|'n')('D'|'d');

SAMETERM : ('S'|'s')('A'|'a')('M'|'m')('E'|'e')('T'|'t')('E'|'e')('R'|'r')('M'|'m');

ISIRI : ('I'|'i')('S'|'s')('I'|'i')('R'|'r')('I'|'i');

ISURI : ('I'|'i')('S'|'s')('U'|'u')('R'|'r')('I'|'i');

ISBLANK : ('I'|'i')('S'|'s')('B'|'b')('L'|'l')('A'|'a')('N'|'n')('K'|'k');

ISLITERAL : ('I'|'i')('S'|'s')('L'|'l')('I'|'i')('T'|'t')('E'|'e')('R'|'r')('A'|'a')('L'|'l');

REGEX : ('R'|'r')('E'|'e')('G'|'g')('E'|'e')('X'|'x');

SUBSTR : ('S'|'s')('U'|'u')('B'|'b')('S'|'s')('T'|'t')('R'|'r');

TRUE : ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

FALSE : ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

LOAD : ('L'|'l')('O'|'o')('A'|'a')('D'|'d');
    
CLEAR : ('C'|'c')('L'|'l')('E'|'e')('A'|'a')('R'|'r');
    
DROP : ('D'|'d')('R'|'r')('O'|'o')('P'|'p');

ADD : ('A'|'a')('D'|'d')('D'|'d');

MOVE : ('M'|'m')('O'|'o')('V'|'v')('E'|'e');

COPY : ('C'|'c')('O'|'o')('P'|'p')('Y'|'y');	
    
CREATE 	: ('C'|'c')('R'|'r')('E'|'e')('A'|'a')('T'|'t')('E'|'e');
    
DELETE : ('D'|'d')('E'|'e')('L'|'l')('E'|'e')('T'|'t')('E'|'e');
    	
INSERT : ('I'|'i')('N'|'n')('S'|'s')('E'|'e')('R'|'r')('T'|'t');
    
USING : ('U'|'u')('S'|'s')('I'|'i')('N'|'n')('G'|'g');	 
    
SILENT : ('S'|'s')('I'|'i')('L'|'l')('E'|'e')('N'|'n')('T'|'t'); 
    	
DEFAULT : ('D'|'d')('E'|'e')('F'|'f')('A'|'a')('U'|'u')('L'|'l')('T'|'t');  
    
ALL : ('A'|'a')('L'|'l')('L'|'l');

DATA : ('D'|'d')('A'|'a')('T'|'t')('A'|'a');
    
WITH : ('W'|'w')('I'|'i')('T'|'t')('H'|'h');
    
INTO : ('I'|'i')('N'|'n')('T'|'t')('O'|'o'); 

TO : ('T'|'t')('O'|'o'); 	 

AS : ('A'|'a')('S'|'s');

GROUP : ('G'|'g')('R'|'r')('O'|'o')('U'|'u')('P'|'p');
    
HAVING : ('H'|'h')('A'|'a')('V'|'v')('I'|'i')('N'|'n')('G'|'g');

UNDEF : ('U'|'u')('N'|'n')('D'|'d')('E'|'e')('F'|'f');
    
BINDINGS : ('B'|'b')('I'|'i')('N'|'n')('D'|'d')('I'|'i')('N'|'n')('G'|'g')('S'|'s');  

SERVICE : ('S'|'s')('E'|'e')('R'|'r')('V'|'v')('I'|'i')('C'|'c')('E'|'e');

BIND : ('B'|'b')('I'|'i')('N'|'n')('D'|'d');

MINUS_KEYWORD : ('M'|'m')('I'|'i')('N'|'n')('U'|'u')('S'|'s');

IRI : ('I'|'i')('R'|'r')('I'|'i');
    	 
URI : ('U'|'u')('R'|'r')('I'|'i');
    		
BNODE : ('B'|'b')('N'|'n')('O'|'o')('D'|'d')('E'|'e');

RAND : ('R'|'r')('A'|'a')('N'|'n')('D'|'d');

ABS: ('A'|'a')('B'|'b')('S'|'s');

CEIL : ('C'|'c')('E'|'e')('I'|'i')('L'|'l');

FLOOR : ('F'|'f')('L'|'l')('O'|'o')('O'|'o')('R'|'r');

ROUND : ('R'|'r')('O'|'o')('U'|'u')('N'|'n')('D'|'d');

CONCAT 	: ('C'|'c')('O'|'o')('N'|'n')('C'|'c')('A'|'a')('T'|'t');

STRLEN : ('S'|'s')('T'|'t')('R'|'r')('L'|'l')('E'|'e')('N'|'n');

UCASE : ('U'|'u')('C'|'c')('A'|'a')('S'|'s')('E'|'e');

LCASE : ('L'|'l')('C'|'c')('A'|'a')('S'|'s')('E'|'e');	

ENCODE_FOR_URI : ('E'|'e')('N'|'n')('C'|'c')('O'|'o')('D'|'d')('E'|'e')'_'('F'|'f')('O'|'o')('R'|'r')'_'('U'|'u')('R'|'r')('I'|'i');

CONTAINS : ('C'|'c')('O'|'o')('N'|'n')('T'|'t')('A'|'a')('I'|'i')('N'|'n')('S'|'s');

STRSTARTS : ('S'|'s')('T'|'t')('R'|'r')('S'|'s')('T'|'t')('A'|'a')('R'|'r')('T'|'t')('S'|'s');

STRENDS : ('S'|'s')('T'|'t')('R'|'r')('E'|'e')('N'|'n')('D'|'d')('S'|'s');

YEAR : ('Y'|'y')('E'|'e')('A'|'a')('R'|'r');

MONTH : ('M'|'m')('O'|'o')('N'|'n')('T'|'t')('H'|'h');

DAY : ('D'|'d')('A'|'a')('Y'|'y');

HOURS : ('H'|'h')('O'|'o')('U'|'u')('R'|'r')('S'|'s');

MINUTES : ('M'|'m')('I'|'i')('N'|'n')('U'|'u')('T'|'t')('E'|'e')('S'|'s');

SECONDS : ('S'|'s')('E'|'e')('C'|'c')('O'|'o')('N'|'n')('M'|'m')('S'|'s');	

TIMEZONE :  ('T'|'t')('I'|'i')('M'|'m')('E'|'e')('Z'|'z')('O'|'o')('N'|'n')('E'|'e');

TZ : ('T'|'t')('Z'|'z');

NOW : ('N'|'n')('O'|'o')('W'|'w');

MD5 : ('M'|'m')('M'|'m')'5';

SHA1 : ('S'|'s')('H'|'h')('A'|'a')'1';

SHA224 : ('S'|'s')('H'|'h')('A'|'a')'224';

SHA256 : ('S'|'s')('H'|'h')('A'|'a')'256';	

SHA384 : ('S'|'s')('H'|'h')('A'|'a')'384'; 

SHA512 : ('S'|'s')('H'|'h')('A'|'a')'512';
    		 
COALESCE : ('C'|'c')('O'|'o')('A'|'a')('L'|'l')('E'|'e')('S'|'s')('C'|'c')('E'|'e');
    	 
IF : ('I'|'i')('F'|'f');

STRLANG : ('S'|'s')('T'|'t')('R'|'r')('L'|'l')('A'|'a')('N'|'n')('G'|'g');
    
STRDT : ('S'|'s')('T'|'t')('R'|'r')('D'|'d')('T'|'t');
    
ISNUMERIC : ('I'|'i')('S'|'s')('N'|'n')('U'|'u')('M'|'m')('E'|'e')('R'|'r')('I'|'i')('C'|'c');	

COUNT : ('C'|'c')('O'|'o')('U'|'u')('N'|'n')('T'|'t');
    
SUM : ('S'|'s')('U'|'u')('M'|'m');	  
    
MIN : ('M'|'m')('I'|'i')('N'|'n');  
    
MAX : ('M'|'m')('A'|'a')('X'|'x');
     
AVG : ('A'|'a')('V'|'v')('G'|'g');

SAMPLE : ('S'|'s')('A'|'a')('M'|'m')('P'|'p')('L'|'l')('E'|'e');    
 
GROUP_CONCAT  :	 ('G'|'g')('R'|'r')('O'|'o')('U'|'u')('P'|'p') '_' ('C'|'c')('O'|'o')('N'|'n')('C'|'c')('A'|'a')('T'|'t');

NOT : ('N'|'n')('O'|'o')('T'|'t');
    
IN : ('I'|'i')('N'|'n');

EXISTS : ('E'|'e')('X'|'x')('I'|'i')('S'|'s')('T'|'t')('S'|'s');
    
SEPARATOR : ('S'|'s')('E'|'e')('P'|'p')('A'|'a')('R'|'r')('A'|'a')('T'|'t')('O'|'o')('R'|'r');

PNAME_NS : p=PN_PREFIX? { setText($p.text); } ':';

//PNAME_NS : PN_PREFIX? ':';

PNAME_LN : a=PNAME_NS b=PN_LOCAL { setText($a.text + $b.text); };

//PNAME_LN : PNAME_NS PN_LOCAL ;
    
BLANK_NODE_LABEL : '_:' t=PN_LOCAL { setText($t.text); };

VAR1 : QUESTION_MARK v=VARNAME { setText($v.text); };

VAR2 : '$' v=VARNAME { setText($v.text); };



NAME : PN_CHARS_U NAME_SUFFIX ;


LANGTAG : '@' ('A'..'Z'|'a'..'z')+ (MINUS ('A'..'Z'|'a'..'z'|DIGIT)+)*;

INTEGER : DIGIT+;

DECIMAL
    : DIGIT+ DOT DIGIT*
    | DOT DIGIT+
    ;

DOUBLE
    : DIGIT+ DOT DIGIT* EXPONENT
    | DOT DIGIT+ EXPONENT
    | DIGIT+ EXPONENT
    ;

INTEGER_POSITIVE : PLUS INTEGER;

DECIMAL_POSITIVE : PLUS DECIMAL;

DOUBLE_POSITIVE : PLUS DOUBLE;

INTEGER_NEGATIVE : MINUS INTEGER;

DECIMAL_NEGATIVE : MINUS DECIMAL;

DOUBLE_NEGATIVE : MINUS DOUBLE;
    
fragment
EXPONENT : ('e'|'E') SIGN? DIGIT+;

STRING_LITERAL1 : '\'' ( options {greedy=false;} : ~( '\'' | '\\' | EOL ) | ECHAR )* '\'' {setText($text.substring(1, $text.length()-1));};

STRING_LITERAL2 : '"' ( options {greedy=false;} : ~( '"' | '\\' | EOL ) | ECHAR )* '"' {setText($text.substring(1, $text.length()-1));};

STRING_LITERAL_LONG1 : '\'\'\'' ( options {greedy=false;} : ( '\'' | '\'\'' )? ( ~('\''|'\\') | ECHAR ) )* '\'\'\'' {setText($text.substring(3, $text.length()-3));};

STRING_LITERAL_LONG2 : '"""' ( options {greedy=false;} : ( '"' | '""' )? ( ~('"'|'\\') | ECHAR ) )* '"""' {setText($text.substring(3, $text.length()-3));};

fragment
ECHAR : '\\' ( 't' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'');
    		
IRI_REF
    :(LESS (options{greedy=false;}: IRI_REF_CHARACTERS)* GREATER) =>  LESS (options{greedy=false;}: IRI_REF_CHARACTERS)* GREATER { setText($text.substring(1, $text.length()-1)); }
    | LESS { $type = LESS; }
    ;
    
fragment
IRI_REF_CHARACTERS
    : ~( LESS | GREATER | '"' | OPEN_CURLY_BRACE | CLOSE_CURLY_BRACE | PIPE | INVERSE | '`' | '\\' | ('\u0000'..'\u0020'))
    ;

fragment
PN_CHARS_U : PN_CHARS_BASE | '_';

fragment
VARNAME : ( PN_CHARS_U | DIGIT ) NAME_SUFFIX;

fragment
NAME_SUFFIX : ( PN_CHARS_U | DIGIT | '\u00B7' | '\u0300'..'\u036F' | '\u203F'..'\u2040' )*;

fragment
PN_CHARS
    : PN_CHARS_U
    | MINUS
    | DIGIT
    | '\u00B7' 
    | '\u0300'..'\u036F'
    | '\u203F'..'\u2040'
    ;

fragment
PN_PREFIX : PN_CHARS_BASE ((PN_CHARS|DOT)* PN_CHARS)?;

fragment
PN_LOCAL : (PN_CHARS_U|DIGIT)  ((PN_CHARS|{    
                    	                       if ( input.LA(1)=='.' ) {
                    	                          int LA2 = input.LA(2);
                    	       	                  if ( !((LA2>='-' && LA2<='.')||(LA2>='0' && LA2<='9')||(LA2>='A' && LA2<='Z')||LA2=='_'||(LA2>='a' && LA2<='z')||LA2=='\u00B7'||(LA2>='\u00C0' && LA2<='\u00D6')||(LA2>='\u00D8' && LA2<='\u00F6')||(LA2>='\u00F8' && LA2<='\u037D')||(LA2>='\u037F' && LA2<='\u1FFF')||(LA2>='\u200C' && LA2<='\u200D')||(LA2>='\u203F' && LA2<='\u2040')||(LA2>='\u2070' && LA2<='\u218F')||(LA2>='\u2C00' && LA2<='\u2FEF')||(LA2>='\u3001' && LA2<='\uD7FF')||(LA2>='\uF900' && LA2<='\uFDCF')||(LA2>='\uFDF0' && LA2<='\uFFFD')) ) {
                    	       	                     return;
                    	       	                  }
                    	                       }
                                           } DOT)* PN_CHARS)?;

fragment
PN_CHARS_BASE
    : 'A'..'Z'
    | 'a'..'z'
    | '\u00C0'..'\u00D6'
    | '\u00D8'..'\u00F6'
    | '\u00F8'..'\u02FF'
    | '\u0370'..'\u037D'
    | '\u037F'..'\u1FFF'
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ;
    	
fragment
DIGIT : '0'..'9';

COMMENT : '#' ( options{greedy=false;} : .)* EOL { $channel=HIDDEN; };

NESTED_ML_COMMENT
    :   '/*' 
        (options {greedy=false;} : NESTED_ML_COMMENT | . )* 
        '*/' {$channel=HIDDEN;}
    ;

SINGLE_LINE_COMMENT : '/''/' ( options{greedy=false;} : .)* EOL { $channel=HIDDEN; };

fragment
EOL : '\n' | '\r' ;

REFERENCE : '^^';

LESS_EQUAL : '<=';

GREATER_EQUAL : '>=';

NOT_EQUAL : '!=';

AND : '&&';

OR : '||';
    
INVERSE : '^';

OPEN_BRACE : '(';

CLOSE_BRACE : ')';

OPEN_CURLY_BRACE : '{';

CLOSE_CURLY_BRACE : '}';

OPEN_SQUARE_BRACKET : '[';

CLOSE_SQUARE_BRACKET : ']';

SEMICOLON : ';';

DOT : '.';

PLUS : '+';

MINUS : '-';

fragment
SIGN : (PLUS|MINUS);
	
ASTERISK : '*';

QUESTION_MARK : '?';
    	
COMMA : ',';

NEGATION : '!';

DIVIDE : '/';

EQUAL : '=';

fragment
LESS : '<';

GREATER : '>';

PIPE : '|';
    	
ANY : .;

// $>

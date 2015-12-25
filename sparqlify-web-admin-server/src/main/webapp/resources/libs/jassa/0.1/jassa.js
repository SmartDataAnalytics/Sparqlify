"use strict";

/**
 * Defines the global variable into which the modules
 * will add their content
 *
 * A note on naming convention:
 * The root objectand classes is spelled with upper camel case.
 * modules, functions and objects are in lower camel case.
 * (modules are just namespaces, and it feels pretty obstrusive writing them in upper camel case)
 *
 */
var Jassa = {
    rdf: {
        vocabs: {
            utils: {},
            xsd: {},
            rdf: {},
            rdfs: {},
            owl: {},
            wgs84: {}
        }
    },

    sparql: {},

    i18n: {},

    sponate: {},

    facets: {},

    vocabs: {},

    utils: {
        collections: {}
    }
};

(function() {

    var ns = Jassa.utils.collections;

    ns.HashMap = Class.create({
        initialize: function(fnEquals, fnHash) {
            this.fnEquals = fnEquals ? fnEquals : _.isEqual;
            this.fnHash = fnHash ? fnHash : (function(x) { return '' + x; });

            this.hashToBucket = {};
        },

        put: function(key, val) {
//			if(key == null) {
//				debugger;
//			}
//			console.log('Putting ' + key + ', ' + val);
            var hash = this.fnHash(key);

            var bucket = this.hashToBucket[hash];
            if(bucket == null) {
                bucket = [];
                this.hashToBucket[hash] = bucket;
            }


            var keyIndex = this._indexOfKey(bucket, key);
            if(keyIndex >= 0) {
                bucket[keyIndex].val = val;
                return;
            }

            var entry = {
                key: key,
                val: val
            };

            bucket.push(entry);
        },

        _indexOfKey: function(bucket, key) {
            if(bucket != null) {

                for(var i = 0; i < bucket.length; ++i) {
                    var entry = bucket[i];

                    var k = entry.key;
                    if(this.fnEquals(k, key)) {
                        //entry.val = val;
                        return i;
                    }
                }

            }

            return -1;
        },

        get: function(key) {
            var hash = this.fnHash(key);
            var bucket = this.hashToBucket[hash];
            var i = this._indexOfKey(bucket, key);
            var result = i >= 0 ? bucket[i].val : null;
            return result;
        },

        remove: function(key) {
            var hash = this.fnHash(key);
            var bucket = this.hashToBucket[hash];
            var i = this._indexOfKey(bucket, key);

            var doRemove = i >= 0;
            if(doRemove) {
                bucket.splice(i, 1);
            }

            return doRemove;
        },

        containsKey: function(key) {
            var hash = this.fnHash(key);
            var bucket = this.hashToBucket[hash];
            var result =  this._indexOfKey(bucket, key) >= 0;
            return result;
        },

        keyList: function() {
            var result = [];

            _.each(this.hashToBucket, function(bucket) {
                var keys = _(bucket).pluck('key')
                result.push.apply(result, keys);
            });

            return result;
        },

        entries: function() {
            var result = [];

            _.each(this.hashToBucket, function(bucket) {
                result.push.apply(result, bucket);
            });

            return result;
        },

        toString: function() {
            var entries = this.entries();
            var entryStrs = entries.map(function(entry) { return entry.key + ': ' + entry.val});
            var result = '{' + entryStrs.join(', ') + '}';
            return result;
        }
    });



    ns.HashBidiMap = Class.create({
        /**
         *
         */
        initialize: function(fnEquals, fnHash, inverseMap) {
            this.forward = new ns.HashMap(fnEquals, fnHash);
            this.inverse = inverseMap ? inverseMap : new ns.HashBidiMap(fnEquals, fnHash, this);
        },

        getInverse: function() {
            return this.inverse;
        },

        put: function(key, val) {
            this.remove(key);

            this.forward.put(key, val);
            this.inverse.forward.put(val, key);
        },

        remove: function(key) {
            var priorVal = this.get(key);
            this.inverse.forward.remove(priorVal);
            this.forward.remove(key);
        },

        getMap: function() {
            return this.forward;
        },

        get: function(key) {
            var result = this.forward.get(key);
            return result;
        },

        keyList: function() {
            var result = this.forward.keyList();
            return result;
        }
    });

    ns.HashSet = Class.create({
        initialize: function(fnEquals, fnHash) {

        }


    });

})();
(function() {

    var ns = Jassa.utils.collections;

    ns.TreeUtils = {

        /**
         * Generic method for visiting a tree structure
         *
         */
        visitDepthFirst: function(parent, fnChildren, fnPredicate) {
            var proceed = fnPredicate(parent);

            if(proceed) {
                var children = fnChildren(parent);

                _(children).each(function(child) {
                    ns.TreeUtils.visitDepthFirst(child, fnChildren, fnPredicate);
                });
            }
        }

    };

})();
(function() {

    var ns = Jassa.rdf;


    // Note: the shortcuts we used actually are quite ok for JavaScript
    // i.e. doing ns.Node.v() rather than ns.NodeFactory.createNode()

    ns.Node = Class.create({
        getUri: function() {
            throw "not a URI node";
        },

        getName: function() {
            throw " is not a variable node";
        },

        getBlankNodeId: function() {
            throw " is not a blank node";
        },

        getBlankNodeLabel: function() {
            //throw " is not a blank node";
            return this.getBlankNodeId().getLabelString();
        },

        getLiteral: function() {
            throw " is not a literal node";
        },

        getLiteralValue: function() {
            throw " is not a literal node";
        },

        getLiteralLexicalForm: function() {
            throw " is not a literal node";
        },

        getLiteralDatatype: function() {
            throw " is not a literal node";
        },

        getLiteralDatatypeUri: function() {
            throw " is not a literal node";
        },

        isBlank: function() {
            return false;
        },

        isUri: function() {
            return false;
        },

        isLiteral: function() {
            return false;
        },

        isVariable: function() {
            return false;
        },

        equals: function(that) {

            // By default we assume non-equality
            var result = false;

            if(that == null) {
                result = false;
            }
            else if(this.isLiteral()) {
                if(that.isLiteral()) {

                    var isSameLex = this.getLiteralLexicalForm() == that.getLiteralLexicalForm();
                    var isSameType = this.getLiteralDatatypeUri() == that.getLiteralDatatypeUri();
                    var isSameLang = this.getLiteralLanguage() == that.getLiteralLanguage();

                    result = isSameLex && isSameType && isSameLang;
                }
            }
            else if(this.isUri()) {
                if(that.isUri()) {
                    result = this.getUri() == that.getUri();
                }
            }
            //else if(this.)
            else {
                throw 'not implemented yet';
            }

            return result;
        }
    });


    ns.Node_Concrete = Class.create(ns.Node, {
        isConcrete: function() {
            return true;
        }
    });


    ns.Node_Uri = Class.create(ns.Node_Concrete, {
        initialize: function(uri) {
            this.uri = uri;
        },

        isUri: function() {
            return true;
        },

        getUri: function() {
            return this.uri;
        },

        toString: function() {
            return '<' + this.uri + '>';
        }
    });

    ns.Node_Blank = Class.create(ns.Node_Concrete, {
        // Note: id is expected to be an instance of AnonId
        initialize: function(id) {
            this.id = id;
        },

        isBlank: function() {
            return true;
        },

        getBlankNodeId: function() {
            return id;
        }
    });

    ns.Node_Fluid = Class.create(ns.Node, {
        isConcrete: function() {
            return false;
        }
    });

    // I don't understand the purpose of this class right now
    // i.e. how it is supposed to differ from ns.Var
    ns.Node_Variable = Class.create(ns.Node_Fluid, {
        isVariable: function() {
            return true;
        }
    });

    ns.Var = Class.create(ns.Node_Variable, {
        initialize: function(name) {
            this.name = name;
        },

        getName: function() {
            return this.name;
        },

        toString: function() {
            return '?' + this.name;
        }
    });


    ns.Node_Literal = Class.create(ns.Node_Concrete, {
        initialize: function(literalLabel) {
            this.literalLabel = literalLabel;
        },

        isLiteral: function() {
            return true;
        },

        getLiteral: function() {
            return this.literalLabel;
        },

        getLiteralValue: function() {
            return this.literalLabel.getValue();
        },

        getLiteralLexicalForm: function() {
            return this.literalLabel.getLexicalForm();
        },

        getLiteralDatatype: function() {
            return this.literalLabel.getDatatype();
        },

        getLiteralDatatypeUri: function() {
            var dtype = this.getLiteralDatatype();
            var result = dtype ? dtype.getUri() : null;
            return result;
        },

        getLiteralLanguage: function() {
            return this.literalLabel.getLanguage();
        },

        toString: function() {
            return this.literalLabel.toString();
        }
    });


    ns.escapeLiteralString = function(str) {
        return str;
    };

    /**
     * An simple object representing a literal -
     * independent from the Node inheritance hierarchy.
     *
     * Differences to Jena:
     *   - No getDatatypeUri method, as there is dtype.getUri()
     */
    ns.LiteralLabel = Class.create({
        /**
         * Note: The following should hold:
         * dtype.parse(lex) == val
         * dtype.unpars(val) == lex
         *
         * However, this class doesn't care about it.
         *
         */
        initialize: function(val, lex, lang, dtype) {
            this.val = val;
            this.lex = lex;
            this.lang = lang;
            this.dtype = dtype;
        },

        /**
         * Get the literal's value as a JavaScript object
         */
        getValue: function() {
            return this.val;
        },

        getLexicalForm: function() {
            return this.lex
        },

        getLanguage: function() {
            return this.lang;
        },

        /**
         * Return the dataype object associated with this literal.
         */
        getDatatype: function() {
            return this.dtype;
        },

        toString: function() {
            var dtypeUri = this.dtype ? this.dtype.getUri() : null;
            var litStr = ns.escapeLiteralString(this.lex);

            var result;
            if(dtypeUri) {
                result = '"' + litStr + '"^^<' + dtypeUri + '>';
            } else {
                result = '"' + litStr + '"' + (this.lang ? '@' + this.lang : '');
            }

            return result;
        }
    });


    ns.AnonId = Class.create({
        getLabelString: function() {
            throw "not implemented";
        }
    });

    ns.AnonIdStr = Class.create(ns.AnonId, {
        initialize: function(label) {
            this.label = label;
        },

        getLabelString: function() {
            return label;
        }
    });


    ns.DatatypeLabel = Class.create({
        parse: function(val) {
            throw 'Not implemented';
        },

        unparse: function(val) {
            throw 'Not implemented';
        }
    });


    ns.DatatypeLabelInteger = Class.create(ns.DatatypeLabel, {
        parse: function(str) {
            var result = parseInt(str, 10);
            return result;
        },

        unparse: function(val) {
            return '' + val;
        }
    });

    ns.DatatypeLabelFloat = Class.create(ns.DatatypeLabel, {
        parse: function(str) {
            var result = parseFloat(str);
            return result;
        },

        unparse: function(val) {
            return '' + val;
        }
    });

    ns.DatatypeLabelString = Class.create(ns.DatatypeLabel, {
        parse: function(str) {
            return str
        },

        unparse: function(val) {
            return val;
        }
    });


    ns.RdfDatatype = Class.create({
        getUri: function() {
            throw "Not implemented";
        },

        unparse: function(value) {
            throw "Not implemented";
        },

        /**
         * Convert a value of this datatype out
         * to lexical form.
         */
        parse: function(str) {
            throw "Not implemented";
        }
    });


    ns.RdfDatatypeBase = Class.create(ns.RdfDatatype, {
        initialize: function(uri) {
            this.uri = uri;
        },

        getUri: function() {
            return this.uri;
        }
    });

    ns.RdfDatatype_Label = Class.create(ns.RdfDatatypeBase, {
        initialize: function($super, uri, datatypeLabel) {
            $super(uri);

            this.datatypeLabel = datatypeLabel;
        },

        parse: function(str) {
            var result = this.datatypeLabel.parse(str);
            return result;
        },

        unparse: function(val) {
            var result = this.datatypeLabel.unparse(val);
            return result;
        }
    });





    ns.NodeFactory = {
        createUri: function(uri) {
            return new ns.Node_Uri(uri);
        },

        createVar: function(name) {
            return new ns.Var(name);
        },

        createPlainLiteral: function(value, lang) {
            var label = new ns.LiteralLabel(value, value, lang);
            var result = new ns.Node_Literal(label);

            return result;
        },

        /**
         * The value needs to be unparsed first (i.e. converted to string)
         *
         */
        createTypedLiteralFromValue: function(val, typeUri) {
            var dtype = rdf.RdfDatatypes[typeUri];
            if(!dtype) {
                console.log('[ERROR] No dtype for ' + typeUri);
                throw 'Bailing out';
            }

            var lex = dtype.unparse(val);
            var lang = null;

            var literalLabel = new rdf.LiteralLabel(val, lex, lang, dtype);

            var result = new rdf.Node_Literal(literalLabel);

            return result;
        },


        /**
         * The string needs to be parsed first (i.e. converted to the value)
         *
         */
        createTypedLiteralFromString: function(str, typeUri) {
            var dtype = rdf.RdfDatatypes[typeUri];
            if(!dtype) {
                console.log('[ERROR] No dtype for ' + typeUri);
                throw 'Bailing out';
            }

            var val = dtype.parse(str);

            var lex = str;
            //var lex = dtype.unparse(val);
            //var lex = s; //dtype.parse(str);
            var lang = null;

            var literalLabel = new rdf.LiteralLabel(val, lex, lang, dtype);

            var result = new rdf.Node_Literal(literalLabel);

            return result;
        },

        createFromTalisRdfJson: function(talisJson) {
            if(!talisJson || typeof(talisJson.type) === 'undefined') {
                throw "Invalid node: " + JSON.stringify(talisJson);
            }

            var result;
            switch(talisJson.type) {
                case 'bnode':
                    throw 'Not implemented yet';
                    break;
                case 'uri':
                    result = ns.NodeFactory.createUri(talisJson.value);
                    break;
                case 'literal':
                    // Virtuoso showed a bug with
                    var lang = talisJson.lang || talisJson['xml:lang'];
                    result = ns.NodeFactory.createPlainLiteral(talisJson.value, lang);
                    break;
                case 'typed-literal':
                    result = ns.NodeFactory.createTypedLiteralFromString(talisJson.value, talisJson.datatype);
                    break;
                default:
                    console.log("Unknown type: '" + talisJson.type + "'");
                    throw 'Bailing out';
            }

            return result;
        }
    };

    // Convenience methods
    _.extend(ns.Node, {
        uri: ns.NodeFactory.createUri,
        v: ns.NodeFactory.createVar
    });


})();




//
//
//// This node approach is broken...
//// I thought I could get away with something cheap because this is JavaScript,
//// but it turns out that good engineering stays good regardless of the target language.
//
//ns.Node = function(type, value, language, datatype) {
//	this.type = type;
//	this.value = value;
//	this.language = language;
//	this.datatype = datatype;
//};
//
//
//ns.Node.classLabel = 'Node';
//
//ns.Node.prototype = {
//		getValue: function() {
//			return this.value;
//		},
//
//		getType: function() {
//			return this.type;
//		},
//
//		getLanguage: function() {
//			return this.language;
//		},
//
//		getDatatype: function() {
//			return this.datatype;
//		},
//
//		equals: function(that) {
//			var result = _.isEqual(this, that);
//			return result;
//		},
//
//		/**
//		 * Warning: If fnNodeMap does not return a copy, the node will not be copied.
//		 * In general, Node should be considered immutable!
//		 *
//		 * @param fnNodeMap
//		 * @returns
//		 */
//		copySubstitute: function(fnNodeMap) {
//			var sub = fnNodeMap(this);
//			var result = (sub == undefined || sub == null) ? this : sub;
//			return result;
//		},
//
//		toString: function() {
//			switch(this.type) {
//			case -1: return "?" + this.value;
//			case 0: return "_:" + this.value;
//			case 1: return "<" + this.value + ">";
//			case 2: return "\"" + this.value + "\"" + (this.language ? "@" + this.language : "");
//			case 3: return "\"" + this.value + "\"" + (this.datatype ? "^^<" + this.datatype + ">" : "");
//			}
//		},
//
//		isVar: function() {
//			return this.type === -1;
//		},
//
//		isUri: function() {
//			return this.type === ns.Node.Type.Uri;
//		},
//
//		toJson: function() {
//			throw "Not implemented yet";
//		}
//};
//
//
//ns.Node.Type = {};
//ns.Node.Type.Variable = -1;
//ns.Node.Type.BlankNode = 0;
//ns.Node.Type.Uri = 1;
//ns.Node.Type.PlainLiteral = 2;
//ns.Node.Type.TypedLiteral = 3;
//
//ns.Node.fromJson = function(talisJson) {
//	return ns.Node.fromTalisJson(talisJson);
//};
//
//ns.Node.fromTalisJson = function(talisJson) {
//	var result = new ns.Node();
//
//	if(!talisJson || typeof(talisJson.type) === 'undefined') {
//		throw "Invalid node: " + JSON.stringify(talisJson);
//	}
//
//	var type;
//	switch(talisJson.type) {
//	case 'bnode': type = 0; break;
//	case 'uri': type = 1; break;
//	case 'literal': type = 2; break;
//	case 'typed-literal': type = 3; break;
//	default: console.log("Unknown type: '" + talisJson.type + "'");
//	}
//
//	result.type = type;
//	result.value = talisJson.value;
//	result.language = talisJson.lang ? talisJson.lang : "";
//	result.datatype = talisJson.datatype ? talisJson.datatype : "";
//
//	// TODO I thought it happened that a literal hat a datatype set, but maybe I was imaginating things
//	if(result.datatype) {
//		result.type = 3;
//	}
//
//	return result;
//	/*
//	var type = -2;
//	if(node.type == "uri") {
//
//	}*/
//};
//
//ns.Node.isNode = function(candidate) {
//	return candidate && (candidate instanceof ns.Node);
//};
//
//ns.Node.isUri = function(candidate) {
//	return ns.Node.isNode(candidate) && candidate.isUri();
//};
//
//
//ns.Node.parse = function(str) {
//	var str = str.trim();
//
//	if(strings.startsWith(str, '<') && strings.endsWith(str, '>')) {
//		return ns.Node.uri(str.substring(1, str.length - 1));
//	} else {
//		throw "Node.parse not implemented for argument: " + str;
//	}
//};
//
//ns.Node.uri = function(str) {
//	return new ns.Node(1, str, null, null);
//};
//
//ns.Node.v = function(name) {
//	return new ns.Node(-1, name, null, null);
//};
//
////ns.Node.blank = function(id) {
////	return new ns.Node(0, id, null, null);
////};
////
////ns.Node.plainLit = function(value, language) {
////	return new ns.Node(2, value, language, null);
////};
////
////ns.Node.typedLit = function(value, datatype) {
////	return new ns.Node(3, value, null, datatype);
////};
//
//ns.Node.forValue = function(value) {
//	var dt = typeof value;
//	if(dt === "number") {
//		return ns.Node.typedLit(value, "http://www.w3.org/2001/XMLSchema#double");
//	} else {
//		console.error("No handling for datatype ", td);
//	}
//
//	//alert(dt);
//};
//
//
//// BAM! Overwrite the node class
(function() {

    var rdf = Jassa.rdf;

    var ns = Jassa.rdf.vocabs.utils;

    /**
     * Creates rdf.Node objects in the target namespace
     * from strings in the source namepsace
     *
     */
    ns.initNodes = function(target, source) {

        if(source == null) {
            source = target.str;

            if(source == null) {
                console.log('No source from where to init nodes');
                throw 'Bailing out';
            }
        }

        _.each(source, function(v, k) {
            target[k] = rdf.Node.uri(v);
        });
    };

})();	(function() {

    var utils = Jassa.rdf.vocabs.utils;
    var ns = Jassa.rdf.vocabs.xsd;

    var p = 'http://www.w3.org/2001/XMLSchema#';

    // String versions
    ns.str = {
        xboolean: p + 'boolean',
        xint: p + 'int',
        decimal: p + 'decimal',
        xfloat: p + 'float',
        xdouble: p + 'double',
        xstring: p + 'string',

        date: p + 'date',
        dateTime: p + 'dateTime'
    };


    utils.initNodes(ns);

//	// Node versions
//	var str = ns.str;
//
//	_.each(ns.str, function(v, k) {
//		ns[k] = rdf.Node.uri(v);
//	});

//	_.extend(ns, {
//		xboolean: rdf.Node.uri(str.xboolean),
//		xint: rdf.Node.uri(str.xint),
//		xfloat: rdf.Node.uri(str.xfloat),
//		xdouble: rdf.Node.uri(str.xdouble),
//		xstring: rdf.Node.uri(str.xstring),
//
//		date: rdf.Node.uri(str.date),
//	    dateTime: rdf.Node.uri(str.dateTime)
//	});

})();
(function() {

    // This file requires the xsd datatypes, whereas xsd depends on rdf-core

    var xsd = Jassa.rdf.vocabs.xsd;
    var s = xsd.str;
    var ns = Jassa.rdf;


    ns.DatatypeLabels = {
        xinteger: new ns.DatatypeLabelInteger(),
        xfloat: new ns.DatatypeLabelFloat(),
        xstring: new ns.DatatypeLabelString(),
        decimal: new ns.DatatypeLabelInteger() // TODO Handle Decimal properly
    };


    ns.RdfDatatypes = {};

    ns.registerRdfDatype = function(uri, label) {
        ns.RdfDatatypes[uri] = new ns.RdfDatatype_Label(uri, label);
    };

    ns.registerRdfDatype(xsd.str.xint, ns.DatatypeLabels.xinteger);
    ns.registerRdfDatype(xsd.str.xstring, ns.DatatypeLabels.xstring);
    ns.registerRdfDatype(xsd.str.xfloat, ns.DatatypeLabels.xfloat);

    ns.registerRdfDatype(xsd.str.decimal, ns.DatatypeLabels.xinteger);

    /**
     * Some default datatypes.
     *
     * TODO This is redundant with the datatypeLabel classes above
     */

//	var xsdps = ns.XsdParsers = {};
//
//	xsdps[s.xboolean] = function(str) { return str == 'true'; };
//	xsdps[s.xint] = function(str) { return parseInt(str, 10); };
//	xsdps[s.xfloat] = function(str) { return parseFloat(str); };
//	xsdps[s.xdouble] = function(str) { return parseFloat(str); };
//	xsdps[s.xstring] = function(str) { return str; };
//
//	xsdps[s.decimal] = function(str) { return parseInt(str, 10); };
//
//
//	// TODO Parse to some object other than string
//	xsdps[s.date] = function(str) { return str; };
//	xsdps[s.dateTime] = function(str) { return str; };



})();(function() {

    var utils = Jassa.rdf.vocabs.utils;
    var ns = Jassa.rdf.vocabs.rdf;

    var p = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#';

    ns.str = {
        type: p + 'type'
    };

    utils.initNodes(ns);

})();
(function() {

    var utils = Jassa.rdf.vocabs.utils;
    var ns = Jassa.rdf.vocabs.rdfs;

    var p = 'http://www.w3.org/2000/01/rdf-schema#';

    ns.str = {
        label: p + 'label',
        subClassOf: p + 'subClassOf'
    };

    utils.initNodes(ns);

})();
(function() {

    var utils = Jassa.rdf.vocabs.utils;
    var ns = Jassa.rdf.vocabs.owl;

    var p = 'http://www.w3.org/2002/07/owl#';

    ns.str = {
        'Class': p + 'Class'
    };

    utils.initNodes(ns);

})();
(function() {

    var utils = Jassa.rdf.vocabs.utils;
    var ns = Jassa.rdf.vocabs.wgs84;

    var p = 'http://www.w3.org/2003/01/geo/wgs84_pos#';

    // String versions
    ns.str = {
        lon: p + "long",
        lat: p + "lat",
    };

    utils.initNodes(ns);

})();(function() {

    var ns = Jassa.sparql;

    /*
     * rdf.Node is the same as sparql.Node, but the former is strongly preferred.
     * This alias for the Node object between the rdf and sparql namespace exists for legacy reasons.
     */
    ns.Node = Jassa.rdf.Node;

})();(function() {

    var rdf = Jassa.rdf;
    var xsd = Jassa.rdf.vocabs.xsd;


    var ns = Jassa.sparql;

    // NOTE This file is currently being portet to make use of classes


    /**
     * An string object that supports variable substitution and extraction
     * to be used for ElementString and ExprString
     *
     */
    ns.SparqlString = Class.create({
        initialize: function(value, varsMentioned) {
            this.value = value;
            this.varsMentioned = varsMentioned ? varsMentioned : [];
        },

        toString: function() {
            return this.value;
        },

        getString: function() {
            return this.value;
        },

        copySubstitute: function(fnNodeMap) {
            var str = this.value;
            var newVarsMentioned = [];

            _(this.varsMentioned).each(function(v) {

                var reStr = '\\?' + v.getName() + '([^_\\w])?';
                var re = new RegExp(reStr, 'g');

                var node = fnNodeMap(v);
                if(node) {
                    console.log('Node is ', node);
                    if(node.isVariable()) {
                        //console.log('Var is ' + node + ' ', node);

                        newVarsMentioned.push(node);
                    }

                    var nodeStr = node.toString();
                    str = str.replace(re, nodeStr + '$1');
                } else {
                    newVarsMentioned.push(v);
                }
            });


            return new ns.SparqlString(str, newVarsMentioned);
        },

        getVarsMentioned: function() {
            return this.varsMentioned;
        }
    });

    ns.SparqlString.classLabel = 'SparqlString';


    ns.SparqlString.create = function(str, vars) {
        vars = vars ? vars : ns.extractSparqlVars(str);

        var result = new ns.SparqlString(str, vars);
        return result;
    };



    /**
     * Expr classes, similar to those in Jena
     *
     * Usally, the three major cases we need to discriminate are:
     * - Varibles
     * - Constants
     * - Functions
     *
     */
    ns.Expr = Class.create({
        isFunction: function() {
            return false;
        },

        isVar: function() {
            return false;
        },

        isConstant: function() {
            return false;
        },

        getFunction: function() {
            throw 'Override me';
        },

        getExprVar: function() {
            throw 'Override me';
        },

        getConstant: function() {
            throw 'Override me';
        },

        copySubstitute: function(fnNodeMap) {
            throw 'Override me';
        }
    });

    // TODO Should we introduce ExprNode ?

    ns.ExprVar = Class.create(ns.Expr, {
        classLabel: 'ExprVar',

        initialize: function(v) {
            this.v = v;
        },

        copySubstitute: function(fnNodeMap) {
            //var node = fnNodeMap(this.v);

            //var result = (n == null) ? this : node;//rdf.NodeValue.makeNode(node);

            //return result;
            //return new ns.ExprVar(this.v.copySubstitute(fnNodeMap));
            return this;
        },

        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args && args.length > 0) {
                throw "Invalid argument";
            }

            var result = new ns.ExprVar(this.v);
            return result;
        },

        isVar: function() {
            return true;
        },

        getExprVar: function() {
            return this;
        },

        asVar: function() {
            return this.v;
        },

        getVarsMentioned: function() {
            return [this.v];
        },

        toString: function() {
            return "" + this.v;
        }
    });

    ns.ExprFunction = Class.create(ns.Expr, {
        isFunction: function() {
            return true;
        },

        getFunction: function() {
            return this;
        }
    });

    ns.ExprFunction0 = Class.create(ns.ExprFunction, {
        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args && args.length > 0) {
                throw "Invalid argument";
            }

            var result = this.$copy(args);
            return result;
        }
    });

    ns.ExprFunction1 = Class.create(ns.ExprFunction, {
        initialize: function(subExpr) {
            this.subExpr = subExpr;
        },

        getArgs: function() {
            return [this.subExpr];
        },

        copy: function(args) {
            if(args.length != 1) {
                throw "Invalid argument";
            }

            var result = this.$copy(args);
            return result;
        },

        getSubExpr: function() {
            return this.subExpr;
        }
    });


    ns.ExprFunction2 = Class.create(ns.ExprFunction, {
        initialize: function(left, right) {
            this.left = left;
            this.right = right;
        },

        getArgs: function() {
            return [this.left, this.right];
        },

        copy: function(args) {
            if(args.length != 2) {
                throw "Invalid argument";
            }

            var result = this.$copy(args[0], args[1]);
            return result;
        },

        getLeft: function() {
            return this.left;
        },

        getRight: function() {
            return this.right;
        }
    });



// TODO Change to ExprFunction1
    ns.E_In = Class.create(ns.Expr, {
        initialize: function(variable, nodes) {

            this.variable = variable;
            this.nodes = nodes;
        },

        getVarsMentioned: function() {
            return [this.variable];
        },

        copySubstitute: function(fnNodeMap) {
            var newElements = _.map(this.nodes, function(x) { return x.copySubstitute(fnNodeMap); });
            return new ns.E_In(this.variable.copySubstitute(fnNodeMap), newElements);
        },

        toString: function() {

            if(!this.nodes || this.nodes.length === 0) {
                //
                return "FALSE";
            } else {
                return "(" + this.variable + " In (" + this.nodes.join(", ") + "))";
            }
        }
    });


    ns.E_Str = Class.create(ns.ExprFunction1, {
//		initialize: function($super) {
//
//		},

        copySubstitute: function(fnNodeMap) {
            return new ns.E_Str(this.subExpr.copySubstitute(fnNodeMap));
        },

        getVarsMentioned: function() {
            return this.subExpr.getVarsMentioned();
        },


        $copy: function(args) {
            return new ns.E_Str(args[0]);
        },

        toString: function() {
            return "str(" + this.subExpr + ")";
        }
    });


    ns.E_Regex = function(expr, pattern, flags) {
        this.expr = expr;
        this.pattern = pattern;
        this.flags = flags;
    };

    ns.E_Regex.prototype = {
            copySubstitute: function(fnNodeMap) {
                return new ns.E_Regex(this.expr.copySubstitute(fnNodeMap), this.pattern, this.flags);
            },

            getVarsMentioned: function() {
                return this.expr.getVarsMentioned();
            },

            getArgs: function() {
                return [this.expr];
            },

            copy: function(args) {
                if(args.length != 1) {
                    throw "Invalid argument";
                }

                var newExpr = args[0];
                var result = new ns.E_Regex(newExpr, this.pattern, this.flags);
                return result;
            },


        toString: function() {
            var patternStr = this.pattern.replace("'", "\\'");
            var flagsStr = this.flags ? ", '" + this.flags.replace("'", "\\'") + "'" : "";


            return "Regex(" + this.expr + ", '" + patternStr + "'" + flagsStr + ")";
        }
    };



    ns.E_Like = function(expr, pattern) {
        this.expr = expr;
        this.pattern = pattern;
    };

    ns.E_Like.prototype = {
            copySubstitute: function(fnNodeMap) {
                return new ns.E_Like(this.expr.copySubstitute(fnNodeMap), this.pattern);
            },

            getVarsMentioned: function() {
                return this.expr.getVarsMentioned();
            },

            getArgs: function() {
                return [this.expr];
            },

            copy: function(args) {
                var result = newUnaryExpr(ns.E_Like, args);
                return result;
            },


        toString: function() {
            var patternStr = this.pattern.replace("'", "\\'");


            return "(" + this.expr + " Like '" + patternStr + "')";
        }
    };



    ns.E_Function = function(uriNode, args) {
        this.uriNode = uriNode;
        this.args = args;
    };

    ns.E_Function.prototype.copySubstitute = function(fnNodeMap) {
        var newArgs = _.map(this.args, fnNodeMap);

        return new ns.E_Function(this.uriNode, newArgs);
    };

    ns.E_Function.prototype.getArgs = function() {
        return this.args;
    };

    ns.E_Function.prototype.copy = function(newArgs) {
        return new ns.E_Function(this.uriNode, newArgs);
    };

    ns.E_Function.prototype.toString = function() {
        var argStr = this.args.join(", ");

        var result = this.uriNode.value + "(" + argStr + ")";
        return result;
    };



    ns.E_Equals = Class.create(ns.ExprFunction2, {

        copySubstitute: function(fnNodeMap) {
            return new ns.E_Equals(this.left.copySubstitute(fnNodeMap), this.right.copySubstitute(fnNodeMap));
        },

        $copy: function(left, right) {
            return new ns.E_Equals(left, right);
        },

        toString: function() {
            return "(" + this.left + " = " + this.right + ")";
        },

        eval: function(binding) {
            // TODO Evaluate the expression
        },
    });


    ns.E_LangMatches = function(left, right) {
        this.left = left;
        this.right = right;
    };

    ns.E_LangMatches.prototype = {
            copySubstitute: function(fnNodeMap) {
                return new ns.E_LangMatches(fnNodeMap(this.left), fnNodeMap(this.right));
            },

            getArgs: function() {
                return [this.left, this.right];
            },

            copy: function(args) {
                return ns.newBinaryExpr(ns.E_LangMatches, args);
            },

            toString: function() {
                return "langMatches(" + this.left + ", " + this.right + ")";
            }
    };


    ns.E_Lang = function(expr) {
        this.expr = expr;
    };

    ns.E_Lang.prototype = {
            copySubstitute: function(fnNodeMap) {
                return new ns.E_Lang(fnNodeMap(this.expr));
            },

            getArgs: function() {
                return [this.expr];
            },

            copy: function(args) {
                var result = newUnaryExpr(ns.E_Lang, args);
                return result;
            },

            toString: function() {
                return "lang(" + this.expr + ")";
            }
    };

    ns.E_Bound = function(expr) {
        this.expr = expr;
    };

    ns.E_Bound.prototype = {
            copySubstitute: function(fnNodeMap) {
                return new ns.E_Bound(fnNodeMap(this.expr));
            },

            getArgs: function() {
                return [this.expr];
            },

            copy: function(args) {
                var result = newUnaryExpr(ns.E_Bound, args);
                return result;
            },

            toString: function() {
                return "bound(" + this.expr + ")";
            }
    };




    ns.E_GreaterThan = function(left, right) {
        this.left = left;
        this.right = right;
    };

    ns.E_GreaterThan.prototype.copySubstitute = function(fnNodeMap) {
        return new ns.E_GreaterThan(fnNodeMap(this.left), fnNodeMap(this.right));
    };

    ns.E_GreaterThan.prototype.getArgs = function() {
        return [this.left, this.right];
    };

    ns.E_GreaterThan.prototype.copy = function(args) {
        return ns.newBinaryExpr(ns.E_GreaterThan, args);
    };

    ns.E_GreaterThan.prototype.toString = function() {
        return "(" + this.left + " > " + this.right + ")";
    };

    ns.E_LessThan = function(left, right) {
        this.left = left;
        this.right = right;
    };

    ns.E_LessThan.prototype.copySubstitute = function(fnNodeMap) {
        return new ns.E_LessThan(fnNodeMap(this.left), fnNodeMap(this.right));
    };

    ns.E_LessThan.prototype.getArgs = function() {
        return [this.left, this.right];
    };

    ns.E_LessThan.prototype.copy = function(args) {
        return ns.newBinaryExpr(ns.E_LessThan, args);
    };

    ns.E_LessThan.prototype.toString = function() {
        return "(" + this.left + " < " + this.right + ")";
    };

    ns.E_LogicalAnd = function(left, right) {
        this.left = left;
        this.right = right;
    };

    ns.E_LogicalAnd.prototype.copySubstitute = function(fnNodeMap) {
        //return new ns.E_LogicalAnd(fnNodeMap(this.left), fnNodeMap(this.right));
        return new ns.E_LogicalAnd(this.left.copySubstitute(fnNodeMap), this.right.copySubstitute(fnNodeMap));
    };

    ns.E_LogicalAnd.prototype.getArgs = function() {
        return [this.left, this.right];
    };

    ns.E_LogicalAnd.prototype.copy = function(args) {
        return ns.newBinaryExpr(ns.E_LogicalAnd, args);
    };

    ns.E_LogicalAnd.prototype.toString = function() {
        return "(" + this.left + " && " + this.right + ")";
    };

    ns.E_LogicalOr = function(left, right) {
        this.left = left;
        this.right = right;
    };

    ns.E_LogicalOr.prototype.copySubstitute = function(fnNodeMap) {
        return new ns.E_LogicalOr(this.left.copySubstitute(fnNodeMap), this.right.copySubstitute(fnNodeMap));
    };

    ns.E_LogicalOr.prototype.getArgs = function() {
        return [this.left, this.right];
    };

    ns.E_LogicalOr.prototype.copy = function(args) {
        return ns.newBinaryExpr(ns.E_LogicalOr, args);
    };

    ns.E_LogicalOr.prototype.toString = function() {
        return "(" + this.left + " || " + this.right + ")";
    };


    ns.E_LogicalNot = function(expr) {
        this.expr = expr;
    };

    ns.E_LogicalNot.prototype = {
            copySubstitute: function(fnNodeMap) {
                return new ns.E_LogicalNot(this.expr.copySubstitute(fnNodeMap));
            },

            getArgs: function() {
                return [this.left, this.right];
            },

            copy: function(args) {
                return ns.newBinaryExpr(ns.E_LogicalOr, args);
            },

            toString: function() {
                return "(!" + this.expr + ")";
            }
    };




    /**
     * If null, '*' will be used
     *
     * TODO Not sure if modelling aggregate functions as exprs is a good thing to do.
     *
     * @param subExpr
     * @returns {ns.E_Count}
     */
    ns.E_Count = function(subExpr, isDistinct) {
        this.subExpr = subExpr;
        this.isDistinct = isDistinct ? isDistinct : false;
    };

    ns.E_Count.prototype.copySubstitute = function(fnNodeMap) {
        var subExprCopy = this.subExpr ? this.subExpr.copySubstitute(fnNodeMap) : null;

        return new ns.E_Count(subExprCopy, this.isDistinct);
    };

    ns.E_Count.prototype.toString = function() {
        return "Count(" + (this.isDistinct ? "Distinct " : "") + (this.subExpr ? this.subExpr : "*") +")";
    };



    ns.E_Min = function(subExpr) {
        this.subExpr = subExpr;
    };

    ns.E_Min.prototype.copySubstitute = function(fnNodeMap) {
        var subExprCopy = this.subExpr ? this.subExpr.copySubstitute(fnNodeMap) : null;

        return new ns.E_Min(subExprCopy);
    };

    ns.E_Min.prototype.getArgs = function() {
        return [this.subExpr];
    };

    ns.E_Min.prototype.copy = function(args) {
        if(args.length != 1) {
            throw "Invalid argument";
        }

        var newSubExpr = args[0];

        var result = new ns.E_Min(newSubExpr);
    };

    ns.E_Min.prototype.toString = function() {
        return "Min(" + this.subExpr + ")";
    };



    ns.E_Max = function(subExpr) {
        this.subExpr = subExpr;
    };

    ns.E_Max.prototype.copySubstitute = function(fnNodeMap) {
        var subExprCopy = this.subExpr ? this.subExpr.copySubstitute(fnNodeMap) : null;

        return new ns.E_Min(subExprCopy);
    };

    ns.E_Max.prototype.getArgs = function() {
        return [this.subExpr];
    };

    ns.E_Max.prototype.copy = function(args) {
        if(args.length != 1) {
            throw "Invalid argument";
        }

        var newSubExpr = args[0];

        var result = new ns.E_Max(newSubExpr);
    };

    ns.E_Max.prototype.toString = function() {
        return "Max(" + this.subExpr + ")";
    };



    ns.ExprString = Class.create(ns.Expr, {
        initialize: function(sparqlString) {
            this.sparqlString = sparqlString;
        },

        copySubstitute: function(fnNodeMap) {
            var newSparqlString = this.sparqlString.copySubstitute(fnNodeMap);
            return new ns.ExprString(newSparqlString);
        },

        getVarsMentioned: function() {
            return this.sparqlString.getVarsMentioned();
        },

        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args.length != 0) {
                throw "Invalid argument";
            }

            return this;
        },

        toString: function() {
            return "(!" + this.expr + ")";
        }
    });

    ns.ExprString.create = function(str, vars) {
        var result = new ns.ExprString(ns.SparqlString.create(str, vars));
        return result;
    };



    // TODO Not sure about the best way to design this class
    // Jena does it by subclassing for each type e.g. NodeValueDecimal


    ns.NodeValue = Class.create(ns.Expr, {
        initialize: function(node) {
            this.node = node;
        },

        isConstant: function() {
            return true;
        },

        getConstant: function() {
            return this;
        },


        getArgs: function() {
            return [];
        },

        getVarsMentioned: function() {
            return [];
        },

        asNode: function() {
            throw "makeNode is not overridden";
        },
//		getNode: function() {
//			return this.node;
//		},

        copySubstitute: function(fnNodeMap) {
            // TODO Perform substitution based on the node value
            // But then we need to map a node to a nodeValue first...
            return this;
            //return new ns.NodeValue(this.node.copySubstitute(fnNodeMap));
        },

        toString: function() {
            if(this.node.datatype === xsd.xdouble.value) {
                return parseFloat(this.node.value);
            }

            // TODO Numeric values do not need the full rdf term representation
            // e.g. "50"^^xsd:double - this method should output "natural/casual"
            // representations
            return this.node.toString();

            /*
            var node = this.node;
            var type = node.type;

            switch(type) {
            case 1: return this.node.toString();
            case 2: return ns.valueFragment(node) + ns.languageFragment(node);
            case 3: return ns.valueFragment(node) + ns.datatypeFragment(node);
            default: {
                    console.warn("Should not happen; type = " + node.type);
                    break;
            }
            }
            */
        }
    });

    /**
     * Static functions for ns.NodeValue
     *
     * Note: It seems we could avoid all these specific sub types and
     * do something more generic
     */
    _.extend(ns.NodeValue, {

        createLiteral: function(val, typeUri) {
            var node = rdf.NodeFactory.createTypedLiteralFromValue(val, typeUri);
            var result = new ns.NodeValueNode(node);
            return result;

//			var dtype = rdf.RdfDatatypes[dtypeUri];
//			if(!dtype) {
//				console.log('[ERROR] No dtype for ' + dtypeUri);
//			}
//
//			var lex = dtype.unparse(val);
//			var lang = null;
//
//			var literalLabel = new rdf.LiteralLabel(val, lex, lang, dtype);
//
//			var node = new rdf.Node_Literal(literalLabel);
//
//			var result = new ns.NodeValueNode(node);
//
//			return result;
        },


        makeString: function(str) {
            return ns.NodeValue.createLiteral(str, xsd.str.xstring);
        },

        makeInteger: function(val) {
            return new ns.NodeValue.createLiteral(val, xsd.str.xint);
        },

        makeFloat: function(val) {
            return new ns.NodeValue.createLiteral(val, xsd.str.xfloat);
        },

        makeNode: function(node) {
            return new ns.NodeValueNode(node);
        }


//		makeFloat: function(val) {
//			return new ns.NodeValueFloat(val);
//		}
    });


    ns.NodeValueNode = Class.create(ns.NodeValue, {
        initialize: function(node) {
            this.node = node;
        },

        asNode: function() {
            return this.node;
        },

        toString: function() {
            return 'NodeValue[' + this.node + ']';
        }
    });


//	ns.NodeValueInteger = Class.create(ns.NodeValue, {
//		initialize: function(val) {
//			this.val = val;
//		},
//
//		getInteger: function() {
//			return this.val;
//		},
//
//		makeNode: function() {
//			var result = rdf.Node.typedLit(str, xsd.str.xstring);
//			return result;
//		}
//	});

//	ns.NodeValueInteger = Class.create(ns.NodeValue, {
//		initialize: function(val) {
//			this.val = val;
//		},
//
//		getInteger: function() {
//			return this.val;
//		},
//
//		makeNode: function() {
//			var result = rdf.Node.typedLit(str, xsd.str.xstring);
//			return result;
//		}
//	});


//	ns.NodeValueString = Class.create(ns.NodeValue, {
//		initialize: function(str) {
//			this.str = str
//		},
//
//
//		// Having a generic get type function is more extensible to custom types
//		// Yet, convenience functions for common types, such as isString(),
//		// would be quite nice from an API perspective
//		getType: function() {
//			return 'string';
//		},
//
//		getString: function() {
//			return this.str;
//		},
//
//		makeNode: function() {
//			var result = rdf.Node.typedLit(str, xsd.str.xstring);
//			return result;
//		}
//	});

    // Jena-style compatibility
//	ns.NodeValue.makeNode = function(node) {
//		return new ns.NodeValue(node);
//	};

    ns.valueFragment = function(node) {
        return '"' + node.value.toString().replace('"', '\\"') + '"';
    };

    ns.languageFragment = function(node) {
        return node.language ? "@" + node.language : "";
    };

    ns.datatypeFragment = function(node) {
        return node.datatype ? '^^<' + node.datatype + '>' : "";
    };




})();




    /*
     * TODO E_Cast should be removed -
     * a cast expression should be modeled as a function taking a single argument which is the value to cast.
     *
     */
//
//	ns.E_Cast = function(expr, node) {
//		this.expr = expr;
//		this.node = node;
//	};
//
//	ns.E_Cast.prototype.copySubstitute = function(fnNodeMap) {
//		return new ns.E_Cast(this.expr.copySubstitute(fnNodeMap), this.node.copySubstitute(fnNodeMap));
//	};
//


//ns.E_Cast.prototype.getVarsMentioned = function() {
//var result = this.expr.getVarsMentioned();
//
//// Note: Actually a variable is invalid in the node postition
//if(node.isVar()) {
//	result.push(result);
//}
//
//return result;
//};
//
//ns.E_Cast.prototype.getArgs = function() {
//return [this.expr];
//};
//
//ns.E_Cast.prototype.copy = function(args) {
//if(args.length != 1) {
//	throw "Invalid argument";
//}
//
//var result =new ns.E_Cast(args[0], this.node);
//return result;
//};
//
//ns.E_Cast.prototype.toString = function() {
//return this.node + "(" + this.expr + ")";
//};
/**
 * Problem:
 * Somehow there needs to be an interface to build queries, but at the same time there needs
 * to be a way to execute them.
 *
 * Having something like Jena's Query object in js would be really really neat.
 *
 *
 *
 *
 * @returns
 */

(function() {

    var rdf = Jassa.rdf;
    var xsd = Jassa.rdf.vocabs.xsd;


    var ns = Jassa.sparql;

    /*
     * rdf.Node is the same as sparql.Node, but the former is strongly preferred.
     * This alias for the Node object between the rdf and sparql namespace exists for legacy reasons.
     */
    ns.Node = Jassa.rdf.Node;

    //var strings = Namespace("org.aksw.ssb.utils.strings");
    //var strings = require('underscore.strings');



    /**
     * A binding is a map from variables to entries.
     * An entry is a on object {v: sparql.Var, node: sparql.Node }
     *
     * The main speciality of this object is that
     * .entries() returns a *sorted* array of variable bindings (sorted by the variable name).
     *  .toString() re-uses the ordering.
     *
     * This means, that two bindings are equal if their strings are equal.
     *
     * TODO We could generalize this behaviour into some 'base class'.
     *
     *
     */
    ns.Binding = function(varNameToEntry) {
        this.varNameToEntry = varNameToEntry ? varNameToEntry : {};
    };

    /**
     * Create method in case the variables are not objects
     *
     * TODO Replace with an ordinary hashMap.
     */
    ns.Binding.create = function(varNameToNode) {

        var tmp = {};
        _.map(varNameToNode, function(node, vStr) {
            tmp[vStr] = {v: ns.Node.v(vStr), node: node};
        });

        var result = new ns.Binding(tmp);
        return result;
    };

    ns.Binding.fromTalisJson = function(b) {

        var tmp = {};
        _.each(b, function(val, k) {
            //var v = rdf.Node.v(k);
            var node = rdf.NodeFactory.createFromTalisRdfJson(val);
            tmp[k] = node;
        });

        var result = ns.Binding.create(tmp);

        return result;
    };

    ns.Binding.prototype = {
        put: function(v, node) {
            this.varNameToEntry[v.getName()] = {v: v, node: node};
        },

        get: function(v) {
            var entry = this.varNameToEntry[v.getName()];

            var result = entry ? entry.node : null;

            return result;
        },

        entries: function() {
            var tmp = _.values(this.varNameToEntry);
            var result = _.sortBy(tmp, function(entry) { return entry.v.getName(); });
            //alert(JSON.stringify(result));
            return result;
        },

        toString: function() {
            var e = this.entries();

            //var result = "[" + e.join()

            var tmp = _.map(e, function(item) {
                return '"' + item.v.getName() + '": "' + item.node + '"';
            });

            var result = '{' + tmp.join(', ') + '}';

            return result;
        },

        getVars: function() {
            var result = [];

            _(this.varNameToEntry).each(function(entry) {
                result.push(entry.v);
            });

            return result;
        }
    };




    ns.Element = function() {

    };


    ns.Element.fromJson = function() {

    };

    ns.Element.toJson = function() {

    };



    ns.orify = function(exprs) {
        var result = ns.opify(exprs, ns.E_LogicalOr);
        return result;
    };

    ns.andify = function(exprs) {
        var result = ns.opify(exprs, ns.E_LogicalAnd);
        return result;
    };


    /**
     * Deprecated
     *
     * This object is overridden by opifyBalanced
     *
     */
    ns.opify = function(exprs, fnCtor) {
        var open = exprs;
        var next = [];

        while(open.length > 1) {

            for(var i = 0; i < open.length; i+=2) {

                var a = open[i];

                if(i + 1 == open.length) {
                    next.push(a);
                    break;
                }

                var b = open[i + 1];

                var newExpr = fnCtor(a, b);

                next.push(newExpr); //;new ns.E_LogicalOr(a, b));
            }

            var tmp = open;
            open = next;
            next = [];
        }

        return open;
    };



    ns.uniqTriples = function(triples) {
        var result =  _.uniq(triples, false, function(x) { return x.toString(); });
        return result;
    };

    /**
     * Combine two arrays of triples into a singe one with duplicates removed
     *
     */
    ns.mergeTriples = function(a, b) {
        var combined = a.concat(b);
        var result = ns.uniqTriples(combined);
        return result;
    };


    //console.log("The namespace is: ", ns);

    //var ns = {};

    ns.varPattern = /\?(\w+)/g;
    //ns.prefixPattern =/(^|\s+)(\w+):\w+(\s+|$)/g;
    ns.prefixPattern =/((\w|-)+):(\w|-)+/g;

    ns.extractVarNames = function(vars) {
        var result = [];
        for(var i = 0; i < vars.length; ++i) {
            var v = vars[i];

            result.push(v.getName());
        }

        return result;
    };

    /**
     * Extract SPARQL variables from a string
     *
     * @param str
     * @returns {Array}
     */
    ns.extractSparqlVars = function(str) {
        var varNames = ns.extractAll(ns.varPattern, str, 1);
        var result = [];
        for(var i = 0; i < varNames.length; ++i) {
            var varName = varNames[i];
            var v = ns.Node.v(varName);
            result.push(v);
        }

        return result;
    };

    ns.extractPrefixes = function(str) {
        return ns.extractAll(ns.prefixPattern, str, 1);
    };

    /**
     * Return a new string with prefixes expanded
     *
     * Yes, it sucks doing it without a proper parser...
     * And yes, the Java world is so much better, it doesn't even compare to this crap here
     *
     */
    ns.expandPrefixes = function(prefixes, str) {
        var usedPrefixes = ns.extractPrefixes(str);


        var result = str;
        for(var i = 0; i < usedPrefixes.length; ++i) {
            var prefix = usedPrefixes[i];

            var url = prefixes[prefix];
            if(!url) {
                continue;
            }


            var re = new RegExp(prefix + ':(\\w+)', 'g');

            result = result.replace(re, '<' + url + '$1>');
            //console.log(result + ' prefixes' + prefix + url);

        }

        return result;
    };


    ns.extractAll = function(pattern, str, index) {
        // Extract variables from the fragment
        var match;
        var result = [];

        while (match = pattern.exec(str)) {
            result.push(match[index]);
        }

        result = _.uniq(result);

        return result;

    };

    /*
    ns.parseJsonRs = function(jsonRs) {
        var bindings = jsonRs.results.bindings;

        var bindings = jsonRs.results.bindings;

        var tmpUris = {};
        for(var i = 0; i < bindings.length; ++i) {

            var binding = bindings[i];

            var newBinding = {};

            $.each(binding, function(varName, node) {
                var newNode = node ? null : Node.parseJson(node);

                newBinding[varName] = newNode;
            });

            bindings[i] = newBinding;
        }
    };
    */



    ns.Triple = function(s, p, o) {
        this.s = s;
        this.p = p;
        this.o = o;
    };

    ns.Triple.prototype.toString = function() {
        //return this.s + " " + this.p + " " + this.o + " .";
        return this.s + " " + this.p + " " + this.o;
    };

    /*
    ns.fnNodeMapWrapper = function(node, fnNodeMap) {
        var sub = fnNodeMap(node);
        var result = (sub == undefined || sub == null) ? node : sub;
        return result;
    };
    */

    ns.Triple.prototype.copySubstitute = function(fnNodeMap) {
        return new ns.Triple(this.s.copySubstitute(fnNodeMap), this.p.copySubstitute(fnNodeMap), this.o.copySubstitute(fnNodeMap));
    };

    ns.Triple.prototype.getSubject = function() {
        return this.s;
    };

    ns.Triple.prototype.getProperty = function() {
        return this.p;
    };

    ns.Triple.prototype.getObject = function() {
        return this.o;
    };

    ns.Triple.prototype.getVarsMentioned = function() {
        var result = [];
        result = ns.Triple.pushVar(result, this.s);
        result = ns.Triple.pushVar(result, this.p);
        result = ns.Triple.pushVar(result, this.o);

        return result;
    };


    ns.Triple.pushVar = function(array, node) {
        return (node.type != -1) ? array : _.union(array, node.value);
    };


    ns.BasicPattern = function(triples) {
        this.triples = triples ? triples : [];
    };

    ns.BasicPattern.prototype.copySubstitute = function(fnNodeMap) {
        var newElements = _.map(this.triples, function(x) { return x.copySubstitute(fnNodeMap); });
        return new ns.BasicPattern(newElements);
    };

    ns.BasicPattern.prototype.toString = function() {
        return this.triples.join(" . ");
    };

    /*
    ns.BasicPattern.prototype.copySubstitute = function() {

    };
    */

    ns.Template = function(bgp) {
        this.bgp = bgp;
    };

    ns.Template.prototype.copySubstitute = function(fnNodeMap) {
        return new ns.Template(this.bgp.copySubstitute(fnNodeMap));
    };

    ns.Template.prototype.toString = function() {
        return "{ " + this.bgp + " }";
    };


    ns.ElementNamedGraph = function(element, namedGraphNode) {
        this.element = element;
        this.namedGraphNode = namedGraphNode;
    };

    ns.ElementNamedGraph.classLabel = 'ElementNamedGraph';

    ns.ElementNamedGraph.prototype = {
        getArgs: function() {
            return [this.element];
        },

        copy: function(args) {
            if(args.length != 1) {
                throw "Invalid argument";
            }

            var newElement = args[0];
            var result = new ns.ElementNamedGraph(newElement, this.namedGraphNode);
            return result;
        },

        toString: function() {
            return "Graph " + this.namedGraphNode + " { " + this.element + " }";
        },

        copySubstitute: function(fnNodeMap) {
            return new ns.ElementNamedGraph(this.element.copySubstitute(fnNodeMap), this.namedGraphNode.copySubstitute(fnNodeMap));
        },

        getVarsMentioned: function() {

            var result = this.element.getVarsMentioned();
            if(this.namedGraphNode.isVar()) {
                _.union(result, [this.namedGraphNode]);
            }

            return result;
        },

        flatten: function() {
            return new ns.ElementNamedGraph(this.element.flatten(), this.namedGraphNode);
        }
    };






    /**
     * An element that injects a string "as is" into a query.
     *
     */
    ns.ElementString = Class.create({
        initialize: function(sparqlString) {
//			if(_(sparqlString).isString()) {
//				debugger;
//			}
            this.sparqlString = sparqlString;
        },

        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args.length != 0) {
                throw "Invalid argument";
            }

            // FIXME: Should we clone the attributes too?
            //var result = new ns.ElementString(this.sparqlString);
            return this;
            //return result;
        },

        toString: function() {
            return this.sparqlString.getString();
        },

        copySubstitute: function(fnNodeMap) {
            var newSparqlString = this.sparqlString.copySubstitute(fnNodeMap);
            return new ns.ElementString(newSparqlString);
        },

        getVarsMentioned: function() {
            return this.sparqlString.getVarsMentioned();
        },

        flatten: function() {
            return this;
        }
    });


    ns.ElementString.create = function(str, vars) {
        var result = new ns.ElementString(ns.SparqlString.create(str, vars));
        return result;
    };

    /*
    ns.ElementSubQueryString = function(value) {
        this.value = value;
    };

    ns.ElementSubQueryString = function(value) {

    }
    */


    ns.ElementSubQuery = function(query) {
        this.query = query;
    };

    ns.ElementSubQuery.classLabel = "ElementSubQuery";

    ns.ElementSubQuery.prototype = {
        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args.length != 0) {
                throw "Invalid argument";
            }

            // FIXME: Should we clone the attributes too?
            var result = new ns.ElementSubQuery(query);
            return result;
        },

        toString: function() {
            return "{ " + this.query + " }";
        },

        copySubstitute: function(fnNodeMap) {
            return new ns.ElementSubQuery(this.query.copySubstitute(fnNodeMap));
        },

        flatten: function() {
            return new ns.ElementSubQuery(this.query.flatten());
        }
    };

    ns.ElementFilter = function(exprs) {
        this.exprs = exprs;
    };

    ns.ElementFilter.classLabel = 'ElementFilter';

    ns.ElementFilter.prototype = {
        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args.length != 0) {
                throw "Invalid argument";
            }

        // 	FIXME: Should we clone the attributes too?
            var result = new ns.ElemenFilter(this.exprs);
            return result;
        },

        copySubstitute: function(fnNodeMap) {
            var exprs = _.map(this.exprs, function(expr) {
                return expr.copySubstitute(fnNodeMap);
            });

            return new ns.ElementFilter(exprs);
        },

        getVarsMentioned: function() {
            return [];
        },

        flatten: function() {
            return this;
        },

        toString: function() {

            var expr = ns.andify(this.exprs);

            return "Filter(" + expr + ")";
        }
    };


    ns.ElementOptional = function(element) {
        this.optionalPart = element;
    };

    ns.ElementOptional.classLabel = 'ElementOptional';

    ns.ElementOptional.prototype = {
        getArgs: function() {
            return [this.optionalPart];
        },

        copy: function(args) {
            if(args.length != 1) {
                throw "Invalid argument";
            }

            // FIXME: Should we clone the attributes too?
            var result = new ns.ElementOptional(this.expr);
            return result;
        },

        getVarsMentioned: function() {
            return this.optionalPart.getVarsMentioned();
        },

        copySubstitute: function(fnNodeMap) {
            return new ns.ElementOptional(this.optionalPart.copySubstitute(fnNodeMap));
        },

        flatten: function() {
            return new ns.ElementOptional(this.optionalPart.flatten());
        },

        toString: function() {
            return "Optional {" + this.optionalPart + "}";
        }
    };


    ns.ElementUnion = function(elements) {
        this.elements = elements ? elements : [];
    };

    ns.ElementUnion.classLabel = 'ElementUnion';

    ns.ElementUnion.prototype = {
        getArgs: function() {
            return this.elements;
        },

        copy: function(args) {
            var result = new ns.ElementUnion(args);
            return result;
        },

        getVarsMentioned: function() {
            var result = [];
            for(var i in this.elements) {
                result = _.union(result, this.elements[i].getVarsMentioned());
            }
            return result;
        },

        copySubstitute: function(fnNodeMap) {
            var tmp = _.map(this.elements, function(element) { return element.copySubstitute(fnNodeMap); });

            return new ns.ElementUnion(tmp);
        },

        flatten: function() {
            var tmp = _.map(this.elements, function(element) { return element.flatten(); });

            return new ns.ElementUnion(tmp);
        },

        toString: function() {
            return "{" + this.elements.join("} Union {") + "}";
        }
    };


    ns.ElementTriplesBlock = function(triples) {
        this.triples = triples ? triples : [];
    };

    ns.ElementTriplesBlock.classLabel = 'ElementTriplesBlock';

    ns.ElementTriplesBlock.prototype = {
        getArgs: function() {
            return [];
        },

        copy: function(args) {
            if(args.length != 0) {
                throw "Invalid argument";
            }

            var result = new ns.ElementTriplesBlock(this.triples);
            return result;
        },

        getTriples: function() {
            return this.triples;
        },

        addTriples: function(otherTriples) {
            this.triples = this.triples.concat(otherTriples);
        },

        uniq: function() {
            this.triples = ns.uniqTriples(this.triples);
    //this.triples = _.uniq(this.triples, false, function(x) { return x.toString(); });
        },

        copySubstitute: function(fnNodeMap) {
            var newElements = _.map(this.triples, function(x) { return x.copySubstitute(fnNodeMap); });
            return new ns.ElementTriplesBlock(newElements);
        },

        getVarsMentioned: function() {
            var result = [];
            for(var i in this.triples) {
                result = _.union(result, this.triples[i].getVarsMentioned());
            }
            return result;
        },

        flatten: function() {
            return this;
        },

        toString: function() {
            return this.triples.join(" . ");
        }
    };


    ns.ElementGroup = function(elements) {
        this.elements = elements ? elements : [];
    };

    ns.ElementGroup.classLabel = 'ElementGroup';

    ns.ElementGroup.prototype = {
        getArgs: function() {
            return this.elements;
        },

        copy: function(args) {
            var result = new ns.ElementTriplesBlock(args);
            return result;
        },

        copySubstitute: function(fnNodeMap) {
            var newElements = _.map(this.elements, function(x) { return x.copySubstitute(fnNodeMap); });
            return new ns.ElementGroup(newElements);
        },

        getVarsMentioned: function() {
            var result = [];
            for(var i in this.elements) {
                result = _.union(result, this.elements[i].getVarsMentioned());
            }
            return result;
        },

        toString: function() {
            //return this.elements.join(" . ");
            return ns.joinElements(" . ", this.elements);
        },


        flatten: function() {
            var processed = ns.ElementUtils.flatten(this.elements);

            if(processed.length === 1) {
                return processed[0];
            } else {
                return new ns.ElementGroup(ns.flattenElements(processed));
            }
        }
    };



    /**
     * Bottom up
     * - Merge ElementTripleBlocks
     * - Merge ElementGroups
     */
    ns.flattenElements = function(elements) {
        var result = [];

        var triples = [];

        var tmps = [];
        _.each(elements, function(item) {
            if(item instanceof ns.ElementGroup) {
                tmps.push.apply(tmps, item.elements);
            } else {
                tmps.push(item);
            }
        });

        _.each(tmps, function(item) {
            if(item instanceof ns.ElementTriplesBlock) {
                triples.push.apply(triples, item.getTriples());
            } else {
                result.push(item);
            }
        });

        if(triples.length > 0) {
            var ts = ns.uniqTriples(triples);

            result.unshift(new ns.ElementTriplesBlock(ts));
        }

        //console.log("INPUT ", elements);
        //console.log("OUTPUT ", result);

        return result;
    };

    ns.joinElements = function(separator, elements) {
        var strs = _.map(elements, function(element) { return "" + element; });
        var filtered = _.filter(strs, function(str){ return str.length != 0; });

        return filtered.join(separator);
    };


    ns.newUnaryExpr = function(ctor, args) {
        if(args.length != 1) {
            throw "Invalid argument";
        }

        var newExpr = args[0];

        var result = new ctor(newExpr);
        return result;
    };


    ns.newBinaryExpr = function(ctor, args) {
        if(args.length != 2) {
            throw "Invalid argument";
        }

        var newLeft = args[0];
        var newRight = args[1];

        var result = new ctor(newLeft, newRight);
        return result;
    };





    /*
     * Not used. Distinct is part of the query object - or at least I hope it to be.
     */
//	ns.E_Distinct = function(subExpr) {
//		this.subExpr = subExpr;
//	};
//
//	ns.E_Distinct.prototype.copySubstitute = function(fnNodeMap) {
//		return new ns.E_Distinct(this.subExpr.copySubstitute(fnNodeMap));
//	};
//
//	ns.E_Distinct.prototype.getArgs = function() {
//		return [this.subExpr];
//	};
//
//	ns.E_Distinct.prototype.copy = function(args) {
//		return new ns.E_Count(this.subExpr);
//	};
//
//
//	ns.E_Distinct.prototype.toString = function() {
//		return "Distinct(" + this.subExpr +")";
//	};


//	ns.ExprVar = function(v) {
//		this.v = v;
//	};
//
//	ns.ExprVar.prototype = {
//		classLabel: 'ExprVar',
//
//		copySubstitute: function(fnNodeMap) {
//			return new ns.ExprVar(this.v.copySubstitute(fnNodeMap));
//		},
//
//		getArgs: function() {
//			return [];
//		},
//
//		copy: function(args) {
//			if(args && args > 0) {
//				throw "Invalid argument";
//			}
//
//			var result = new ns.ExprVar(this.v);
//			return result;
//		},
//
//		toString: function() {
//			return "" + this.v;
//		},
//
//		accept: function(visitor) {
//			var fn = visitor["visit" + this.classLabel];
//
//			var args = [this].concat(arguments.slice(1));
//			var result = fn.apply(visitor, args);
//			return result;
//		}
//	};




    ns.QueryType = {};
    ns.QueryType.Unknown = -1;
    ns.QueryType.Select = 0;
    ns.QueryType.Construct = 1;
    ns.QueryType.Ask = 2;
    ns.QueryType.Describe = 3;


    // TODO Duplication - ns.Order and ns.SortCondition are the same - the latter should be retained!
//	ns.OrderDir = {};
//	ns.OrderDir.Asc = 0;
//	ns.OrderDir.Desc = -1;
//
//	ns.Order = function(expr, direction) {
//		this.expr = expr;
//		this.direction = direction ? direction : ns.OrderDir.Asc;
//	};
//
//	ns.Order.prototype.toString = function() {
//
//		var result = "" + this.expr;
//
//		if(this.direction == ns.OrderDir.Desc) {
//			result = "Desc(" + result + ")";
//		}
//
//		return result;
//	};
//

    ns.VarExprList = function() {
        this.vars = [];
        this.varToExpr = {};
    };

    ns.VarExprList.prototype = {
        getVarList: function() {
            return this.vars;
        },

        getExprMap: function() {
            return this.varToExpr;
        },

        add: function(v, expr) {
            this.vars.push(v);

            if(expr) {
                this.varToExpr[v.value] = expr;
            }
        },


        addAll: function(vars) {
            this.vars.push.apply(this.vars, vars);
        },

        entries: function() {
            var result = [];
            for(var i = 0; i < this.vars.length; ++i) {
                var v = this.vars[i];
                var expr = this.varToExpr[v.value];

                result.push({v:v, expr:expr});
            }

            return result;
        },

        copySubstitute: function(fnNodeMap) {
            var result = new ns.VarExprList();

            var entries = this.entries();
            for(var i = 0; i < entries.length; ++i) {
                var entry = entries[i];
                var newVar = fnNodeMap(entry.v);
                var newExpr = entry.expr ? entry.expr.copySubstitute(fnNodeMap) : null;

                result.add(newVar, newExpr);
            }

            return result;
        },

        toString: function() {
            var arr = [];
            var projEntries = this.entries();
            for(var i = 0; i < projEntries.length; ++i) {
                var entry = projEntries[i];
                var v = entry.v;
                var expr = entry.expr;

                if(expr) {
                    arr.push("(" + expr + " As " + v + ")");
                } else {
                    arr.push("" + v);
                };
            }

            var result = arr.join(" ");
            return result;
        }
    };


    ns.SortCondition = function(expr, direction) {
        this.expr = expr;
        this.direction = direction;
    };

    ns.SortCondition.prototype = {
            getExpr: function() {
                return this.expr;
            },

            getDirection: function() {
                return this.direction;
            },

            toString: function() {
                var result;
                if(this.direction >= 0) {
                    result = "Asc(" + this.expr + ")";
                } else if(this.direction < 0) {
                    result = "Desc(" + this.expr + ")";
                }

                return result;
            },

            copySubstitute: function(fnNodeMap) {
                var exprCopy = this.expr.copySubstitute(fnNodeMap);

                var result = new ns.SortCondition(exprCopy, this.direction);

                return result;
            }
    };


    ns.Query = function() {
        this.type = 0; // select, construct, ask, describe

        this.distinct = false;
        this.reduced = false;

        this.isResultStar = false;

        this.projectVars = new ns.VarExprList();
        //this.projectVars = []; // The list of variables to appear in the projection
        //this.projectExprs = {}; // A map from variable to an expression

        //this.projection = {}; // Map from var to expr; map to null for using the var directly

        //this.order = []; // A list of expressions

        this.groupBy = [];
        this.orderBy = [];


        this.elements = [];

        this.constructTemplate = null;

        this.limit = null;
        this.offset = null;
    };


    ns.Query.prototype = {
        getElements: function() {
            return this.elements;
        },

        getProjectVars: function() {
            return this.projectVars;
        },

        setProjectVars: function(projectVars) {
            this.projectVars = projectVars;
        },

        getGroupBy: function() {
            return this.groupBy;
        },

        getOrderBy: function() {
            return this.orderBy;
        },

        toStringOrderBy: function() {
            var result = (this.orderBy && this.orderBy.length > 0)
                ? "Order By " + this.orderBy.join(" ") + " "
                : "";
                //console.log("Order: ", this.orderBy);
            return result;
        },

        toStringGroupBy: function() {
            var result = (this.groupBy && this.groupBy.length > 0)
                ? "Group By " + this.groupBy.join(" ") + " "
                : "";
                //console.log("Order: ", this.orderBy);
            return result;
        }
    };


    ns.fnIdentity = function(x) { return x; };

    ns.Query.prototype.clone = function() {
        return this.copySubstitute(ns.fnIdentity);
    };

    ns.Query.prototype.flatten = function() {
        var result = this.clone();

        var tmp = _.map(result.elements, function(element) { return element.flatten(); });

        var newElements = ns.flattenElements(tmp);

        result.elements = newElements;

        return result;
    };

    ns.Query.prototype.copySubstitute = function(fnNodeMap) {
        var result = new ns.Query();
        result.type = this.type;
        result.distinct = this.distinct;
        result.reduced = this.reduced;
        result.isResultStar = this.isResultStar;
        result.limit = this.limit;
        result.offset = this.offset;

        result.projectVars = this.projectVars.copySubstitute(fnNodeMap);

        //console.log("PROJECTION  " + this.projectVars + " --- " + result.projectVars);

        /*
        for(key in this.projection) {
            var value = this.projection[key];

            var k = fnNodeMap(ns.Node.v(key));
            var v = value ? value.copySubstitute(fnNodeMap) : null;

            result.projection[k] = v;
        }*/

        if(this.constructTemplate) {
            result.constructTemplate = this.constructTemplate.copySubstitute(fnNodeMap);
        }

        result.orderBy = this.orderBy == null
            ? null
            :  _.map(this.orderBy, function(item) { return item.copySubstitute(fnNodeMap); });

        result.groupBy = this.groupBy == null
            ? null
            :  _.map(this.groupBy, function(item) { return item.copySubstitute(fnNodeMap); });


        result.elements = _.map(this.elements, function(element) { return element.copySubstitute(fnNodeMap); });

        //console.log("CLONE ORIG " + this);
        //console.log("CLONE RES " + result);

        return result;
    };


    /**
     * Convenience function for setting limit, offset and distinct from JSON
     *
     * @param options
     */
    ns.Query.prototype.setOptions = function(options) {
        if(typeof options === 'undefined') {
            return;
        }

        if(typeof options.limit !== 'undefined') {
            this.setLimit(options.limit);
        }

        if(typeof(options.offset) !== 'undefined') {
            this.setOffset(options.offset);
        }

        if(typeof(options.distinct) !== 'undefined') {
            this.setDistinct(options.distinct);
        }
    };

    ns.Query.prototype.setOffset = function(offset) {
        this.offset = offset ? offset : null;
    };

    ns.Query.prototype.setLimit = function(limit) {
        if(limit === 0) {
            this.limit = 0;
        } else {
            this.limit = limit ? limit : null;
        }
    };

    ns.Query.prototype.setDistinct = function(enable) {
        this.distinct = (enable === true) ? true : false;
    };

    ns.Query.prototype.toString = function() {
        switch(this.type) {
        case ns.QueryType.Select: return this.toStringSelect();
        case ns.QueryType.Construct: return this.toStringConstruct();

        }
    };


    ns.Query.prototype.toStringProjection = function() {
        if(this.isResultStar) {
            return "*";
        }

        return "" + this.projectVars;
    };


    ns.Query.prototype.toStringLimitOffset = function() {
        var result = "";

        if(this.limit != null) {
            result += " Limit " + this.limit;
        }

        if(this.offset != null) {
            result += " Offset " + this.offset;
        }

        return result;
    };



    ns.Query.prototype.toStringSelect = function() {
        var distinctStr = this.distinct ? "Distinct " : "";

        //console.log("Elements: ", this.elements);
        var result = "Select " + distinctStr + this.toStringProjection() + " {" + ns.joinElements(" . ", this.elements) + "} " + this.toStringGroupBy() + this.toStringOrderBy() + this.toStringLimitOffset();

        return result;
    };

    ns.Query.prototype.toStringConstruct = function() {
        var result = "Construct " + this.constructTemplate + " {" + ns.joinElements(" . ", this.elements) + "}" + this.toStringOrderBy() + this.toStringLimitOffset();

        return result;
    };



    /**
     * Creates a new (compound) expressions from an array
     * of individual exrpessions.
     * [a, b, c, d] with ctor set to "E_LogicalAnd" (abbr. And) will become
     * And(And(a, b), And(c, d))
     *
     */
    ns.opifyBalanced = function(exprs, ctor) {
        //console.warn("Constructor", ctor);

        if(exprs.length === 0) {
            return null;
        }

        var open = exprs;

        while(open.length > 1) {
            var next = [];

            for(var i = 0; i < open.length; i+=2) {
                var hasSecond = i + 1 < open.length;

                var a = open[i];

                if(hasSecond) {
                    b = open[i + 1];
                    next.push(new ctor(a, b));
                } else {
                    next.push(a);
                };
            }

            open = next;
        }

        return open[0];
    };

    ns.opify = ns.opifyBalanced;



    /*
    var testElement = new ns.ElementTriplesBlock([]);

    var json = serializer.serialize(testElement);

    alert('serialized: ' + JSON.stringify(json));


    var obj = serializer.deserialize(json);
    alert('deserialized: ' + JSON.stringify(obj));

    //serializeElement(testElement);
    */

})();

// Move some utility functions from Elements here
(function() {

    var col = Jassa.utils.collections;

    var ns = Jassa.sparql;

    /**
     * Another class that mimics Jena's behavour.
     *
     * @param prefix
     * @param start
     * @returns {ns.GenSym}
     */
    ns.GenSym = function(prefix, start) {
        this.prefix = prefix ? prefix : 'v';
        this.nextValue = start ? start : 0;
    };

    ns.GenSym.prototype.next = function() {
        ++this.nextValue;

        var result = this.prefix + "_" + this.nextValue;

        return result;
    };


    /**
     *
     * @param generator
     * @param blacklist Array of strings
     * @returns {ns.GeneratorBlacklist}
     */
    ns.GeneratorBlacklist = function(generator, blacklist) {
        this.generator = generator;
        this.blacklist = blacklist;
    };

    ns.GeneratorBlacklist.prototype = {
        next: function() {
            var result;

            do {
                result = this.generator.next();
            } while(_(this.blacklist).contains(result));

            return result;
        }
    };

    ns.fnToString = function(x) {
        return x.toString();
    };

    ns.fnGetVarName = function(x) {
        return x.getName();
    };



    ns.ElementUtils = {
        flatten: function(elements) {
            var result = _.map(elements, function(element) { return element.flatten(); });

            return result;
        },

        /**
         * Returns a map that maps *each* variable from vbs to a name that does not appear in vas.
         */
        createDistinctVarMap: function(vas, vbs, generator) {
            var vans = vas.map(ns.fnGetVarName);
            var vbns = vbs.map(ns.fnGetVarName);

            // Get the var names that are in common
            //var vcns = _(vans).intersection(vbns);

            if(generator == null) {
                var g = new ns.GenSym('v');
                generator = new ns.GeneratorBlacklist(g, vans);
            }

            // Rename all variables that are in common
            var result = new col.HashBidiMap(ns.fnNodeEquals);
            //var rename = {};

            _(vbs).each(function(oldVar) {
                var vbn = oldVar.getName();

                var newVar;
                if(_(vans).contains(vbn)) {
                    var newName = generator.next();
                    newVar = ns.Node.v(newName);

                } else {
                    newVar = oldVar;
                }

                //rename[vcn] = newVar;

                // TODO Somehow re-use existing var objects...
                //var oldVar = ns.Node.v(vcn);

                result.put(oldVar, newVar);
            });

            return result;
        },

        /**
         * distinctMap is the result of making vbs and vas distinct
         *
         * [?s ?o] [?s ?p] join on ?o = ?s
         *
         * Step 1: Make overlapping vars distinct
         * [?s ?o] [?x ?p] -> {?s: ?x, ?p: ?p}
         *
         * Step 2: Make join vars common again
         * [?s ?o] [?x ?s] -> {?s: ?x, ?p: ?s}
         */
        createJoinVarMap: function(sourceVars, targetVars, sourceJoinVars, targetJoinVars, generator) {

            if(sourceJoinVars.length != targetJoinVars.length) {
                console.log('[ERROR] Cannot join on different number of columns');
                throw 'Bailing out';
            }

            var result = ns.ElementUtils.createDistinctVarMap(sourceVars, targetVars, generator);

            for(var i = 0; i < sourceJoinVars.length; ++i) {
                var sourceJoinVar = sourceJoinVars[i];
                var targetJoinVar = targetJoinVars[i];

                // Map targetVar to sourceVar
                result.put(targetJoinVar, sourceJoinVar);
                //rename[targetVar.getName()] = sourceVar;
            }

            return result;
        },

        /**
         * Var map must be a bidi map
         */
        createRenamedElement: function(element, varMap) {
            var fnSubst = function(v) {
                var result = varMap.get(v);//[v.getName()];
                return result;
            };

            //debugger;
            var newElement = element.copySubstitute(fnSubst);

            return newElement;
        },


        /**
         * Rename all variables in b that appear in the array of variables vas.
         *
         *
         */
//		makeElementDistinct: function(b, vas) {
//			//var vas = a.getVarsMentioned();
//			var vbs = b.getVarsMentioned();
//
//			var vans = vas.map(ns.fnGetVarName);
//			var vbns = vbs.map(ns.fnGetVarName);
//
//			// Get the var names that are in common
//			var vcns = _(vans).intersection(vbns);
//
//			var g = new ns.GenSym('v');
//			var gen = new ns.GeneratorBlacklist(g, vans);
//
//			// Rename all variables that are in common
//			var rename = new col.HashBidiMap(ns.fnNodeEquals);
//			//var rename = {};
//
//			_(vcns).each(function(vcn) {
//				var newName = gen.next();
//				var newVar = ns.Node.v(newName);
//				//rename[vcn] = newVar;
//
//				// TODO Somehow re-use existing var objects...
//				var oldVar = ns.Node.v(vcn);
//
//				rename.put(oldVar, newVar);
//			});
//
//			console.log('Common vars: ' + vcns + ' rename: ' + JSON.stringify(rename.getMap()));
//
//			var fnSubst = function(v) {
//				var result = rename.get(v);//[v.getName()];
//				return result;
//			};
//
//			//debugger;
//			var newElement = b.copySubstitute(fnSubst);
//
//			var result = {
//				map: rename,
//				element: newElement
//			};
//
//			return result;
//		}
    };


})();/**
 * Sparql endpoint class.
 * Allows execution of sparql queries against a preconfigured service
 *
 */
(function() {

    var ns = Jassa.sparql;

    /**
     * SparqlServiceHttp
     *
     *
     * @param serviceUri The HTTP service URL where to send the query to
     * @param defaultGraphUris The RDF graphs on which to run the query by default
     * @param httpArgs A JSON object with additional arguments to include in HTTP requests
     */
    ns.SparqlServiceHttp = function(serviceUri, defaultGraphUris, httpArgs) {
        this.serviceUri = serviceUri;
        this.setDefaultGraphs(defaultGraphUris);

        this.httpArgs = httpArgs;
    };

    ns.SparqlServiceHttp.prototype = {

        /**
         * This method is intended to be used by caches,
         *
         * A service is not assumed to return the same result for
         * a query if this method returned different hashes.
         *
         *
         */
        getStateHash: function() {
            var idState = {
                    serviceUri: this.serviceUri,
                    defaultGraphUris: this.defaultGraphUris
            }

            var result = JSON.stringify(idState);

            return result;
        },

        setDefaultGraphs: function(uriStrs) {
            this.defaultGraphUris = uriStrs ? uriStrs : [];
        },

        getDefaultGraphs: function() {
            return this.defaultGraphUris;
        },

        execAny: function(query, options) {

            //console.log("Preparing SPARQL query: " + query);

            // TODO Make this a switch
            if(true) {
                if(query.flatten) {
                    var before = query;
                    query = before.flatten();

                    //console.log("FLATTEN BEFORE: " + before, before);
                    //console.log("FLATTEN AFTER:"  + query, query);
                }
            }


            // Force the query into a string
            var queryString = "" + query;

            if(!queryString) {
                console.error("Empty queryString - should not happen");
            }

//			if(this.proxyServiceUri) {
//				httpOptions[this.proxyParamName] = serviceUri;
//				serviceUri = this.proxyServiceUri;
//			}


            var result = ns.execQuery(this.serviceUri, this.defaultGraphUris, queryString, this.httpArgs, options);

            return result;
        },


        execSelect: function(query, options) {
            return this.execAny(query, options);
        },

        execAsk: function(query, options) {
            return this.execAny(query, options).pipe(function(json) { return json['boolean']; });
        },

        // TODO What to return: e.g. RdfJson vs RdfQuery
        execConstruct: function(query, options) {
            return this.execAny(query, options);
        },


        execDescribe: function(query, options) {
            return this.execAny(query, options);
        }
    };


    /**
     *
     * SparqlServiceDelay
     */
    ns.SparqlServiceDelay = function(delegate, delay) {
        this.delegate = delegate;
        this.scheduler = new Scheduler(delay);
    };

    ns.SparqlServiceDelay.prototype = {
        execSelect: function(queryString, callback) {
            return delegate.execSelect(queryString, callback);
        },

        execAsk: function(queryString, callback) {
            return delegate.execAsk(queryString, callback);
        }
    };



    // Great! Writing to the object in a deferred done handler causes js to freeze...
    ns.globalSparqlCache = {};

    //ns.globalSparqlCacheQueue = [];

    /**
     * Adapted from http://www.openlinksw.com/blog/~kidehen/?id=1653
     *
     * @param baseURL
     * @param query
     * @param callback
     * @param format
     */
    ns.execQuery = function(baseURL, defaultGraphUris, query, httpArgsEx, ajaxOptions) {
        var options = {};

        if(ajaxOptions == null) {
            ajaxOptions = {};
        }

        options.format = ajaxOptions.format ? ajaxOptions.format : 'application/json';

        var params = _.map(defaultGraphUris, function(item) {
            var pair = {key: "default-graph-uri", value: item };
            return pair;
        });

        params.push({key: "query", value: query});

        _.each(httpArgsEx, function(v, k) {

            if(_(v).isArray()) {
                for(var i = 0; i < v.length; ++i) {
                    var t = v[i];

                    params.push({key: k, value: t});
                }
            } else {
                params.push({key: k, value: v});
            }
        });

        var querypart = '';
        _.each(params, function(param) {
            querypart += param.key + '=' + encodeURIComponent(param.value) + '&';
        });

        var url = baseURL + '?' + querypart;

        var ajaxObj = {
            url: url,
            dataType: 'json'
        };

        if(ajaxOptions) {
            _.extend(ajaxObj, ajaxOptions);
        }


        var useCache = false;

        var data = null;
        var hash = null;


        var cache = ns.globalSparqlCache;
        //var cacheQueue = ns.globalSparqlCacheQueue;

//		while(cacheQueue.length > 0) {
//			var item = cacheQueue.pop();
//
//			cache[item.hash] = item.response;
//		}

        if(useCache) {
            hash = JSONCanonical.stringify(ajaxObj); //JSONCanonical.stringify(ajaxObj);
            var rawData = cache[hash];
            if(rawData) {
                data = JSON.parse(rawData);
            }
            //console.log('SPARQL Data for hash ' + hash, data);
        }

        var result = jQuery.Deferred();
        if(data) {
            result.resolve(data);
        } else {
            var result = jQuery.ajax(ajaxObj);

            if(useCache) {
                result.pipe(function(response) {


                    cache[hash] = JSON.stringify(response);
                    //alert(response);
                    //c[hash] = response;
                    //cache[hash.substr(0, 6)] = response;
                    //cacheQueue.push({hash: hash, response: response});
                });

                //ns.running[hash] = result;
            }
        }

        return result;
    };

})();

(function() {

    // FIXME: Maybe rename to SparqlServicePaginate(d)?
    var ns = Jassa.sparql;


    ns.Paginator = function(query, pageSize) {
        this.query = query;
        this.nextOffset = query.offset ? query.offset : 0;
        this.nextRemaining = (query.limit || query.limit === 0) ? query.limit : null;

        this.pageSize = pageSize;
    };


    // Returns the next limit and offset
    ns.Paginator.prototype.next = function() {
        this.query.offset = this.nextOffset === 0 ? null : this.nextOffset;

        if(this.nextRemaining == null) {
            this.query.limit = this.pageSize;
            this.nextOffset += this.pageSize;
        } else {
            var limit = Math.min(this.pageSize, this.nextRemaining);
            this.nextOffset += limit;
            this.nextRemaining -= limit;

            if(limit === 0) {
                return null;
            }

            this.query.limit = limit;
        }

        return this.query;
    };

    ns.SparqlServicePaginator = function(backend, pageSize) {
        this.backend = backend;
        this.pageSize = pageSize ? pageSize : 0;
    };

    ns.SparqlServicePaginator.prototype = {
        getStateHash: function() {
            return this.backend.getStateHash();
        }
    };

    /*
    ns.SparqlServicePaginator.prototype.executeConstructRec = function(paginator, prevResult, deferred) {

    };
    */

    ns.SparqlServicePaginator.prototype.executeSelectRec = function(paginator, prevResult, deferred) {
        var query = paginator.next();
        if(!query) {
            deferred.resolve(prevResult);
            return;
        }

        var self = this;


        //console.log("Backend: ", this.backend);

        var queryExecution = this.backend.executeSelect(query);
        queryExecution.done(function(jsonRs) {

            if(!jsonRs) {
                throw "Null result set for query: " + query;
            }

            // If result set size equals pageSize, request more data.
            var result;
            if(!prevResult) {
                result = jsonRs;
            } else {
                prevResult.results.bindings = prevResult.results.bindings.concat(jsonRs.results.bindings);
                result = prevResult;
            }

            var resultSetSize = jsonRs.results.bindings.length;
            //console.debug("ResultSetSize, PageSize: ", resultSetSize, self.pageSize);
            if(resultSetSize < self.pageSize) {
                deferred.resolve(result);
            } else {
                return self.executeSelectRec(paginator, result, deferred);
            }

        }).fail(function() {
            deferred.fail();
        });
    };

    ns.SparqlServicePaginator.prototype.executeSelect = function(query) {
        var clone = query.clone();
        var paginator = new ns.Paginator(clone, this.pageSize);

        var deferred = $.Deferred();

        this.executeSelectRec(paginator, null, deferred);

        return deferred.promise();
    };

    ns.SparqlServicePaginator.prototype.executeConstruct = function(query) {
        console.error("Not implemented yet");
    };

})();

(function() {

    var ns = Jassa.sparql;


    ns.evaluators = {
        '&&': function() {

        }

    };


    ns.ExprEvaluator = Class.create({

        eval: function(expr, binding) {

            var result;

            if(expr.isVar()) {
                var e = expr.getExprVar();
                result = this.evalExprVar(e, binding);
            }
            else if(expr.isFunction()) {
                var e = expr.getFunction();
                result = this.evalExprFunction(e, binding);
            }
            else if(expr.isConstant()) {
                var e = expr.getConstant();
                result = this.evalConstant(e, binding);
            }
            else {
                throw 'Unsupported expr type';
            }

            return result;
        },


        evalExprVar: function(expr, binding) {
            //console.log('Expr' + JSON.stringify(expr));
            var v = expr.asVar();

            var node = binding.get(v);

            var result;
            if(node == null) {
                //console.log('No Binding for variable "' + v + '" in ' + expr + ' with binding ' + binding);
                //throw 'Bailing out';
                result = ns.NodeValue.makeString("(null)");
            } else {
                result = ns.NodeValue.makeNode(node);
            }

            return result;
        },


        evalExprFunction: function(expr, binding) {

        },

        evalNodeValue: function(expr, binding) {
        }

    });


})();(function() {

    var ns = Jassa.sponate;


//	ns.Map = Class.create({
//		initialize: function(fnEquals, fnHash) {
//			this.fnHash = fnHash ? fnHash : (function(x) { return '' + x; });
//			this.hashToItems = {};
//		},
//
//		put: function(key, val) {
//			var hash = fnHash(key);
//
//		}
//	});


    /**
     * Datastructure for a map which retains order of inserts
     *
     */
    ns.MapList = Class.create({
        initialize: function() {
            this.items = [];
            this.keyToIndex = {};
        },

        put: function(key, item) {
            if(key == null) {
                console.log('key must not be null');
                throw 'Bailing out';
            }

            var index = this.keyToIndex[key];
            if(index) {
                console.log('Index already existed');
                throw 'Bailing out';
            }

            index = this.items.length;
            this.items.push(item);

            this.keyToIndex[key] = index;
        },

        get: function(key) {
            var index = this.keyToIndex[key];

            var result = (index == null) ? null : this.items[index];

            return result;
        },

        getByIndex: function(index) {
            return this.items[index];
        },

        getItems: function() {
            return this.items;
        },

        getKeyToIndex: function() {
            return this.keyToIndex;
        }

//		asArray: function() {
//			return this.items.slice(0);
//		},
//
//		asMap: function() {
//
//		}
    });


})();(function() {

    var sparql = Jassa.sparql;
    var col = Jassa.utils.collections;

    var ns = Jassa.sponate;


    /**
     * A path of attributes.
     *
     * Just an array of attribute names.
     *
     *
     */
    ns.AttrPath = Class.create({
        initialize: function(steps) {
            this.steps = steps ? steps : [];
        },

        getSteps: function() {
            return this.steps;
        },

        toString: function() {
            return this.steps.join('.');
        },

        slice: function(start, end) {
            var result = this.steps.slice(start, end);
            return result;
        },

        first: function() {
            return this.steps[0];
        },

        at: function(index) {
            return this.steps[index];
        },

        concat: function(that) {
            var tmp = this.steps.concat(that.getSteps());
            var result = new ns.AttrPath(tmp);
            return result;
        },


        /**
         * Retrieve the value of a path in a json document
         *
         */
        find: function(doc) {
            var result = doc;

            var steps = this.steps;
            for(var i = 0; i < steps.length; ++i) {
                var attr = steps[i];

                if(!_(result).isObject()) {
                    console.log('[ERROR] Cannot access attribute of non-object', this.steps, doc, result);
                    throw 'Bailing out';
                }

                result = result[attr];
            }

            return result;
        }
    });

    ns.AttrPath.parse = function(str) {
        var steps = str.split('.');

        return new ns.AttrPath(steps);
    };



    /*
     * patterns
     *
     * This object's state are the 'blue brint' for building the json documents from sparql bindings
     *
     */

    ns.callVisitor = function(name, self, args) {

//		if(self !== this) {
//			console.log('Different this pointers');
//		}

        // The first argument is the visitor
        var visitor = args[0];

        var fn = visitor[name];

        if(!fn) {
            console.log('[ERROR] No visitor with name ' + name + ' in ', self);
            throw 'Bailing out';
        }

        var tmp = Array.prototype.slice.call(args, 1);
        var xargs = [self].concat(tmp);
        //console.log('xargs', xargs.length, xargs);

        //debugger;
        var result = fn.apply(visitor, xargs);

        return result;

    };

    /**
     *
     *
     */
    ns.Pattern = Class.create({
        callVisitor: function(name, self, args) {
            var result = ns.callVisitor(name, self, args);
            return result;
        },

        accept: function() {
            throw 'override me';
        },

        toString: function() {
            return 'override me';
        },

        getVarsMentioned: function() {
            throw 'override me';
        },

        /**
         * Get the list of sub patterns; empty array if none
         */
        getSubPatterns: function() {
            throw 'override me';
        },

        $getReferences: function(result) {
            throw 'override me';
        },

        /**
         * Find a pattern by an object of type ns.AttrPath.
         * If a string is passed, it will be parsed first.
         *
         *
         */
        findPattern: function(rawAttrPath, start) {

            var attrPath;
            if(_(attrPath).isString()) {
                attrPath = ns.AttrPath.parse(rawAttrPath);
            } else {
                attrPath = rawAttrPath();
            }

            start = start ? start : 0;

            var result = this.$findPattern(attrPath, start);
            return result;
        },

        $findPattern: function() {
            console.log('[ERROR] "findPattern" is not supported on this kind of object');
            throw 'Bailing out';
        }
    });


    ns.Iterator = Class.create({
        next: function() {
            throw 'Not overridden';
        },

        hasNext: function() {
            throw 'Not overridden';
        }
    });


    ns.IteratorAbstract = Class.create(ns.Iterator, {
        initialize: function() {
            this.current = null;
            this.advance = true;
            this.finished = false;
        },

        finish: function() {
            this.finished = true;

            this.close();
            return null;
        },

        $prefetch: function() {
//			try {
            this.current = this.prefetch();
//			}
//			catch(Exception e) {
//				current = null;
//				logger.error("Error prefetching data", e);
//			}
        },

        hasNext: function() {
            if(this.advance) {
                this.$prefetch();
                this.advance = false;
            }

            return this.finished == false;
        },

        next: function() {
            if(this.finished) {
                throw 'No more elments';
            }

            if(this.advance) {
                this.$prefetch();
            }

            this.advance = true;
            return this.current;
        },


        prefetch: function() {
            throw 'Not overridden';
        }
    });



/*
    ns.IteratorDepthFirst = Class.create({
        initialize: function(node, fnGetChildern, fnGetValue) {
            this.fnGetChildren = fnGetChildren;
            this.fnGetValue = fnGetValue;
        },

        prefetch: function() {

        }
    });
*/

    ns.PatternUtils = {
        /**
         * Get all patterns in a pattern
         */
        getRefs: function(pattern) {
            var result = [];

            var fn = function(pattern) {
                var proceed = true
                if(pattern instanceof ns.PatternRef) {
                    result.push(pattern);
                    proceed = false;
                }

                return proceed;
            }

            col.TreeUtils.visitDepthFirst(pattern, ns.PatternUtils.getChildren, fn);

            return result;
        },

        getChildren: function(pattern) {
            return pattern.getSubPatterns();
        }

        /**
         * Generic method for visiting a tree structure
         *
         */
//		visitDepthFirst: function(parent, fnChildren, fnPredicate) {
//			var proceed = fnPredicate(parent);
//
//			if(proceed) {
//				var children = fnChildren(parent);
//
//				_(children).each(function(child) {
//					ns.PatternUtils.visitDepthFirst(child, fnChildren, fnPredicate);
//				});
//			}
//		}

    };

    /**
     * A pattern for a single valued field.
     *
     * Can carry a name to a client side aggregator to use.
     *
     *
     */
    ns.PatternLiteral = Class.create(ns.Pattern, {
        initialize: function(expr, aggregatorName) {
            this.expr = expr;
            this.aggregatorName = aggregatorName;
        },

        getExpr: function() {
            return this.expr;
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitLiteral', this, arguments);
            return result;
        },

        toString: function() {
            return '' + this.expr;
        },

        getVarsMentioned: function() {
            var result = this.expr.getVarsMentioned();
            return result;
        },

        getSubPatterns: function() {
            return [];
        }
    });


    /**
     * A pattern for a map from *predefined* keys to patterns.
     *
     */
    ns.PatternObject = Class.create(ns.Pattern, {
        initialize: function(attrToPattern) {
            this.attrToPattern = attrToPattern;
        },

        getMembers: function() {
            return this.attrToPattern;
        },

//		putPattern: function(attr, subPattern) {
//			var p = this.attrToPattern[attr];
//			if(p) {
//				throw 'Sub pattern already set for ' + attr;
//			}
//
//			this.attrToPattern[attr] = subPattern;
//		},

        getPattern: function(attr) {
            return this.attrToPattern[attr];
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitObject', this, arguments);
            return result;
        },

        toString: function() {
            var parts = [];
            _(this.attrToPattern).each(function(v, k) { parts.push('"' + k + '": ' + v); });

            var result = '{' + parts.join(',') + '}';
            return result;
        },

        getVarsMentioned: function() {
            var result = [];

            var fnToString = (function(x) {
                //console.log('x: ' + x, x, x.toString());
                return x.toString();
            });

            _.each(this.attrToPattern, function(member, k) {
                result = result.concat(member.getVarsMentioned());
            });
            result = _.uniq(result, false, fnToString);

            return result;
        },

        $findPattern: function(attrPath, start) {
            var attr = attrPath.at(start);

            var pattern = this.attrToPattern[attr];

            var result;
            if(pattern) {
                result = pattern.findPattern(attrPath, start + 1);
            } else {
                result = null;
            }

            return result;
        },

        getSubPatterns: function() {
            var result = [];

            _.each(this.attrToPattern, function(member, k) {
                result.push(member);
            });

            return result;
        }
    });


    /**
     * A pattern for a map from *variable* keys to patters
     *
     * map[keyExpr(binding)] = pattern(binding);
     *
     * The subPattern corresponds to the element contained
     *
     * TODO An array can be seen as a map from index to item
     * So formally, PatternMap is thus the best candidate for a map, yet
     * we should add a flag to treat this pattern as an array, i.e. the groupKey as an index
     *
     */
    ns.PatternMap = Class.create(ns.Pattern, {
        initialize: function(keyExpr, subPattern, isArray) {
            this.keyExpr = keyExpr;
            this.subPattern = subPattern;
            this._isArray = isArray;
        },

        getKeyExpr: function() {
            return this.keyExpr;
        },

        getSubPattern: function() {
            return this.subPattern;
        },

        isArray: function() {
            return this._isArray;
        },

        toString: function() {
            var result = '[' + this.subPattern + ' / ' + this.keyExpr + '/' + this.type + ']';
            return result;
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitMap', this, arguments);
            return result;
        },

        getVarsMentioned: function() {
            var result = this.subPattern.getVarsMentioned();
            return result;
        },

        getSubPatterns: function() {
            return [this.subPattern];
        }
    });


    /**
     * A PatternRef represents a reference to another Mapping.
     * However, because we allow forward references, we might not be able
     * to resolve references during parsing.
     * For this reason, we first just store the original configuration
     * in the stub object, and later resolve it into a full blown refSpec.
     *
     */
    ns.PatternRef = Class.create(ns.Pattern, {
        initialize: function(stub) {
            this.stub = stub;
            this.refSpec = null;
        },

        getStub: function() {
            return this.stub;
        },

        setRefSpec: function(refSpec) {
            this.refSpec = refSpec;
        },

        getRefSpec: function() {
            return this.refSpec;
        },

        toString: function() {
            return JSON.stringify(this);
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitRef', this, arguments);
            return result;
        },

        getVarsMentioned: function() {
            var result = [];

            var stub = this.stub;
            if(stub.joinColumn != null) {
                // TODO HACK Use proper expression parsing here
                var v = rdf.Node.v(stub.joinColumn.substr(1));
                result.push(v);
            } else {
                console.log('[ERROR] No join column declared; cannot get variable');
                throw 'Bailing out';
            }


            return result;

            //if(refSpec)
        },

        getSubPatterns: function() {
            return [];
        }
    });


    /**
     * A reference to a table of which some columns are source, and others are target columns
     *
     */
    ns.JoinTableRef = Class.create({
        initialize: function(tableName, sourceColumns, targetColumns) {
            this.tableName = tableName;
            this.sourceColumns = sourceColumns;
            this.targetColumns = targetColumns;
        },

        getTableName: function() {
            return this.tableName;
        },

        getSourceColumns: function() {
            return this.sourceColumns;
        },

        getTargetColumns: function() {
            return this.targetColumn;
        },

        toString: function() {
            var result
                = '(' + this.sourceColumns.join(', ') + ') '
                + this.tableName
                + ' (' + this.targetJoinColumns.join() + ')';

            return result;
        }
    });

    /**
     *
     *
     */
    ns.TableRef = Class.create({
        initialize: function(tableName, columnNames) {
            this.tableName = tableName;
            this.columnNames = columnNames;
        },

        getTableName: function() {
            return this.tableName;
        },

        getColumnNames: function() {
            return this.columnNames;
        },

        toString: function() {
            var result = this.tableName + '(' + this.columnNames.join(', ') + ')';
            return result;
        }
    });


    /**
     * A reference to another map's pattern
     *
     */
//	ns.RefPattern = Class.create({
//		initialize: function(mapName, attrPath) {
//			this.mapName = mapName;
//			this.attrPath = attrPath;
//		},
//
//		getMapName: function() {
//			return this.mapName;
//		},
//
//		getAttrPath: function() {
//			return this.attrPath;
//		},
//
//		toString: function() {
//			var result = this.mapName + '::' + attrPath;
//			return result;
//		}
//
//	});


    /**
     * A reference to another map
     *
     */
    ns.MapRef = Class.create({
        initialize: function(mapName, tableRef, attrPath) {
            this.mapName = mapName;
            this.tableRef = tableRef;
        },

        getMapName: function() {
            return this.mapName;
        },

        getTableRef: function() {
            return this.tableRef;
        },

        getAttrPath: function() {
            return this.attrPath;
        },

        toString: function() {
            var result = this.patternRef + '/' + tableRef + '@' + attrPath;
            return result;
        }
    });

    /**
     * Specification of a reference.
     *
     *
     */
    ns.RefSpec = Class.create({

        initialize: function(sourceMapRef, targetMapRef, isArray, joinTableRef) {
            this.sourceMapRef = sourceMapRef;
            this.targetMapRef = targetMapRef;
            this.isArray = isArray;
            this.joinTableRef = joinTableRef;
        },

        getSourceMapRef: function() {
            return this.sourceMapRef;
        },

        getTargetMapRef: function() {
            return this.targetMapRef;
        },

        isArray: function() {
            this.isArray;
        },

        getJoinTableRef: function() {
            return this.joinTabelRef;
        },

        toString: function() {
            var result = this.sourceMapRef + ' references ' + this.targetMapRef + ' via ' + this.joinTableRef + ' as array? ' + this.isArray;
            return result;
        }
    });


    /*
     * Aggregators
     */

    ns.Aggregator = Class.create({
        getPattern: function() {
            throw new 'override me';
        },

        getJson: function() {
            throw 'override me';
        }
    });

    ns.AggregatorLiteral = Class.create(ns.Aggregator, {
        initialize: function(patternLiteral) {
            this.patternLiteral = patternLiteral;

            this.node = null;
        },

        getPattern: function() {
            return this.patternLiteral;
        },

        process: function(binding, context) {
            var expr = this.patternLiteral.getExpr();

            var exprEvaluator = context.exprEvaluator;

            var ex = exprEvaluator.eval(expr, binding);
            if(ex.isConstant()) {
                var c = ex.getConstant();
                var node = c.asNode();

                this.setNode(node);

            } else {
                console.log('[ERROR] Could not evaluate to constant');
                throw 'Bailing out';
            }
        },

        setNode: function(newNode) {
            var oldNode = this.node;

            if(oldNode == null) {
                this.node = newNode;
            }
            else {
                if(!oldNode.equals(newNode)) {
                    console.log('[ERROR] Value already set: Attempted to override ' + oldNode + ' with ' + newNode);
                }
            }
        },

        getJson: function() {
            var node = this.node;

            var result;
            if(node) {
                if(node.isUri()) {
                    result = node.toString();
                } else if (node.isLiteral()) {
                    result = node.getLiteralValue();
                } else {
                    throw 'Unsupported node type';
                }
            }

            return result;
        }

    });

    ns.AggregatorObject = Class.create(ns.Aggregator, {

        /**
         * An aggregator factory must have already taken
         * care of initializing the attrToAggr map.
         *
         */
        initialize: function(patternObject, attrToAggr) {
            this.pattersObject = this.patternObject;
            this.attrToAggr = attrToAggr;
        },


        process: function(binding, context) {

            _(this.attrToAggr).each(function(aggr, attr) {
                aggr.process(binding, context);
            });

        },

        getJson: function() {
            var result = {};

            _(this.attrToAggr).each(function(aggr, attr) {
                var json = aggr.getJson();
                result[attr] = json;
            });

            return result;
        }
    });


    ns.AggregatorMap = Class.create(ns.Aggregator, {
        initialize: function(patternMap) {
            this.patternMap = patternMap;

            this.keyToAggr = new ns.MapList();
        },

        getPattern: function() {
            return this.patternMap
        },

        process: function(binding, context) {
            var pattern = this.patternMap;

            var keyExpr = pattern.getKeyExpr();
            var subPattern = pattern.getSubPattern();
            var isArray = pattern.isArray();

            var exprEvaluator = context.exprEvaluator;
            var aggregatorFactory = context.aggregatorFactory;

            var keyEx = exprEvaluator.eval(keyExpr, binding);

            if(!keyEx.isConstant()) {
                console.log('[ERROR] Could not evaluate key to a constant ' + JSON.stringify(keyEx) + ' with binding ' + binding);
                throw 'Bailing out';
            }

            var key = keyEx.getConstant().asNode();

            var keyStr = '' + key;

            var aggr = this.keyToAggr.get(keyStr);

            if(aggr == null) {
                aggr = aggregatorFactory.create(subPattern);

                this.keyToAggr.put(keyStr, aggr);
            }

            aggr.process(binding, context);
        },

        getJson: function() {
            var result;

            var isArray = this.patternMap.isArray();
            if(isArray) {
                result = this.getJsonArray();
            } else {
                result = this.getJsonMap();
            }

            return result;
        },

        getJsonArray: function() {
            var result = [];

            var aggrs = this.keyToAggr.getItems();
            var result = aggrs.map(function(aggr) {
                var data = aggr.getJson();
                return data;
            });

            return result;
        },

        getJsonMap: function() {
            var result = {};

            var aggrs = this.keyToAggr.getItems();
            var keyToIndex = this.keyToAggr.getKeyToIndex();

            _(keyToIndex).each(function(index, aggr) {
                var aggr = items[index];
                var data = aggr.getJson();
                result[key] = data;
            });

            return result;
        }

    });


    ns.AggregatorRefCounter = 0;

    /**
     * TODO: An aggregatorRef cannot turn itself into a proxy,
     * instead, the parent object needs to be enhanced with proxy capabilities
     *
     * I see two options:
     * (a) We make use of the ns.Field class, and pass each aggregator the field from which it is referenced.
     * This is somewhat ugly, because then the aggregator needs to know how to react when being
     * placed into an array or an object
     *
     * (b) We make a postprocessing step of the (almost) final json and check which properties
     * and array elements point to proxy objects
     *
     * This post processing is maybe the best solution, as it reduces complexity here
     * and we separate the concerns.
     *
     */
    ns.AggregatorRef = Class.create(ns.Aggregator, {
        initialize: function(patternRef) {
            // th
            this.name = '' + (ns.AggregatorRefCounter++);

            this.patternRef = patternRef;

            this.json = null;
            //this.map = new ns.MapList();

            this.bindings = [];
        },

        /**
         * The name is used so we can refer to a specific aggregator
         *
         *
         */
        getName: function() {
            return this.name;
        },

        process: function(binding, context) {
            this.bindings.push(binding);

            //context.registryRef.addRef(this, binding)
        },

        getJson: function() {
            return this.json;
        },

        // The sponate system takes care of resolving references
        setJson: function(json) {
            this.json = json;
        }
    });


    /**
     *
     * AggregatorFactory
     *
     * Recursively instantiates aggregators based on patterns.
     *
     */
    ns.AggregatorFactory = Class.create({
        initialize: function() {
            //this.pattern = pattern;

            // Registry for custom aggregators
            //this.nameToAggregator = {};
        },

        create: function(pattern) {
            var result = pattern.accept(this);
            return result;
        },


        visitObject: function(patternObject) {
            var attrToAggr = {};

            var self = this;
            var members = patternObject.getMembers();
            _(members).each(function(attrPattern, attr) {
                var aggr = self.create(attrPattern);

                attrToAggr[attr] = aggr;
            });

            //console.log('attrToAggr ', attrToAggr);
            var result = new ns.AggregatorObject(patternObject, attrToAggr);
            return result;
        },

        visitArray: function(pattern) {
            return ns.AggregatorArray(pattern);
        },

        visitMap: function(patternMap) {
            return new ns.AggregatorMap(patternMap);
        },

        visitLiteral: function(patternLiteral) {
            return new ns.AggregatorLiteral(patternLiteral);
        },

        visitRef: function(patternRef) {
            return new ns.AggregatorRef(patternRef);
        }
    });



    /**
     * A collection to keep track of references.
     *
     * Intended to be called in AggregatorRef.process
     *
     */
    ns.RegistryRef = Class.create({
        initialize: function() {

        },

        addRef: function(aggergatorRef, binding) {

        }
    });



    ns.AggregatorFacade = Class.create({

        initialize: function(pattern) {
            this.context = {
                exprEvaluator: new sparql.ExprEvaluator(),
                aggregatorFactory: new ns.AggregatorFactory(),
                refRegistry: new ns.RegistryRef()
            };

            this.rootAggregator = this.context.aggregatorFactory.create(pattern);
        },

        process: function(binding) {
            this.rootAggregator.process(binding, this.context);
        },


        getJson: function() {
            var result = this.rootAggregator.getJson();

            return result;
        }
    });


    ns.ParserPattern = Class.create({

        initialize: function() {
            this.attrs = {
                id: 'id',
                ref: 'ref'
            };
        },

        /**
         * An array can indicate each of the following meanings:
         *
         * - [ string ]
         *   If the argument is a string, we have an array of literals,
         *   whereas the string will be interpreted as an expression.
         *
         * - [ object ]
         *
         *   If the argument is an object, the following intepretation rules apply:
         *
         *   - If there is an 'id' attribute, we interpret it as an array of objects, with the id as the grouping key,
         *     and a subPattern corresponding to the object
         *   [{ id: '?s' }]
         *
         *   - If there is a 'ref' attribute, we intepret the object as a specification of a reference
         *
         *
         *   - If neither 'id' nor 'ref' is specified ...
         *   TODO i think then the object should be interpreted as some kind of *explicit* specification, wich 'id' and 'ref' variants being syntactic sugar for them
         *
         */
        parseArray: function(val) {

            if(val.length != 1) {
                console.log('[ERROR] Arrays must have exactly one element that is either a string or an object');
                throw 'Bailing out';
            }

            var config = val[0];

            var result;
            if(_(config).isString()) {

                result = this.parseArrayLiteral(config);

            } else if(_(config).isObject()) {

                result = this.parseArrayConfig(config);

            } else {
                throw 'Bailing out';
            }

            return result;
        },

        parseArrayConfig: function(config) {

            var idAttr = this.attrs.id;
            var refAttr = this.attrs.ref;

            var hasId = config[idAttr] != null;
            var hasRef = config[refAttr] != null;

            if(hasId && hasRef) {
                console.log('[ERROR] id and ref are mutually exclusive');
                throw 'Bailing out';
            }

            var result;
            if(hasId) {

                var subPattern = this.parseObject(config);
                //console.log(config, JSON.stringify(subPattern));

                // Expects a PatternLiteral
                var idPattern = subPattern.getPattern(idAttr);
                var idExpr = idPattern.getExpr();
                result = new ns.PatternMap(idExpr, subPattern, true);

            } else if(hasRef) {
                result = this.parseArrayRef(config);
            } else {
                console.log('[ERROR] Not implemented');
                throw 'Bailing out';
            }

            return result;
        },


        /**
         * Here we only keep track that we encountered a reference.
         * We cannot validate it here, as we lack information
         *
         *
         */
        parseArrayRef: function(config) {

            var result = new ns.PatternRef(config);
            return result;
        },

        parseArrayLiteral: function() {

        },


        parseLiteral: function(val) {
            var expr = this.parseExprString(val);

            var result = new ns.PatternLiteral(expr);
            return result;
        },

        /**
         * An object is an entity having a set of fields,
         * whereas fields can be of different types
         *
         */
        parseObject: function(val) {

            var attrToPattern = {};

            var self = this;
            _(val).each(function(v, attr) {
                var v = val[attr];
                var subPattern = self.parsePattern(v);

                attrToPattern[attr] = subPattern;
            });

            var result = new ns.PatternObject(attrToPattern);
            return result;
        },

//		parsePattern: function(fieldName, val) {
//			// if the value is an array, create an array field
//			// TODO An array field can be either an array of literals or of objects
//			// How to represent them?
//			// Maybe we could have Object and Literal Fields plus a flag whether these are arrays?
//			// So then we wouldn't have a dedicated arrayfield.
//			// if the value is an object, create an object reference field
//
//			// friends: ArrayField(
//		},

        parsePattern: function(val) {

            var result;

            if(_(val).isString()) {
                result = this.parseLiteral(val);
            }
            else if(_(val).isArray()) {
                result = this.parseArray(val);
            }
            else if(_(val).isObject()) {
                result = this.parseObject(val);
            }
            else {
                throw "Unkown item type";
            }


            return result;
        },


        parseExpr: function(obj) {
            var result;

            if(_.isString(obj)) {
                result = this.parseExprString(obj);
            }

            return result;
        },

        parseExprString: function(str) {
            var result;

            if(_(str).startsWith('?')) {
                var varName = str.substr(1);
                var v = sparql.Node.v(varName);
                result = new sparql.ExprVar(v);

            } else {
                result = sparql.NodeValue.makeString(str);
                // TODO: This must be a node value
                //result = sparql.Node.plainLit(str);
            }

            // TODO Handle special strings, such as ?\tag

            //console.log('Parsed', str, 'to', result);

            return result;
        }

    });



    ns.parseExpr = function(str) {

    }

})();

(function() {

    var sparql = Jassa.sparql;

    var ns = Jassa.sponate;

    /**
     * The cursor is both a flow api and a result set / iterator.
     *
     * (Not sure I like this design, i.e. making distinct concepts look like if they were same,
     * but that's the way ppl do JavaScript, sigh)
     *
     * Calling next, hasNext or forEach starts retrieving the data
     *
     */
    ns.Cursor = Class.create({
        hasNext: function() {

        },

        next: function() {

        },


        forEach: function(fn) {
            while(this.hasNext()) {
                var json = this.next();

                fn(json);
            }
        }
    });


    ns.CursorFlow = Class.create({


        hasNext: function() {

        },

        skip: function(n) {

        },

        limit: function(n) {

        },

        sort: function(attr) {

        }

    });


    ns.QueryFlow = Class.create({
        initialize: function(store, criteria) {
            this.store = store;
            this.criteria = criteria;
        },

        /*
        find: function(criteria) {
            this.criteria = criteria;
            return this;
        },
        */

        asList: function() {
            var promise = this.execute();

            // TODO This should be part of the store facade
            var result = promise.pipe(function(it) {
                var arr = [];
                while(it.hasNext()) {
                    arr.push(it.next());
                }

                return arr;
            });

            return result;
        },

        hasNext: function() {

        },

        next: function() {

        },


        // TODO This is a hack right now - not sure how to design the execution yet
        execute: function() {
            var config = {
                criteria: this.criteria
            };

            var result = this.store.execute(config);
            return result;
        }

    });


    /**
     *
     * TODO We need to attach a post processor, e.g. for ?/ label
     *
     */
    ns.Store = Class.create({
        /**
         * A sparql service (assumed to return talis json rdf)
         *
         */
        initialize: function(service, context, mappingName) {
            this.service = service;
            this.context = context;
            this.mappingName = mappingName;
        },

        find: function(crit) {
            var criteriaParser = this.context.getCriteriaParser();


            var criteria = criteriaParser.parse(crit);

            var result = new ns.QueryFlow(this, criteria);
            return result;
        },


        execute: function(config) {
            // TODO Compile the criteria to
            // a) SPARQL filters
            // b) post processors

            var context = this.context;
            var criteria = config.criteria;

            //console.log('context', JSON.stringify(this.context), this.context.getNameToMapping());

            var mapping = this.context.getMapping(this.mappingName);


            // Resolve references if this has not been done yet
            // TODO Optimize this by caching prior resolution
            ns.ContextUtils.resolveMappingRefs(this.context, mapping);
            console.log('Refs: ', mapping.getPatternRefs());


            // Compile criterias
            var criteriaCompiler = new ns.CriteriaCompilerSparql();

            //var elementCriteria = criteriaCompiler.compile(context, mapping, criteria);
            //console.log('Compliled criteria: ' + elementCriteria);





            //console.log('mapping:', mapping);

            // Retrieve the mapping's table and the associated element
            var element = this.context.getElement(mapping.getTableName());



            var pattern = mapping.getPattern();
            //console.log('Pattern here ' + JSON.stringify(pattern));


            var vars = pattern.getVarsMentioned();
            //console.log('' + vars);


            var idExpr;
            if(pattern instanceof ns.PatternMap) {
                idExpr = pattern.getKeyExpr();
            }

            //console.log('' + pattern, idExpr);
            //console.log('idExpr' + idExpr);


            // Query generation
            var query = new sparql.Query();
            query.getElements().push(element);
            _(vars).each(function(v) { query.getProjectVars().add(v); });
            if(idExpr != null) {
                //console.log('Expr' + JSON.stringify(idExpr));

                var sc = new sparql.SortCondition(idExpr, 1);

                query.getOrderBy().push(sc);
            }
            //query.setLimit(10);


            // TODO: We need to deal with references
            var self = this;
            var processResult = function(it) {
                var instancer = new ns.AggregatorFacade(pattern);
                //var instancer = new sponate.PatternVisitorData(pattern);
                //var instancer = new sponate.FactoryAggregator();
                // TODO

                while(it.hasNext()) {
                    var binding = it.next();

                    instancer.process(binding);
                }

                var json = instancer.getJson();



                //console.log('Final json: ' + JSON.stringify(json));

                var result;
                if(_(json).isArray()) {


                    var filtered = _(json).filter(function(item) {
                        var isMatch = criteria.match(item);
                        return isMatch;
                    })

                    var all = json.length;
                    var fil = filtered.length;
                    var delta = all - fil;

                    console.log('[DEBUG] ' + delta + ' items filtered on the client ('+ fil + '/' + all + ' remaining) using criteria ' + JSON.stringify(criteria));


                    result = new ns.IteratorArray(filtered);

                } else {
                    throw 'Implement me';
                }

                return result;
            };


            var result = self.service.execSelect(query).pipe(processResult);

            return result;
            //console.log('' + query);



            // TODO We are no longer retrieving triples, but objects
            // Thus limit and offset applies to entities -> sub query!
        }
    });


    ns.QueryPlan = Class.create({
        initialize: function() {

        }
    });

})();

/*
Advanced
Novel
Grandiose
Enhanced
Library /
Api
for
Magic Sparql (Marql)
or simply: Angular + Magic Sparql = Angular Marql
*/

/*
 * Thinking about how to create the join stuff...
 *
 * We need to distinguish two levels:
 * - Projection
 * - Selection
 *
 * Generic query structure:
 *
 * Select projectionVars {
 *   { Select Distinct ?s {
 *     SelectionElement
 *   } Limit foo Offset bar }
 *   Optional {
 *      Projection(?s)
 *   }
 * }
 *
 * We can perform optimizations of the selection and projection element are isomorph, but
 * we can add this later.
 *
 *
 * Projection will always follow the join rules that have been configured in the references
 *
 * For the selection however, whenever a criteria spans accross ref boundaries, we
 * directly join in the referenced map's element as to perform the filter on the database
 *
 * This means, we need some kind of collection where we can just add in joins as we encounter them
 * In fact, this is the purpose of the CriteraRewriterSparql:
 * The result of compiling a criteria is a concept - which internally has all the joins set
 *
 * And how to do the projection when there is eager fetching?
 * Again we collect all joins, however, this time we combine them with OPTIONALS
 *
 * So what does the 'QueryPlan' or whatever object look like?
 *
 *
 * Note: Each proxyObject should also have some special attribute like
 * @proxyState or @proxyControl
 * Which then reveals which properties are the ones being proxied
 *
 * then we could do something like object['@proxyControl'].myProperty.fetch(10, 20)
 * object['@proxyControl'].myProperty.count() // This needs to trigger a batch count though
 *
 *
 * So the goal is to be able to retrieve only parts of an relation
 *
 * Actually, isn't this just like having store objects again?
 *
 * foo = store.castles.find().asList();
 * var bar =foo.owners.limit(10).find().asList();
 * bar.friends.facebook.limit(10).find(name: {$regex:...}).asList();
 *
 * find().asStores(); ->
 *
 * find()
 *
 * Yup, that's how it is.
 *
 * So if we want to do it like this, we must not fetch all values of the join column in advance,
 * but rather track the groupKey of the parent PatternMap
 *
 *
 * So what does the query plan look like?
 * Well, I don't think we need something like that -
 * we just need to satisfy all references.
 *
 * open = [initial mapping]
 * closed =[]
 *
 *
 * Compiling the criteria:
 * C
 *
 * If we hit a ref,
 *
 *
 * gen = new GenSym('a');
 * while(!open is empty) {
 *    sourceMapping = open.pop();
 *    if(closed.contains(sourceMapping)) {
 *        circular reference; create a proxy instead (we could allow a certain number of round trips though)
 *    }
 *    close.push(sourceMapping);
 *
 *    refs = sourceMapping.getRefs();
 *    var sourceAlias = gen.next(); // create a new alias for the mapping
 *    			// or maybe the alias is less for the mapping and more for its table
 *
 *    for(ref in ref) {
 *        if ref.joinType = immediate { // TODO align terminology with hibernate
 *            targetMapping = context.getMapping(ref.getTargetMappingName)
 *
 *            var targetAlias
 *
 *
 *
 *        }
 *
 *    }
 *
 *
 *
 * }
 *
 *
 * owners: [{ aggColumn: '?s', joinColumn: '?x', }]
 *
 * -> Aggregator {
 *     refSpec: { targetMapping: 'owners', aggColumn: ?s, sourcColumn: ?x, targetColumn: ?z}
 *
 *     bindings: [{?s='<>'}, {   }] // The bindings that were passed to the aggregator
 * }
 *
 *
 * Required operations:
 * - Find all aggregators with the same refSpec
 * -
 *
 * castles:
 * [{id: ?s, name:[{ref: labels, attr:name}]]
 * ?s a Castle
 *
 *
 * labels:
 * [{id: ?s, name: ?l}]
 * ?s label ?l
 *
 *
 *
 * aliasToMapping: { //Note: This points to mapping objects
 *     a: { mappingName: castles , aggregator , aggregatorRefs}
 * }
 *
 * [?s a Castle] With[] As a
 * [?x label ?l] With [x->s] As b   | b->{s->x}    x->{b, s}
 *
 * joinGraph: {
 *   root: a,
 *   joins: {
 *     a: {target: b, sourceColumns, targetColumns, optional: true}
 *   }
 * }
 *
 * For each row, k
 *
 * }
 *
 *
 */

(function() {

    /*
     * This file enhances sponate with relational json document mappings
     */

    var sparql = Jassa.sparql;
    var ns = Jassa.sponate;


    /**
     * A simple table definition
     *
     */
    ns.Table = Class.create({
        /**
         * TODO: Not sure what the type of schema should be... - is it a name or an object? Probably name.
         *
         */
        initialize: function(name, columnNames, schema) {
            this.name = name;
            this.columnNames = columnNames;
            this.schema = schema;
        },

        getName: function() {
            return this.name;
        },

        getColumnNames: function() {
            return this.columnNames;
        },

        getSchema: function() {
            return schema;
        },

        toString: function() {
            return this.name + '(' + this.columnNames.join(', ') + ')';
        }
    });


    /**
     * A fake element parser. Replace it with something better at some point.
     *
     */
    ns.SparqlParserFake = Class.create({
        initialize: function() {
            this.prefixes = {};
        },

        parseElement: function(str) {
            var vars = sparql.extractSparqlVars(str);

            var result = ns.ElementString.create(str, vars);
        }
    });


    /**
     * This class represents a relational schema.
     *
     * Right now its just table tables and their columns.
     *
     */
    ns.Schema = Class.create({
        initialize: function() {
            this.nameToTable = {};
        },

        //createTable: function(name, )
        addTable: function(table) {
            var tableName = table.getName();

            this.nameToTable[tableName] = table;
        },

        getTable: function(tableName) {
            var result = this.nameToTable[tableName];
            return result;
        }
    });


    ns.PrefixMap = Class.create({
        initialize: function(prefixes) {
            this.prefixes = prefixes ? prefixes : {};
        },

        addPrefix: function(prefix, urlBase) {
            this.prefixes[prefix] = urlBase;
        },

        getPrefix: function(prefix) {
            var result = this.prefixes[prefix];
            return result;
        },

        addJson: function(json) {
            _.extend(this.prefixes, json);
        },

        getJson: function() {
            return this.prefixes;
        }
    });


    /*
    ns.SparqlTable = Class.create({
        initialize: function(context, table, element) {
            this.context = context;
            this.table = table;
            this.element = element;
        },

        getContext: function() {
            return this.context;
        },

        getTable: function() {
            return this.table;
        },

        getElement: function() {
            return element;
        },

        toString: function() {
            return '' + this.table + ' with ' + this.element;
        }
    });
    */

    /**
     *
     * A Sponate context is the central object for storing all relevant
     * information about the mappings
     *
     * - prefixes
     * - the relational schema
     * - mappings from table to SPARQL elements
     *
     * TODO Better rename to SponateContext, as to reduce ambiguity with other context objects
     */
    ns.Context = Class.create({

        initialize: function(schema) {
            this.schema = schema ? schema : new ns.Schema();
            this.prefixMap = new ns.PrefixMap();

            // TODO We should not map to element directly, but to ElementProvider
            this.tableNameToElement = {};

            // Note: the names of mappings and tables are in different namespaces
            // In fact, in most cases tables are implicitely created - with the name of the mapping
            this.nameToMapping = {};

            this.patternParser = new ns.ParserPattern();

            this.criteriaParser = new ns.CriteriaParser();
        },

        getSchema: function() {
            return this.schema;
        },

        getPrefixMap: function() {
            return this.prefixMap;
        },

        getPatternParser: function() {
            return this.patternParser;
        },

        getTableNameToElement: function() {
            return this.tableNameToElement;
        },

        getNameToMapping: function() {
            return this.nameToMapping;
        },

        mapTableNameToElement: function(tableName, element) {
            this.tableNameToElement[tableName] = element;
        },

        addMapping: function(mapping) {
            var name = mapping.getName();
            this.nameToMapping[name] = mapping;
        },

        getMapping: function(mappingName) {
            var result = this.nameToMapping[mappingName];
            return result;
        },

        getElement: function(tableName) {
            var result = this.tableNameToElement[tableName];
            return result;
        },

        getCriteriaParser: function() {
            return this.criteriaParser;
        }
    });

    ns.ContextUtils = {

        /**
         * Creates and adds a sparql table to a context
         *
         */
        createTable: function(context, name, elementStr) {
            var prefixes = context.getPrefixMap().getJson();

            var vars = sparql.extractSparqlVars(elementStr);

            var str = sparql.expandPrefixes(prefixes, elementStr);

            var element = sparql.ElementString.create(str, vars);

            // TODO Maybe prefix them with '?' ???
            //var varNames = sparql.extractVarNames(vars);
            var colNames = vars.map(function(v) { return v.toString(); });

            var table = new ns.Table(name, colNames);

            context.getSchema().addTable(table);

            context.mapTableNameToElement(name, element);

        },


        /**
         * Resolve all reference patterns of a mapping:
         *
         *
         *
         */
//		resolveMappingRefs: function(context, mappingName) {
//			var sourceMapping = context.getMapping(mappingName);
//
//			if(sourceMapping == null) {
//				console.log('[ERROR] No mapping: ' + mappingName);
//				throw 'Bailing out';
//			}

        resolveMappingRefs: function(context, sourceMapping) {
            var patternRefs = sourceMapping.getPatternRefs();

            _(patternRefs).each(function(patternRef) {

                var stub = patternRef.getStub();
                var refSpec = ns.ContextUtils.createRefSpec(sourceMapping, stub, context);
                patternRef.setRefSpec(refSpec);

            });
        },

        /**
         * Resolves references in PatternRef objects
         * against the context
         *
         */
        createRefSpec: function(sourceMapping, stub, context) {
            var schema = context.getSchema();

            var sourceMappingName = sourceMapping.getName();
            var targetMappingName = stub.ref;

            var targetMapping = context.getMapping(targetMappingName);
            if(targetMapping == null) {
                console.log('[ERROR] Target mapping ' + targetMapping + ' not defined');
                throw 'Bailing out';
            }

            var sourceTableName = sourceMapping.getTableName();
            var targetTableName = targetMapping.getTableName();

            var sourceTable = schema.getTable(sourceTableName);
            var targetTable = schema.getTable(targetTableName);

            // Cardinality 1 means no array
            var isArray = stub.card == 1 ? false : true;

            // TODO attr path

            var sourceColumns;
            var targetColumns;

            if(stub.joinColumn) {
                sourceColumns = [stub.joinColumn];
            }

            if(stub.refJoinColumn) {
                targetColumns = [stub.refJoinColumn];
            }

    //		ns.validateColumnRefs(sourceTable, sourceColumns);
    //		ns.validateColumnRefs(targetTable, targetColumns);

            var joinTable = stub.joinTable;
            if(joinTable != null) {
                console.log('[ERROR] Implement me');
                throw 'Bailing out';
            }


            var sourceTableRef = new ns.TableRef(sourceTableName, sourceColumns);
            var targetTableRef = new ns.TableRef(targetTableName, targetColumns);

            var sourceMapRef = new ns.MapRef(sourceMappingName, sourceTableRef, null);
            var targetMapRef = new ns.MapRef(targetMappingName, targetTableRef, null);

            var result = new ns.RefSpec(sourceMapRef, targetMapRef, isArray, null);

            return result;
        }

    }

})();(function() {

    // TODO Differntiate between developer utils and user utils
    // In fact, the latter should go to the facade file

    var sparql = Jassa.sparql;
    var col = Jassa.utils.collections;
    var ns = Jassa.sponate;


    ns.ServiceSponateSparqlHttp = Class.create({
        initialize: function(rawService) {
            this.rawService = rawService;
        },

        execSelect: function(query, options) {
            var promise = this.rawService.execSelect(query, options);

            var result = promise.pipe(function(json) {
                var bindings = json.results.bindings;

                var tmp = bindings.map(function(b) {
                    //console.log('Talis Json' + JSON.stringify(b));
                    var bindingObj = sparql.Binding.fromTalisJson(b);
                    //console.log('Binding obj: ' + bindingObj);
                    return bindingObj;
                });

                var it = new ns.IteratorArray(tmp);

                //console.log()

                return it;
            });

            return result;
        }
    });


    /**
     * A factory for backend services.
     * Only SPARQL supported yet.
     *
     */
    ns.ServiceUtils = {

        createSparqlHttp: function(serviceUrl, defaultGraphUris, httpArgs) {

            var rawService = new sparql.SparqlServiceHttp(serviceUrl, defaultGraphUris, httpArgs);
            var result = new ns.ServiceSponateSparqlHttp(rawService);

            return result;
        }
    };


    /**
     * Utility class to create an iterator over an array.
     *
     */
    ns.IteratorArray = function(array, offset) {
        this.array = array;
        this.offset = offset ? offset : 0;
    };

    ns.IteratorArray.prototype = {
        hasNext: function() {
            var result = this.offset < this.array.length;
            return result;
        },

        next: function() {
            var hasNext = this.hasNext();

            var result;
            if(hasNext) {
                result = this.array[this.offset];

                ++this.offset;
            }
            else {
                result = null;
            }

            return result;
        }
    };


    /*
    ns.AliasedElement = Class.create({
        initialize: function(element, alias) {
            this.element = element;
            this.alias = alias;
        },

        getElement: function() {
            return this.element;
        },

        getAlias: function() {
            return this.alias;
        },

        toString: function() {
            return '' + this.element + ' As ' + this.alias;
        }
    });
    */

    /**
     * A convenient facade on top of a join builder
     *
     */
    ns.JoinNode = Class.create({
        initialize: function(joinBuilder, alias) {
            this.joinBuilder = joinBuilder;
            this.alias = alias;
        },

        getJoinBuilder: function() {
            return this.joinBuilder;
        },

        getElement: function() {
            return this.joinBuilder.getElement(this.alias);
        },

        getVarMap: function() {
            return this.joinBuilder.getVarMap(this.alias);
        },

        // Returns all join node object
        // joinBuilder = new joinBuilder();
        // node = joinBuilder.getRootNode();
        // node.join([?s], element, [?o]);
        //    ?s refers to the original element wrapped by the node
        //    ?o also refers to the original element of 'element'
        //
        // joinBuilder.getRowMapper();
        // joinBuilder.getElement();
        getJoinNodes: function() {
            var state = this.joinBuilder.getState(this.alias);

            var joinBuilder = this.joinBuilder;
            var result = _(state.joins).map(function(alias) {
                return this.joinBuilder.getJoinNode(alias);
            });

            return result;
        },

        join: function(sourceJoinVars, targetElement, targetJoinVars) {
            var result = this.joinBuilder.addJoin(this.alias, sourceJoinVars, targetElement, targetJoinVars);

            return result;
        }
    });


    /**
     * a: castle
     *
     *
     * b: owners
     *
     *
     */
    ns.JoinBuilderElement = Class.create({
        initialize: function(rootElement) {

            if(rootElement == null) {
                console.log('[Error] Root element must not be null');
                throw 'Bailing out';
            }


            this.usedVarNames = [];
            this.usedVars = [];

            this.aliasGenerator = new sparql.GenSym('a');
            this.varNameGenerator = new sparql.GeneratorBlacklist(new sparql.GenSym('v'), this.usedVarNames);


            this.aliasToState = {};
            this.rootAlias = this.aliasGenerator.next();


            var rootState = this.createTargetState(this.rootAlias, new col.HashBidiMap(), [], rootElement, []);

            this.aliasToState[this.rootAlias] = rootState;

            this.rootNode = rootState.joinNode; //new ns.JoinNode(rootAlias);
        },

        getRootNode: function() {
            return this.rootNode;
        },

        getJoinNode: function(alias) {
            var state = this.aliasToState[alias];

            var result = state ? state.joinNode : null;

            return result;
        },


        getState: function(alias) {
            return this.aliasToState[alias];
        },

        getElement: function(alias) {
            var state = this.aliasToState[alias];
            var result = state ? state.element : null;
            return result;
        },

//		getElement: function(alias) {
//			return this.aliasToElement[alias];
//		},
//
//		getJoinNode: function(alias) {
//			return this.aliasToJoinNode[alias];
//		},
//
//		getVarMap: function(alias) {
//			return this.aliasToVarMap[alias];
//		},

        addVars: function(vars) {

            var self = this;
            _(vars).each(function(v) {

                var varName = v.getName();
                var isContained = _(self.usedVarNames).contains(varName);
                if(!isContained) {
                    self.usedVarNames.push(varName);
                    self.usedVars.push(v);
                }
            });
        },

        createTargetState: function(targetAlias, sourceVarMap, sourceJoinVars, targetElement, targetJoinVars) {
            var sjv = sourceJoinVars.map(function(v) {
                var rv = sourceVarMap.get(v);
                return rv;
            });

            //var sourceVars = this.ge; // Based on renaming!
            var oldTargetVars = targetElement.getVarsMentioned();
            var targetVarMap = sparql.ElementUtils.createJoinVarMap(this.usedVars, oldTargetVars, sjv, targetJoinVars, this.varGenerator);

            var newTargetElement = sparql.ElementUtils.createRenamedElement(targetElement, targetVarMap);

            var newTargetVars = targetVarMap.getInverse().keyList();
            this.addVars(newTargetVars);


            var result = new ns.JoinNode(this, targetAlias);

            var targetState = {
                varMap: targetVarMap,
                joinNode: result,
                element: newTargetElement,
                joins: []
            };

            return targetState;
        },



        addJoin: function(sourceAlias, sourceJoinVars, targetElement, targetJoinVars) {
            var sourceState = this.aliasToState[sourceAlias];
            var sourceVarMap = sourceState.varMap;

            var targetAlias = this.aliasGenerator.next();
            var targetState = this.createTargetState(targetAlias, sourceVarMap, sourceJoinVars, targetElement, targetJoinVars);

            //var targetVarMap = targetState.varMap;
            //var newTargetVars = targetVarMap.getInverse().keyList();


            sourceState.joins.push(targetAlias);


            this.aliasToState[targetAlias] = targetState;

            var result = targetState.joinNode;
            return result;
        },

        getElements: function() {
            var result = [];

            var rootNode = this.getRootNode();

            col.TreeUtils.visitDepthFirst(rootNode, ns.JoinBuilderUtils.getChildren, function(node) {
                result.push(node.getElement());
                return true;
            });

            return result;
        },

        getAliasToVarMap: function() {
            var result = {};
            _(this.aliasToState).each(function(state, alias) {
                result[alias] = state.varMap;
            });

            return result;
        }


//		getVarMap: function() {
//			_.each()
//		}
    });

    ns.JoinBuilderUtils = {
        getChildren: function(node) {
            return node.getJoinNodes();
        }
    }

    ns.JoinBuilderElement.create = function(rootElement) {
        var joinBuilder = new ns.JoinBuilderElement(rootElement);
        var result = joinBuilder.getRootNode();

        return result;
    };

    ns.fnNodeEquals = function(a, b) { return a.equals(b); };

    /*
     * We need to map a generated var back to the alias and original var
     * newVarToAliasVar:
     * {?foo -> {alias: 'bar', var: 'baz'} }
     *
     * We need to map and alias and a var to the generater var
     * aliasToVarMap
     * { bar: { baz -> ?foo } }
     *
     *
     *
     *
     */
//	ns.VarAliasMap = Class.create({
//		initialize: function() {
//			// newVarToOrig
//			this.aliasToVarMap = new ns.HashMap(ns.fnNodeEquals)
//			this.newVarToAliasVar = new ns.HashMap(ns.fnNodeEquals);
//		},
//
//		/*
//		addVarMap: function(alias, varMap) {
//
//		},
//
//		put: function(origVar, alias, newVar) {
//			this.newVarToAliasVar.put(newVar, {alias: alias, v: origVar});
//
//			var varMap = this.aliasToBinding[alias];
//			if(varMap == null) {
//				varMap = new ns.BidiHashMap();
//				this.aliasToVarMap[alias] = varMap;
//			}
//
//			varMap.put(newVar, origVar);
//		},
//		*/
//
//		getOrigAliasVar: function(newVar) {
//			var entry = this.newVarToAliasVar.get(newVar);
//
//			var result = entry == null ? null : entry;
//		},
//
//		getVarMap: function(alias) {
//		}
//	});
//
//
//	ns.VarAliasMap.create = function(aliasToVarMap) {
//		var newVarToAliasVar = new ns.HashMap()
//
//	};
//

    ns.JoinElement = Class.create({
        initialize: function(element, varMap) {
            this.element = element;
        }

    });


    ns.JoinUtils = {
        /**
         * Create a join between two elements
         */
        join: function(aliasEleA, aliasEleB, joinVarsB) {
            //var aliasA = aliasEleA.

            var varsA = eleA.getVarsMentioned();
            var varsB = eleB.getVarsMentioned();


        },



        /**
         * This method prepares all the joins and mappings to be used for the projects
         *
         *
         *
         * transient joins will be removed unless they join with something that is
         * not transient
         *
         */
        createMappingJoin: function(context, rootMapping) {
            var generator = new sparql.GenSym('a');
            var rootAlias = generator.next();

            // Map<String, MappingInfo>
            var aliasToState = {};

            // ListMultimap<String, JoinInfo>
            var aliasToJoins = {};


            aliasToState[rootAlias] = {
                mapping: rootMapping,
                aggs: [] // TODO The mapping's aggregators
            };

            var open = [a];

            while(open.isEmpty()) {
                var sourceAlias = open.shift();

                var sourceState = aliasToState[sourceAlias];
                var sourceMapping = sourceState.mapping;

                ns.ContextUtils.resolveMappingRefs(this.context, sourceMapping);

                var refs = mapping.getPatternRefs();

                // For each reference, if it is an immediate join, add it to the join graph
                // TODO And what if it is a lazy join??? We want to be able to batch those.
                _(refs).each(function(ref) {
                    var targetMapRef = ref.getTargetMapRef();

                    var targetAlias = generator.next();

                    aliasToState[targetAlias] = {
                        mapping: targetMapping
                    };

                    var joins = aliasToJoins[sourceAlias];
                    if(joins == null) {
                        joins = [];
                        aliasToJoins[sourceAlias] = joins;
                    }

                    var join = {
                        targetAlias: targetAlias,
                        isTransient: true
                    };

                    joins.push(join);
                });


                var result = {
                    aliasToState: aliasToState,
                    aliasToJoins: aliasToJoins
                };

                return result;
            }
        }

    };


    ns.GraphItem = Class.create({
        initialize: function(graph, id) {
            this.graph = graph;
            this.id = id;
        },

        getGraph: function() {
            return this.graph;
        },

        getId: function() {
            return this.id;
        }
    });


    ns.Node = Class.create(ns.GraphItem, {
        initialize: function($super, graph, id) {
            $super(graph, id);
        },

        getOutgoingEdges: function() {
            var result = this.graph.getEdges(this.id);
            return result;
        }
    });


    ns.Edge = Class.create({

        initialize: function(graph, id, nodeIdFrom, nodeIdTo) {
            this.graph = graph;
            this.id = id;
            this.nodeIdFrom = nodeIdFrom;
            this.nodeIdTo = nodeIdTo;
        },

        getNodeFrom: function() {
            var result = this.graph.getNode(this.nodeIdFrom);
            return result;
        },

        getNodeTo: function() {
            var result = this.graph.getNode(this.nodeIdTo);
            return result;
        }

    });


    /**
     *
     */
    ns.Graph = Class.create({
        initialize: function(fnCreateNode, fnCreateEdge) {
            this.fnCreateNode = fnCreateNode;
            this.fnCretaeEdge = fnCreateEdge;

            this.idToNode = {};

            // {v1: {e1: data}}
            // outgoing edges
            this.nodeIdToEdgeIdToEdge = {};
            this.idToEdge = {};

            this.nextNodeId = 1;
            this.nextEdgeId = 1;
        },

        createNode: function(/* arguments */) {
            var nodeId = '' + (++this.nextNodeId);

            var tmp = Array.prototype.slice.call(arguments, 0);
            var xargs = [this, nodeId].concat(tmp);

            var result = this.fnCreateNode.apply(this, xargs);
            this.idToNode[nodeId] = result;

            return result;
        },

        createEdge: function(nodeIdFrom, nodeIdTo /*, arguments */) {
            var edgeId = '' + (++this.nextEdgeId);

            var tmp = Array.prototype.slice.call(arguments, 0);
            // TODO Maybe we should pass the nodes rather than the node ids
            var xargs = [graph, nodeIdFrom, nodeIdTo].concat(tmp);


            var result = this.fnEdgeNode.apply(this, xargs);

            var edgeIdToEdge = this.nodeIdToEdgeIdToEdge[edges];
            if(edgeIdToEdge == null) {
                edgeIdToEdge = {};
                this.nodeIdToEdgeIdToEdge = edgeIdToEdge;
            }

            edgeIdToEdge[edgeId] = result;
            this.idToEdge[edgeId] = result;


            return result;
        }

    });

    ns.NodeJoinElement = Class.create(ns.Node, {
        initialize: function($super, graph, nodeId, element, alias) {
            $super(graph, nodeId);

            this.element = element; // TODO ElementProvider?
            this.alias = alias;
        },

        getElement: function() {
            return this.element;
        },

        getAlias: function() {
            return this.alias;
        }
    });


    ns.fnCreateMappingJoinNode = function(graph, nodeId) {
        console.log('Node arguments:', arguments);
        return new ns.MappingJoinNode(graph, nodeId);
    };


    ns.fnCreateMappingEdge = function(graph, edgeId) {
        return new ns.MappingJoinEdge(graph, edgeId);
    };


    ns.JoinGraphElement = Class.create(ns.Graph, {
        initialize: function($super) {
            $super(ns.fnCreateMappingJoinNode, ns.fnCreateMappingEdge);
        }
    });


    /**
     * This row mapper splits a single binding up into multiple ones
     * according to how the variables are mapped by aliases.
     *
     *
     */
    ns.RowMapperAlias = Class.create({
        initialize: function(aliasToVarMap) {
            this.aliasToVarMap = aliasToVarMap;
        },

        /**
         *
         * Returns a map from alias to bindings
         * e.g. { a: binding, b: binding}
         */
        map: function(binding) {
            //this.varAliasMap

            var vars = binding.getVars();

            var result = {};

            _(this.aliasToVarMap).each(function(varMap, alias) {

                var b = new sparql.Binding();
                result[alias] = b;

                var newToOld = varMap.getInverse();
                var newVars = newToOld.keyList();

                _(newVars).each(function(newVar) {
                    var oldVar = newToOld.get(newVar);

                    var node = binding.get(newVar);
                    b.put(oldVar, node);
                });

            });

            return result;
//
//			var varAliasMap = this.varAliasMap;
//			_(vars).each(function(v) {
//
//				var node = binding.get(v);
//
//				var aliasVar = varAliasMap.getOrigAliasVar(v);
//				var ov = aliasVar.v;
//				var oa = aliasVar.alias;
//
//				var resultBinding = result[oa];
//				if(resultBinding == null) {
//					resultBinding = new ns.Binding();
//					result[oa] = resultBinding;
//				}
//
//				resultBinding.put(ov, node);
//			});
//
//
//			return result;
        }
    });


    ns.MappingJoinEdge = Class.create(ns.Edge, {
        initialize: function($super, graph, edgeId) {
            $super(graph, graph, edgeId);
        }
    });



})();

/**
 * An API that hides the underlying complexity.
 *
 */
(function() {

    var sparql = Jassa.sparql;
    var ns = Jassa.sponate;


    ns.Mapping = Class.create({
        initialize: function(name, pattern, tableName, patternRefs) {
            this.name = name;
            this.pattern = pattern;
            this.tableName = tableName;

            // Cached value; inferred from pattern
            this.patternRefs = patternRefs;
        },

        getName: function() {
            return this.name;
        },

        getPattern: function() {
            return this.pattern;
        },

        getTableName: function() {
            return this.tableName;
        },

        getPatternRefs: function() {
            return this.patternRefs;
        }
    });

    /**
     * An easy to use API on top of the more complex system.
     *
     * TODO Add example how to invoke
     *
     */
    ns.StoreFacade = Class.create({

        /**
         * Service and prefixes (a JSON map) are two common things that
         * make sense to pass in to the constructor.
         *
         *
         */
        initialize: function(service, prefixes) {
            this.service = service;

            this.context = new ns.Context();
            this.context.getPrefixMap().addJson(prefixes);
        },

        /**
         * Add a mapping specification
         *
         */
        addMap: function(spec) {
            var name = spec.name;

            var jsonTemplate = spec.template;
            var from = spec.from;


            var pattern = this.context.getPatternParser().parsePattern(jsonTemplate);

            var patternRefs = ns.PatternUtils.getRefs(pattern);

            //console.log('Parsed pattern', JSON.stringify(pattern));

            // The table name is the same as that of the mapping
            ns.ContextUtils.createTable(this.context, name, from, patternRefs);


            var mapping = new ns.Mapping(name, pattern, name, patternRefs);

            this.context.addMapping(mapping);

            // Create a new store object
            this.createStore(name);

            return this;
        },

        createStore: function(name) {

            if(name in this) {
                console.log('[ERROR] An attribute / store with name ' + name + ' already exists');
                throw 'Bailing out';
            }

            this[name] = new ns.Store(this.service, this.context, name);
        },

        /*
         * Functions for access to underlying components
         */

        getSchema: function() {
            return schema;
        }
    });


})();
(function() {

    var ns = Jassa.sponate;

    /*
    ns.AttrStep = Class.create({
        initialize: function(name) {
            this.name = name;
        }
    });
    */



    /**
     * Parser for criterias.
     *
     * A criteria is a form of constraint.
     *
     */
    ns.CriteriaParser = Class.create({

        parse: function(crit) {

            var rootPath = new ns.AttrPath();

            var result = this.parseAny(crit, rootPath);
            return result;
        },

        parseAny: function(crit, basePath) {

            var result;
            if(crit == null) {
                result = new ns.CriteriaTrue();
            }
            else if(_(crit).isObject()) {
                result = this.parseObject(crit, basePath);
            }
            else if(_(crit).isArray()) {
                throw 'Not implemented';
            } else { // Primitive value; treat as equals
                result = this.parse_$eq(basePath, crit);
            }

            return result;
        },


        parseObject: function(critObject, basePath) {

            //var basePath = context.basePath;

            var self = this;
            var criterias = _(critObject).map(function(val, key) {

                var criteria;

                if(_(key).startsWith('$')) {
                    // Call some special function
                    var fnName = 'parse_' + key;
                    var fn = self[fnName];

                    if(!fn) {
                        console.log('[ERROR] No criteria implementation for ' + key);
                        throw 'Bailing out';
                    }

                    criteria = fn.call(self, basePath, val);

                } else {
                    var tmpPath = ns.AttrPath.parse(key);
                    var attrPath = basePath.concat(tmpPath);

                    if(!val) {
                        console.log('[ERROR] No value for attribute ' + key);
                        throw 'Bailing out';
                    }

                    criteria = self.parseAny(val, attrPath);
                }

                return criteria;
            });

            var result;
            if(criterias.length == 1) {
                result = criterias[0];
            } else {
                result = new ns.CriteriaLogicalAnd(criterias);
            }

            return result;
        },


        parse_$eq: function(attrPath, val) {
            return new ns.CriteriaEq(attrPath, val);
        },

        parse_$gt: function(attrPath, val) {
            return new ns.CriteriaGt(attrPath, val);
        },

        parse_$gte: function(attrPath, val) {
            return new ns.CriteriaGte(attrPath, val);
        },

        parse_$lt: function(attrPath, val) {
            return new ns.CriteriaLt(attrPath, val);
        },

        parse_$lte: function(attrPath, val) {
            return new ns.CriteriaLte(attrPath, val);
        },

        parse_$ne: function(attrPath, val) {
            return new ns.CriteriaNe(attrPath, val);
        },

        parse_$regex: function(attrPath, val) {
            var regex;

            if(_(val).isString()) {
                regex = new RegExp(val);
            } else if (val instanceof RegExp) {
                regex = val
            } else {
                console.log('[ERROR] Not a regex: ' + val);
                throw 'Bailing out';
            }

            var result = new ns.CriteriaRegex(attrPath, regex);
            return result;
        },


        parse_$elemMatch: function(attrPath, val) {

            var c = this.parse(val);

            var criterias;
            if(c instanceof ns.CriteriaLogicalAnd) {
                criterias = c.getCriterias();
            } else {
                criterias = [c];
            }

            var result = new ns.CriteriaElemMatch(attrPath, criterias);
            return result;
        },

        parse_$or: function(attrPath, val) {

            if(!_(val).isArray()) {
                console.log('Argument of $or must be an array');
                throw 'Bailing out';
            }

            var self = this;
            var criterias = val.map(function(crit) {
                var result = self.parse(crit);
                return result;
            });

            var result;
            if(criterias.length == 1) {
                result = criterias[0];
            } else {
                result = new ns.CriteriaLogicalOr(attrPath, criterias);
            }

            return result;
        }


    });


    /**
     * Criterias
     *
     * Represent constraints on JSON documents
     *
     * For convenience, they can readily match json (i.e. no compilation to a filter object necessary)
     * Note: Usually good engineering mandates separating these concerns, and maybe we shoot ourselves into the foot by not doing it here
     * So in the worst case, we would create a FilterFactory which creates filters from criterias.
     * a filter is equivalent to a predicate - i.e. a function that returns boolean.
     *
     *
     */

    ns.Criteria = Class.create({
        getOpName: function() {
            throw 'Not overridden';
        },

        match: function(doc) {
            throw 'Not overridden';
        },

        callVisitor: function(name, self, args) {
            var result = ns.callVisitor(name, self, args);
            return result;
        },

        accept: function(visitor) {
            throw 'Not overridden';
        }
    });


    ns.CriteriaBase = Class.create(ns.Criteria, {
        initialize: function(opName) {
            this.opName = opName;
        },

        getOpName: function() {
            return this.opName;
        }
    });


    ns.CriteriaFalse = Class.create(ns.Criteria, {
        initialize: function($super) {
            $super('$false');
        },

        match: function(doc) {
            return false;
        }
    });

    ns.CriteriaTrue = Class.create(ns.Criteria, {
        initialize: function($super) {
            $super('$true');
        },

        match: function(doc) {
            return true;
        }
    });



    ns.CriteriaPath = Class.create(ns.CriteriaBase, {
        initialize: function($super, opName, attrPath) {
            $super(opName);
            this.attrPath = attrPath;
            if(!attrPath) {
                throw 'npe';
            }
        },

        getAttrPath: function() {
            return this.attrPath;
        },

        match: function(doc) {
            var val = this.attrPath.find(doc);

            var result = this.$match(doc, val);

            return result;
        },


        // Minor convenience, as the base function already took care of resolving the value
        $match: function(doc, val) {
            throw 'Not overridden';
        }

    });



    /**
     * @param the document on which to apply the criteria
     */
    ns.CriteriaEq = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, value) {
            $super('$eq', attrPath);
            this.value = value;
        },

        $match: function(doc, val) {
            var result = val == this.value;
            return result;
        }
    });


    ns.CriteriaGt = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, value) {
            $super('$gt', attrPath);
            this.value = value;
        },

        $match: function(doc, val) {
            var result = val > this.value;
            return result;
        }
    });

    ns.CriteriaGte = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, value) {
            $super('$gte', attrPath);
            this.value = value;
        },

        $match: function(doc, val) {
            var result = val >= this.value;
            return result;
        }
    });


    ns.CriteriaLt = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, value) {
            $super('$lt', attrPath);
            this.value = value;
        },

        $match: function(doc, val) {
            var result = val < this.value;
            return result;
        }
    });

    ns.CriteriaLte = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, value) {
            $super('$lte', attrPath);
            this.value = value;
        },

        $match: function(doc, val) {
            var result = val <= this.value;
            return result;
        }
    });


    ns.CriteriaNe = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, value) {
            $super('$ne', attrPath);
            this.value = value;
        },

        $match: function(doc, val) {
            var result = val != this.value;
            return result;
        }
    });



    /**
     * @param the document on which to apply the criteria
     */
    ns.CriteriaRegex = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, regex) {
            $super('$regex', attrPath);
            this.regex = regex;
        },

        $match: function(doc, val) {
            var result = this.regex.test(val);

            return result;
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitRegex', this, arguments);
            return result;
        }
    });




    /**
     * A criteria where
     *
     */
    ns.CriteriaLogicalAnd = Class.create(ns.CriteriaBase, {
        initialize: function($super, criterias) {
            $super('$and');
            this.criterias = criterias;
        },

        getCriterias: function() {
            return this.criterias;
        },

        match: function(doc) {
            var result = _(this.criterias).every(function(criteria) {
                var subResult = criteria.match(doc);
                return subResult;
            });

            return result;
        }
    });


    ns.CriteriaLogicalOr = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, criterias) {
            $super('$or', attrPath);
            this.criterias = criterias;
        },

        getCriterias: function() {
            return this.criterias;
        },

        $match: function(doc, val) {

            var result = _(this.criterias).some(function(criteria) {
                var subResult = criteria.match(val);
                return subResult;
            });

            return result;
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitLogicalOr', this, arguments);
            return result;
        }

    });


    /**
     * http://docs.mongodb.org/manual/reference/operator/elemMatch/#op._S_elemMatch
     *
     * "Matching arrays must have at least one element that matches all specified criteria."
     *
     */
    ns.CriteriaElemMatch = Class.create(ns.CriteriaPath, {
        initialize: function($super, attrPath, criterias) {
            $super('$elemMatch', attrPath);
            this.criterias = criterias;
        },

        getCriterias: function() {
            return this.criterias;
        },

        $match: function(doc, val) {
            if(!_(val).isArray()) {
                console.log('[ERROR] Cannon apply $elemMatch to non-array', val);
                throw 'Bailing out';
            }

            console.log('$elemMatch ' + this.attrPath);

            var result = this.matchArray(val);
            return result;
        },

        // There has to be at least 1 item which satisfies all of the criterias
        matchArray: function(docArray) {

            var self = this;
            var result = _(docArray).some(function(doc) {
                var itemMatch = _(self.criterias).every(function(criteria) {
                    //console.log('Matching doc', doc, criteria);
                    var criteriaMatch = criteria.match(doc);
                    return criteriaMatch;
                });

                return itemMatch;
            })

            return result;
        },

        accept: function(visitor) {
            var result = this.callVisitor('visitElemMatch', this, arguments);
            return result;
        }
    });



})();

(function() {

    var ns = Jassa.sponate;


    ns.CriteriaCompilerSparql = Class.create({


        /**
         * Generates
         *
         * The result is a SPARQL concept
         *
         */
        compile: function(context, mapping, criteria) {
            var joinGraph = new ns.Graph(ns.fnCreateMappingJoinNode, ns.fnCreateMappingEdge);

            var joinNode = joinGraph.createNode(mapping);
            var result = criteria.accept(this, criteria, context, joinGraph, joinNode);

            return result;
        },


        findPattern: function(pattern, attrPath) {

            // At each step check whether we encounter a reference
            _(attrPath.getSteps()).each(function(step) {
                pattern.find();
            });
        },


        visitElemMatch: function(criteria, context, graph, joinNode) {

            var refSpec = criteria.getRefSpec();

            alert('yay' +  JSON.stringify(refSpec));
        },

        /**
         *
         *
         */
        visitRef: function(criteria, context, graph, joinNode) {

        },


        visitGt: function() {

        },

        visitLogicalOr: function() {

        }

    });


})();/*
 * With Sponate we use jQuery as the 'standard' deferred api.
 *
 * We are not going to abstract this away, we'll just provide a wrapper/bridge to angular.
 *
 * http://xkcd.com/927/
 *
 */

// TODO We need to intercept store creation to add the plugin,
// in other words, we need a store factory

(function() {

    var tmp = Jassa.sponate;

    if(!tmp.angular) {
        tmp.angular = {};
    }

    var ns = Jassa.sponate.angular;



    ns.bridgePromise = function(jqPromise, ngDeferred, ngScope, fn) {
        jqPromise.done(function(data) {

            var d = fn ? fn(data) : data;
            ngDeferred.resolve(d);

            if(ngScope) {
                ngScope.$apply();
            }

        }).fail(function() {
            ngDeferred.fail();
        });

        return ngDeferred.promise;
    }

})();

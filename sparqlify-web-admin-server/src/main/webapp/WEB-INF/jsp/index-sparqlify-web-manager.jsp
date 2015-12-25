<!doctype html>
<html ng-app="SparqlifyWebAdmin">
<head>
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <title>Sparqlify Web Admin</title>
    <link rel="stylesheet" href="resources/libs/twitter-bootstrap/3.3.6/css/bootstrap.css" />
    <link rel="stylesheet" href="resources/css/app.css" />

    <script src="resources/libs/jquery/1.9.1/jquery.js"></script>

<!--     <script src="resources/libs/twitter-bootstrap/3.3.6/js/bootstrap.js"></script> -->

<!--     <script src="resources/libs/angularjs/1.2.0-rc.2/angular.js"></script> -->
    <script src="resources/libs/angularjs/1.4.8/angular.js"></script>

    <script src="resources/libs/ui-router/0.2.0/angular-ui-router.js"></script>
<!--     <script src="resources/libs/angular-ui/0.6.0/ui-bootstrap-tpls-0.6.0.js"></script> -->
    <script src="resources/libs/angular-ui/0.14.3/ui-bootstrap-tpls-0.14.3.js"></script>

    <script src="resources/libs/underscore/1.4.4/underscore.js"></script>
    <script src="resources/libs/underscore.string/2.3.0/underscore.string.js"></script>
    <script src="resources/libs/prototype/1.7.1/prototype.js"></script>

<!-- 	<script src="resources/libs/angularjs/1.0.8/angular.js"></script> -->

    <script src="resources/libs/jassa/0.1/jassa.js"></script>

<!-- 	<script src="resources/js/sparqlify-web-manager/app.js"></script> -->
<!-- 	<script src="resources/js/sparqlify-web-manager/controllers.js"></script> -->


    <script type="text/javascript">
        _.mixin(_.str.exports());

        var prefixes = {
            'rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
            'rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
            'owl': 'http://www.w3.org/2002/07/owl#',
            'o': 'http://example.org/ontology/',
            'r': 'http://example.org/resource/',
            'ldso': 'http://linkeddata.org/integrated-stack-schema/'
        };

        var rdf = Jassa.rdf;
        var sparql = Jassa.sparql;
        var sponate = Jassa.sponate;

        /*
         * GeoKnow Generator data source integration
         */
            var service2 = sponate.ServiceUtils.createSparqlHttp('http://generator.geoknow.eu:8890/sparql');
            var store2 = new sponate.StoreFacade(service2, prefixes);

            store2.addMap({
                name: 'rdbs',
                template: [{
                    id: '?s',
                    dbType: '?t',
                    dbName: '?n',
                    dbHost: '?h',
                    dbPort: '?p',
                    dbUser: '?u',
                    dbPass: '?w'
                }],
                from: '?s a ldso:Database ; ldso:dbType ?t ; ldso:dbName ?n ; ldso:dbHost ?h ; ldso:dbPort ?p ; ldso:dbUser ?u ; ldso:dbPassword ?w .'
            });

            //store2.rdbs.find().asList().done(function(items) { alert(JSON.stringify(items)); });

        /*
         * Sponate
         */
        var serviceUrl = 'manager/api/sparql';

        var service = sponate.ServiceUtils.createSparqlHttp(serviceUrl);
        var store = new sponate.StoreFacade(service, prefixes);

        store.addMap({
            name: 'contexts',
            template: [{
                id: '?s',
                _id: '?sid',
                status: '?status',
                config: {
                    id: '?c',
                    _id: '?cid',
                    contextPath: '?path',
                    dataSource: {
                        id: '?d',
                        jdbcUrl: '?durl',
                        username: '?duser'
                    },
                    maxResultSetRows: '?mrsr',
                    maxExecutionTimeInSeconds: '?met',
                    resource: {
                        id: '?r',
                        data: '?rdata'
                    }
                }
            }],
            from: '?s a o:Rdb2RdfExecution ; o:id ?sid ; o:status ?status ; o:config ?c . ?c o:id ?cid ; o:contextPath ?path ; o:dataSource ?d ; o:resource ?r  . Optional { ?c o:maxExecutionTime ?met } . Optional { ?c o:maxResultSetRows ?mrsr } . ?d o:jdbcUrl ?durl ; o:username ?duser . ?r o:data ?rdata .'
        });

        store.addMap({
            name: 'logs',
            template: [{
                id: '?s',
                logEntries: [{
                    id: '?l',
                    text: '?l'
                }]
            }],
            from: '?s a o:Rdb2RdfExecution ; o:logEntries ?e . ?e o:logEntry ?l .'
        });



        var parseJdbcUrl = function(url) {
            //var re = //
        };


    </script>

    <script src="resources/js/controllers/app.js"></script>

</head>

<body ng-controller="ContextListCtrl" ng-init="init()">

    <div class="container">
        <h2>Sparqlify Service Management</h2>

        <button class="btn btn-large btn-primary" ng-click="openAddNewService()">Add New Service</button>


        <h3>Existing Services</h3>
        <div ui-view></div>

        <div class="row-fluid">
            <div class="span6 offset3">


                <div ng-show="selectedItemId">
                    <h3>Details for <i>{{selectedItemData.config.contextPath}}</i> <button class="btn" ng-click="selectedItemId = null">Hide</button></h3>

                    <div class="frame">
                        <uib-tabset>
                            <uib-tab heading="Log">
                                <table class="table table-striped">
                                    <colgroup>
                                        <col width="100px" />
                                    </colgroup>
                                    <th>Level</th><th>Message</th>
                                    <tr ng-repeat="entry in selectedItemLog"><td>Info</td><td>{{entry.text}}</td></tr>
                                </table>
                            </uib-tab>
                            <uib-tab heading="Mapping">
                                <pre>{{selectedItemData.config.resource.data}}</pre>
                            </uib-tab>
        <!-- 					<tab heading="Info"> -->
        <!-- 						<table class="table"> -->
        <!-- 							<colgroup> -->
        <!-- 								<col width="100px" /> -->
        <!-- 							</colgroup> -->
        <!-- 							<tr><td>Status</td><td>{{selectedItemData.status}}</td></tr> -->
        <!-- 						</table> -->
        <!-- 					</tab> -->
                        </uib-tabset>
                    </div>
                </div>

        <!--     <tab heading="Static title">Static content</tab> -->
        <!-- 		<tab ng-repeat="tab in tabs" active="tab.active" disabled="tab.disabled"> -->
        <!-- 			<tab-heading> -->
        <!-- 				{{tab.heading}} -->
        <!-- 			</tab-heading> -->
        <!-- 			{{tab.content}}	 -->
        <!-- <!-- 			<div ng-include={{tab.content}}" /> -->
        <!-- 		</tab> -->

        <!--     <tab select="alertMe()"> -->
        <!--       <tab-heading> -->
        <!--         <i class="icon-bell"></i> Select me for alert! -->
        <!--       </tab-heading> -->
        <!--       I've got an HTML heading, and a select callback. Pretty cool! -->
        <!--     </tab> -->

            </div>
        </div>
    </div>

</body>
</html>

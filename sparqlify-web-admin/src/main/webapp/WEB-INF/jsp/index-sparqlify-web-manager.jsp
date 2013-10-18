<!doctype html>
<html ng-app="SparqlifyWebAdmin">
<head>
	<meta content="text/html; charset=utf-8" http-equiv="Content-Type">
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

	<title>Sparqlify Web Admin</title>
	<link rel="stylesheet" href="resources/libs/twitter-bootstrap/2.3.2/css/bootstrap.min.css" />

	<script src="resources/libs/jquery/1.9.1/jquery.js"></script>
	<script src="resources/libs/underscore/1.4.4/underscore.js"></script>
	<script src="resources/libs/underscore.string/2.3.0/underscore.string.js"></script>
	<script src="resources/libs/prototype/1.7.1/prototype.js"></script>
	
<!-- 	<script src="resources/libs/angularjs/1.0.8/angular.js"></script> -->
	
	<script src="resources/libs/angularjs/1.2.0-rc.2/angular.js"></script>
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
			'r': 'http://example.org/resource/'
		};
	
		var rdf = Jassa.rdf;
		var sparql = Jassa.sparql;
		var sponate = Jassa.sponate;
	
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
					resource: {
						id: '?r',
						data: '?rdata'
					}
				}
			}],
			from: '?s a o:Rdb2RdfExecution ; o:id ?sid ; o:status ?status ; o:config ?c . ?c o:id ?cid ; o:contextPath ?path ; o:dataSource ?d ; o:resource ?r . ?d o:jdbcUrl ?durl ; o:username ?duser . ?r o:data ?rdata .'
		});
			
		
		var parseJdbcUrl = function(url) {
			//var re = //
		};

		/*
		 * Angular JS
		 */	
		var myModule = angular.module('SparqlifyWebAdmin', []);

		myModule.factory('contextService', function($rootScope, $q, $http) {
			return {
				getContexts: function(filterText) {
					var criteria = {};
					if(filterText != null && filterText.length > 0) {
						// TODO: This is essentially a 'filter-any' for which there should be a util method
						criteria = {
							$or: [{
								config: {
									$or: [{
										contextPath: {$regex: filterText}
									}, {
										dataSource: {
											$or: [{
												jdbcUrl: {$regex: filterText}
											}, {
												username: {$regex: filterText}
											}]
										}
									}, {
										resource: {data : {$regex: filterText}}
									}]
								},
							}, {
								status: {$regex: filterText}
							}]
						};
						//criteria = {name: {$or: [{$regex: filterText}, {$regex: 'orp'}]}};

						//criteria = {owners: {$elemMatch: {name: {$regex: filterText}}}};

// 						criteria = {
// 								$or: [
// 								      {name: {$regex: filterText}},
// 								      {owners: {$elemMatch: {name: {$regex: filterText}}}}
// 						]};
					}
	 				
//					criteria = {};
					var promise = store.contexts.find(criteria).asList();
					//promise.done(function(x) {console.log('data', x); });
					var result = sponate.angular.bridgePromise(promise, $q.defer(), $rootScope);
					return result;
		        },

		        doPostRequest: function(url, data, preventJson) {
		        	console.log('data: ', data);
		        	var promise = $http({
		        	    method: 'POST',
		        	    url: url,
		        	    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		        	    transformRequest: function(obj) {
		        	        var str = [];
		        	        for(var p in obj) {
		        	        	var k = encodeURIComponent(p);
		        	        	
		        	        	var o = obj[p];
		        	        	if(!preventJson) {
		        	        		o = JSON.stringify(o);
		        	        	}
		        	        	var v = encodeURIComponent(o);
		        	        	str.push(k + '=' + v);
		        	        	console.log('v = ', v);
		        	        }
		        	        return str.join("&");
		        	    },
		        	    data: data
		        	});

		        	return promise;
		        },
		        
		        // TODO Rename to service
		        createContext: function(data) {
		        	return this.doPostRequest('manager/api/action/createContext', data);
		        },
		        
		        deleteContext: function(id) {
		        	return this.doPostRequest('manager/api/action/deleteContext', {id: id});
		        },
		        
		        startService: function(id) {
		        	return this.doPostRequest('manager/api/action/startService', {id: id}, true);
		        },

		        stopService: function(id) {
		        	return this.doPostRequest('manager/api/action/stopService', {id: id}, true);
		        },
		        
		        restartService: function(id) {
		        	var self = this;
		        	this.stopService(id).then(function() {
		        		self.startService(id);
		        	});
		        }
		   };
		});

		myModule.controller('ContextListCtrl', function($scope, contextService) {
			$scope.doFilterContexts = function() {
				$scope.contexts = contextService.getContexts($scope.filterText);
			};
			
			$scope.init = function() {
				$scope.doFilterContexts();
			};
			
	        $scope.deleteContext = function(id) {
	        	return contextService.deleteContext(id);
	        };

	        $scope.startService = function(id) {
	        	return contextService.startService(id);
	        };

	        $scope.stopService = function(id) {
	        	return contextService.stopService(id);
	        };
		});

		
		myModule.controller('CreateMappingCtrl', function($scope, contextService) {
// 			$scope.doFilterContexts = function() {
// 				$scope.contexts = contextService.getContexts($scope.filterText);
// 			};
			
			$scope.create = function() {
				var data = {
					contextPath: $scope.path,
					textResource: {
						data: $scope.mappingText,
						type: 'rdb-rdf-mapping',
						format: 'sml'
					},
					jdbcDataSource: {
						jdbcUrl: 'jdbc:postgresql://' + $scope.hostname + '/' + $scope.dbname,
						username: $scope.username,
						password: $scope.password
					}
				};
				
				var postData = {
					data: data
				};
				
				var promise = contextService.createContext(postData);
				promise.success(function() {
					alert('yay');
				}).error(function() {
					alert('fail');
				});
				
				
				//$scope.doFilterContexts();
			};
		});

		
		// Utility filter for comma separated values
		// Source: http://stackoverflow.com/questions/16673439/comma-separated-p-angular
		myModule.filter('map', function() {
			return function(input, propName) {
				return input.map(function(item) {
					return item[propName];
				});
			};
		});
		
	</script>
</head>

<body ng-controller="ContextListCtrl" data-ng-init="init()">

	<div class="row-fluid">
		<div class="span8 offset2">
			<form ng-submit="doFilterContexts()">
		    	<input type="text" ng-model="filterText" />
				<input class="btn-primary" type="submit" value="Filter" />
			</form>
		
			<table class="table table-striped">
				<thead>
<!-- 					<th>Id</th> -->
					<th>Path</th>
					<th>Jdbc Url</th>
					<th>Username</th>
					<th>Mapping</th>
					<th>Status</th>
					<th>Service</th>
					<th>Config</th>
				</thead>
				<tr ng-repeat="context in contexts">
<!-- 					<td>{{context.id}}</td> -->
					<td>{{context.config.contextPath}}</td>
					<td>{{context.config.dataSource.jdbcUrl}}</td>
					<td>{{context.config.dataSource.username}}</td>
					<td>{{context.config.resource.data}}</td>
					<td>{{context.status}}</td>
					<td>
						<a href="" ng-show="context.status=='STOPPED'" ng-click="startService(context.id.slice(1, -1))">Start</a>
						<a href="" ng-show="context.status!='STOPPED'" ng-click="stopService(context.id.slice(1, -1))">Stop</a>
						<a href="" ng-click="restartService(context.id)">Restart</a>
					</td>
					<td>
						<a href="" ng-click="editContext(context._id)">Edit</a>
						<a href="" ng-click="deleteContext(context._id)">Delete</a>						
					</td>
				</tr>
			</table>

		</div>
	</div>

	<div class="row-fluid">
		<div class="span6 offset3">
		
			<div ng-controller="CreateMappingCtrl">
				<form novalidate class="css-form">
					<table class="table table-condensed">
					<tr><td>Path:</td><td><input type="text" style="width:95%" ng-model="path" required /></td></tr>
					<tr><td>Hostname:</td><td><input type="text" ng-model="hostname" required /></td></tr>
					<tr><td>Database:</td><td><input type="text" ng-model="dbname" required /></td></tr>
		
					<tr><td>Username:</td><td><input type="text" ng-model="username" required /></td></tr>
					<tr><td>Password:</td><td><input type="password" ng-model="password" required /></td></tr>
		
					<tr><td>Mapping:</td><td><textarea rows="25" cols="80" ng-model="mappingText" required></textarea></td></tr>
					
		<!-- 			URL to Mapping: <input type="text" ng-model="mappingUrl" required /> -->
		
					<tr><td></td><td><button ng-click="cancel()">Cancel</button>
					<button ng-click="create()">Create</button></td></tr>
					</table>
				</form>
			</div>
		</div>
	</div>
</body>
</html>

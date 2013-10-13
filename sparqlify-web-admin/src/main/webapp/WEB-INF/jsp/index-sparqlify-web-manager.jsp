<!doctype html>
<html ng-app="SparqlifyWebAdmin">
<head>

	<script src="resources/libs/jquery/1.9.1/jquery.js"></script>
	<script src="resources/libs/underscore/1.4.4/underscore.js"></script>
	<script src="resources/libs/underscore.string/2.3.0/underscore.string.js"></script>
	<script src="resources/libs/prototype/1.7.1/prototype.js"></script>
	<script src="resources/libs/angularjs/1.0.8/angular.js"></script>
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
			}],
			from: '?s a o:Rdb2RdfConfig ; o:contextPath ?path ; o:dataSource ?d ; o:resource ?r . ?d o:jdbcUrl ?durl ; o:username ?duser . ?r o:data ?rdata .'
		});
			

		/*
		 * Angular JS
		 */	
		var myModule = angular.module('SparqlifyWebAdmin', []);

		myModule.factory('contextService', function($rootScope, $q, $http) {
			return {
				getContexts: function(filterText) {
					var criteria = {};
					if(filterText != null && filterText.length > 0) {						
						criteria = {
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
					var result = sponate.angular.bridgePromise(promise, $q.defer(), $rootScope);
					return result;
		        },

		        doPostRequest: function(url, data) {
		        	console.log('data: ', data);
		        	var promise = $http({
		        	    method: 'POST',
		        	    url: url,
		        	    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		        	    transformRequest: function(obj) {
		        	        var str = [];
		        	        for(var p in obj)
		        	        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(JSON.stringify(obj[p])));
		        	        return str.join("&");
		        	    },
		        	    data: data
		        	});

		        	return promise;
		        },
		        
		        createContext: function(data) {
		        	return this.doPostRequest('manager/api/action/createContext', data);
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
				</thead>
				<tr ng-repeat="context in contexts">
<!-- 					<td>{{context.id}}</td> -->
					<td>{{context.contextPath}}</td>
					<td>{{context.dataSource.jdbcUrl}}</td>
					<td>{{context.dataSource.username}}</td>
					<td>{{context.resource.data}}</td>
				</tr>
			</table>

		</div>
	</div>

	<div ng-controller="CreateMappingCtrl">
		<form novalidate class="css-form">
			Path: <input type="text" ng-model="path" required /><br />
			Hostname: <input type="text" ng-model="hostname" required /><br />
			Database: <input type="text" ng-model="dbname" required /><br />

			Username: <input type="text" ng-model="username" required /><br />
			Password: <input type="password" ng-model="password" required /><br />

			Mapping: <textarea rows="25" cols="80" ng-model="mappingText" required></textarea>
			
<!-- 			URL to Mapping: <input type="text" ng-model="mappingUrl" required /> -->
			<br />
			<button ng-click="cancel()">Cancel</button>
			<button ng-click="create()">Create</button>
		</form>
	</div>
</body>
</html>

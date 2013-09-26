/// <reference path="../libs/DefinitelyTyped/angularjs/angular.d.ts" />

'use strict';


angular.module('SparqlifyWebManager', []).
	controller('DashboardCtrl', ['$scope', function($scope) {
		
				
		$scope.endpoints = [
			{path: 'foo', hostname: 'host', dbname: 'bar', username: 'baz'},
			{path: 'foo', hostname: 'host', dbname: 'bar', username: 'baz'}
		];
		
	}]).
	controller('CreateMappingCtrl', ['$scope', function($scope) {
		//alert('yay');
		$scope.username = 'foo';
		
		$scope.master = {};
	
		$scope.create = function(user) {
			$scope.master = angular.copy(user);
		};
	
		$scope.cancel = function() {
			//$scope.user = angular.copy($scope.master);
			
		};
	
		$scope.isUnchanged = function(user) {
			return angular.equals(user, $scope.master);
		};
	
		//$scope.reset();
	}]);


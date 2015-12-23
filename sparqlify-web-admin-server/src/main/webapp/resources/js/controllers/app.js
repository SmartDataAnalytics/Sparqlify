'use strict';

angular.module('SparqlifyWebAdmin', [
    'ui.router',
    'ui.bootstrap'
])

.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise("/dashboard");


    // Now set up the states
    $stateProvider.state('dashboard', {
        url : "/dashboard",
        templateUrl : "resources/partials/execution-list.html",

    }).state('dashboard.list', {
        url : "/list",
        templateUrl : "partials/state1.list.html",
        controller : function($scope) {
            $scope.items = [ "A", "List", "Of", "Items" ];
        }
    });

// 			$routeProvider.when('/', {
// 				templateUrl : 'resources/partials/executionist.html'
// 			});
}])

.factory('contextService', function($rootScope, $q, $http) {
    return {
        getContexts : function(filterText) {
            var criteria = {};
            if (filterText != null && filterText.length > 0) {
                // TODO: This is essentially a 'filter-any' for which there should be a util method
                criteria = {
                    $or : [{
                        config : {
                            $or : [{
                                contextPath : {
                                    $regex : filterText
                                }
                            }, {
                                dataSource : {
                                    $or : [{
                                        jdbcUrl : {
                                            $regex : filterText
                                        }
                                    }, {
                                        username : {
                                            $regex : filterText
                                        }
                                    }]
                                }
                            }, {
                                resource : {
                                    data : {
                                        $regex : filterText
                                    }
                                }
                            }]
                        },
                }, {
                    status : {
                        $regex : filterText
                    }
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

    findServices: function(criteria) {
        var promise = store.contexts.find(criteria).asList();
        //promise.done(function(x) {console.log('data', x); });
        var result = sponate.angular.bridgePromise(promise, $q.defer(), $rootScope);
        return result;
    },

    findServiceLogs: function(criteria) {
        var promise = store.logs.find(criteria).asList();
        //promise.done(function(x) {console.log('data', x); });
        var result = sponate.angular.bridgePromise(promise, $q.defer(), $rootScope);
        return result;
    },

    doPostRequest : function(url, data, preventJson) {
        console.log('data: ', data);
        var promise = $http({
            method : 'POST',
            url : url,
            headers : {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            transformRequest : function(obj) {
                var str = [];
                for ( var p in obj) {
                    var k = encodeURIComponent(p);

                    var o = obj[p];
                    if (!preventJson) {
                        o = JSON.stringify(o);
                    }
                    var v = encodeURIComponent(o);
                    str.push(k + '=' + v);
                    console.log('v = ', v);
                }
                return str.join("&");
            },
            data : data
        });

        return promise;
    },

    // TODO Rename to service
    createService : function(data) {
        return this.doPostRequest(
                'manager/api/action/createService',
                data);
    },

    deleteService : function(id) {
        return this.doPostRequest(
                'manager/api/action/deleteService',
                {
                    id : id
                }, true);
    },

    startService : function(id) {
        return this.doPostRequest(
                'manager/api/action/startService',
                {
                    id : id
                }, true);
    },

    stopService : function(id) {
        return this.doPostRequest(
                'manager/api/action/stopService', {
                    id : id
                }, true);
    },

    restartService : function(id) {
        var self = this;
        return this.stopService(id).then(
                function() {
                    self.startService(id);
                });
        }
    };
})


//.controller('AppCtrl', ['$scope', '$uibModal', function() {
//    $scope.openAddNewService = function(size) {
//
//        var modalInstance = $uibModal.open({
//          animation: true, //$scope.animationsEnabled,
//          templateUrl: 'partials/add-new-service.html',
//          controller: 'ModalInstanceCtrl',
//          size: size,
//          resolve: {
//            items: function () {
//              return $scope.items;
//            }
//          }
//        });
//
//        modalInstance.result.then(function (selectedItem) {
//          $scope.selected = selectedItem;
//        }, function () {
//          $log.info('Modal dismissed at: ' + new Date());
//        });
//      };
//}])

.controller('CreateNewServiceModalInstanceCtrl', ['$scope', '$uibModalInstance', 'items', function ($scope, $uibModalInstance, items) {

//    $scope.createService = function() {
//
//
//        //$scope.doFilterContexts();
//    };

//    $scope.items = items;
//    $scope.selected = {
//        item: $scope.items[0]
//    };
//
    $scope.ok = function () {
        var data = {
            contextPath : $scope.path,
            textResource : {
                data : $scope.mappingText,
                type : 'rdb-rdf-mapping',
                format : 'sml'
            },
            jdbcDataSource : {
                jdbcUrl : 'jdbc:postgresql://' + $scope.hostname + '/'
                        + $scope.dbname,
                username : $scope.username,
                password : $scope.password
            },
            maxResultRows: $scope.maxResultRows,
            maxExecutionTimeInSeconds: $scope.maxExecutionTimeInSeconds
        };

        $uibModalInstance.close(data);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}])

.controller('ContextListCtrl', ['$scope', 'contextService', '$uibModal', function($scope, contextService, $uibModal) {
    $scope.openAddNewService = function(size) {

        var modalInstance = $uibModal.open({
            animation: true, //$scope.animationsEnabled,
            templateUrl: 'resources/partials/add-new-service.html',
            controller: 'CreateNewServiceModalInstanceCtrl',
            size: size,
            resolve: {
                items: function () {
                    return $scope.items;
                }
            }
        });

        modalInstance.result.then(function (data) {
            var postData = {
                data : data
            };

            var promise = contextService.createService(postData);
            promise.success(function() {
                $scope.doFilterContexts();
                alert('yay');
            }).error(function() {
                alert('fail');
            });

        }, function () {
          //$log.info('Modal dismissed at: ' + new Date());
        });
    };

    $scope.doFilterContexts = function(filterText) {
        contextService.getContexts(filterText).then(function(contexts) {
            $scope.contexts = contexts;
        });
    };

    $scope.init = function() {
        $scope.doFilterContexts();
    };

    $scope.deleteService = function(id, path) {
        var decision = confirm('Really delete service with id ' + id + '?');
        if(decision === true) {
            contextService.deleteService(id).then($scope.doFilterContexts)
        }
    };

    //$scope.serviceCtrl = contextService;

    $scope.startService = function(id) {
        return contextService.startService(id).then($scope.doFilterContexts);
    };

    $scope.stopService = function(id) {
        return contextService.stopService(id).then($scope.doFilterContexts);
    };

    $scope.restartService = function(id) {
        return contextService.stopService(id).then($scope.doFilterContexts);
    };


    $scope.showServiceDetails = function(id) {
        contextService.findServices({id: id}).then(function(items) {
            console.log('selectedItemData: ', items);
            if(items && items.length > 0) {
                $scope.selectedItemId = id;
                $scope.selectedItemData = items[0];
            }
        });

        contextService.findServiceLogs({id: id}).then(function(items) {
            if(items && items.length > 0) {
                $scope.selectedItemLog = items[0].logEntries;
            }
        });
    };

}])

//.controller('CreateMappingCtrl', function($scope, contextService) {
//    // 			$scope.doFilterContexts = function() {
//    // 				$scope.contexts = contextService.getContexts($scope.filterText);
//    // 			};
//
//
//})

// Utility filter for comma separated values
// Source: http://stackoverflow.com/questions/16673439/comma-separated-p-angular
.filter('map', function() {
    return function(input, propName) {
        return input.map(function(item) {
            return item[propName];
        });
    };
})

;



var TabsDemoCtrl = function ($scope) {
  $scope.tabs = [
    { title:"Dynamic Title 1", content:"Dynamic content 1" },
    { title:"Dynamic Title 2", content:"Dynamic content 2", disabled: true }
  ];

  $scope.alertMe = function() {
    setTimeout(function() {
      alert("You've selected the alert tab!");
    });
  };

  $scope.navType = 'pills';
};

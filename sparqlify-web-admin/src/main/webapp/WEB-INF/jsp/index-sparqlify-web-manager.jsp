<!doctype html>
<html ng-app="SparqlifyWebManager">
<head>
<script src="resources/js/libs/angularjs/1.0.8/angular.js"></script>

<script src="resources/js/sparqlify-web-manager/app.js"></script>
<script src="resources/js/sparqlify-web-manager/controllers.js"></script>

</head>
<body>

	<div ng-controller="DashboardCtrl">
		<table>
			<thead>
				<tr>
					<th>Path</th>
					<th>Hostname</th>
					<th>Database</th>
					<th>Username</th>
				</tr>
			</thead>
			<tbody>
				<tr ng-repeat="endpoint in endpoints">
					<td>{{endpoint.path}}</td>
					<td>{{endpoint.hostname}}</td>
					<td>{{endpoint.dbname}}</td>
					<td>{{endpoint.username}}</td>
				</tr>
			</tbody>
		</table>
	</div>

	<div ng-controller="CreateMappingCtrl">
		<form novalidate class="css-form">
			Path: <input type="text" ng-model="path" required /><br />
			Hostname: <input type="text" ng-model="hostname" required /><br />
			Database: <input type="text" ng-model="dbname" required /><br />

			Username: <input type="text" ng-model="username" required /><br />
			Password: <input type="password" ng-model="mapping.password" required />
			<br />
			<button ng-click="cancel()">Cancel</button>
			<button ng-click="create()">Create</button>
		</form>
	</div>
</body>
</html>

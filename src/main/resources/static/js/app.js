/**
  xonda
*/
var fillerApp=angular.module('fillerApp', ['ngRoute']);

fillerApp.config(function($routeProvider){
   $routeProvider.
       when('/', {
           templateUrl: './principal.html',
           controller: 'fillerController'
       }).
       otherwise({
           redirectTo: '/'
       });
});

fillerApp.controller('fillerController',['$scope', '$http', '$window', function(scope, http, window){
    scope.send = function(dataObj) {
        var res = http.post('/filler', dataObj);
        res.success(function(data, status, headers, config) {
            $("body").append("<iframe src='./filler?FilePath=" + data.filePath+ "' style='display: none;' ></iframe>");
        });
    };
}]);
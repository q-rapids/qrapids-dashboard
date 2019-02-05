var app = angular.module('NewSIApp', []);
app.controller('NewSICtrl', function($scope, $http) {

    $scope.data = [];

    $scope.getAllFactorQuality = function(){
        var url = "api/QualityFactors/getAll?prj="+sessionStorage.getItem("prj");
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
            console.log(response.data);
        })
    };

});

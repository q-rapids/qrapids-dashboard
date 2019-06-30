var app = angular.module('TablesApp', []);
app.controller('TablesCtrl', function($scope, $http) {

    $scope.data = [];
    $scope.data_aux = [];

    $scope.getStratInd = function (){
        $http({
            method : "GET",
            url : "../api/StrategicIndicators/CurrentEvaluation"
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getKPIEval = function(){
        $http({
            method : "GET",
            url : "../api/StrategicIndicators/HistoricalData",
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getFeedback = function(){
        var id = getParameterByName('id');
        var url =  "../api/Feedback_Factors/" + id;
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getAllQRs = function () {
        var url =  "api/qr?prj=" + sessionStorage.getItem("prj");
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.showAlertForQR = function (alert) {
        $("#alertModal").modal();
        $("#alertType").val(alert.type);
        $("#alertName").val(alert.name);
        $("#alertDate").val(alert.date);
        $("#alertThreshold").val(alert.threshold);
        $("#alertValue").val(alert.value);
    };

    $scope.substractOneWeek = function (dateCurrent) {
        var date = new Date(dateCurrent);
        date.setDate(date.getDate() - 7);
        return date.toISOString().slice(0,10);
    };

    $scope.getAllDecisions = function () {
        var url = "api/decisions?qrs=true&prj=" + sessionStorage.getItem("prj");
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getAlerts = function(){
        var url =  "api/alerts?prj=" + sessionStorage.getItem("prj");
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            getQualityModel();
            console.log(qualityModelRelations);
            $scope.data = response.data;
            $scope.data.forEach(function (alert) {
                var relations = qualityModelRelations.get(alert.id_element);

                var strategicIndicators = relations.strategicIndicators;
                var strategicIndicatorsText = [];
                strategicIndicators.forEach(function (strategicIndicator) {
                    strategicIndicatorsText.push(strategicIndicator.id);
                });
                alert.strategicIndicators = strategicIndicatorsText.join(", ");

                var factors = relations.factors;
                var factorsText = [];
                factors.forEach(function (factor) {
                    factorsText.push(factor.id);
                });
                alert.factors = factorsText.join(", ");
            });
            clearAlertsPendingBanner();
        })
    };

    var qualityModelRelations = new Map();

    function getQualityModel () {
        jQuery.ajax({
            dataType: "json",
            type: "GET",
            url : "../api/qualityModel",
            async: false,
            success: function (data) {
                data.forEach(function (strategicIndicator) {
                    strategicIndicator.factors.forEach(function (factor) {
                        if (qualityModelRelations.has(factor.id)) {
                            var elements = qualityModelRelations.get(factor.id);
                            elements.strategicIndicators.push({
                                id: strategicIndicator.id
                            });
                        } else {
                            qualityModelRelations.set(factor.id, {
                                factors: [],
                                strategicIndicators: [{
                                    id: strategicIndicator.id
                                }]
                            });
                        }

                        factor.metrics.forEach(function (metric) {
                            if (qualityModelRelations.has(metric.id)) {
                                var elements = qualityModelRelations.get(metric.id);
                                elements.factors.push({
                                    id: factor.id
                                });
                                elements.strategicIndicators.push({
                                    id: strategicIndicator.id
                                });
                            } else {
                                qualityModelRelations.set(metric.id, {
                                    factors: [{
                                        id: factor.id
                                    }],
                                    strategicIndicators: [{
                                        id: strategicIndicator.id
                                    }]
                                });
                            }
                        })
                    });
                });
            }
        });
    }

    $scope.getQR = function(alertId){
        var url =  "api/alerts/" + alertId + "/qrPatterns";
        $http({
            method : "GET",
            url : url
        }).then(function (response) {
            if (response.data.length == 0){
                alert("No QR");
            }
            else if (response.data.length == 1) {
                $scope.showQRCandidate(response.data[0], alertId);
            }
            else {
                $("#QRCandidates").empty();
                for (var i = 0; i < response.data.length; i++) {
                    $("#QRCandidates").append('<button class="list-group-item">' + response.data[i].name + '</button>');
                }

                $('.list-group-item').on('click', function() {
                    var $this = $(this);

                    $('.active').removeClass('active');
                    $this.toggleClass('active');
                });

                $("#QRListModal").modal();

                $("#showQRButton").unbind();
                $("#showQRButton").click(function () {
                    var isElementSelected = $('.active').length > 0;
                    if (isElementSelected) {
                        var QRPosition = $('.active').index();
                        var QRCandidate = response.data[QRPosition];
                        $scope.showQRCandidate(QRCandidate, alertId);
                        $("#QRListModal").modal('hide');
                    }
                });
            }
        })
    };

    $scope.showQRCandidate = function(QRCandidate, alertId) {
        $("#QRModal").modal();
        var QRRequirement = $("#QRRequirement");
        var QRDescription = $("#QRDescription");
        var QRGoal = $("#QRGoal");
        var QRDecisionRationale = $("#QRDecisionRationale");
        var QRBacklogUrlDiv = $("#QRBacklogUrlDiv");
        var QRType = $("#QRType");

        QRType.text("Quality Requirement Candidate");
        QRRequirement.val(QRCandidate.forms[0].fixedPart.formText);
        QRRequirement.prop("readonly", false);
        QRDescription.val(QRCandidate.forms[0].description);
        QRDescription.prop("readonly", false);
        QRGoal.val(QRCandidate.goal);
        QRGoal.prop("readonly", true);
        QRBacklogUrlDiv.hide();
        QRDecisionRationale.val("");
        QRDecisionRationale.prop("readonly", false);

        var addQR = function () {
            var requirement = $("#QRRequirement").val();
            var description = $("#QRDescription").val();
            var rationale = $("#QRDecisionRationale").val();
            var addQRUrl = "api/alerts/"+alertId+"/qr?prj=" + sessionStorage.getItem("prj");
            var body = new URLSearchParams();
            body.set('requirement', requirement);
            body.set('description', description);
            body.set('goal', QRCandidate.goal);
            body.set('rationale', rationale);
            body.set('patternId', QRCandidate.id);
            $http({
                method: "POST",
                url: addQRUrl,
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: body.toString()
            }).then(function (response) {
                var qualityRequirement = response.data;
                // show QR info
                $("#QRModal").modal('hide');
                if (qualityRequirement.backlogId && qualityRequirement.backlogUrl) {
                    $("#messageModalTitle").text("The Quality Requirement has been added to the backlog successfully");
                    $("#messageModalContent").html("<b>Issue id: </b>" + qualityRequirement.backlogId + "</br>" +
                        "<b>Issue url: </b><a href='" + qualityRequirement.backlogUrl + "' target='_blank'>" + qualityRequirement.backlogUrl + "</a>");
                    $("#messageModal").modal();
                }
                $scope.getAlerts();
            }).catch(function (response) {
                if (response.status === 500) {
                    $("#QRModal").modal('hide');
                    $scope.getAlerts();
                    alert("Error on saving the quality requirement to the backlog")
                }
                else {
                    $("#QRModal").modal('hide');
                    $scope.getAlerts();
                    alert("Error on saving the quality requirement")
                }
            });
        };

        var ignoreQR = function () {
            var rationale = $("#QRDecisionRationale").val();
            var ignoreQRUrl = "api/alerts/"+alertId+"/ignore?prj=" + sessionStorage.getItem("prj");
            var body = new URLSearchParams();
            body.set('rationale', rationale);
            body.set('patternId', QRCandidate.id);
            $http({
                method: "POST",
                url: ignoreQRUrl,
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: body.toString()
            }).then(function () {
                $("#QRModal").modal('hide');
                $scope.getAlerts();
            });
        };

        var simulateQR = function () {
            location.href = "Simulation/QR?alert="+alertId+"&pattern="+QRCandidate.id;
        };
        var simulateButton = $("#simulateButton");
        simulateButton.click(simulateQR);

        var decisionTypeButton = $("#decisionTypeButton");
        decisionTypeButton.prop("disabled", false);
        var decisionTypeText = $("#decisionType");
        decisionTypeText.text("Select");
        var saveButton = $("#saveButton");
        saveButton.prop("disabled", true);

        $("#addQR").on('click', function () {
            decisionTypeText.text("Add Quality Requirement");
            saveButton.prop("disabled", false);
            saveButton.unbind();
            saveButton.click(addQR);
        });

        $("#ignoreQR").on('click', function () {
            decisionTypeText.text("Ignore Quality Requirement");
            saveButton.prop("disabled", false);
            saveButton.unbind();
            saveButton.click(ignoreQR);
        });

        $("#cancelSaveFooter").show();
        $("#closeFooter").hide();
    };

    $scope.getDecision = function (alertId) {
        var url =  "api/alerts/" + alertId + "/decision";
        $http({
            method : "GET",
            url : url
        }).then(function (response) {
            showDecision(response.data);
        });
    };

    showDecision = function (decision) {
        console.log(decision);
        $("#QRModal").modal();
        var QRRequirement = $("#QRRequirement");
        var QRDescription = $("#QRDescription");
        var QRGoal = $("#QRGoal");
        var QRBacklogUrlDiv = $("#QRBacklogUrlDiv");
        var QRBacklogUrl = $("#QRBacklogUrl");
        var QRDecisionRationale = $("#QRDecisionRationale");
        var decisionTypeText = $("#decisionType");
        var decisionTypeButton = $("#decisionTypeButton");
        var QRType = $("#QRType");

        QRRequirement.val(decision.qrRequirement);
        QRRequirement.prop("readonly", true);
        QRDescription.val(decision.qrDescription);
        QRDescription.prop("readonly", true);
        QRGoal.val(decision.qrGoal);
        QRGoal.prop("readonly", true);
        QRBacklogUrl.prop('href', decision.qrBacklogUrl);
        QRBacklogUrl.prop('target', '_blank');
        QRBacklogUrl.text(decision.qrBacklogUrl);
        QRDecisionRationale.val(decision.decisionRationale);
        QRDecisionRationale.prop("readonly", true);
        if (decision.decisionType === "ADD") {
            QRBacklogUrlDiv.show();
            QRType.text("Quality Requirement Added");
            decisionTypeText.text("Add Quality Requirement");
        }
        else {
            QRBacklogUrlDiv.hide();
            QRType.text("Quality Requirement Ignored");
            decisionTypeText.text("Ignore Quality Requirement");
        }
        decisionTypeButton.prop("disabled", true);

        $("#cancelSaveFooter").hide();
        $("#closeFooter").show();
    };

    $scope.newAlert = function () {
        var url = "api/notifyAlert";
        $http({
            method: "POST",
            url: url,
            data: {
                element : {
                    id: "duplication",
                    name: "Duplication Density",
                    type: "METRIC",
                    value: "0.4",
                    threshold: "0.5",
                    category: "duplication",
                    project_id: sessionStorage.getItem("prj")
                }
            }
        }).then(function () {
            //location.href = "QualityAlerts";
        });
    };

    $scope.newAlertFactor = function () {
        var url = "api/notifyAlert";
        $http({
            method: "POST",
            url: url,
            data: {
                element : {
                    id: "testingperformance",
                    name: "Performance of the tests",
                    type: "FACTOR",
                    value: "0.4",
                    threshold: "0.5",
                    category: "testingperformance",
                    project_id: sessionStorage.getItem("prj")
                }
            }
        }).then(function () {
            //location.href = "QualityAlerts";
        });
    };

    $scope.getKPIFactor = function (){
        var id = getParameterByName('id');
        if (id != null) {
            navTextSimple();
            var url = "../api/DetailedStrategicIndicators/CurrentEvaluation/" + id;
        } else {
            var url = "../api/DetailedStrategicIndicators/CurrentEvaluation";
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getKPIFactorTable = function(){
        var id = getParameterByName('id');
        if (id != null) {
            navTextSimple();
            var url = "../api/DetailedStrategicIndicators/HistoricalData/" + id;
        } else {
            var url = "../api/DetailedStrategicIndicators/HistoricalData";
        }
        $http({
            method : "GET",
            url : url,//"../api/DetailedStrategicIndicators/HistoricalData",
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getFactorQuality = function(){
        var id = getParameterByName('id');
        if (id != null) {
            navTextSimple();
            var url = "../api/QualityFactors/CurrentEvaluation/" + id;
        } else {
            var url = "../api/QualityFactors/CurrentEvaluation";
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getFactorQualityHistoric = function(){
        var id = getParameterByName('id');
        if (id != null) {
            navTextSimple();
            var url = "../api/QualityFactors/HistoricalData/" + id;
        } else {
            var url = "../api/QualityFactors/HistoricalData";
        }
        $http({
            method : "GET",
            url : url,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getMetricsTable = function(){
        var id = getParameterByName('id');
        if (id != null) {
            navTextComplex();
            var url = "../api/Metrics/CurrentEvaluation/" + id;
        }
        else {
            var url = "../api/Metrics/CurrentEvaluation";
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getMetricsTableHistorical = function(){
        var id = getParameterByName('id');
        if (id != null) {
            navTextComplex();
            var url = "../api/Metrics/HistoricalData/" + id;
        }
        else {
            var url = "../api/Metrics/HistoricalData";
        }
        $http({
            method : "GET",
            url : url,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            $scope.data = response.data;
        })
    };

    $scope.getURL = function(id, name, si, url2, isqf, cmd){
        si = getParameterByName('name');
        siid = getParameterByName('id');
        if (!isqf || si.length == 0) url2 = url2 + "?id=" + id + "&name=" + name;
        else {
            url2 = url2 + "?id=" + id + "&name=" + name + "&si=" + si + "&siid=" + siid + "&cmd=" + cmd;
        }
        var from = getParameterByName('from');
        var to = getParameterByName('to');
        if ($('#datepickerFrom').length || (from.length != 0 && to.length != 0)) {
            if ($('#datepickerFrom').length)
                url2 = url2 + "&from=" + $('#datepickerFrom').val() + "&to=" + $('#datepickerTo').val();
            else
                url2 = url2 + "&from=" + from + "&to=" + to;
        }
        location.href = url2;
    };

    $scope.getURLQR = function(name, url){
        url = url + "?name=" + name;
        location.href = url;
    };

    $scope.gotoQR = function(id){
      alert(id);
    };

    $scope.sortType = 'date';
    $scope.sortReverse = true;

    $scope.sortBy = function(keyName){
        if($scope.sortType === keyName) {
            $scope.sortReverse = !$scope.sortReverse;
        } else {
            $scope.sortReverse = false;
        }
        $scope.sortType = keyName;
        console.log('Type', $scope.sortType, 'Reverse', $scope.sortReverse);
    };
});



function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function changeFormat(date){
    var mydate = new Date(date);
    var year = mydate.getFullYear();
    var month = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"][mydate.getMonth()];
    var day = mydate.getDate();

    var formattedDate = day + ' ' + month + ' ' + year

    return formattedDate;
}

function navTextSimple() {
    var name = getParameterByName('name');
    if (name.length != 0) {
        $('a#originSIQF').text(name);
        var currentURL = window.location.href;
        if (currentURL.search("/Detailed") !== -1) {
            $('a#originSIQF').text(name + ' (SI)');
            $('h1#title').text('Detailed ' + name + ' Strategic Indicator');
        }
        else {
            $('a#originSIQF').text(name + ' (DSI)');
            $('h1#title').text('Factors for ' + name + ' Strategic Indicator');
        }
    }
}

function navTextComplex() {
    var name = getParameterByName('name');
    var si = getParameterByName('si');
    if (name.length !== 0) {
        if (si.length !== 0) {
            $('a#originSI').text(si + ' (DSI)');
            $('span#arrow').text('>');
            $('a#origin').text(name + ' (QF)');
        }
        else {
            $('a#origin').text(name + (' (QF)'));
        }
        $('h1#title').text('Metrics for ' + name + ' Factor');
    }
}

function giveQR(name, comment, description, type){
    var formData2 = new FormData();

    formData2.append("name", name);

    formData2.append("comment", comment);

    formData2.append("description", description);

    formData2.append("type", type);

    $.ajax({
        url: '../qr_value',
        data: formData2,
        type: "POST",
        contentType: false,
        processData: false,
        success: function() {
            location.reload(true);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
        }
    })
}
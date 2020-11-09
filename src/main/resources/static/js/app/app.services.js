var app = angular.module('TablesApp', []);
app.controller('TablesCtrl', function($scope, $http) {

    $scope.data = [];
    $scope.data_aux = [];

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));

    var profileId = sessionStorage.getItem("profile_id");

    $scope.getStratInd = function (){
        $http({
            method : "GET",
            url : "../api/strategicIndicators/current?profile="+profileId
        }).then(function mySuccess(response) {
            response.data.forEach(function (strategicIndicator) {
                var siDate = new Date(strategicIndicator.date);
                var today = new Date();
                today.setHours(0);
                today.setMinutes(0);
                today.setSeconds(0);
                var millisecondsInOneDay = 86400000;
                var millisecondsBetweenAssessmentAndToday = today.getTime() - siDate.getTime();
                var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
                if (oldAssessment) {
                    var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                    strategicIndicator.warning = "The assessment is " + daysOld + " days old. \n";
                }

                var mismatchDays = strategicIndicator.mismatchDays;
                if (mismatchDays > 0) {
                    strategicIndicator.warning += "The assessment of the factors and the strategic \nindicator has a difference of " + mismatchDays + " days. \n";
                }

                var missingFactors = strategicIndicator.missingFactors;
                if (missingFactors.length > 0) {
                    var factors = missingFactors.length === 1 ? missingFactors[0] : [ missingFactors.slice(0, -1).join(", "), missingFactors[missingFactors.length - 1] ].join(" and ");
                    strategicIndicator.warning += "The following factors were missing when \nthe strategic indicator was assessed: " + factors;
                }
            });
            $scope.data = response.data;
            $scope.sortType = 'name';
            $scope.sortReverse = false;
        })
    };

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));
    var profileId = sessionStorage.getItem("profile_id");

    $scope.getKPIEval = function(){
        $http({
            method : "GET",
            url : "../api/strategicIndicators/historical?profile="+profileId,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            var data = [];
            response.data.forEach(function (strategicIndicatorEval) {
                data.push({
                    id: strategicIndicatorEval.id,
                    name: strategicIndicatorEval.name,
                    date: strategicIndicatorEval.date,
                    description: strategicIndicatorEval.description,
                    value: strategicIndicatorEval.value_description,
                    categories: strategicIndicatorEval.categories_description,
                    rationale: strategicIndicatorEval.rationale
                });
            });
            $scope.data = data;
            $scope.sortType = 'name';
            $scope.sortReverse = false;
        })
    };

    $scope.getFeedback = function(){
        var id = getParameterByName('id');
        var url =  "../api/strategicIndicators/" + id + "/feedbackReport";
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
        $("#alertId").val(alert.id_element);
        $("#alertDate").val(alert.date);
        $("#alertType").val(alert.type);
        $("#alertElementId").val(alert.category);
        $("#alertName").val(alert.name);
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
            $scope.data = response.data;
            $scope.data.forEach(function (alert) {
                var relations = qualityModelRelations.get(alert.id_element);

                var strategicIndicators = relations.strategicIndicators;
                var strategicIndicatorsText = [];
                strategicIndicators.forEach(function (strategicIndicator) {
                    strategicIndicatorsText.push(strategicIndicator.name);
                });
                alert.strategicIndicators = strategicIndicatorsText.join(", ");

                var factors = relations.factors;
                var factorsText = [];
                factors.forEach(function (factor) {
                    factorsText.push(factor.name);
                });
                alert.factors = factorsText.join(", ");
            });
            clearAlertsPendingBanner();
        })
    };

    var qualityModelRelations = new Map();
    var strategicIndicatorsMap = new Map();
    var factorsMap = new Map();

    function getQualityModel () {
        getSIsAndFactorsNames();
        jQuery.ajax({
            dataType: "json",
            type: "GET",
            url : "api/strategicIndicators/qualityModel?prj=" + sessionStorage.getItem("prj"),
            async: false,
            success: function (data) {
                data.forEach(function (strategicIndicator) {
                    strategicIndicator.factors.forEach(function (factor) {
                        if (qualityModelRelations.has(factor.id)) {
                            var elements = qualityModelRelations.get(factor.id);
                            elements.strategicIndicators.push({
                                id: strategicIndicator.id,
                                name: strategicIndicatorsMap.get(strategicIndicator.id)
                            });
                        } else {
                            qualityModelRelations.set(factor.id, {
                                factors: [],
                                strategicIndicators: [{
                                    id: strategicIndicator.id,
                                    name: strategicIndicatorsMap.get(strategicIndicator.id)
                                }]
                            });
                        }

                        factor.metrics.forEach(function (metric) {
                            if (qualityModelRelations.has(metric.id)) {
                                var elements = qualityModelRelations.get(metric.id);
                                elements.factors.push({
                                    id: factor.id,
                                    name: factorsMap.get(factor.id)
                                });
                                elements.strategicIndicators.push({
                                    id: strategicIndicator.id,
                                    name: strategicIndicatorsMap.get(strategicIndicator.id)
                                });
                            } else {
                                qualityModelRelations.set(metric.id, {
                                    factors: [{
                                        id: factor.id,
                                        name: factorsMap.get(factor.id)
                                    }],
                                    strategicIndicators: [{
                                        id: strategicIndicator.id,
                                        name: strategicIndicatorsMap.get(strategicIndicator.id)
                                    }]
                                });
                            }
                        })
                    });
                });
            }
        });
    }

    function getSIsAndFactorsNames () {
        jQuery.ajax({
            dataType: "json",
            type: "GET",
            url: "api/strategicIndicators/qualityFactors/current?prj=" + sessionStorage.getItem("prj"),
            async: false,
            success: function (strategicIndicators) {
                strategicIndicators.forEach(function (strategicIndicator) {
                    strategicIndicatorsMap.set(strategicIndicator.id, strategicIndicator.name);
                    strategicIndicator.factors.forEach(function (factor) {
                        factorsMap.set(factor.id, factor.name);
                    })
                })
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
            var ignoreQRUrl = "api/alerts/"+alertId+"/qr/ignore?prj=" + sessionStorage.getItem("prj");
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
        var url = "api/alerts";
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
        var url = "api/alerts";
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
        console.log("sessionStorage: profile_id");
        console.log(sessionStorage.getItem("profile_id"));
        var profileId = sessionStorage.getItem("profile_id");
        var id = getParameterByName('id');
        if (id !== "") {
            navTextSimple();
            var url = "../api/strategicIndicators/" + id + "/qualityFactors/current?profile="+profileId;
        } else {
            var url = "../api/strategicIndicators/qualityFactors/current?profile="+profileId;
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            var data = [];
            response.data.forEach(function (strategicIndicatorEval) {
                strategicIndicatorEval.factors.forEach(function (factor) {
                    var strategicIndicator = {
                        id: strategicIndicatorEval.id,
                        date: strategicIndicatorEval.date,
                        strategicIndicatorName: strategicIndicatorEval.name,
                        strategicIndicatorValue: strategicIndicatorEval.value_description,
                        factorName: factor.name,
                        description: factor.description,
                        value: factor.value_description,
                        rationale: factor.rationale
                    };

                    //Warnings
                    var siDate = new Date(strategicIndicatorEval.date);
                    var today = new Date();
                    today.setHours(0);
                    today.setMinutes(0);
                    today.setSeconds(0);
                    var millisecondsInOneDay = 86400000;
                    var millisecondsBetweenAssessmentAndToday = today.getTime() - siDate.getTime();
                    var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
                    if (oldAssessment) {
                        var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                        strategicIndicator.warning = "The " + strategicIndicatorEval.name +  " assessment is " + daysOld + " days old. \n";
                    }

                    var mismatchDays = strategicIndicatorEval.mismatchDays;
                    if (mismatchDays > 0) {
                        strategicIndicator.warning += "The assessment of the factors and the " + strategicIndicatorEval.name + " strategic \nindicator has a difference of " + mismatchDays + " days. \n";
                    }

                    var missingFactors = strategicIndicatorEval.missingFactors;
                    if (missingFactors.length > 0) {
                        var factors = missingFactors.length === 1 ? missingFactors[0] : [ missingFactors.slice(0, -1).join(", "), missingFactors[missingFactors.length - 1] ].join(" and ");
                        strategicIndicator.warning += "The following factors were missing when \nthe " + strategicIndicatorEval.name + " strategic indicator was assessed: " + factors;
                    }

                    data.push(strategicIndicator);
                });
            });
            $scope.data = data;
            $scope.sortType = 'strategicIndicatorName';
            $scope.sortReverse = false;
        })
    };

    $scope.getKPIFactorTable = function(){
        console.log("sessionStorage: profile_id");
        console.log(sessionStorage.getItem("profile_id"));
        var profileId = sessionStorage.getItem("profile_id");
        var id = getParameterByName('id');
        if (id !== "") {
            navTextSimple();
            var url = "../api/strategicIndicators/" + id + "/qualityFactors/historical?profile="+profileId;
        } else {
            var url = "../api/strategicIndicators/qualityFactors/historical?profile="+profileId;
        }
        $http({
            method : "GET",
            url : url,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            var data = [];
            response.data.forEach(function (strategicIndicatorEval) {
                strategicIndicatorEval.factors.forEach(function (factor) {
                    data.push({
                        id: strategicIndicatorEval.id,
                        date: factor.date,
                        strategicIndicatorName: strategicIndicatorEval.name,
                        factorName: factor.name,
                        description: factor.description,
                        value: factor.value_description,
                        rationale: factor.rationale
                    })
                });
            });
            $scope.data = data;
            $scope.sortType = 'strategicIndicatorName';
            $scope.sortReverse = false;
        })
    };

    $scope.getFactorQuality = function(){
        var siid = getParameterByName('siid');
        navTextComplex();
        if (siid !== "") {
            var url = "../api/strategicIndicators/" + siid + "/qualityFactors/metrics/current";
        } else {
            var profileId = sessionStorage.getItem("profile_id");
            var url = "../api/qualityFactors/metrics/current?profile="+profileId;
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            var data = [];
            response.data.forEach(function (factorEval) {
                var id = getParameterByName('id');
                if (id !== "") { // see concrete detailed factor
                    if (factorEval.id == id) {
                        factorEval.metrics.forEach(function (metric) {
                            data.push({
                                id: factorEval.id,
                                date: metric.date,
                                factorName: factorEval.name,
                                metricName: metric.name,
                                description: metric.description,
                                value: metric.value_description,
                                rationale: metric.rationale
                            })
                        });
                    }
                } else { // see all detailed factors
                    factorEval.metrics.forEach(function (metric) {
                        data.push({
                            id: factorEval.id,
                            date: metric.date,
                            factorName: factorEval.name,
                            metricName: metric.name,
                            description: metric.description,
                            value: metric.value_description,
                            rationale: metric.rationale
                        })
                    });
                }
            });
            $scope.data = data;
            $scope.sortType = 'factorName';
            $scope.sortReverse = false;
        })
    };

    $scope.getDetailedFactorQualityHistoric = function(){
        var siid = getParameterByName('siid');
        navTextComplex();
        if (siid !== "") {
            var url = "../api/strategicIndicators/" + siid + "/qualityFactors/metrics/historical";
        } else {
            var profileId = sessionStorage.getItem("profile_id");
            var url = "../api/qualityFactors/metrics/historical?profile="+profileId;
        }
        $http({
            method : "GET",
            url : url,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            var data = [];
            response.data.forEach(function (factorEval) {
                var id = getParameterByName('id');
                if (id !== "") { // see concrete detailed factor
                    if (factorEval.id == id) {
                        factorEval.metrics.forEach(function (metric) {
                            data.push({
                                id: factorEval.id,
                                date: metric.date,
                                factorName: factorEval.name,
                                metricName: metric.name,
                                description: metric.description,
                                value: metric.value_description,
                                rationale: metric.rationale
                            })
                        });
                    }
                } else {
                    factorEval.metrics.forEach(function (metric) {
                        data.push({
                            id: factorEval.id,
                            date: metric.date,
                            factorName: factorEval.name,
                            metricName: metric.name,
                            description: metric.description,
                            value: metric.value_description,
                            rationale: metric.rationale
                        })
                    });
                }
            });
            $scope.data = data;
            $scope.sortType = 'factorName';
            $scope.sortReverse = false;
        })
    };

    $scope.getMetricsTable = function(){
        var id = getParameterByName('id');
        if (id !== "") {
            navTextComplex();
            var url = "../api/qualityFactors/" + id + "/metrics/current"
        }
        else {
            var url = "../api/metrics/current";
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            var data = [];
            response.data.forEach(function (metricEval) {
                data.push({
                    id: metricEval.id,
                    date: metricEval.date,
                    name: metricEval.name,
                    description: metricEval.description,
                    value: metricEval.value_description,
                    rationale: metricEval.rationale
                });
            });
            $scope.data = data;
            $scope.sortType = 'name';
            $scope.sortReverse = false;
        })
    };

    $scope.getMetricsTableHistorical = function(){
        var id = getParameterByName('id');
        if (id !== "") {
            navTextComplex();
            var url = "../api/qualityFactors/" + id + "/metrics/historical";
        }
        else {
            var url = "../api/metrics/historical";
        }
        $http({
            method : "GET",
            url : url,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            var data = [];
            var for_data = response.data;
            if (id !== "") {
                for_data = response.data[0].metrics;
            }
            for_data.forEach(function (metricEval) {
                data.push({
                    id: metricEval.id,
                    date: metricEval.date,
                    name: metricEval.name,
                    description: metricEval.description,
                    value: metricEval.value_description,
                    rationale: metricEval.rationale
                });
            });
            $scope.data = data;
            $scope.sortType = 'name';
            $scope.sortReverse = false;
        })
    };

    $scope.getURL = function(id, name, si, url2, isqf, isdqf, cmd){
        si = getParameterByName('si');
        if (si.length == 0)
            si = getParameterByName('name');
        siid = getParameterByName('siid');
        if (siid.length == 0)
            siid = getParameterByName('id');
        if (si.length == 0) url2 = url2 + "?id=" + id + "&name=" + name;
        else if (getParameterByName('si').length == 0 && isdqf) url2 = url2 + "?id=" + id + "&name=" + name;
        else {
            url2 = url2 + "?id=" + id + "&name=" + name + "&si=" + si + "&siid=" + siid + "&cmd=" + cmd;
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

    $scope.customComparator = function (v1, v2) {
        if (v1.value <= v2.value)
            return -1;
        else if (v2.value < v1.value)
            return 1;
    }

    $scope.getQualFact = function (){
        var id = getParameterByName('id');
        if (id !== "") {
            navTextSimple();
            var url = "../api/strategicIndicators/" + id + "/qualityFactors/current";
        } else {
            var profileId = sessionStorage.getItem("profile_id");
            var url = "../api/qualityFactors/current?profile="+profileId;
        }
        $http({
            method : "GET",
            url : url
        }).then(function mySuccess(response) {
            var result = response.data;
            if (id){
                result = response.data[0].factors;
            }
            result.forEach(function (factor) {
                var qfDate = new Date(factor.date);
                var today = new Date();
                today.setHours(0);
                today.setMinutes(0);
                today.setSeconds(0);
                var millisecondsInOneDay = 86400000;
                var millisecondsBetweenAssessmentAndToday = today.getTime() - qfDate.getTime();
                var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
                if (oldAssessment) {
                    var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                    factor.warning = "The assessment is " + daysOld + " days old. \n";
                }

                var mismatchDays = factor.mismatchDays;
                if (mismatchDays > 0) {
                    factor.warning += "The assessment of the metrics and the factors \n has a difference of " + mismatchDays + " days. \n";
                }

                var missingMetrics = factor.missingMetrics;
                if (missingMetrics && missingMetrics.length > 0) {
                    var factors = missingMetrics.length === 1 ? missingMetrics[0] : [ missingMetrics.slice(0, -1).join(", "), missingMetrics[missingMetrics.length - 1] ].join(" and ");
                    factor.warning += "The following metrics were missing when \nthe factor was assessed: " + factors;
                }
            });
            $scope.data = result;
            $scope.sortType = 'name';
            $scope.sortReverse = false;
        })
    };

    $scope.getFactorQualityHistoric = function(){
        var id = getParameterByName('id');
        if (id !== "") {
            navTextSimple();
            var url = "../api/strategicIndicators/" + id + "/qualityFactors/historical";
        } else {
            var profileId = sessionStorage.getItem("profile_id");
            var url = "../api/qualityFactors/historical?profile="+profileId;
        }
        $http({
            method : "GET",
            url : url,
            params: {from: $('#datepickerFrom').val(),
                to: $('#datepickerTo').val()}
        }).then(function mySuccess(response) {
            var data = [];
            var result = response.data;
            if (id){
                result = response.data[0].factors;
            }
            console.log(response.data);
            result.forEach(function (factorEval) {
                data.push({
                    id: factorEval.id,
                    date: factorEval.date,
                    name: factorEval.name,
                    description: factorEval.description,
                    value: factorEval.value.toFixed(2).replace(".", ","),
                    rationale: factorEval.rationale
                })
            });
            $scope.data = data;
            $scope.sortType = 'name';
            $scope.sortReverse = false;
        })
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
    var id = getParameterByName('id');
    var name = getParameterByName('name');
    var si = getParameterByName('si');
    if (id.length !== 0) {
        if (si.length === 0) {
            $('a#origin').text(name + ' (QF)'); // in DQF view
            $('a#originDQF').text(name + ' (DQF)'); // in Metric view
        } else {
            // in DQF view
            $('a#originSI').text(si + ' (DSI)');
            $('span#arrow').text('>');
            $('a#origin').text(name + ' (QF)');
            // in Metric view
            $('a#originDSI').text(si + ' (DSI)');
            $('span#arrow1').text('>');
            $('a#originQF').text(name + ' (QF)');
            $('span#arrow2').text('>');
            $('a#originDQF').text(name + ' (DQF)');
        }
        if (currentURL.search("/Detailed") !== -1) {
            $('h1#title').text('Detailed ' + name + ' Factor');
        } else {
            $('h1#title').text('Metrics for ' + name + ' Factor');
        }
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
var simulationColor = "#0579A8";
var currentColor = "#696969";

var patterns = [];

var strategicIndicators = [];
var qualityFactors = [];
var metrics = [];
var detailedCharts = [];
var factorsCharts = [];

var alertId;
var patternId;

function getDetailedStrategicIndicators () {
    jQuery.ajax({
        dataType: "json",
        url: "../api/DetailedStrategicIndicators/CurrentEvaluation",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            function compare (a, b) {
                if (a.name < b.name) return -1;
                else if (a.name > b.name) return 1;
                else return 0;
            }
            data.sort(compare);
            var titles = [];
            var ids = [];
            var labels = [];
            var values = [];
            for (i = 0; i < data.length; ++i) {
                //for each dsi save name to titles vector and id to ids vector
                titles.push(data[i].name);
                strategicIndicators.push({
                    id: data[i].id,
                    name: data[i].name
                });
                strategicIndicators[i].factors = [];
                ids.push(data[i].id);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].factors.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (data[i].factors[j].name.length < 27)
                        labels[i].push(data[i].factors[j].name);
                    else
                        labels[i].push(data[i].factors[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].factors[j].value);
                    strategicIndicators[i].factors.push({
                        id: data[i].factors[j].id,
                        name: data[i].factors[j].name
                    });
                }
            }
            showDetailedStrategicIndicators(titles, ids, labels, values);
        }
    });
}

function showDetailedStrategicIndicators (titles, ids, labels, values) {
    for (i = 0; i < titles.length; ++i) {
        var p = document.createElement('p');
        p.innerHTML = titles[i];
        p.style.fontSize = "16px";
        p.style.color = "#000"
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "15px 5px 15px 5px";
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        ctx.style.display = "inline";
        document.getElementById("radarDetailed").appendChild(div).appendChild(ctx);
        div.appendChild(p)
        ctx.getContext("2d");
        if (labels[i].length < 3) {
            labels[i].push(null);
            //values[i].push(null);
        }
        var chart = new Chart(ctx, {    //draw chart with the following config
            type: 'radar',
            data: {
                labels: labels[i],
                datasets: [{
                    label: titles[i],
                    backgroundColor: 'rgba(105, 105, 105, 0.2)',
                    borderColor: currentColor,
                    pointBackgroundColor: currentColor,
                    pointBorderColor: currentColor,
                    data: values[i],
                    fill: true
                }]
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: titles[i]
                },
                responsive: false,
                legend: {
                    display: false
                },
                scale: {    //make y axis scale 0 to 1 and set maximum number of axis lines
                    ticks: {
                        min: 0,
                        max: 1,
                        maxTicksLimit: 5
                    }
                }
            }
        });
        detailedCharts.push(chart);
        window.myLine = chart;
    }
}

function getFactors () {
    jQuery.ajax({
        dataType: "json",
        url: "../api/QualityFactors/CurrentEvaluation",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            var titles = [];
            var ids = [];
            var labels = [];
            var values = [];
            for (i = 0; i < data.length; ++i) {
                //for each dsi save name to titles vector and id to ids vector
                titles.push(data[i].name);
                qualityFactors.push({
                    id: data[i].id,
                    name: data[i].name
                });
                qualityFactors[i].metrics = [];
                ids.push(data[i].id);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].metrics.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (data[i].metrics[j].name.length < 27)
                        labels[i].push(data[i].metrics[j].name);
                    else
                        labels[i].push(data[i].metrics[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].metrics[j].value);
                    qualityFactors[i].metrics.push({
                        id: data[i].metrics[j].id,
                        name: data[i].metrics[j].name,
                        value: data[i].metrics[j].value
                    });
                }
            }
            showFactors(titles, ids, labels, values);
        }
    });
}

function showFactors (titles, ids, labels, values) {
    for (i = 0; i < titles.length; ++i) {
        var p = document.createElement('p');
        p.innerHTML = titles[i];
        p.style.fontSize = "16px";
        p.style.color = "#000"
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "15px 5px 15px 5px";
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        ctx.style.display = "inline";
        document.getElementById("radarFactors").appendChild(div).appendChild(ctx);
        div.appendChild(p)
        ctx.getContext("2d");
        if (labels[i].length === 2) {
            labels[i].push(null);
            //values[i].push(null);
        }
        var chart = new Chart(ctx, {    //draw chart with the following config
            type: 'radar',
            data: {
                labels: labels[i],
                datasets: [{
                    label: titles[i],
                    backgroundColor: 'rgba(105, 105, 105, 0.2)',
                    borderColor: currentColor,
                    pointBackgroundColor: currentColor,
                    pointBorderColor: currentColor,
                    data: values[i],
                    fill: true
                }]
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: titles[i]
                },
                responsive: false,
                legend: {
                    display: false
                },
                scale: {    //make y axis scale 0 to 1 and set maximum number of axis lines
                    ticks: {
                        min: 0,
                        max: 1,
                        maxTicksLimit: 5
                    }
                }
            }
        });
        factorsCharts.push(chart);
        window.myLine = chart;
    }
}

function loadQRPattern (patternId, alertId) {
    jQuery.ajax({
        dataType: "json",
        url: "../api/alerts/" + alertId + "/qrPatterns",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            patterns = data;
            if (patterns.length > 1) $("#patternListButton").prop("disabled", false);
            else $("#patternListButton").prop("disabled", true);

            for (var i = 0; i < patterns.length; i++) {
                if (patterns[i].id == patternId) {
                    showQRPattern(patterns[i])
                }
            }
        }
    });
}

function getAllQRPatterns () {
    jQuery.ajax({
        dataType: "json",
        url: "../api/qrPatterns",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            patterns = data;
            if (patterns.length > 1) $("#patternListButton").prop("disabled", false);
            else $("#patternListButton").prop("disabled", true);
        }
    });
}

function showQRPattern (pattern) {
    var QRRequirement = $("#QRRequirementSimulation");
    var QRDescription = $("#QRDescriptionSimulation");
    var QRGoal = $("#QRGoalSimulation");
    QRRequirement.val(pattern.forms[0].fixedPart.formText);
    QRDescription.val(pattern.forms[0].description);
    QRGoal.val(pattern.goal);
    getAllMetricsAndShowMetricForPattern(pattern.id);
}

function getAllMetricsAndShowMetricForPattern (patternId){
    var url = "../api/Metrics/CurrentEvaluation";
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            metrics = response;
            getAndShowMetricsForPattern(patternId);
        }
    });
}

function getAndShowMetricsForPattern (patternId) {
    $.ajax({
        url: "../api/qrPatterns/"+patternId+"/metrics",
        type: "GET",
        success: function (metricsForPattern) {
            metrics.forEach(function (metric) {
                metricsForPattern.forEach(function (metricForPattern) {
                    if (metric.id === metricForPattern)
                        showMetricSlider(metric);
                })
            });
        }
    });
}

function showMetricSlider (metric) {
    var metricsDiv = $("#metricsSliders");
    var div = document.createElement('div');
    div.style.marginTop = "1em";
    div.style.marginBottom = "1em";

    var label = document.createElement('label');
    label.id = metric.id;
    label.textContent = metric.name;
    label.title = metric.description;
    div.appendChild(label);

    div.appendChild(document.createElement('br'));

    var slider = document.createElement("input");
    slider.id = "sliderValue" + metric.id;
    slider.style.width = "80%";
    var sliderConfig = {
        id: "slider" + metric.id,
        min: 0,
        max: 1,
        step: 0.01,
        value: metric.value
    };
    // Add original value
    var start, end;
    if (metric.value === 0) {
        start = 0;
        end = 0.03;
    }
    else if (metric.value === 1) {
        start = 0.97;
        end = 1;
    }
    else {
        start = metric.value - 0.015;
        end = metric.value + 0.015;
    }
    sliderConfig.rangeHighlights = [{
        start: start,
        end: end
    }];
    div.appendChild(slider);
    metricsDiv.append(div);
    $("#"+slider.id).slider(sliderConfig);
    $(".slider-rangeHighlight").css("background", currentColor);

    //Change QR value
    updateQRValueText(metric.value.toFixed(2));
}

function updateQRValueText (value) {
    var requirement = $("#QRRequirementSimulation").val();
    $("#QRRequirementSimulation").val(requirement.replace(/%.*%|\d*\.?\d+/, value));
}

$("#patternListButton").click(function () {
    $("#QRCandidates").empty();
    for (var i = 0; i < patterns.length; i++) {
        $("#QRCandidates").append('<button class="list-group-item">' + patterns[i].name + '</button>');
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
            var position = $('.active').index();
            var pattern = patterns[position];
            patternId = pattern.id;
            showQRPattern(pattern);
            $('#metricsSliders').empty();
            $("#QRListModal").modal('hide');
        }
    });
});

$('#restore').click(function () {
    $('#metricsSliders').empty();
    removeSimulation();
    getAllMetricsAndShowMetricForPattern(patternId);
});

function removeSimulation() {
    d3.selectAll('.simulation').remove();
    if (factorsCharts[0].data.datasets.length > 1) {
        for (var i = 0; i < factorsCharts.length; i++) {
            factorsCharts[i].data.datasets.shift();
            factorsCharts[i].update();
        }
    }
    if (detailedCharts[0].data.datasets.length > 1) {
        for (var i = 0; i < detailedCharts.length; i++) {
            detailedCharts[i].data.datasets.shift();
            detailedCharts[i].update();
        }
    }
}

$('#apply').click(function () {
    var metricsSlider = [];

    Array.from($("#metricsSliders").children()).forEach(function(element) {
        metricsSlider.push({
            id: element.children[0].id,
            name: element.children[0].textContent,
            value: element.children[3].value
        });

        updateQRValueText(element.children[3].value);
    });

    for (var i = 0; i < qualityFactors.length; i++) {
        var qualityFactor = qualityFactors[i];
        var dataset = {
            label: qualityFactor.name,
            backgroundColor: 'rgba(5, 121, 168, 0.2)',
            borderColor: simulationColor,
            pointBackgroundColor: simulationColor,
            pointBorderColor: simulationColor,
            data: [],
            fill: true
        };
        for (var j = 0; j < qualityFactor.metrics.length; j++) {
            var metric = qualityFactor.metrics[j];
            var newMetric = metricsSlider.find(function (element) {
                return element.id === metric.id;
            });
            if (newMetric) dataset.data.push(newMetric.value);
            else dataset.data.push(metric.value);
        }

        if (factorsCharts[i].data.datasets.length > 1)
            factorsCharts[i].data.datasets[0].data = dataset.data;
        else
            factorsCharts[i].data.datasets.unshift(dataset);
        factorsCharts[i].update();
    }

    var newMetrics = [];
    for (var i = 0; i < metricsSlider.length; i++) {
        var previousMetric = metrics.find(function (element) {
            return element.id === metricsSlider[i].id
        });
        if (parseFloat(metricsSlider[i].value) !== parseFloat(previousMetric.value.toFixed(2)))
            newMetrics.push(metricsSlider[i]);
    }

    var year = metrics[0].date.year;
    var month = "";
    if (metrics[0].date.monthValue < 10)
        month = "0" + metrics[0].date.monthValue;
    else
        month = metrics[0].date.monthValue;
    var day = "";
    if (metrics[0].date.dayOfMonth < 10)
        day = "0" + metrics[0].date.dayOfMonth;
    else
        day = metrics[0].date.dayOfMonth;
    var date = year + "-" + month + "-" + day;

    $.ajax({
        url: "../api/QualityFactors/Simulate?date="+date,
        data: JSON.stringify(newMetrics),
        type: "POST",
        contentType: 'application/json',
        success: function(qualityFactors) {
            for (var i = 0; i < strategicIndicators.length; i++) {
                var strategicIndicator = strategicIndicators[i];
                var dataset = {
                    label: strategicIndicator.name,
                    backgroundColor: 'rgba(5, 121, 168, 0.2)',
                    borderColor: simulationColor,
                    pointBackgroundColor: simulationColor,
                    pointBorderColor: simulationColor,
                    data: [],
                    fill: true
                };
                for (var j = 0; j < strategicIndicator.factors.length; j++) {
                    var factor = strategicIndicator.factors[j];
                    var newFactor = qualityFactors.find(function (element) {
                        return element.id === factor.id;
                    });
                    dataset.data.push(newFactor.value);
                }

                if (detailedCharts[i].data.datasets.length > 1)
                    detailedCharts[i].data.datasets[0].data = dataset.data;
                else
                    detailedCharts[i].data.datasets.unshift(dataset);
                detailedCharts[i].update();
            }
            simulateSI(qualityFactors);
        }
    });
});

function simulateSI (qualityFactors) {
    var qfs = [];
    for (var i = 0; i < qualityFactors.length; i++) {
        qfs.push({
            id: qualityFactors[i].id,
            name: qualityFactors[i].name,
            value: qualityFactors[i].value
        });
    }

    var formData = new FormData();
    formData.append("factors", JSON.stringify(qfs));

    $.ajax({
        url: "../api/Simulate",
        data: formData,
        type: "POST",
        contentType: false,
        processData: false,
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 405)
                alert(textStatus);
        },
        success: function(result) {
            data = result;
            drawSimulationNeedle("gaugeChart", 200, 237, simulationColor);
        }
    });
}

$('#decision').click(function () {
    $("#QRModal").modal();
    $("#simulateButton").hide();

    $("#QRGoal").val($("#QRGoalSimulation").val());
    $("#QRRequirement").val($("#QRRequirementSimulation").val());
    $("#QRDescription").val($("#QRDescriptionSimulation").val());
    $("#QRDecisionRationale").val("");

    var addQR = function () {
        var goal = $("#QRGoal").val();
        var requirement = $("#QRRequirement").val();
        var description = $("#QRDescription").val();
        var rationale = $("#QRDecisionRationale").val();
        var addQRUrl;
        if (alertId) addQRUrl = "../api/alerts/"+alertId+"/qr";
        else addQRUrl = "../api/qr";
        $.ajax({
            method : "GET",
            url : "../api/backlogUrl",
            dataType: "json"
        }).then(function (response) {
            // add QR to backlog
            var backlogUrl = "../"+response.backlogUrl;
            if (backlogUrl !== "") {
                $.ajax({
                    method: "POST",
                    url: backlogUrl,
                    data: {
                        issue_summary: requirement,
                        issue_description: description,
                        issue_type: "Story"
                    },
                    dataType: "json"
                }).then(function (response) {
                    // add QR to database
                    var issue = response;
                    var body = new URLSearchParams();
                    body.set('requirement', requirement);
                    body.set('description', description);
                    body.set('goal', goal);
                    body.set('rationale', rationale);
                    body.set('backlogId', issue.issue_id);
                    body.set('backlogUrl', issue.issue_url);
                    body.set('patternId', patternId);
                    $.ajax({
                        method: "POST",
                        url: addQRUrl,
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        },
                        data: body.toString()
                    }).then(function () {
                        // show QR info
                        $("#QRModal").modal('hide');
                        $("#messageModalTitle").text("The Quality Requirement has been added to the backlog successfully");
                        $("#messageModalContent").html("<b>Issue id: </b>" + issue.issue_id + "</br>" +
                            "<b>Issue url: </b><a href='" + issue.issue_url + "' target='_blank'>" + issue.issue_url + "</a>");
                        $("#messageModal").modal();
                        $("#acceptButton").on('click', function () {
                            if (patternId && alertId)
                                location.href = "../QualityAlerts";
                        });
                    });
                }, function () {
                    // In case of error, add QR to database with null backlog values
                    var body = new URLSearchParams();
                    body.set('requirement', requirement);
                    body.set('description', description);
                    body.set('goal', goal);
                    body.set('rationale', rationale);
                    body.set('backlogId', "");
                    body.set('backlogUrl', "");
                    body.set('patternId', patternId);
                    $.ajax({
                        method: "POST",
                        url: addQRUrl,
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded"
                        },
                        data: body.toString()
                    }).then(function () {
                        $("#QRModal").modal('hide');
                        if (patternId && alertId)
                            location.href = "../QualityAlerts";
                    });
                });
            } else {
                // add QR to database
                var body = new URLSearchParams();
                body.set('requirement', requirement);
                body.set('description', description);
                body.set('goal', goal);
                body.set('rationale', rationale);
                body.set('backlogId', "");
                body.set('backlogUrl', "");
                body.set('patternId', patternId);
                $.ajax({
                    method: "POST",
                    url: addQRUrl,
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: body.toString()
                }).then(function () {
                    $("#QRModal").modal('hide');
                    if (patternId && alertId)
                        location.href = "../QualityAlerts";
                });
            }
        });
    };

    var ignoreQR = function () {
        var rationale = $("#QRDecisionRationale").val();
        var ignoreQRUrl;
        if (alertId) ignoreQRUrl = "../api/alerts/"+alertId+"/ignore";
        else ignoreQRUrl = "../api/qr/ignore";
        var body = new URLSearchParams();
        body.set('rationale', rationale);
        body.set('patternId', patternId);
        $.ajax({
            method: "POST",
            url: ignoreQRUrl,
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: body.toString()
        }).then(function () {
            $("#QRModal").modal('hide');
            if (patternId && alertId)
                location.href = "../QualityAlerts";
        });
    };

    var decisionTypeButton = $("#decisionTypeButton");
    decisionTypeButton.prop("disabled", false);
    var decisionTypeText = $("#decisionType");
    decisionTypeText.text("Select");
    var saveButton = $("#saveButton");
    saveButton.prop("disabled", true);

    $("#addQR").on('click', function () {
        decisionTypeText.text("Add QR");
        saveButton.prop("disabled", false);
        saveButton.unbind();
        saveButton.click(addQR);
    });

    $("#ignoreQR").on('click', function () {
        decisionTypeText.text("Ignore QR");
        saveButton.prop("disabled", false);
        saveButton.unbind();
        saveButton.click(ignoreQR);
    });
});

window.onload = function () {
    $("#simulationColor").css("background-color", simulationColor);
    $("#simulationColorDetailed").css("background-color", simulationColor);
    $("#simulationColorFactors").css("background-color", simulationColor);
    $("#currentColor").css("background-color", currentColor);
    $("#currentColorDetailed").css("background-color", currentColor);
    $("#currentColorFactors").css("background-color", currentColor);

    getFactors();
    getDetailedStrategicIndicators();
    getData(200, 237, false, false, currentColor);

    patternId = getParameterByName("pattern");
    alertId = getParameterByName("alert");
    if (patternId && alertId) {
        loadQRPattern(patternId, alertId);
    } else {
        getAllQRPatterns();
    }
};
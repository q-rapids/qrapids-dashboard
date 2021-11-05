var isdsi = false;
var isqf = true;
var isdqf = false;

var qualityModelSIMetrics = new Map();

var url;
if (getParameterByName('id').length !== 0) {
    url = parseURLSimple("../api/strategicIndicators/qualityFactors/historical");
} else {
    var profileId = sessionStorage.getItem("profile_id");
    url = parseURLSimple("../api/qualityFactors/historical?profile="+profileId);
}

//initialize data vectors
var texts = [];
var value = [];
var labels = [];
var ids = [];

var categories = [];

function getData() {
    getQualityModel();
    getDecisions();
    texts = [];
    value = [];
    labels = [];
    ids = [];
    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        data: {
            "from": $('#datepickerFrom').val(),
            "to": $('#datepickerTo').val()
        },
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            if (getParameterByName('id').length !== 0) {
                data = data[0].factors;
            }
            sortDataAlphabetically(data);
            console.log("inside " + url);
            console.log(data);
            j = 0;
            var line = [];
            var decisionsAdd = [];
            var decisionsIgnore = [];
            if (data[j]) {
                last = data[j].id;
                texts.push(data[j].name);
                labels.push([data[j].name]);
                ids.push(data[j].id);
            }
            while (data[j]) {
                //check if we are still on the same Factor
                if (data[j].id != last) {
                    var val = [line];
                    if (decisionsAdd.length > 0) {
                        val.push(decisionsAdd);
                    }
                    if (decisionsIgnore.length > 0) {
                        val.push(decisionsIgnore);
                    }
                    value.push(val);
                    line = [];
                    decisionsAdd = [];
                    decisionsIgnore = [];
                    last = data[j].id;
                    texts.push(data[j].name);
                    var labelsForOneChart = [];
                    labelsForOneChart.push(data[j].name);
                    buildDecisionVectors(decisionsAdd, decisionsIgnore, data[j].id);
                    if (decisionsAdd.length > 0)
                        labelsForOneChart.push("Added decisions");
                    if (decisionsIgnore.length > 0)
                        labelsForOneChart.push("Ignored decisions");
                    labels.push(labelsForOneChart);
                    ids.push(data[j].id);
                }
                //push date and value to line vector
                if (!isNaN(data[j].value.first)) {
                    line.push({
                        x: data[j].date,
                        y: data[j].value.first
                    });
                }
                ++j;
            }
            //push line vector to values vector for the last metric
            if (data[j - 1]) {
                var val = [line];
                if (decisionsAdd.length > 0)
                    val.push(decisionsAdd);
                if (decisionsIgnore.length > 0)
                    val.push(decisionsIgnore);
                value.push(val);
            }
            getFactorsCategories();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                warningUtils("Error","Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400)
                warningUtils("Error", "Datasource connection failed.");
        }
    });
}

function getQualityModel () {
    jQuery.ajax({
        dataType: "json",
        type: "GET",
        url : "../api/strategicIndicators/qualityModel",
        async: false,
        success: function (data) {
            data.forEach(function (strategicIndicator) {
                var metrics = [];
                strategicIndicator.factors.forEach(function (factor) {
                    factor.metrics.forEach(function (metric) {
                        metrics.push(metric.id);
                    })
                });
                qualityModelSIMetrics.set(strategicIndicator.id, metrics);
            });
        }
    });
}

function buildDecisionVectors (decisionsAdd, decisionsIgnore, strategicIndicatorId) {
    var metricsForStrategicIndicator = qualityModelSIMetrics.get(strategicIndicatorId);
    if (metricsForStrategicIndicator) {
        metricsForStrategicIndicator.forEach(function (metricId) {
            if (decisions.has(metricId)) {
                var metricDecisions = decisions.get(metricId);
                for (var l = 0; l < metricDecisions.length; l++) {
                    if (metricDecisions[l].type === "ADD") {
                        decisionsAdd.push({
                            x: metricDecisions[l].date,
                            y: 1.1,
                            requirement: metricDecisions[l].requirement,
                            comments: metricDecisions[l].comments
                        });
                    }
                    else {
                        decisionsIgnore.push({
                            x: metricDecisions[l].date,
                            y: 1.2,
                            requirement: metricDecisions[l].requirement,
                            comments: metricDecisions[l].comments
                        });
                    }
                }
            }
        });
    }
}

function sortDataAlphabetically (data) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    data.sort(compare);
}

function getFactorsCategories () {
    jQuery.ajax({
        url: "../api/qualityFactors/categories",
        type: "GET",
        async: true,
        success: function (response) {
            categories = response;
            drawChart();
        }
    });
}

window.onload = function() {
    getData();
};
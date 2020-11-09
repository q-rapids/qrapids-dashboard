var isdsi = true;
console.log("sessionStorage: profile_id");
console.log(sessionStorage.getItem("profile_id"));
var profileId = sessionStorage.getItem("profile_id");
var url = parseURLSimple("../api/strategicIndicators/qualityFactors/historical?profile="+profileId);

var qualityModelSIMetrics = new Map();

//initialize data vectors
var texts = [];
var ids = [];
var labels = [];
var value = [];

var categories = [];

function getData() {
    getQualityModel();
    getDecisions();
    texts = [];
    ids = [];
    labels = [];
    value = [];
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
            sortDataAlphabetically(data);
            for (var i = 0; i < data.length; ++i) {
                //for each dsi save name to texts vector and id to ids vector
                if (data[i].factors.length > 0) {
                    texts.push(data[i].name);
                    ids.push(data[i].id);

                    value.push([[]]);
                    last = data[i].factors[0].id;
                    labels.push([data[i].factors[0].name]);
                    k = 0;
                    for (j = 0; j < data[i].factors.length; ++j) {
                        //check if we are still on the same factor
                        if (last != data[i].factors[j].id) {
                            labels[i].push(data[i].factors[j].name);
                            last = data[i].factors[j].id;
                            ++k;
                            value[i].push([]);
                        }
                        //push date and value to values vector
                        if (!isNaN(data[i].factors[j].value))
                        {
                            value[i][k].push(
                                {
                                    x: data[i].factors[j].date,
                                    y: data[i].factors[j].value
                                }
                            );
                        }
                    }
                    var decisionsAdd = [];
                    var decisionsIgnore = [];
                    buildDecisionVectors(decisionsAdd, decisionsIgnore, data[i].id);
                    if (decisionsAdd.length > 0) {
                        value[i].push(decisionsAdd);
                        labels[i].push("Added decisions");
                    }
                    if (decisionsIgnore.length > 0) {
                        value[i].push(decisionsIgnore);
                        labels[i].push("Ignored decisions");
                    }
                } else {
                    data.splice(i, 1);
                    --i;
                }
            }
            getFactorsCategories();
        }
    });
}

function getQualityModel () {

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));
    var profileId = sessionStorage.getItem("profile_id");

    jQuery.ajax({
        dataType: "json",
        type: "GET",
        url : "../api/strategicIndicators/qualityModel?profile="+profileId,
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
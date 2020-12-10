var isdsi = false;
var isqf = false;
var isdqf = true;

var profileId = sessionStorage.getItem("profile_id");
var url = parseURLComposed("../api/qualityFactors/metrics/historical?profile="+profileId);

//initialize data vectors
var texts = [];
var ids = [];
var labels = [];
var value = [];

var categories = [];

var decisions = new Map();

function getData() {
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
            console.log(url);
            console.log("getData() in QF Historical");
            sortDataAlphabetically(data);
            console.log(data);
            for (i = 0; i < data.length; ++i) {
                //for each qf save name to texts vector and id to ids vector
                if (data[i].metrics.length > 0) {
                    texts.push(data[i].name);
                    ids.push(data[i].id);

                    value.push([[]]);
                    last = data[i].metrics[0].id;
                    labels.push([data[i].metrics[0].name]);
                    k = 0;
                    var decisionsAdd = [];
                    var decisionsIgnore = [];
                    for (j = 0; j < data[i].metrics.length; ++j) {
                        //check if we are still on the same metric
                        if (last !== data[i].metrics[j].id) {
                            buildDecisionVectors(decisionsAdd, decisionsIgnore, data[i].metrics[j - 1].id);
                            // New metric
                            labels[i].push(data[i].metrics[j].name);
                            last = data[i].metrics[j].id;
                            ++k;
                            value[i].push([]);
                        }
                        //push date and value to values vector
                        if (!isNaN(data[i].metrics[j].value)) {
                            value[i][k].push(
                                {
                                    x: data[i].metrics[j].date,
                                    y: data[i].metrics[j].value
                                }
                            );
                        }
                    }
                    buildDecisionVectors(decisionsAdd, decisionsIgnore, data[i].metrics[data[i].metrics.length - 1].id);
                    // Add decisions to chart
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
            console.log(texts);
            console.log(ids);
            console.log(labels);
            console.log(value);
            getMetricsCategories();
        }
    });
}

function buildDecisionVectors (decisionsAdd, decisionsIgnore, metricId) {
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
}

function sortDataAlphabetically (data) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    data.sort(compare);
}

function getMetricsCategories () {
    jQuery.ajax({
        url: "../api/metrics/categories",
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
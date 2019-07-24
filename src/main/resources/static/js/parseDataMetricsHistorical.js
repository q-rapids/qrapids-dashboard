var isSi = false;
var isdsi = false;
var isqf = false;

var url;
if (getParameterByName('id').length !== 0) {
    url = parseURLMetrics("../api/qualityFactors/metrics/historical");
} else {
    url = parseURLMetrics("../api/metrics/historical");
}

//initialize data vectors
var texts = [];
var value = [];
var labels = [];

var decisions = new Map();

function getData() {
    getDecisions();
    texts = [];
    value = [];
    labels = [];
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
            j = 0;
            var line = [];
            var decisionsAdd = [];
            var decisionsIgnore = [];
            if (data[j]) {
                last = data[j].id;
                texts.push(data[j].name);
                labels.push([data[j].name]);
            }
            while (data[j]) {
                //check if we are still on the same metric
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
                    if (decisions.has(data[j].id)) {
                        var metricDecisions = decisions.get(data[j].id);
                        for (var i = 0; i < metricDecisions.length; i++) {
                            if (metricDecisions[i].type === "ADD") {
                                decisionsAdd.push({
                                    x: metricDecisions[i].date,
                                    y: 1.1,
                                    requirement: metricDecisions[i].requirement,
                                    comments: metricDecisions[i].comments
                                });
                            }
                            else {
                                decisionsIgnore.push({
                                    x: metricDecisions[i].date,
                                    y: 1.2,
                                    requirement: metricDecisions[i].requirement,
                                    comments: metricDecisions[i].comments
                                });
                            }
                        }
                        if (decisionsAdd.length > 0)
                            labelsForOneChart.push("Added decisions");
                        if (decisionsIgnore.length > 0)
                            labelsForOneChart.push("Ignored decisions");
                    }
                    labels.push(labelsForOneChart);
                }
                //push date and value to line vector
                if (!isNaN(data[j].value)) {
                    line.push({
                        x: data[j].date,
                        y: data[j].value
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
            drawChart();
        }
    });
}

window.onload = function() {
    getData();
};
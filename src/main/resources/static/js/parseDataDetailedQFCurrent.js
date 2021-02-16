var isdsi = false;
var id = false;

var url;
if (getParameterByName('id').length !== 0) {
    url = parseURLComposed("../api/qualityFactors/metrics/current");
    id = true;
} else {
    var profileId = sessionStorage.getItem("profile_id");
    url = parseURLComposed("../api/qualityFactors/metrics/current?profile="+profileId);
}

//initialize data vectors
var titles = [];
var labels = [];
var ids = [];
var values = [];
var warnings = [];
var categories = [];

function getData() {
    titles = [];
    labels = [];
    ids = [];
    values = [];
    categories = [];

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(url);
            console.log("getData() in QF Current");
            sortDataAlphabetically(data);
            console.log(data);
            for (i = 0; i < data.length; ++i) {
                //for each qf save name to titles vector and id to ids vector
                titles.push(data[i].name + "<br/>" + data[i].value_description);
                ids.push(data[i].id);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].metrics.length; ++j) {
                    //for each metric save name to labels vector and value to values vector
                    if (data[i].metrics[j].name.length < 27)
                        labels[i].push(data[i].metrics[j].name);
                    else
                        labels[i].push(data[i].metrics[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].metrics[j].value);
                }

                // Warnings
                var messages = [];

                var today = new Date();
                today.setHours(0);
                today.setMinutes(0);
                today.setSeconds(0);
                var millisecondsInOneDay = 86400000;
                var millisecondsBetweenAssessmentAndToday = today.getTime() - new Date(data[i].date).getTime();
                var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
                if (oldAssessment) {
                    var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                    var message = "The assessment is " + daysOld + " days old.";
                    messages.push(message)
                }

                var mismatchDays = data[i].mismatchDays;
                if (mismatchDays > 0) {
                    var message = "The assessment of the factors and the metrics has a difference of " + mismatchDays + " days.";
                    messages.push(message);
                }

                var missingMetrics = data[i].missingMetrics;
                if (missingMetrics && missingMetrics.length > 0) {
                    var factors = missingMetrics.length === 1 ? missingMetrics[0] : [ missingMetrics.slice(0, -1).join(", "), missingMetrics[missingMetrics.length - 1] ].join(" and ");
                    var message = "The following metrics were missing when the quality factor was assessed: " + factors + ".";
                    messages.push(message);
                }

                warnings.push(messages);
            }
            getMetricsCategories();
        }
    });

    console.log(titles);
    console.log(labels);
    console.log(values);
}

function sortDataAlphabetically (data) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    data.sort(compare);
}

function getMetricsCategories() {
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
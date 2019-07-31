var url = parseURLSimple("../api/strategicIndicators/qualityFactors/current");

var isdsi = true;

//initialize data vectors
var titles = [];
var ids = [];
var labels = [];
var values = [];
var warnings = [];

function getData() {
    //empty previous data
    titles = [];
    ids = [];
    labels = [];
    values = [];

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            var assessmentDate;
            for (i = 0; i < data.length; ++i) {
                //for each dsi save name to titles vector and id to ids vector
                var siDate = new Date(data[i].date);
                if (!assessmentDate) {
                    assessmentDate = siDate;
                } else if (assessmentDate < siDate) {
                    assessmentDate = siDate;
                }
                titles.push(data[i].name + "<br/>" + data[i].value_description);
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
                }

                // Warnings
                var messages = [];

                var today = new Date();
                today.setHours(0);
                today.setMinutes(0);
                today.setSeconds(0);
                var millisecondsInOneDay = 86400000;
                var millisecondsBetweenAssessmentAndToday = today.getTime() - siDate.getTime();
                var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
                if (oldAssessment) {
                    var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                    var message = "The assessment is " + daysOld + " days old.";
                    messages.push(message)
                }

                var mismatchDays = data[i].mismatchDays;
                if (mismatchDays > 0) {
                    var message = "The assessment of the factors and the strategic indicator has a difference of " + mismatchDays + " days.";
                    messages.push(message);
                }

                var missingFactors = data[i].missingFactors;
                if (missingFactors.length > 0) {
                    var factors = missingFactors.length === 1 ? missingFactors[0] : [ missingFactors.slice(0, -1).join(", "), missingFactors[missingFactors.length - 1] ].join(" and ");
                    var message = "The following factors were missing when the strategic indicator was assessed: " + factors + ".";
                    messages.push(message);
                }

                warnings.push(messages);
            }
            $("#assessmentDate").text(assessmentDate.toLocaleDateString());
            drawChart();
        }
    });
}
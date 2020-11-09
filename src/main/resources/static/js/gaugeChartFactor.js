var lowThresh;
var upperThresh;
var angle;
var target;
var tau = Math.PI / 2;
var id = false;

var url;
if (getParameterByName('id').length !== 0) {
    id = true;
    url = parseURLSimple("../api/strategicIndicators/qualityFactors/current");
} else {
    var profileId = sessionStorage.getItem("profile_id");
    url = parseURLSimple("../api/qualityFactors/current?profile="+profileId);
}

var urlLink;

function getData(width, height) {
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            getFactorsCategories(data, width, height);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                alert("Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400)
                alert("Datasource connection failed.");
        }
    });
}

function getFactorsCategories (data, width, height) {
    jQuery.ajax({
        url: "../api/qualityFactors/categories",
        type: "GET",
        async: true,
        success: function (categories) {
            if (id) { // in case we show factors for one detailed si
                drawChart(data[0].factors, "gaugeChart", width, height, categories, true);
            } else { // in case we show all factors
                drawChart(data, "gaugeChart", width, height, categories, true);
            }
        }
    });
}

function drawChart(factors, container, width, height, categories) {

    for (i = 0; i < factors.length; ++i) {

        //0 to 1 values to angular values
        angle = factors[i].value * 180 + 90;
        upperThresh = 0.66 * Math.PI - Math.PI / 2;
        lowThresh = 0.33 * Math.PI - Math.PI / 2;

        var arc = d3.arc()      //create arc starting at -90 degreees
            .innerRadius(70 * width / 250)
            .outerRadius(110 * width / 250)
            .startAngle(-tau);

        //create chart svg with hyperlink
        var svg = d3.select(container).append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("class", "chart")
            .append("g")
            .attr("transform",
                "translate(" + width / 2 + "," + height / 2 + ")");

        //draw blue background for charts
        svg.append("path")
            .datum({endAngle: Math.PI / 2})
            .style("fill", "#0579A8")
            .attr("d", arc);

        categories.forEach(function (category) {
            var threshold = category.upperThreshold * Math.PI - Math.PI / 2;
            svg.append("path")
                .datum({endAngle: threshold})
                .style("fill", category.color)
                .attr("d", arc);
        });

        //create needle
        var arc2 = d3.arc()
            .innerRadius(0)
            .outerRadius(100 * width / 250)
            .startAngle(-0.05)
            .endAngle(0.05);

        //draw needle in correct position depending on it's angle
        svg.append("path")
            .style("fill", "#000")
            .attr("d", arc2)
            .attr("transform", "translate(" + -100 * width / 250 * Math.cos((angle - 90) / 180 * Math.PI) + "," + -100 * width / 250 * Math.sin((angle - 90) / 180 * Math.PI) + ") rotate(" + angle + ")");

        //create small circle at needle base
        var arc3 = d3.arc()
            .innerRadius(0)
            .outerRadius(10 * width / 250)
            .startAngle(0)
            .endAngle(Math.PI * 2);

        //draw needle base
        svg.append("path")
            .style("fill", "#000")
            .attr("d", arc3);

        //add text under the gauge
        var name;
        if (factors[i].name.length > 23) name = factors[i].name.slice(0, 20) + "...";
        else name = factors[i].name;
        svg.append("text")
            .attr("id", "name" + i)
            .attr("x", 0)
            .attr("y", 50 * width / 250)
            .attr("text-anchor", "middle")
            .attr("title", factors[i].name)
            .style("font-size", 11 + 8 * width / 250 + "px")
            .text(name);

        d3.select("#name" + i).append("title").text(factors[i].name);

        //add label under the text
        var text;
        if (isNaN(factors[i].value))
            text = factors[i].value;
        else text = factors[i].value.toFixed(2);
        svg.append("text")
            .attr("x", 0)
            .attr("y", 50 * width / 250 + 30)
            .attr("text-anchor", "middle")
            .style("font-size", 11 + 6 * width / 250 + "px")
            .text(text);

    }
}

function drawChart(factors, container, width, height, categories, chartHyperlinked, color) {
    sortDataAlphabetically(factors);
    console.log("dibuixar en gauge chart");
    console.log(factors);
    if (color == null) {
        color = "#000";
    }

    var assessmentDate;
    for (i = 0; i < factors.length; ++i) {
        var qfDate = new Date(factors[i].date);
        if (!assessmentDate) {
            assessmentDate = qfDate;
        } else if (assessmentDate < qfDate) {
            assessmentDate = qfDate;
        }

        var div = document.createElement('div');
        div.id = container + "DivChart" + i;
        div.style.display = "inline-block";
        div.style.margin = "10px";
        document.getElementById(container).appendChild(div);

        //0 to 1 values to angular values
        angle = factors[i].value * 180 + 90;
        upperThresh = 0.66 * Math.PI - Math.PI / 2;
        lowThresh = 0.33 * Math.PI - Math.PI / 2;

        //create arc starting at -90 degreees
        var arc = d3.arc()
            .innerRadius(70*width/250)
            .outerRadius(110*width/250)
            .startAngle(-tau);

        var textColor;
        //create chart svg with hyperlink inside the "container"
        if (chartHyperlinked){

            if (getParameterByName('id').length !== 0) { // we come from DSI
                urlLink = "../DetailedQualityFactors/CurrentChart" + representationMode + "?id="
                    + factors[i].id + "&name=" + factors[i].name
                    + "&siid=" + getParameterByName('id') + "&si=" + getParameterByName('name');
            } else { // we come from factor
                urlLink = "../DetailedQualityFactors/CurrentChart" + representationMode + "?id="
                    + factors[i].id + "&name=" + factors[i].name;
            }


            // --> all the chart is hyperlinked
            console.log('#'+div.id);
            var svg = d3.select('#'+div.id).append("svg")
                .attr("width", width)
                .attr("height", height)
                .style("margin", 5)
                .attr("class", "chart")
                .append("a")
                .attr("xlink:href", function (d) {
                    return urlLink
                })
                .append("g")
                .attr("transform",
                    "translate(" + width / 2 + "," + height / 2 + ")");

            textColor = "#0177a6";
        }
        else {
            var svg = d3.select('#'+div.id).append("svg")
                .attr("width", width)
                .attr("height", height)
                .style("margin", 5)
                .attr("class", "chart")
                .append("g")
                .attr("transform",
                    "translate(" + width / 2 + "," + height / 2 + ")");

            textColor = "#000"
        }

        // in case of factors we have categories
        categories.forEach(function (category) {
            var threshold = category.upperThreshold * Math.PI - Math.PI / 2;
            svg.append("path")
                .datum({endAngle: threshold})
                .style("fill", category.color)
                .attr("d", arc);
        });

        //create needle
        var arc2 = d3.arc()
            .innerRadius(0)
            .outerRadius(100*width/250)
            .startAngle(-0.05)
            .endAngle(0.05);

        //draw the needle in correct position depending on it's angle
        svg.append("path")
            .style("fill", color)
            .attr("d", arc2)
            .attr("transform", "translate(" + -100*width/250 * Math.cos((angle - 90) / 180 * Math.PI) + "," + -100*width/250 * Math.sin((angle - 90) / 180 * Math.PI) + ") rotate(" + angle + ")");

        //create small circle at needle base
        var arc3 = d3.arc()
            .innerRadius(0)
            .outerRadius(10*width/250)
            .startAngle(0)
            .endAngle(Math.PI * 2);

        //draw the needle base
        svg.append("path")
            .style("fill", color)
            .attr("d", arc3);

        //add text under the gauge with the name of the element (strategic indicator)
        svg.append("text")
            .attr("x", 0)
            .attr("y", 50*width/250)
            .attr("text-anchor", "middle")
            .attr("font-family", "sans-serif")
            .attr("fill", textColor)
            .style("font-size", "16px")
            .text(factors[i].name);


        //add label under the name with the value description
        var valueDescriptionColor;
        if (chartHyperlinked) valueDescriptionColor = textColor;
        else valueDescriptionColor = color;

        svg.append("text")
            .attr("class", "text"+i)
            .attr("x", 0)
            .attr("y", 50*width/250 + 25)
            .attr("text-anchor", "middle")
            .attr("font-family", "sans-serif")
            .attr("fill", valueDescriptionColor)
            .style("font-size", "14px")
            .text(factors[i].value_description);

        // Warnings
        if (chartHyperlinked) {
            var message = "";

            var today = new Date();
            today.setHours(0);
            today.setMinutes(0);
            today.setSeconds(0);
            var millisecondsInOneDay = 86400000;
            var millisecondsBetweenAssessmentAndToday = today.getTime() - qfDate.getTime();
            var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
            if (oldAssessment) {
                var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                message += "The assessment is " + daysOld + " days old.";
            }

            var mismatchDays = factors[i].mismatchDays;
            if (mismatchDays > 0) {
                if (message !== "") {
                    message += "\n"
                }
                message += "The assessment of the factors and the strategic indicator has a difference of " + mismatchDays + " days.";
            }

            var missingMetrics = factors[i].missingMetrics;
            if (missingMetrics && missingMetrics.length > 0) {
                var factors = missingMetrics.length === 1 ? missingMetrics[0] : [missingMetrics.slice(0, -1).join(", "), missingMetrics[missingMetrics.length - 1]].join(" and ");
                if (message !== "") {
                    message += "\n"
                }
                message += "The following metrics were missing when the quality factor was assessed: " + factors + ".";
            }

            if (message !== "") {
                addWarning(div, message);
            }
        }
    }

    $("#assessmentDate").text(assessmentDate.toLocaleDateString());
}

function sortDataAlphabetically (factors) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    factors.sort(compare);
}

function addWarning(div, message) {
    var warning = document.createElement("span");
    warning.setAttribute("class", "glyphicon glyphicon-alert");
    warning.title = message
    warning.style.paddingLeft = "1em";
    warning.style.fontSize = "15px";
    warning.style.color = "yellow";
    warning.style.textShadow = "-2px 0 2px black, 0 2px 2px black, 2px 0 2px black, 0 -2px 2px black";
    div.append(warning);
}
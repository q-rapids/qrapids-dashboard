//get data from API
var data;
var feed;

var lowThresh;
var upperThresh;
var angle;
var target;
var tau = Math.PI / 2;
var urlLink;

function checkCategories() {
    $.ajax({
        url: '../api/strategicIndicators/categories',
        type: "GET",
        success: function(categories) {
            if (categories.length === 0) {
                warningUtils("Warning", "You need to define Strategic Indicator categories in order to see the chart correctly. " +
                    "Please, go to the Categories section of the Configuration menu and define them.");
            }
        }
    });
}
checkCategories();

function getData(width, height, showButtons, chartHyperlinked, color) {
    var serverUrl = sessionStorage.getItem("serverUrl");

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));

    var profileId = sessionStorage.getItem("profile_id");
    var url = "/api/strategicIndicators/current?profile="+profileId;
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (callData) {
            data = callData;
            drawChart("gaugeChart", width, height, showButtons, chartHyperlinked, color);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                warningUtils("Error","Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400) {
                warningUtils("Error", "Datasource connection failed.");
            }
        }
    });
}

function seeFeedback(i){

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));
    var profileId = sessionStorage.getItem("profile_id");

    jQuery.ajax({
        dataType: "json",
        url: '../api/strategicIndicators/current?profile='+profileId,
        cache: false,
        type: "GET",
        async: true,
        success: function (callData) {
            data = callData;
            drawChart();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                warningUtils("Error","Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400) {
                warningUtils("Error", "Datasource connection failed.");
            }
        }
    });
    console.log(data);
}

function drawChart(container, width, height, showButtons, chartHyperlinked, color) {
    sortDataAlphabetically();
    console.log("dibuixar en gauge chart");
    console.log(data);
    if (color == null) {
        color = "#000";
    }
    var someSIhasBN = false;
    var assessmentDate;
    for (i = 0; i < data.length; ++i) {
        var siDate = new Date(data[i].date);
        if (!assessmentDate) {
            assessmentDate = siDate;
        } else if (assessmentDate < siDate) {
            assessmentDate = siDate;
        }

        var div = document.createElement('div');
        div.id = container + "DivChart" + i;
        div.style.display = "inline-block";
        div.style.margin = "10px";
        document.getElementById(container).appendChild(div);

        //0 to 1 values to angular values
        angle = data[i].value.first * 180 + 90;
        upperThresh = 0.66 * Math.PI - Math.PI / 2;
        lowThresh = 0.33 * Math.PI - Math.PI / 2;

        //create arc starting at -90 degreees
        var arc = d3.arc()
            .innerRadius(70*width/250)
            .outerRadius(110*width/250)
            .startAngle(-tau);

        var textColor;
        //create chart svg with hyperlink inide the "container"
        if (chartHyperlinked){
            urlLink = "../DetailedStrategicIndicators/CurrentChart" + DSIRepresentationMode + "?id="
                + data[i].id + "&name=" + data[i].name;

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

        for (j = data[i].probabilities.length - 1; j > -1; --j) {
            svg.append("path")
                .datum({endAngle: (j+1)/(data[i].probabilities.length) * Math.PI - Math.PI / 2})
                .style("fill", data[i].probabilities[data[i].probabilities.length -1 - j].color)
                .attr("d", arc);
        }

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
            .text(data[i].name);

//    .style("font-size", 11+8*width/250+"px")

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
            .text(data[i].value_description);

        //            .style("font-size", 11+6*width/250+"px")


        // Buttons just bellow the Chart
        if (showButtons) {
            var br = document.createElement("br");
            div.appendChild(br);

            var feedbackBtn= document.createElement("button");
            feedbackBtn.id = "buttonFeedback"+data[i].dbId;
            feedbackBtn.dbId = data[i].dbId;
            feedbackBtn.classList.add('btn');
            feedbackBtn.classList.add('btn-default');
            feedbackBtn.setAttribute("pos", i.toString());
            feedbackBtn.onclick = function () {
                location.href = "FeedbackReport" + "?id=" + this.dbId;
            };
            feedbackBtn.appendChild(document.createTextNode("Show Feedback"));
            if (data[i].dbId == null) feedbackBtn.disabled = true;
            if (!data[i].hasFeedback) feedbackBtn.style.display = "none";
            div.appendChild(feedbackBtn);

            if (data[i].hasBN) someSIhasBN = true
        }

        // Warnings
        if (chartHyperlinked) {
            var message = "";

            var today = new Date();
            today.setHours(0);
            today.setMinutes(0);
            today.setSeconds(0);
            var millisecondsInOneDay = 86400000;
            var millisecondsBetweenAssessmentAndToday = today.getTime() - siDate.getTime();
            var oldAssessment = millisecondsBetweenAssessmentAndToday > millisecondsInOneDay;
            if (oldAssessment) {
                var daysOld = Math.round(millisecondsBetweenAssessmentAndToday / millisecondsInOneDay);
                message += "The assessment is " + daysOld + " days old.";
            }

            var mismatchDays = data[i].mismatchDays;
            if (mismatchDays > 0) {
                if (message !== "") {
                    message += "\n"
                }
                message += "The assessment of the factors and the strategic indicator has a difference of " + mismatchDays + " days.";
            }

            var missingFactors = data[i].missingFactors;
            if (missingFactors.length > 0) {
                var factors = missingFactors.length === 1 ? missingFactors[0] : [missingFactors.slice(0, -1).join(", "), missingFactors[missingFactors.length - 1]].join(" and ");
                if (message !== "") {
                    message += "\n"
                }
                message += "The following factors were missing when the strategic indicator was assessed: " + factors + ".";
            }

            if (message !== "") {
                addWarning(div, message);
            }
        }
    }

    if (someSIhasBN && currentURL.search("/CurrentChart") !== -1) {
        var feedbackBtn= document.createElement("button");
        feedbackBtn.id = "buttonFeedback";
        feedbackBtn.classList.add('btn');
        feedbackBtn.classList.add('btn-default');
        feedbackBtn.onclick = function () {
            location.href = "Feedback";
        };
        feedbackBtn.appendChild(document.createTextNode("Feedback"));
        document.getElementById("projectSelectorDiv").appendChild(feedbackBtn);
    }
    sessionStorage.setItem("assessmentDate", assessmentDate.toLocaleDateString());
    $("#assessmentDate").text(assessmentDate.toLocaleDateString());
}

function drawSimulationNeedle (container, width, height, color) {
    d3.selectAll('.simulation').remove();
    sortDataAlphabetically();

    console.log("drawSimulationNeedle");
    console.log(data);

    for (i = 0; i < data.length; ++i) {
        var divId = container + "DivChart" + i;
        var svg = d3.select('#' + divId).select("svg");
        angle = data[i].value.first * 180 + 90;

        //create needle
        var arc2 = d3.arc()
            .innerRadius(0)
            .outerRadius(100 * width / 250)
            .startAngle(-0.05)
            .endAngle(0.05);

        //draw the blue needle in correct position depending on it's angle
        svg.append("path")
            .style("fill", color)
            .attr("class", "simulation")
            .attr("d", arc2)
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ") translate(" + -100 * width / 250 * Math.cos((angle - 90) / 180 * Math.PI) + "," + -100 * width / 250 * Math.sin((angle - 90) / 180 * Math.PI) + ") rotate(" + angle + ")");

        //create small circle at needle base
        var arc3 = d3.arc()
            .innerRadius(0)
            .outerRadius(10 * width / 250)
            .startAngle(0)
            .endAngle(Math.PI * 2);

        //draw the black needle base
        svg.append("path")
            .style("fill", color)
            .attr("class", "simulation")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .attr("d", arc3);

        svg.append("text")
            .attr("class", "simulation")
            .attr("x", 0)
            .attr("y", 50 * width / 250 + 50)
            .attr("text-anchor", "middle")
            .attr("font-family", "sans-serif")
            .attr("fill", color)
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .style("font-size", "16px")
            .text(data[i].value_description);

        var beforeText = $( "text.text"+i ).text();
        var beforeValue = beforeText.split("(");
        beforeValue = parseFloat(beforeValue[1]);
        var afterValue = data[i].value.first.toFixed(2);

        if (beforeValue < afterValue) {
            if (beforeValue == 0)
                beforeValue = 0.001;
            var inc = ((afterValue - beforeValue)/beforeValue)*100;
            svg.append("polygon") // increase icon
                .attr("class", "simulation")
                .attr("points", "160,10 150,25 170,25 160,10" )
                .attr("style", "fill:green;stroke:green;stroke-width:1");
            svg.append("text")
                .attr("class", "simulation")
                .attr("x", 86)
                .attr("y", -97)
                .attr("text-anchor", "middle")
                .attr("font-family", "sans-serif")
                .attr("fill", "green")
                .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
                .style("font-size", "12px")
                .text(inc.toFixed(0) + "%");
        } else if ( beforeValue > afterValue) {
            var dec = ((beforeValue - afterValue)/beforeValue)*100;
            svg.append("polygon") // decrease icon
                .attr("class", "simulation")
                .attr("points", "160,25 150,10 170,10 160,25" )
                .attr("style", "fill:darkred;stroke:darkred;stroke-width:1");
            svg.append("text")
                .attr("class", "simulation")
                .attr("x", 86)
                .attr("y", -97)
                .attr("text-anchor", "middle")
                .attr("font-family", "sans-serif")
                .attr("fill", "darkred")
                .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
                .style("font-size", "12px")
                .text(dec.toFixed(0) + "%");
        } else {
            svg.append("polygon") // steady icon
                .attr("class", "simulation")
                .attr("points", "170,11 190,11 190,15 170,15" )
                .attr("style", "fill:dodgerblue;stroke:steelblue;stroke-width:1");
            svg.append("polygon") // steady icon
                .attr("class", "simulation")
                .attr("points", "170,24 190,24 190,20 170,20" )
                .attr("style", "fill:dodgerblue;stroke:steelblue;stroke-width:1");
        }
    }
}

function sortDataAlphabetically () {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    data.sort(compare);
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
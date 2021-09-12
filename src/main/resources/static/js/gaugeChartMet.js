//get data from API
var feed;

var lowThresh;
var upperThresh;
var angle;
var target;
var tau = Math.PI / 2;
var id = false;

var factors;

var url;
if (getParameterByName('id').length !== 0) {
    id = true;
    url = parseURLComposed("../api/qualityFactors/metrics/current");
} else {
    var profileId = sessionStorage.getItem("profile_id");
    url = parseURLComposed("../api/metrics/current?profile="+profileId);
}

var metricsDB = [];

var urlLink;

var groupByFactor = new Boolean(sessionStorage.getItem("groupByFactor"));


function clickCheckbox(){
    var checkbox = document.getElementById("groupByFactorCheckbox");
    sessionStorage.removeItem("groupByFactor");
    if (checkbox.checked == true)
        sessionStorage.setItem("groupByFactor", checkbox.checked.toString());
    location.href = serverUrl + "/Metrics/CurrentChartGauge";
}

function getData(width, height) {
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            sortDataAlphabetically(data);
            jQuery.ajax({
                dataType: "json",
                url: "../api/metrics",
                cache: false,
                type: "GET",
                async: true,
                success: function (dataDB) {
                    metricsDB = dataDB;
                    getFactors(data, width, height);
                    //getMetricsCategories(data, width, height);
                }});
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

function getFactors(data, width, height) {
    if (id)
        url = parseURLComposed("../api/qualityFactors/metrics/current");
    else
        url = "../api/qualityFactors/metrics/current?profile=" + profileId
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (dataF) {
            sortMyDataAlphabetically(dataF);
            factors = dataF;
            console.log("factors");
            console.log(factors);
            getMetricsCategories(data, width, height);
        }
    });
}

function sortMyDataAlphabetically (factors) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    factors.sort(compare);
}

function getMetricsCategories (data, width, height) {
    jQuery.ajax({
        url: "../api/metrics/categories",
        type: "GET",
        async: true,
        success: function (categories) {
            console.log("groupByFactor " + groupByFactor);
            if (id) { // in case we show metrics for one detailed factor
                if (groupByFactor.valueOf() == true)
                    drawChartByFactor(data[0].metrics, "#gaugeChart", width, height, categories);
                else drawChart(data[0].metrics, "#gaugeChart", width, height, categories);
            } else { // in case we show all metrics
                if (groupByFactor.valueOf() == true)
                    drawChartByFactor(data, "#gaugeChart", width, height, categories);
                else drawChart(data, "#gaugeChart", width, height, categories);
            }
        }
    });
}

function drawChart(metrics, container, width, height, categories) {
    for (i = 0; i < metrics.length; ++i) {
        drawMetricGauge(0, i, metrics[i], container, width, height, categories);
    }
}

function drawChartByFactor(metrics, container, width, height, categories) {
    var gaugeChart = $("#gaugeChart");
    for (j = 0; j < factors.length; j++) {
        var divF = document.createElement('div');
        divF.style.marginTop = "1em";
        divF.style.marginBottom = "1em";

        var labelF = document.createElement('label');
        labelF.id = factors[j].id;
        labelF.textContent = factors[j].name;
        divF.appendChild(labelF);

        gaugeChart.append(divF);
        for (i = 0; i < factors[j].metrics.length; ++i) {
            drawMetricGauge(j, i, factors[j].metrics[i], container, width, height, categories);
        }
    }
    // Add metrics without factor
    var divNOF = document.createElement('div');
    divNOF.id = "divwithoutfactor";
    divNOF.style.marginTop = "1em";
    divNOF.style.marginBottom = "1em";

    var labelNOF = document.createElement('label');
    labelNOF.id = "withoutfactor";
    labelNOF.textContent = "Metrics not associated to any factor";
    divNOF.appendChild(labelNOF);

    metrics.forEach(function (metric) {
        var msvg = document.getElementById(metric.id);
        if (!msvg) {
            if (!document.getElementById("divwithoutfactor"))
                gaugeChart.append(divNOF);
            drawMetricGauge(j, i, metric, container, width, height, categories);
        }
    });
}

function drawMetricGauge(j, i, metric, container, width, height, categories) {
    //0 to 1 values to angular values
    angle = metric.value * 180 + 90;
    upperThresh = 0.66 * Math.PI - Math.PI / 2;
    lowThresh = 0.33 * Math.PI - Math.PI / 2;

    var arc = d3.arc()      //create arc starting at -90 degreees
        .innerRadius(70*width/250)
        .outerRadius(110*width/250)
        .startAngle(-tau);

    //make chart a hyperlink
    var textColor = "#000";
    var findMet = metricsDB.find(function (element) {
        return element.externalId === metric.id;
    });
    if (findMet) { // if metric not found it will be undefined
        urlLink = findMet.webUrl;
    }
    if (urlLink) {
        //create chart svg with hyperlink
        var svg = d3.select(container).append("svg")
            .attr("id", metric.id)
            .attr("width", width)
            .attr("height", height)
            .attr("class", "chart")
            .append("a")
            .attr("xlink:href", function (d) {
                return urlLink
            })
            .attr("target","_blank")
            .append("g")
            .attr("transform",
                "translate(" + width / 2 + "," + height / 2 + ")");
        textColor = "#0177a6";
    } else {
        //create chart svg
        var svg = d3.select(container).append("svg")
            .attr("id", metric.id)
            .attr("width", width)
            .attr("height", height)
            .attr("class", "chart")
            .append("g")
            .attr("transform",
                "translate(" + width / 2 + "," + height / 2 + ")");
    }

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
        .outerRadius(100*width/250)
        .startAngle(-0.05)
        .endAngle(0.05);

    //draw needle in correct position depending on it's angle
    svg.append("path")
        .style("fill", "#000")
        .attr("d", arc2)
        .attr("transform", "translate(" + -100*width/250 * Math.cos((angle - 90) / 180 * Math.PI) + "," + -100*width/250 * Math.sin((angle - 90) / 180 * Math.PI) + ") rotate(" + angle + ")");

    //create small circle at needle base
    var arc3 = d3.arc()
        .innerRadius(0)
        .outerRadius(10*width/250)
        .startAngle(0)
        .endAngle(Math.PI * 2);

    //draw needle base
    svg.append("path")
        .style("fill", "#000")
        .attr("d", arc3);

    //add text under the gauge
    var name;
    if (metric.name.length > 23) name = metric.name.slice(0, 20) + "...";
    else name = metric.name;
    svg.append("text")
        .attr("id", "name" + i + j)
        .attr("x", 0)
        .attr("y", 50*width/250)
        .attr("text-anchor", "middle")
        .attr("fill", textColor)
        .attr("title", metric.name)
        .style("font-size", 11+8*width/250+"px")
        .text(name);

    d3.select("#name"+i+j).append("title").text(metric.name);

    //add label under the text
    var text;
    if (isNaN(metric.value))
        text = metric.value;
    else text = metric.value.toFixed(2);
    svg.append("text")
        .attr("x", 0)
        .attr("y", 50*width/250 + 30)
        .attr("text-anchor", "middle")
        .attr("fill", textColor)
        .style("font-size", 11+6*width/250+"px")
        .text(text);
}

function sortDataAlphabetically (metrics) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    metrics.sort(compare);
}
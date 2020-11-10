//get data from API
var feed;

var lowThresh;
var upperThresh;
var angle;
var target;
var tau = Math.PI / 2;
var id = false;

var url;
if (getParameterByName('id').length !== 0) {
    id = true;
    url = parseURLComposed("../api/qualityFactors/metrics/current");
} else {
    var profileId = sessionStorage.getItem("profile_id");
    url = parseURLComposed("../api/metrics/current?profile="+profileId);
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
            sortDataAlphabetically(data);
            getMetricsCategories(data, width, height);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                alert("Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400) {
                alert("Datasource connection failed.");
            }
        }
    });
}

function getMetricsCategories (data, width, height) {
    jQuery.ajax({
        url: "../api/metrics/categories",
        type: "GET",
        async: true,
        success: function (categories) {
            if (id) { // in case we show metrics for one detailed factor
                drawChart(data[0].metrics, "#gaugeChart", width, height, categories);
            } else { // in case we show all metrics
                drawChart(data, "#gaugeChart", width, height, categories);
            }
        }
    });
}

function drawChart(metrics, container, width, height, categories) {

    for (i = 0; i < metrics.length; ++i) {

        //0 to 1 values to angular values
        angle = metrics[i].value * 180 + 90;
        upperThresh = 0.66 * Math.PI - Math.PI / 2;
        lowThresh = 0.33 * Math.PI - Math.PI / 2;

        var arc = d3.arc()      //create arc starting at -90 degreees
            .innerRadius(70*width/250)
            .outerRadius(110*width/250)
            .startAngle(-tau);

        //make chart a hyperlink
        urlLink = "../Metrics/CurrentChart?id="
            + metrics[i].id + "&name=" + metrics[i].name;

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
        if (metrics[i].name.length > 23) name = metrics[i].name.slice(0, 20) + "...";
        else name = metrics[i].name;
        svg.append("text")
            .attr("id", "name" + i)
            .attr("x", 0)
            .attr("y", 50*width/250)
            .attr("text-anchor", "middle")
            .attr("title", metrics[i].name)
            .style("font-size", 11+8*width/250+"px")
            .text(name);

        d3.select("#name"+i).append("title").text(metrics[i].name);

        //add label under the text
        var text;
        if (isNaN(metrics[i].value))
            text = metrics[i].value;
        else text = metrics[i].value.toFixed(2);
        svg.append("text")
            .attr("x", 0)
            .attr("y", 50*width/250 + 30)
            .attr("text-anchor", "middle")
            .style("font-size", 11+6*width/250+"px")
            .text(text);

    }
}

function sortDataAlphabetically (metrics) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    metrics.sort(compare);
}
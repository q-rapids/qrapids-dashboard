//get data from API
var data;
var feed;

var lowThresh;
var upperThresh;
var angle;
var target;
var tau = Math.PI / 2;
var urlLink;

var currentProduct;
var products = document.getElementById("productSelector");

var serverUrl = sessionStorage.getItem("serverUrl");

products.addEventListener("change", function() {
    currentProduct = products.options[products.selectedIndex].value;
    sessionStorage.setItem("currentProduct", currentProduct);
    getData(200, 200, true, true);
});

function buildSelector() {
    var url = "/api/products";
    if (serverUrl) {
        url = serverUrl + url;
    }
	jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
        	var productSelector = document.getElementById('productSelector');
        	for (var i = 0; i < data.length; i++) {
        		var option = document.createElement("option");
        	    option.value = data[i].id;
        	    option.text = data[i].name;
        	    productSelector.appendChild(option);
            }
            if (!(currentProduct = sessionStorage.getItem("currentProduct"))) {
                currentProduct = data[0].id;
                sessionStorage.setItem("currentProduct", currentProduct);
            }
            productSelector.value = currentProduct;
        	console.log(currentProduct);
        	getData(200, 200, true, true);
        }
    });
}

function getData(width, height, showButtons, chartHyperlinked) {
    var url = "/api/products/" + currentProduct + "/current";
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
            document.getElementById("gaugeChart").innerHTML = "";
            drawChart("#gaugeChart", width, height, showButtons, chartHyperlinked);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                alert("Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400)
                console.log("productEvaluation.js");
                alert("Datasource connection failed.");
        }
    });
}

function drawChart(container, width, height, showButtons, chartHyperlinked) {
    for (i = 0; i < data.length; ++i) {
        //0 to 1 values to angular values
        angle = data[i].value.first * 180 + 90;
        upperThresh = 0.66 * Math.PI - Math.PI / 2;
        lowThresh = 0.33 * Math.PI - Math.PI / 2;

        //create arc starting at -90 degreees
        var arc = d3.arc()
            .innerRadius(70*width/250)
            .outerRadius(110*width/250)
            .startAngle(-tau);

        var svg = d3.select(container).append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("class", "chart")
            .append("g")
            .attr("transform",
                "translate(" + width / 2 + "," + height / 2 + ")");

        for (j = data[i].probabilities.length - 1; j > -1; --j) {
            //draw arc from -90 to upper threshold degrees in orange
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

        //draw the black needle in correct position depending on it's angle
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

        //draw the black needle base
        svg.append("path")
            .style("fill", "#000")
            .attr("d", arc3);

        //add text under the gauge with the name of the element
        svg.append("text")
            .attr("x", 0)
            .attr("y", 50*width/250)
            .attr("text-anchor", "middle")
            .attr("font-family", "sans-serif")
            .attr("fill", "#000000")
            .style("font-size", "16px")
            .text(data[i].name);

        //add label under the name with the value description
        svg.append("text")
            .attr("x", 0)
            .attr("y", 50*width/250 + 30)
            .attr("text-anchor", "middle")
            .attr("font-family", "sans-serif")
            .attr("fill", "#000000")
            .style("font-size", "14px")
            .text(data[i].value_description);
    }
}

window.onload = function() {
    buildSelector();
};


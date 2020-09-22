var serverUrl = sessionStorage.getItem("serverUrl");

var isdsi = true;

//initialize data vectors
var titles = [];
var ids = [];
var labels = [];
var values = [];

var currentProduct;
var products = document.getElementById("productSelector");

products.addEventListener("change", function() {
	currentProduct = products.options[products.selectedIndex].value;
    sessionStorage.setItem("currentProduct", currentProduct);
    getData();
});

function buildSelector() {
    var urlProducts = "/api/products";
    if (serverUrl) {
        urlProducts = serverUrl + urlProducts;
    }
	jQuery.ajax({
        dataType: "json",
        url: urlProducts,
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
        	getData();
        }
    });
}


function getData() {
    //empty previous data
    titles = [];
    ids = [];
    labels = [];
    values = [];

    var url = "/api/products/" + currentProduct + "/projects/current";
    if (serverUrl) {
        url = serverUrl + url;
    }

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            for (i = 0; i < data.length; ++i) {
                titles.push(data[i].first);
                ids.push(data[i].first);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].second.length; ++j) {
                    if (data[i].second[j].name.length < 27)
                        labels[i].push(data[i].second[j].name);
                    else
                        labels[i].push(data[i].second[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].second[j].value.first);
                }
            }
            document.getElementById("radarChart").innerHTML = "";
            drawChart();
        }
    });
}

function drawChart() {
    for (i = 0; i < titles.length; ++i) {
        var a = document.createElement('a');
        a.innerHTML = titles[i];
        a.style.fontSize = "16px";
        a.style.color = "#000000";
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "0px 5px 60px 5px";
        var p = document.createElement('p');
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        //ctx.height = 350;
        ctx.style.display = "inline";
        document.getElementById("radarChart").appendChild(div).appendChild(ctx);
        div.appendChild(p).appendChild(a);
        ctx.getContext("2d");
        window.myLine = new Chart(ctx, {    //draw chart with the following config
            type: 'radar',
            data: {
                labels: labels[i],
                datasets: [{
                    label: titles[i],
                    backgroundColor: 'rgba(1, 119, 166, 0.2)',
                    borderColor: 'rgb(1, 119, 166)',
                    data: values[i],
                    fill: true
                }]
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: titles[i]
                },
                responsive: false,
                legend: {
                    display: false
                },
                scale: {    //make y axis scale 0 to 1 and set maximum number of axis lines
                    ticks: {
                        min: 0,
                        max: 1,
                        stepSize: 0.2,
                    }
                },
                tooltips: {
                    filter: function (tooltipItem) {
                        return tooltipItem.datasetIndex === 0;
                    },
                    callbacks: {
                        label: function(tooltipItem, data) {
                            var label = data.labels[tooltipItem.index] || '';

                            if (label) {
                                label += ': ';
                            }
                            label += Math.round(tooltipItem.yLabel * 100) / 100;
                            return label;
                        },
                        title: function(tooltipItem, data) {
                            return data.datasets[0].label;
                        }
                    }
                }
            }
        });
    }
}

window.onload = function() {
	buildSelector();
};
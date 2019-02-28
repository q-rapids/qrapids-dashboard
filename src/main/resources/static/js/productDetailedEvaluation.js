var url = parseURLSimple("../api/products/detailedCurrentEvaluation/");

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
    getData();
});

function buildSelector() {
	jQuery.ajax({
        dataType: "json",
        url: "../api/products",
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
        	currentProduct = data[0].id;
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

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url + currentProduct,
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
                        maxTicksLimit: 5
                    }
                }
            }
        });
    }
}

window.onload = function() {
	buildSelector();
};
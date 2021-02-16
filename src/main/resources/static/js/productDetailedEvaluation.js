var serverUrl = sessionStorage.getItem("serverUrl");

var isdsi = true;

//initialize data vectors
var titles = [];
var ids = [];
var labels = [];
var values = [];
var categories = [];

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
    categories = [];

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
            getCategories();
        }
    });
}

function getCategories() {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/strategicIndicators/categories";
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url).then (function(cat) {
        categories.push({
            name: cat[0].name, // high category
            color: cat[0].color,
            upperThreshold: 1,
        });
        for (var i = 1; i < cat.length; i++) {
            categories.push({
                name: cat[i].name, // high category
                color: cat[i].color,
                upperThreshold: categories[i-1].upperThreshold - 1/cat.length,
            });
        }
        drawChart();
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
        // add empty labels to make triangle charts
        if (labels[i].length === 2) {
            labels[i].push(null);
        } else if (labels[i].length === 1) {
            labels[i].push(null);
            labels[i].push(null);
        }
        // make dataset to show: data + categories
        var dataset = [];
        dataset.push({ // data
            label: titles[i],
            backgroundColor: 'rgba(1, 119, 166, 0.2)',
            borderColor: 'rgb(1, 119, 166)',
            data: values[i],
            fill: false
        });
        console.log(categories);
        for (var k = categories.length-1; k >= 0; --k) {
            var fill = categories.length-1-k;
            if (k == categories.length-1) fill = true;
            dataset.push({
                label: categories[k].name,
                borderWidth: 1,
                backgroundColor: hexToRgbA(categories[k].color, 0.3),
                borderColor: hexToRgbA(categories[k].color, 0.3),
                pointHitRadius: 0,
                pointHoverRadius: 0,
                pointRadius: 0,
                pointBorderWidth: 0,
                pointBackgroundColor: 'rgba(0, 0, 0, 0)',
                pointBorderColor: 'rgba(0, 0, 0, 0)',
                data: [].fill.call({ length: labels[i].length }, categories[k].upperThreshold),
                fill: fill
            })
        }
        console.log("dataset");
        console.log(dataset);
        window.myLine = new Chart(ctx, {    //draw chart with the following config
            type: 'radar',
            data: {
                labels: labels[i],
                datasets: dataset
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
                        label: function (tooltipItem, data) {
                            var label = data.labels[tooltipItem.index] || '';

                            if (label) {
                                label += ': ';
                            }
                            label += Math.round(tooltipItem.yLabel * 100) / 100;
                            return label;
                        },
                        title: function (tooltipItem, data) {
                            return data.datasets[0].label;
                        }
                    }
                }
            }
        });
    }
}

function hexToRgbA(hex,a=1){ // (hex color, opacity)
    var c;
    if(/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)){
        c= hex.substring(1).split('');
        if(c.length== 3){
            c= [c[0], c[0], c[1], c[1], c[2], c[2]];
        }
        c= '0x'+c.join('');
        return 'rgba('+[(c>>16)&255, (c>>8)&255, c&255].join(',')+','+ a +')';
    }
    throw new Error('Bad Hex');
}

window.onload = function() {
	buildSelector();
};
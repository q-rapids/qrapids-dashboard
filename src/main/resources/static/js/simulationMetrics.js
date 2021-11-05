var simulationColor = "#0579A8";
var currentColor = "#696969";

var strategicIndicators = [];
var qualityFactors = [];
var metrics = [];
var detailedCharts = [];
var factorsCharts = [];
var categories = [];

var profileId = sessionStorage.getItem("profile_id");

function getAllMetrics(){
    var url = "../api/metrics/current?profile="+profileId;
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            metrics = response;
            getMetricsCategoriesAndShow();
        }
    });
}

function getMetricsCategoriesAndShow () {
    var url = "../api/metrics/categories";
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            categories = response;
            showMetricsSliders();
        }
    });
}

function getMetricsCategories (titles, ids, labels, values) {
    var url = "../api/metrics/categories";
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            categories = response;
            showFactors(titles, ids, labels, values);
        }
    });
}

function getFactorsCategories (titles, ids, labels, values) {
    var url = "../api/qualityFactors/categories";
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            categories = response;
            showDetailedStrategicIndicators(titles, ids, labels, values)
        }
    });
}

function showMetricsSliders () {
    // Metrics categories
    var rangeHighlights = [];
    var start = 0;
    categories.sort(function (a, b) {
        return a.upperThreshold - b.upperThreshold;
    });
    for (var i = 0; i < categories.length; i++) {
        var end = categories[i].upperThreshold;
        var offset = 0;
        if (end < 1) offset = 0.02;
        var range = {
            start: start,
            end: end + offset,
            class: categories[i].name
        };
        rangeHighlights.push(range);
        start = end;
    }

    var metricsDiv = $("#metricsSliders");
    metrics.forEach(function (metric) {
        var div = document.createElement('div');
        div.id = "div" + metric.id;
        div.style.marginTop = "1em";
        div.style.marginBottom = "1em";

        var label = document.createElement('label');
        label.id = metric.id;
        label.textContent = metric.name;
        label.title = metric.description;
        div.appendChild(label);

        div.appendChild(document.createElement('br'));

        var slider = document.createElement("input");
        slider.id = "sliderValue" + metric.id;
        slider.style.width = "80%";
        var value = 0;
        if (metric.value !== 'NaN')
            value = metric.value;
        var sliderConfig = {
            id: "slider" + metric.id,
            min: 0,
            max: 1,
            step: 0.01,
            value: value
        };
        sliderConfig.rangeHighlights = [];
        Array.prototype.push.apply(sliderConfig.rangeHighlights, rangeHighlights);
        // Add original value
        var start, end;
        if (metric.value === 0) {
            start = 0;
            end = 0.03;
        }
        else if (metric.value === 1) {
            start = 0.97;
            end = 1;
        }
        else {
            start = metric.value - 0.015;
            end = metric.value + 0.015;
        }
        sliderConfig.rangeHighlights.push({
            start: start,
            end: end
        });
        div.appendChild(slider);
        metricsDiv.append(div);
        $("#"+slider.id).slider(sliderConfig);
    });
    $(".slider-rangeHighlight").css("background", currentColor);
    for (var j = 0; j < categories.length; j++) {
        $(".slider-rangeHighlight." + categories[j].name).css("background", categories[j].color)
    }
    if (qualityFactors.length > 0)
        checkMetricsSliders();
}

function getDetailedStrategicIndicators () {
        jQuery.ajax({
        dataType: "json",
        url: "../api/strategicIndicators/qualityFactors/current?profile="+profileId,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            function compare (a, b) {
                if (a.id < b.id) return -1;
                else if (a.id > b.id) return 1;
                else return 0;
            }
            data.sort(compare);
            var titles = [];
            var ids = [];
            var labels = [];
            var values = [];
            for (i = 0; i < data.length; ++i) {
                //for each dsi save name to titles vector and id to ids vector
                titles.push(data[i].name);
                strategicIndicators.push({
                    id: data[i].id,
                    name: data[i].name
                });
                strategicIndicators[i].factors = [];
                ids.push(data[i].id);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].factors.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (data[i].factors[j].name.length < 27)
                        labels[i].push(data[i].factors[j].name);
                    else
                        labels[i].push(data[i].factors[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].factors[j].value.first);
                    strategicIndicators[i].factors.push({
                        id: data[i].factors[j].id,
                        name: data[i].factors[j].name
                    });
                }
            }
            getFactorsCategories (titles, ids, labels, values);
        }
    });
}

function showDetailedStrategicIndicators (titles, ids, labels, values) {
    for (i = 0; i < titles.length; ++i) {
        var p = document.createElement('p');
        p.innerHTML = titles[i];
        p.style.fontSize = "16px";
        p.style.color = "#000"
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "15px 5px 15px 5px";
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        ctx.style.display = "inline";
        document.getElementById("radarDetailed").appendChild(div).appendChild(ctx);
        div.appendChild(p)
        ctx.getContext("2d");
        if (labels[i].length === 2) {
            labels[i].push(null);
            //values[i].push(null);
        }
        var dataset = [];
        dataset.push({
            label: titles[i],
            backgroundColor: 'rgba(105, 105, 105, 0.2)',
            borderColor: currentColor,
            pointBackgroundColor: currentColor,
            pointBorderColor: currentColor,
            data: values[i],
            fill: false
        });
        var cat = categories;
        cat.sort(function (a, b) {
            return b.upperThreshold - a.upperThreshold;
        });
        for (var k = cat.length-1; k >= 0; --k) {
            var fill = cat.length-1-k;
            if (k == cat.length-1) fill = true;
            dataset.push({
                label: cat[k].name,
                borderWidth: 1,
                backgroundColor: hexToRgbA(cat[k].color, 0.3),
                borderColor: hexToRgbA(cat[k].color, 0.3),
                pointHitRadius: 0,
                pointHoverRadius: 0,
                pointRadius: 0,
                pointBorderWidth: 0,
                pointBackgroundColor: 'rgba(0, 0, 0, 0)',
                pointBorderColor: 'rgba(0, 0, 0, 0)',
                data: [].fill.call({ length: labels[i].length }, cat[k].upperThreshold),
                fill: fill
            })
        }
        var chart = new Chart(ctx, {    //draw chart with the following config
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
                        if ((tooltipItem.datasetIndex === 0) || (tooltipItem.datasetIndex === 1))
                            return true;
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
        detailedCharts.push(chart);
        window.myLine = chart;
    }
}

function getFactors () {
    jQuery.ajax({
        dataType: "json",
        url: "../api/qualityFactors/metrics/current?profile="+profileId,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            sortMyDataAlphabetically(data);
            var titles = [];
            var ids = [];
            var labels = [];
            var values = [];
            for (i = 0; i < data.length; ++i) {
                //for each dsi save name to titles vector and id to ids vector
                titles.push(data[i].name);
                qualityFactors.push({
                    id: data[i].id,
                    name: data[i].name
                });
                qualityFactors[i].metrics = [];
                ids.push(data[i].id);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].metrics.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (data[i].metrics[j].name.length < 27)
                        labels[i].push(data[i].metrics[j].name);
                    else
                        labels[i].push(data[i].metrics[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].metrics[j].value);
                    qualityFactors[i].metrics.push({
                        id: data[i].metrics[j].id,
                        name: data[i].metrics[j].name
                    });
                }
            }
            checkMetricsSliders();
            getMetricsCategories(titles, ids, labels, values);
        }
    });
}

function checkMetricsSliders() {
    metrics.forEach(function (metric) {
        var present = false;
        qualityFactors.forEach(function (qualityFactor) {
            qualityFactor.metrics.forEach(function (factorMetric) {
                if (metric.id === factorMetric.id)
                    present = true;
            });
        });
        if (!present) {
            var warning = document.createElement("span");
            warning.setAttribute("class", "glyphicon glyphicon-alert");
            warning.title = "This metric is not related to any factor"
            warning.style.paddingLeft = "1em";
            warning.style.fontSize = "15px";
            warning.style.color = "yellow";
            warning.style.textShadow = "-2px 0 2px black, 0 2px 2px black, 2px 0 2px black, 0 -2px 2px black";
            var divMetric = $("#div"+metric.id);
            divMetric.append(warning);
        }
    });
}

function showFactors (titles, ids, labels, values) {
    for (i = 0; i < titles.length; ++i) {
        var p = document.createElement('p');
        p.innerHTML = titles[i];
        p.style.fontSize = "16px";
        p.style.color = "#000"
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "15px 5px 15px 5px";
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 400;
        ctx.style.display = "inline";
        document.getElementById("radarFactors").appendChild(div).appendChild(ctx);
        div.appendChild(p)
        ctx.getContext("2d");
        if (labels[i].length === 2) {
            labels[i].push(null);
        } else if (labels[i].length === 1) {
            labels[i].push(null);
            labels[i].push(null);
        }
        var dataset = [];
        dataset.push({
            label: titles[i],
            backgroundColor: 'rgba(105, 105, 105, 0.2)',
            borderColor: currentColor,
            pointBackgroundColor: currentColor,
            pointBorderColor: currentColor,
            data: values[i],
            fill: false
        });
        var cat = categories;
        cat.sort(function (a, b) {
            return b.upperThreshold - a.upperThreshold;
        });
        for (var k = cat.length-1; k >= 0; --k) {
            var fill = cat.length-1-k;
            if (k == cat.length-1) fill = true;
            dataset.push({
                label: cat[k].name,
                borderWidth: 1,
                backgroundColor: hexToRgbA(cat[k].color, 0.3),
                borderColor: hexToRgbA(cat[k].color, 0.3),
                pointHitRadius: 0,
                pointHoverRadius: 0,
                pointRadius: 0,
                pointBorderWidth: 0,
                pointBackgroundColor: 'rgba(0, 0, 0, 0)',
                pointBorderColor: 'rgba(0, 0, 0, 0)',
                data: [].fill.call({ length: labels[i].length }, cat[k].upperThreshold),
                fill: fill
            })
        }
        var chart = new Chart(ctx, {    //draw chart with the following config
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
                        if ((tooltipItem.datasetIndex === 0) || (tooltipItem.datasetIndex === 1))
                            return true;
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
        factorsCharts.push(chart);
        window.myLine = chart;
    }
}

$('#apply').click(function () {
    var metricsSlider = [];

    Array.from($("#metricsSliders").children()).forEach(function(element) {
        metricsSlider.push({
            id: element.children[0].id,
            name: element.children[0].textContent,
            value: element.children[3].value
        });
    });

    for (var i = 0; i < qualityFactors.length; i++) {
        var qualityFactor = qualityFactors[i];
        var dataset = {
            label: qualityFactor.name,
            backgroundColor: 'rgba(5, 121, 168, 0.2)',
            borderColor: simulationColor,
            pointBackgroundColor: simulationColor,
            pointBorderColor: simulationColor,
            data: [],
            fill: false
        };
        for (var j = 0; j < qualityFactor.metrics.length; j++) {
            var metric = qualityFactor.metrics[j];
            var newMetric = metricsSlider.find(function (element) {
                return element.id === metric.id;
            });
            dataset.data.push(newMetric.value);
        }

        if (factorsCharts[i].data.datasets.length > 4)
            factorsCharts[i].data.datasets[0].data = dataset.data;
        else {
            factorsCharts[i].data.datasets.unshift(dataset);
            // change categories fill property (we add simulated data)
            factorsCharts[i].data.datasets[3].fill = factorsCharts[i].data.datasets[3].fill +1;
            factorsCharts[i].data.datasets[4].fill = factorsCharts[i].data.datasets[4].fill +1;
        }
        factorsCharts[i].update();
    }

    var newMetrics = [];
    for (var i = 0; i < metricsSlider.length; i++) {
        var previousMetric = metrics.find(function (element) {
            return element.id === metricsSlider[i].id
        });
        if (previousMetric.value !== 'NaN' && parseFloat(metricsSlider[i].value) !== parseFloat(previousMetric.value.toFixed(2)))
            newMetrics.push(metricsSlider[i]);
    }


    var date = metrics[0].date;

    $.ajax({
        url: "../api/qualityFactors/simulate?date="+ date + "&profile="+profileId,
        data: JSON.stringify(newMetrics),
        type: "POST",
        contentType: 'application/json',
        success: function(qualityFactors) {
            // only for METRICS_FACTORS profile: simulate gauge factors
            if (sessionStorage.getItem("profile_qualitylvl") == "METRICS_FACTORS") {
                data = qualityFactors;
                drawSimulationNeedleFactors("gaugeChartFactors", 200, 237, simulationColor);
            } else {
                for (var i = 0; i < strategicIndicators.length; i++) {
                    var strategicIndicator = strategicIndicators[i];
                    var dataset = {
                        label: strategicIndicator.name,
                        backgroundColor: 'rgba(5, 121, 168, 0.2)',
                        borderColor: simulationColor,
                        pointBackgroundColor: simulationColor,
                        pointBorderColor: simulationColor,
                        data: [],
                        fill: false
                    };
                    for (var j = 0; j < strategicIndicator.factors.length; j++) {
                        var factor = strategicIndicator.factors[j];
                        var newFactor = qualityFactors.find(function (element) {
                            return element.id === factor.id;
                        });
                        if (newFactor)
                            dataset.data.push(newFactor.value.first);
                    }

                    if (detailedCharts[i].data.datasets.length > 4)
                        detailedCharts[i].data.datasets[0].data = dataset.data;
                    else {
                        detailedCharts[i].data.datasets.unshift(dataset);
                        // change categories fill property (we add simulated data)
                        detailedCharts[i].data.datasets[3].fill = detailedCharts[i].data.datasets[3].fill + 1;
                        detailedCharts[i].data.datasets[4].fill = detailedCharts[i].data.datasets[4].fill + 1;
                    }
                    detailedCharts[i].update();
                }
                simulateSI(qualityFactors);
            }
        },
        error: function() {
            warningUtils("Error", "Metrics simulation failed");
        }
    });
});

function simulateSI (qualityFactors) {
    var qfs = [];
    for (var i = 0; i < qualityFactors.length; i++) {
        qfs.push({
            id: qualityFactors[i].id,
            name: qualityFactors[i].name,
            value: qualityFactors[i].value.first
        });
    }

    var formData = new FormData();
    formData.append("factors", JSON.stringify(qfs));

    $.ajax({
        url: "../api/strategicIndicators/simulate?profile="+profileId,
        data: formData,
        type: "POST",
        contentType: false,
        processData: false,
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 500)
                alert(textStatus);
        },
        success: function(result) {
            data = result;
            drawSimulationNeedle("gaugeChart", 200, 237, simulationColor);
        }
    });
}

$('#restore').click(function () {
    $('#metricsSliders').empty();
    removeSimulation();
    getAllMetrics();
});

function removeSimulation() {
    d3.selectAll('.simulation').remove();
    if (factorsCharts[0].data.datasets.length > 4) {
        for (var i = 0; i < factorsCharts.length; i++) {
            factorsCharts[i].data.datasets.shift();
            // change categories fill property (we remove simulated data)
            factorsCharts[i].data.datasets[2].fill = factorsCharts[i].data.datasets[2].fill -1;
            factorsCharts[i].data.datasets[3].fill = factorsCharts[i].data.datasets[3].fill -1;
            factorsCharts[i].update();
        }
    }
    if (sessionStorage.getItem("profile_qualitylvl") == "ALL") {
        if (detailedCharts[0].data.datasets.length > 4) {
            for (var i = 0; i < detailedCharts.length; i++) {
                detailedCharts[i].data.datasets.shift();
                // change categories fill property (we remove simulated data)
                detailedCharts[i].data.datasets[2].fill = detailedCharts[i].data.datasets[2].fill - 1;
                detailedCharts[i].data.datasets[3].fill = detailedCharts[i].data.datasets[3].fill - 1;
                detailedCharts[i].update();
            }
        }
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
    $("#simulationColor").css("background-color", simulationColor);
    $("#simulationColorDetailed").css("background-color", simulationColor);
    $("#simulationColorDetailedFactors").css("background-color", simulationColor);
    $("#simulationColorFactors").css("background-color", simulationColor);
    $("#currentColor").css("background-color", currentColor);
    $("#currentColorDetailed").css("background-color", currentColor);
    $("#currentColorDetailedFactors").css("background-color", currentColor);
    $("#currentColorFactors").css("background-color", currentColor);
    getAllMetrics();
    getFactors();
    if (sessionStorage.getItem("profile_qualitylvl") == "ALL") {
        getDetailedStrategicIndicators();
        getData(200, 237, false, false, currentColor);
        document.getElementById("gaugeChartFactors").hidden = true;
    } else { // in case of metrics&factors profile quality level we only show factors info
        getDataFactors(200, 237, false, currentColor);
        document.getElementById("radarDetailed").hidden = true;
        document.getElementById("gaugeChart").hidden = true;
    }
};
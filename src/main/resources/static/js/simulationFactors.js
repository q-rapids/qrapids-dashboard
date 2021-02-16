var simulationColor = "#0579A8";
var currentColor = "#696969";

var qualityFactors = [];
var strategicIndicators = [];
var categories = [];
var detailedCharts = [];

function getAllQualityFactors () {
    var url = "../api/qualityFactors/current";
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            qualityFactors = response;
            getQualityFactorsCategories();
        }
    });
}

function getQualityFactorsCategories () {
    var url = "../api/qualityFactors/categories";
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            categories = response;
            showQualityFactorSliders();
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

function showQualityFactorSliders () {
    // Factor categories
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

    var qualityFactorsDiv = $("#qualityFactors");
    qualityFactors.forEach(function (qualityFactor) {
        var div = document.createElement('div');
        div.id = "div" + qualityFactor.id;
        div.style.marginTop = "1em";
        div.style.marginBottom = "1em";

        var label = document.createElement('label');
        label.id = qualityFactor.id;
        label.textContent = qualityFactor.name;
        label.title = qualityFactor.description;
        div.appendChild(label);

        div.appendChild(document.createElement('br'));

        var slider = document.createElement("input");
        slider.id = "sliderValue" + qualityFactor.id;
        slider.style.width = "70%";
        var sliderConfig = {
            id: "slider" + qualityFactor.id,
            min: 0,
            max: 1,
            step: 0.01,
            value: qualityFactor.value.first
        };
        sliderConfig.rangeHighlights = [];
        Array.prototype.push.apply(sliderConfig.rangeHighlights, rangeHighlights);
        // Add original value
        var start, end;
        if (qualityFactor.value.first === 0) {
            start = 0;
            end = 0.03;
        }
        else if (qualityFactor.value.first === 1) {
            start = 0.97;
            end = 1;
        }
        else {
            start = qualityFactor.value.first - 0.015;
            end = qualityFactor.value.first + 0.015;
        }
        sliderConfig.rangeHighlights.push({
            start: start,
            end: end
        });
        div.appendChild(slider);
        qualityFactorsDiv.append(div);
        $("#"+slider.id).slider(sliderConfig);
    });
    $(".slider-rangeHighlight").css("background", currentColor);
    for (var i = 0; i < categories.length; i++) {
        $(".slider-rangeHighlight." + categories[i].name).css("background", categories[i].color)
    }
    if (strategicIndicators.length > 0)
        checkFactorsSliders();
}

function getDetailedStrategicIndicators () {
    var serverUrl = sessionStorage.getItem("serverUrl");
    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));
    var profileId = sessionStorage.getItem("profile_id");
    var url = "/api/strategicIndicators/qualityFactors/current?profile="+profileId;
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
            checkFactorsSliders();
            getFactorsCategories (titles, ids, labels, values);
            //showDetailedStrategicIndicators(titles, ids, labels, values);
        }
    });
}

function checkFactorsSliders() {
    qualityFactors.forEach(function (qualityFactor) {
        var present = false;
        strategicIndicators.forEach(function (strategicIndicator) {
            strategicIndicator.factors.forEach(function (siFactor) {
                if (qualityFactor.id === siFactor.id)
                    present = true;
            });
        });
        var profileId = sessionStorage.getItem("profile_id");
        if (!present && (profileId == null || profileId == "null")) {
            var warning = document.createElement("span");
            warning.setAttribute("class", "glyphicon glyphicon-alert");
            warning.title = "This quality factor is not related to any strategic indicator"
            warning.style.paddingLeft = "1em";
            warning.style.fontSize = "15px";
            warning.style.color = "yellow";
            warning.style.textShadow = "-2px 0 2px black, 0 2px 2px black, 2px 0 2px black, 0 -2px 2px black";
            var divFactor = $("#div"+qualityFactor.id);
            divFactor.append(warning);
        } else if (!present) { // remove unused factor if we have profile
            var divFactor = $("#div"+qualityFactor.id);
            divFactor.remove();
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
        document.getElementById("radarChart").appendChild(div).appendChild(ctx);
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
        console.log(chart);
        detailedCharts.push(chart);
        window.myLine = chart;
    }
}


$('#apply').click(function () {

    var qualityFactors = [];

    Array.from($("#qualityFactors").children()).forEach(function(element) {
        qualityFactors.push({
            id: element.children[0].id,
            name: element.children[0].textContent,
            value: element.children[3].value
        });
    });

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
            dataset.data.push(newFactor.value);
        }
        if (detailedCharts[i].data.datasets.length > 4) {
            detailedCharts[i].data.datasets[0].data = dataset.data;
        } else {
            detailedCharts[i].data.datasets.unshift(dataset);
            // change categories fill property (we add simulated data)
            detailedCharts[i].data.datasets[3].fill = detailedCharts[i].data.datasets[3].fill +1;
            detailedCharts[i].data.datasets[4].fill = detailedCharts[i].data.datasets[4].fill +1;
        }
        detailedCharts[i].update();
    }

    var formData = new FormData();
    formData.append("factors", JSON.stringify(qualityFactors));

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));
    var profileId = sessionStorage.getItem("profile_id");

    $.ajax({
        url: "../api/strategicIndicators/simulate?profile="+profileId,
        data: formData,
        type: "POST",
        contentType: false,
        processData: false,
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 405)
                alert(textStatus);
        },
        success: function(result) {
            data = result;
            drawSimulationNeedle("gaugeChart", 200, 237, simulationColor);
        }
    });
});

$('#restore').click(function () {
    $('#simulationResult').empty();
    $('#qualityFactors').empty();
    removeSimulation();
    getAllQualityFactors();
});

function removeSimulation() {
    d3.selectAll('.simulation').remove();
    if (detailedCharts[0].data.datasets.length > 4) {
        for (var i = 0; i < detailedCharts.length; i++) {
            detailedCharts[i].data.datasets.shift();
            // change categories fill property (we remove simulated data)
            detailedCharts[i].data.datasets[2].fill = detailedCharts[i].data.datasets[2].fill -1;
            detailedCharts[i].data.datasets[3].fill = detailedCharts[i].data.datasets[3].fill -1;
            detailedCharts[i].update();
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
    $("#currentColor").css("background-color", currentColor);
    $("#currentColorDetailed").css("background-color", currentColor);
    getDetailedStrategicIndicators();
    getData(200, 237, false, false, currentColor);
    getAllQualityFactors();
};
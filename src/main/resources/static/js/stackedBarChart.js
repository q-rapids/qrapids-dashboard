var colors = [  '#ff9933','#33cc33','#ff5050','#ccc935',
                '#c060c9','#0177a6','#f44336','#9c27b0',
                '#2196f3','#00897B','#7986CB','#8bc34a',
                '#d46119','#c22723','#f0dc37','#45edc7',
                '#607d8b','#F06292','#673ab7','#cddc39',
                '#3f51b5','#00bcd4','#ff5722', '#4caf50',
                '#03a9f4','#ffc107','#dc3866','#9e9e9e',
                '#795548','#000000'];

var options = {
    series: [],
    chart: {
        type: 'bar',
        height: 350,
        stacked: true,
        toolbar: {
            show: true
        },
        zoom: {
            enabled: true
        }
    },
    responsive: [{
        breakpoint: 480,
        options: {
            legend: {
                position: 'bottom',
                offsetX: -10,
                offsetY: 0
            }
        }
    }],
    plotOptions: {
        bar: {
            horizontal: false,
        },
    },
    yaxis: {
        min: 0,
        max: 1,
        decimalsInFloat: 2,
    },
    xaxis: {
        type: 'category',
        categories: [],
        labels: {
            formatter: function(value, timestamp, index) {
                var parts = value.split(": &nbsp;");
                return parts[0]
            }
        }
    },
    legend: {
        position: 'right',
        offsetY: 15
    },
    fill: {
        opacity: 1
    },
    tooltip: {
        x: {
            show: true,
            formatter: function(value, timestamp, index) {
                return value
            }
        },
        y: {
            formatter: function(value, { series, seriesIndex, dataPointIndex, w }) {
                var v = Object.values(myTooltips[seriesIndex]);
                return v[0][dataPointIndex]
            },
            title: {
                formatter: (seriesName) => seriesName,
            },
        },
    },
    annotations: {
        position: 'back'
    },
    dataLabels: {
        enabled: true,
    }
};

var chart = new ApexCharts(document.querySelector("#StackedBarChart"), options);
chart.render();

var myTooltips = [];

function drawChart() {
    let mapForChart = new Map();
    let mapForTooltips = new Map();
    var xaxis_cat = [];
    var dataLabels = true;
    for (i = 0; i < titles.length; ++i) {
        var parts = titles[i];
        xaxis_cat.push(parts);
        for(j = 0; j < labels[i].length; ++j){
            if (!mapForChart.has(labels[i][j])) { // map doesn't have this label
                var data = [].fill.call({ length: titles.length }, 0);
                var tooltips = [].fill.call({ length: titles.length }, 0);

                var w = parseFloat(weights[i][j]);
                var sum = sumWeights(weights[i]);
                if (sum > 1 || w == 0 || w == -1) { // if w == 0 means old data; if w == -1 means SI with Bayesian Network
                    if (metrics) {
                        data[i] = (parseFloat(values[i][j]/w) * (w/sum)).toFixed(2);
                        tooltips[i] = parseFloat(values[i][j]/w).toFixed(2) + " (" + ((w/sum)*100).toFixed(0) + "%)";
                    } else {
                        if (w == 0) {
                            data[i] = (parseFloat(values[i][j]) * (1 / labels[i].length)).toFixed(2);
                            tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((1/labels[i].length) * 100).toFixed(0) + "%)";
                        } else if (w == -1) {
                            dataLabels = false;
                            data[i] = (parseFloat(values[i][j]) * (1 / labels[i].length)).toFixed(2);
                            tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (NA)";
                        } else {
                            data[i] = (parseFloat(values[i][j]) * (w / sum)).toFixed(2);
                            tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((w/sum) * 100).toFixed(0) + "%)";
                        }
                    }
                } else {
                    data[i] = parseFloat(values[i][j]).toFixed(2);
                    tooltips[i] = (values[i][j]/w).toFixed(2) + " (" + w*100 + "%)";
                }
                mapForTooltips.set(labels[i][j], tooltips);
                mapForChart.set(labels[i][j], data);
            } else { // map has this label
                var data = mapForChart.get(labels[i][j]);
                var tooltips = mapForTooltips.get(labels[i][j]);
                var w = parseFloat(weights[i][j]);
                var sum = sumWeights(weights[i]);
                if ( sum > 1 || w == 0 || w == -1) {
                    if (metrics) {
                        data[i] = (parseFloat(values[i][j]/w) * (w/sum)).toFixed(2);
                        tooltips[i] = parseFloat(values[i][j]/w).toFixed(2) + " (" + ((w/sum)*100).toFixed(0) + "%)";
                    } else {
                        if ( w == 0 ) {
                            data[i] = (parseFloat(values[i][j]) * (1/labels[i].length)).toFixed(2);
                            tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((1/labels[i].length)*100).toFixed(0) + "%)";
                        } else if (w == -1) {
                            dataLabels = false;
                            data[i] = (parseFloat(values[i][j]) * (1/labels[i].length)).toFixed(2);
                            tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (NA)";
                        } else {
                            data[i] = (parseFloat(values[i][j]) * (w/sum)).toFixed(2);
                            tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((w/sum)*100).toFixed(0) + "%)";
                        }
                    }
                } else {
                    data[i] = parseFloat(values[i][j]).toFixed(2);
                    tooltips[i] = (values[i][j]/w).toFixed(2) + " (" + w*100 + "%)";
                }
                mapForChart.set(labels[i][j], data);
                mapForTooltips.set(labels[i][j], tooltips);
            }
        }
    }

    var series = [];
    for (var [key, value] of mapForChart) {
        var data = Object.values(value);
        data.pop(); // delete length attribute
        series.push({
            name: key,
            data: data
        });
        var value = Object.values(mapForTooltips.get(key));
        value.pop();
        myTooltips.push({
            values: value
        });
    }

    console.log(series);

    chart.updateSeries(series);
    if (series.length < 30) {
        chart.updateOptions({  xaxis: {
                type: 'category',
                categories: xaxis_cat,
            },
            colors: colors,
            dataLabels: {
                enabled: dataLabels,
            }
        });
    } else {
        chart.updateOptions({  xaxis: {
                type: 'category',
                categories: xaxis_cat,
            },
            colors: randomColors(series.length),
            dataLabels: {
                enabled: dataLabels,
            }
        });
    }

    chart.addYaxisAnnotation({
        y: categories[0].pos,
        strokeDashArray: 0,
        borderColor: categories[0].color,
        fillColor: categories[0].color,
    });
    chart.addYaxisAnnotation({
        y: categories[1].pos,
        strokeDashArray: 0,
        borderColor: categories[1].color,
        fillColor: categories[1].color,
    });
}

function randomColors(size) {
    var colors = [];
    for (var c = 0; c < size; c++){
        colors.push('#'+(0x1000000+(Math.random())*0xffffff).toString(16).substr(1,6));
    }
    return colors;
}

function sumWeights(weights) {
    var sum = 0;
    for(var j = 0; j < weights.length; ++j) {
        sum += parseFloat(weights[j]);
    }
    return sum;
}



window.onload = function() {
    getData();
};
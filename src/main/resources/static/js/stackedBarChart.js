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
        offsetY: 40
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
    }
};

var chart = new ApexCharts(document.querySelector("#StackedBarChart"), options);
chart.render();

var myTooltips = [];

function drawChart() {
    console.log("titles:");
    console.log(titles);
    console.log("labels:");
    console.log(labels);
    console.log("weights:");
    console.log(weights);
    console.log("values:");
    console.log(values);

    let mapForChart = new Map();
    let mapForTooltips = new Map();
    var categories = [];
    var colors = [];
    for (i = 0; i < titles.length; ++i) {
        var parts = titles[i];
        categories.push(parts);
        for(j = 0; j < labels[i].length; ++j){
            if (!mapForChart.has(labels[i][j])) {
                colors.push('#'+(0x1000000+(Math.random())*0xffffff).toString(16).substr(1,6));

                var data = [].fill.call({ length: titles.length }, 0);
                var tooltips = [].fill.call({ length: titles.length }, 0);

                var w = parseFloat(weights[i][j]);
                if (sumWeights(weights[i]) > 1 || w == 0 ) {
                    if ( w == 0 ) {
                        data[i] = parseFloat(values[i][j]) * (1/labels[i].length);
                        tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((1/labels[i].length)*100).toFixed(0) + "%)";
                    }
                    else {
                        data[i] = parseFloat(values[i][j]) * (w/labels[i].length);
                        tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((w/labels[i].length)*100).toFixed(0) + "%)";
                    }
                } else {
                    data[i] = parseFloat(values[i][j]);
                    tooltips[i] = (values[i][j]/w).toFixed(2) + " (" + w*100 + "%)";
                }
                mapForTooltips.set(labels[i][j], tooltips);
                mapForChart.set(labels[i][j], data);
            } else {
                var data = mapForChart.get(labels[i][j]);
                var tooltips = mapForTooltips.get(labels[i][j]);
                var w = parseFloat(weights[i][j]);
                if (sumWeights(weights[i]) > 1 || w == 0 ) {
                    if ( w == 0 ) {
                        data[i] = parseFloat(values[i][j]) * (1/labels[i].length);
                        tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((1/labels[i].length)*100).toFixed(0) + "%)";
                    }
                    else {
                        data[i] = parseFloat(values[i][j]) * (w/labels[i].length);
                        tooltips[i] = parseFloat(values[i][j]).toFixed(2) + " (" + ((w/labels[i].length)*100).toFixed(0) + "%)";
                    }
                } else {
                    data[i] = parseFloat(values[i][j]);
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
    console.log(myTooltips);
    console.log(series);
    chart.updateSeries(series);

    chart.updateOptions({  xaxis: {
            type: 'category',
            categories: categories,
        },
        colors: colors,
    });
// TODO: Categories from DB must be used
    chart.addYaxisAnnotation({
        y: 0.33,
        strokeDashArray: 5,
        borderColor: '#ff0000',
        fillColor: '#ff0000',
    });
    chart.addYaxisAnnotation({
        y: 0.67,
        strokeDashArray: 5,
        borderColor: '#00ff00',
        fillColor: '#00ff00',
    });
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
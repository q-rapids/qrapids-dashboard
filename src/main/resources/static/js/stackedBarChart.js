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
    xaxis: {
        type: 'category',
        categories: [],
    },
    legend: {
        position: 'right',
        offsetY: 40
    },
    fill: {
        opacity: 1
    }
};

var chart = new ApexCharts(document.querySelector("#StackedBarChart"), options);
chart.render();

function drawChart() {
    console.log("titles:");
    console.log(titles);
    console.log("labels:");
    console.log(labels);
    console.log("values:");
    console.log(values);

    let map = new Map();
    var categories = [];
    var colors = [];
    for (i = 0; i < titles.length; ++i) {
        var parts = titles[i].split("<br/>");
        categories.push(parts[0]);
        for(j = 0; j < labels[i].length; ++j){
            if (!map.has(labels[i][j])) {
                colors.push('#'+(0x1000000+(Math.random())*0xffffff).toString(16).substr(1,6));
                var data = [].fill.call({ length: titles.length }, 0);
                data[i] = values[i][j];
                map.set(labels[i][j], data);
            } else {
                var data = map.get(labels[i][j]);
                data[i] = values[i][j];
                map.set(labels[i][j], data);
            }
        }
    }
    var series = [];
    for (var [key, value] of map) {
        var data = Object.values(value);
        data.pop(); // delete length attribute
        series.push({
            name: key,
            data: data
        });
    }
    console.log(series);
    chart.updateSeries(series);


    chart.updateOptions({  xaxis: {
            type: 'category',
            categories: categories,
        },
        colors: colors,
    });
}

window.onload = function() {
    getData();
};
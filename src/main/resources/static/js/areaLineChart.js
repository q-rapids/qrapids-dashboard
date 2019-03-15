var timeFormat = 'YYYY-MM-DD';
var config = [];

var colors = ['rgb(1, 119, 166)', 'rgb(255, 153, 51)', 'rgb(51, 204, 51)', 'rgb(255, 80, 80)', 'rgb(204, 201, 53)', 'rgb(192, 96, 201)'];

Chart.plugins.register({
    afterDraw: function(chart) {
        if (chart.data.datasets.length === 0) {
            // No data is present
            var ctx = chart.chart.ctx;
            var width = chart.chart.width;
            var height = chart.chart.height;
            chart.clear();

            ctx.save();
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.font = "Helvetica Nueue";
            ctx.fillText(chart.data.error, width / 2, height / 2, width);
            ctx.restore();
        }
    }
});

function drawChart() {
    config = [];
    for (var i = 0; i < text.length; ++i) {    //create config for each chart
        var c = {
            type: 'line',
            data: {
                datasets: []
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: text[i]
                },
                responsive: false,
                scales: {
                    xAxes: [{
                        type: "time",
                        time: {
                            unit: 'day',
                            format: timeFormat,
                            tooltipFormat: 'll'
                        },
                        scaleLabel: {
                            display: true,
                            labelString: 'Date'
                        }
                    }],
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'value'
                        },
                        ticks: {
                            max: 1,
                            min: 0
                        }
                    }]
                }
            }
        };
        if (dades[i].length > 0) {
            c.data.datasets.push({
                label: text[i],
                backgroundColor: 'rgb(1, 119, 166)',
                borderColor: 'rgb(1, 119, 166)',
                fill: false,
                data: dades[i]
            }/*,{
            label: '80% upper',
            backgroundColor: 'rgb(222, 255, 1)',
            borderColor: 'rgb(222, 255, 1)',
            fill: 2,
            data: upper80[i]
            },{
                label: '80% lower',
                backgroundColor: 'rgb(222, 255, 1)',
                borderColor: 'rgb(222, 255, 1)',
                fill: false,
                data: lower80[i]
            },{
                label: '95% upper',
                backgroundColor: 'rgb(92, 240, 182)',
                borderColor: 'rgb(92, 240, 182)',
                fill: 4,
                data: upper95[i]
            },{
                label: '95% lower',
                backgroundColor: 'rgb(92, 240, 182)',
                borderColor: 'rgb(92, 240, 182)',
                fill: false,
                data: lower95[i]
            }*/);
        }
        else {
            c.data.error = errors[i];
        }
        config.push(c);
    }

    for (i = 0; i < text.length; ++i) {
        var a = document.createElement('a');
        var from = getParameterByName('from');
        var to = getParameterByName('to');
        a.innerHTML = text[i];
        a.style.fontSize = "16px";
        var div = document.createElement('div');
        div.style.display = "inline-block";
        var p = document.createElement('p');
        var ctx = document.createElement('canvas');
        ctx.id = 'canvas' + i;
        ctx.width = 350;
        ctx.height = 350;
        ctx.style.display = "inline";
        document.getElementById("chartContainer").appendChild(div).appendChild(ctx);
        div.appendChild(p).appendChild(a);
        ctx.getContext("2d");
        window.myLine = new Chart(ctx, config[i]);  //draw chart
    }
}

window.onload = function() {
    getData();
};
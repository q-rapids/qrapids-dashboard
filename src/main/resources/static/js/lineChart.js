var timeFormat = 'YYYY-MM-DD';

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
    for (i = 0; i < dades.length; ++i) {
        var a = document.createElement('a');
        if (isSi) {
            //if its a SI chart make it a hyperlink
            var currentURL = window.location.href;
            if (currentURL.match("/PredictionChart")) urlLink = "../DetailedStrategicIndicators/PredictionChart?id=" + ids[i] + "&name=" + text[i];
            else urlLink = "../DetailedStrategicIndicators/HistoricChart?id=" + ids[i] + "&name=" + text[i];

            a.setAttribute("href", urlLink);
        }
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
        //set chart config
        var config = {
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
                legend: {
                    display: false
                },
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
                        },
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 20
                        }
                    }],
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'value'
                        },
                        ticks: {    //make y axis scale 0 to 1
                            max: 1.0,
                            min: 0
                        }
                    }]
                }
            }
        };

        if (dades[i].length > 0) {
            config.data.datasets.push({
                label: text[i],
                backgroundColor: 'rgb(1, 119, 166)',
                borderColor: 'rgb(1, 119, 166)',
                data: dades[i],
                fill: false
            });
        }
        else if (typeof errors !== "undefined"){
            config.data.error = errors[i];
        }
        else {
            config.data.error = "No data to display";
        }

        //draw chart
        window.myLine = new Chart(ctx, config);
    }
}


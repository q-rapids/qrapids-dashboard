var timeFormat = 'YYYY-MM-DD';

function drawChart() {
    for (i = 0; i < dades.length; ++i) {
        var a = document.createElement('a');
        if (isSi) {
            //if its a SI chart make it a hyperlink
            var currentURL = window.location.href;
            if (currentURL.match("/PredictionChart")) urlLink = "../DetailedStrategicIndicators/PredictionChart?id=" + ids[i] + "&name=" + text[i];
            else urlLink = "../DetailedStrategicIndicators/HistoricChart?id=" + ids[i] + "&name=" + text[i];

            //add from + to to link
            var from = getParameterByName('from');
            var to = getParameterByName('to');
            if ($('#datepickerFrom').length) {
                urlLink = urlLink + "&from=" + $('#datepickerFrom').val() + "&to=" + $('#datepickerTo').val();
            }
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
                datasets: [{
                    label: text[i],
                    backgroundColor: 'rgb(1, 119, 166)',
                    borderColor: 'rgb(1, 119, 166)',
                    data: dades[i],
                    fill: false
                }]
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

        //draw chart
        window.myLine = new Chart(ctx, config);
    }
}


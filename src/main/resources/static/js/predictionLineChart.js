var timeFormat = 'YYYY-MM-DD';
var config = [];
var charts = [];

if (isqf || isdsi) // qf and dsi -> no intervals of confidence
    var colors = ['rgb(1, 119, 166)', 'rgb(255, 153, 51)', 'rgb(51, 204, 51)', 'rgb(255, 80, 80)', 'rgb(204, 201, 53)', 'rgb(192, 96, 201)'];
else // metrics and si -> intervals of confidence
    var colors = ['rgb(75, 149, 179)', 'rgb(1, 119, 166)', 'rgb( 254, 126, 0)', 'rgb( 254, 126, 0)', 'rgb( 255, 177, 101)', 'rgb( 255, 177, 101)'];

Chart.plugins.register({
    afterDraw: function(chart) {
        var allEmpty = true;
        for (var i = 0; i < chart.data.datasets.length; i++) {
            if (chart.data.datasets[i].data.length > 0) allEmpty = false;
        }
        if (allEmpty) {
            // No data is present
            var ctx = chart.chart.ctx;
            var width = chart.chart.width;
            var height = chart.chart.height;
            chart.clear();

            ctx.save();
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.font = "Helvetica Nueue";
            ctx.fillText(chart.data.errors[0], width / 2, height / 2, width);
            ctx.restore();
        }
    }
});

function drawChart() {
    config = [];
    console.log("dins del drawChart");
    console.log(value);
    console.log(texts);
    console.log(labels);
    for (var i = 0; i < texts.length; ++i) {    //create config for each chart
        var c = {
            type: 'line',
            data: {
                datasets: [],
                errors: []
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: texts[i]
                },
                responsive: false,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 13,
                        generateLabels: function (chart) {
                            var data = chart.data;
                            var maxLength = Math.round(70/data.datasets.length);

                            return data.datasets.map(function (dataset, i) {
                                var label = dataset.label;
                                if (label.length > maxLength + 3) {
                                    label = label.substring(0, maxLength) + "...";
                                }

                                return {
                                    text: label,
                                    fillStyle: dataset.backgroundColor,
                                    strokeStyle: dataset.borderColor,
                                    lineWidth: dataset.borderWidth,
                                    hidden: dataset.hidden,
                                    index: i
                                }
                            })
                        }
                    },
                    onClick: function(e, legendItem) {
                        var index = legendItem.index;
                        var chart = this.chart;
                        chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                        chart.update();
                    }
                },
                scales: {
                    xAxes: [{
                        type: "time",
                        time: {
                            unit: 'day',
                            parser: timeFormat,
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
                            max: 1.2,
                            min: 0
                        }
                    }]
                },
                tooltips: {
                    enabled: false,
                    custom: function(tooltipModel) {
                        // Tooltip Element
                        var tooltipEl = document.getElementById('chartjs-tooltip');

                        // Create element on first render
                        if (!tooltipEl) {
                            tooltipEl = document.createElement('div');
                            tooltipEl.id = 'chartjs-tooltip';
                            tooltipEl.innerHTML = '<table></table>';
                            document.body.appendChild(tooltipEl);
                        }

                        // Hide if no tooltip
                        if (tooltipModel.opacity === 0) {
                            tooltipEl.style.opacity = 0;
                            return;
                        }

                        // Set caret Position
                        tooltipEl.classList.remove('above', 'below', 'no-transform');
                        if (tooltipModel.yAlign) {
                            tooltipEl.classList.add(tooltipModel.yAlign);
                        } else {
                            tooltipEl.classList.add('no-transform');
                        }

                        function getBody(bodyItem) {
                            return bodyItem.lines;
                        }

                        // Set Text
                        if (tooltipModel.body) {
                            var titleLines = tooltipModel.title || [];
                            var bodyLines = tooltipModel.body.map(getBody);

                            var innerHtml = '<thead>';

                            titleLines.forEach(function(title) {
                                innerHtml += '<tr><th>' + title + '</th></tr>';
                            });
                            innerHtml += '</thead><tbody>';

                            bodyLines.forEach(function(body, i) {
                                var colors = tooltipModel.labelColors[i];
                                var style = 'background:' + colors.backgroundColor.replace(', 0.3)', ')').replace('rgba', 'rgb');
                                style += '; border-color:' + colors.borderColor;
                                style += '; border-width: 2px';
                                var span = '<span class="chartjs-tooltip-key" style="' + style + '"></span>';
                                if (body[0].indexOf("<b>") === 0)
                                    innerHtml += '<tr><td>' + body + '</td></tr>';
                                else
                                    innerHtml += '<tr><td>' + span + body + '</td></tr>';
                            });
                            innerHtml += '</tbody>';

                            var tableRoot = tooltipEl.querySelector('table');
                            tableRoot.innerHTML = innerHtml;
                        }

                        // `this` will be the overall tooltip
                        var position = this._chart.canvas.getBoundingClientRect();

                        // Display, position, and set styles for font
                        tooltipEl.style.opacity = 1;
                        tooltipEl.style.position = 'absolute';
                        tooltipEl.style.left = position.left + window.pageXOffset + tooltipModel.caretX + 'px';
                        tooltipEl.style.top = position.top + window.pageYOffset + tooltipModel.caretY + 'px';
                        tooltipEl.style.fontFamily = tooltipModel._bodyFontFamily;
                        tooltipEl.style.fontSize = tooltipModel.bodyFontSize + 'px';
                        tooltipEl.style.fontStyle = tooltipModel._bodyFontStyle;
                        tooltipEl.style.padding = tooltipModel.yPadding + 'px ' + tooltipModel.xPadding + 'px';
                        tooltipEl.style.pointerEvents = 'none';
                    }
                },
                annotation: {
                    annotations: []
                }
            }
        };

        for (j = 0; j < value[i].length; ++j) {
            if (value[i][j].length === 0) hidden = true;
            var showLine = true;
            var pointStyle = 'circle';
            var pointRadius = 3;
            var borderWidth = 1;
            var color = colors[j % colors.length];
            // to paint areas for confidence interval series
            if ((labels[i][j] == "80" || labels[i][j] == "95") && prediction) {
                c.data.datasets.push({
                    label: labels[i][j],
                    hidden: false,
                    backgroundColor: color.replace(')', ', 0.3)').replace('rgb', 'rgba'),
                    borderColor: color.replace(')', ', 0.3)').replace('rgb', 'rgba'),
                    pointHitRadius: 1.5,
                    pointHoverRadius: 1.5,
                    pointRadius: 1.5,
                    pointBorderWidth: 1.5,
                    fill: 1,
                    data: value[i][j],
                    showLine: showLine,
                    pointStyle: pointStyle,
                    radius: pointRadius,
                    borderWidth: borderWidth
                });
            } else { // to paint mean serie
                c.data.datasets.push({
                    label: labels[i][j],
                    hidden: false,
                    backgroundColor: color,
                    borderColor: color,
                    fill: false,
                    data: value[i][j],
                    showLine: showLine,
                    pointStyle: pointStyle,
                    radius: pointRadius,
                    borderWidth: borderWidth
                });
            }

            if (!showLine) {
                c.options.tooltips.callbacks = {
                    label: function (tooltipItems, data) {
                        var posY = data.datasets[tooltipItems.datasetIndex].data[0].y;
                        if (posY === 1.1 || posY === 1.2) {
                            return "<b>Requirement: </b>" + data.datasets[tooltipItems.datasetIndex].data[tooltipItems.index].requirement + "<br/>" +
                                "<b>Comments: </b>" + data.datasets[tooltipItems.datasetIndex].data[tooltipItems.index].comments;
                        } else
                            return data.datasets[tooltipItems.datasetIndex].label + ': ' + tooltipItems.yLabel;
                    }
                };
            }

            if (typeof errors !== 'undefined') {
                c.data.errors.push(errors[i][j]);
            }
            else {
                c.data.errors.push("No data to display");
            }
        }

        //Add category lines
        if (typeof categories !== 'undefined') {
            var annotations = [];
            var lineHighCategory = {
                type: 'line',
                drawTime: 'beforeDatasetsDraw',
                mode: 'horizontal',
                scaleID: 'y-axis-0',
                value: categories[1].upperThreshold,
                borderColor: categories[0].color,
                borderWidth: 1,
                label: {
                    enabled: false,
                    content: categories[0].name
                }
            };
            annotations.push(lineHighCategory);

            var lineLowCategory = {
                type: 'line',
                drawTime: 'beforeDatasetsDraw',
                mode: 'horizontal',
                scaleID: 'y-axis-0',
                value: categories[categories.length - 1].upperThreshold,
                borderColor: categories[categories.length - 1].color,
                borderWidth: 1,
                label: {
                    enabled: false,
                    content: categories[categories.length - 1].name
                }
            };
            annotations.push(lineLowCategory);

            c.options.annotation.annotations = annotations;
        }

        config.push(c);
    }

    for (i = 0; i < texts.length; ++i) {
        var a = document.createElement('a');
        var currentURL = window.location.href;
        if (isdsi) {  //if it is a Stacked Line Chart for Detailed Strategic Indicators
            urlLink = "../QualityFactors/PredictionChart?id=" + ids[i] + "&name=" + texts[i];
            a.setAttribute("href", urlLink);
        } else if (isqf) { //if it is a Stacked Line Chart for Quality Factors
            var name = getParameterByName('name');
            var id = getParameterByName('id');
            if (name.length != 0) {//if we know from which Detailed Strategic Indicator we are coming
                urlLink = "../Metrics/PredictionChart?id=" + ids[i] + "&si=" + name + "&siid=" + id + "&name=" + texts[i];
            }
            else {
                urlLink = "../Metrics/PredictionChart?id=" + ids[i] + "&name=" + texts[i];
            }
            a.setAttribute("href", urlLink);
        } else if (isSi) {
            //if its a SI chart make it a hyperlink
            urlLink = "../DetailedStrategicIndicators/PredictionChart?id=" + ids[i] + "&name=" + texts[i];
            a.setAttribute("href", urlLink);
        }
        a.innerHTML = texts[i];
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

        var chart = new Chart(ctx, config[i]);
        charts.push(chart);
        window.myLine = chart;  //draw chart
    }

    var fit = sessionStorage.getItem("fitToContent");
    if (fit === "true") {
        $("#fitToContent").prop("checked", true);
        fitToContent();
    } else {
        $("#fitToContent").prop("checked", false);
        normalRange();
    }
}

function getMax (datasets) {
    var max = 0;
    for(var i = 0; i < datasets.length; i++) {
        var dataset = datasets[i];
        if(datasets[i].hidden) {
            continue;
        }
        dataset.data.forEach(function(d) {
            if(d.y > max) {
                max = d.y
            }
        });
    }
    return max;
}

function getMin (datasets) {
    var min = 1;
    for(var i = 0; i < datasets.length; i++) {
        var dataset = datasets[i];
        if(datasets[i].hidden) {
            continue;
        }
        dataset.data.forEach(function(d) {
            if(d.y < min) {
                min = d.y
            }
        });
    }
    return min;
}

$("#fitToContent").change(function () {
    if ($(this).is(":checked")) {
        sessionStorage.setItem("fitToContent", "true");
        fitToContent();
    } else {
        sessionStorage.setItem("fitToContent", "false");
        normalRange();
    }
});



function fitToContent() {
    charts.forEach(function (chart) {
        console.log(chart);
        var max = getMax(chart.config.data.datasets);
        var min = getMin(chart.config.data.datasets);
        if (max === min) {
            max += 0.001;
            min -= 0.001;
        }
        chart.config.options.scales.yAxes[0].ticks.max = max;
        chart.config.options.scales.yAxes[0].ticks.min = min;

        chart.config.options.legend.onClick = function (e, legendItem) {
            var index = legendItem.index;
            var c = this.chart;
            c.data.datasets[index].hidden = !c.data.datasets[index].hidden;
            var max = getMax(c.data.datasets);
            var min = getMin(c.data.datasets);
            if (max === min) {
                max += 0.001;
                min -= 0.001;
            }
            c.options.scales.yAxes[0].ticks.max = max;
            c.options.scales.yAxes[0].ticks.min = min;
            c.update();
        };

        chart.update();
    });
}

function normalRange() {
    charts.forEach(function (chart) {
        chart.config.options.scales.yAxes[0].ticks.max = 1.2;
        chart.config.options.scales.yAxes[0].ticks.min = 0;

        chart.config.options.legend.onClick = function(e, legendItem) {
            var index = legendItem.index;
            var chart = this.chart;
            chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
            chart.update();
        };

        chart.update();
    });
}


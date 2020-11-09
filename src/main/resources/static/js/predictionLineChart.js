var timeFormat = 'YYYY-MM-DD';
var config = [];
var charts = [];

if (isdqf || isdsi) {// dqf and dsi -> no intervals of confidence
    var colors = ['rgb(1, 119, 166)', 'rgb(255, 153, 51)', 'rgb(51, 204, 51)', 'rgb(255, 80, 80)',
        'rgb(204, 201, 53)', 'rgb(192, 96, 201)'];
} else // si, factors and metrics -> intervals of confidence
    var colors = ['rgb(1, 119, 166)', 'rgb(1, 119, 166)', 'rgb( 254, 126, 0)', 'rgb( 254, 126, 0)', 'rgb( 255, 177, 101)', 'rgb( 255, 177, 101)'];

if (isdqf || isdsi) { // dqf and dsi -> no intervals of confidence
    $('#showConfidence').prop("disabled",true);
}
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
                        // default function
                        var index = legendItem.index;
                        var chart = this.chart;
                        chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                        chart.update();
                    },
                    filter: null,
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
            var pointRadius = 2.5;
            var borderWidth = 1;
            var color = [];
            if (isdqf || isdsi) {
                var num = value[i].length/2;
                if (j < num) { // if we work with historical data
                    color = colors[j % colors.length];
                } else { // if we work with predicted data
                    color = colors[(j-num) % colors.length];
                }
            } else color = colors[j % colors.length];
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
                if (labels[i][j].includes("Predicted ")) { // normal line for predicted data
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
                } else { // dashed line for historical data
                    c.data.datasets.push({
                        label: labels[i][j],
                        hidden: false,
                        backgroundColor: color,
                        borderColor: color,
                        borderDash: [5,5],
                        fill: false,
                        data: value[i][j],
                        showLine: showLine,
                        pointStyle: pointStyle,
                        radius: 0,
                        borderWidth: borderWidth
                    });
                }

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

        // filter legend in case of SIs, TODO: Factors i Metrics
        if (!isdqf && !isdsi) {
            var filter = function(legendItem) {
                // hide duplicated 80 and 95 from legend and Predicted data too
                if (legendItem.index === 3 || legendItem.index === 5 || legendItem.index === 1) {
                    return false;
                }
                return true;
            };
            c.options.legend.labels.filter = filter;
        } else  { // filter legend in case of DSIs and Factors
            var filter = function(legendItem) {
                // hide Predicted data from legend
                if (legendItem.text.includes('Predicted') || legendItem.text.includes('Predic...')) {
                    return false;
                }
                return true;
            };
            c.options.legend.labels.filter = filter;
        }

        config.push(c);
    }

    for (i = 0; i < texts.length; ++i) {
        var a = document.createElement('a');
        if (isdsi) {  //if it is a Stacked Line Chart for Detailed Strategic Indicators
            urlLink = "../QualityFactors/PredictionChart?id=" + ids[i] + "&name=" + texts[i];
            a.setAttribute("href", urlLink);
        }  else if (isqf) { //if it is a Stacked Line Chart for Quality Factors
            var name = getParameterByName('name');
            var id = getParameterByName('id');
            if (name.length != 0) {//if we know from which Detailed Strategic Indicator we are coming
                urlLink = "../DetailedQualityFactors/PredictionChart?id=" + ids[i] + "&si=" + name + "&siid=" + id + "&name=" + texts[i];
            }
            else {
                urlLink = "../DetailedQualityFactors/PredictionChart?id=" + ids[i] + "&name=" + texts[i];
            }
            a.setAttribute("href", urlLink);
        }else if (isdqf) { //if it is a Stacked Line Chart for Detailed Quality Factors
            var name = getParameterByName('si');
            var id = getParameterByName('siid');
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

    var show = sessionStorage.getItem("showConfidence");
    if (show === "true") {
        $("#showConfidence").prop("checked", true);
        // show confidence intervals ticked
        showConfidence();
    } else {
        // show confidence intervals not ticked
        $("#showConfidence").prop("checked", false);
        notShowConfidence();
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

$("#showConfidence").change(function () {
    if ($(this).is(":checked")) {
        // show confidence intervals ticked
        sessionStorage.setItem("showConfidence", "true");
        showConfidence();
    } else {
        // show confidence intervals not ticked
        sessionStorage.setItem("showConfidence", "false");
        notShowConfidence();
    }
});

function showConfidence() {
    charts.forEach(function (chart) {
        for (var i = 1; i < chart.config.data.datasets.length; i++) {
            if(chart.config.data.datasets[i].label === "80" || chart.config.data.datasets[i].label === "95") {
                chart.config.data.datasets[i].hidden = false;
            }
        }
        chart.update();
    });
}

function notShowConfidence() {
    charts.forEach(function (chart) {
        for (var i = 1; i < chart.config.data.datasets.length; i++) {
            if(chart.config.data.datasets[i].label === "80" || chart.config.data.datasets[i].label === "95") {
                chart.config.data.datasets[i].hidden = true;
            }
        }
        chart.update();
    });
}


function fitToContent() {
    charts.forEach(function (chart) {
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
            var chart = this.chart;
            if (legendItem.text === "80" || legendItem.text === "95") {
                chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                chart.data.datasets[index + 1].hidden = !chart.data.datasets[index + 1].hidden;
            } else if (!isdqf && !isdsi) { // hide & show logic for SIs, TODO:Factors and Metrics
                chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                if(chart.data.datasets[index].hidden == true) { // if hide hist. data
                    // for predicted data
                    chart.data.datasets[index + 1].hidden = true;
                    // for 80
                    chart.data.datasets[index + 2].hidden = true;
                    chart.data.datasets[index + 3].hidden = true;
                    //for 95
                    chart.data.datasets[index + 4].hidden = true;
                    chart.data.datasets[index + 5].hidden = true;
                } else { // if show hist. data
                    // for predicted data
                    chart.data.datasets[index + 1].hidden = false;
                    var show = sessionStorage.getItem("showConfidence");
                    if (show == "true") {
                        // for 80
                        chart.data.datasets[index + 2].hidden = false;
                        chart.data.datasets[index + 3].hidden = false;
                        //for 95
                        chart.data.datasets[index + 4].hidden = false;
                        chart.data.datasets[index + 5].hidden = false;
                    } else if (show == "false") {
                        // for 80
                        chart.data.datasets[index + 2].hidden = true;
                        chart.data.datasets[index + 3].hidden = true;
                        //for 95
                        chart.data.datasets[index + 4].hidden = true;
                        chart.data.datasets[index + 5].hidden = true;
                    }
                }
            } else { // hide & show logic for DSIs and Factors
                var num = chart.data.datasets.length/2;
                chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                chart.data.datasets[index + num].hidden = !chart.data.datasets[index + num].hidden;
            }
            var max = getMax(chart.data.datasets);
            var min = getMin(chart.data.datasets);
            if (max === min) {
                max += 0.001;
                min -= 0.001;
            }
            chart.options.scales.yAxes[0].ticks.max = max;
            chart.options.scales.yAxes[0].ticks.min = min;
            chart.update();
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
            if (legendItem.text === "80" || legendItem.text === "95") {
                chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                chart.data.datasets[index + 1].hidden = !chart.data.datasets[index + 1].hidden;
            } else if (!isdqf && !isdsi) { // hide & show logic for SIs, TODO:Factors and Metrics
                chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                if(chart.data.datasets[index].hidden == true) { // if hide hist. data
                    // for predicted data
                    chart.data.datasets[index + 1].hidden = true;
                    // for 80
                    chart.data.datasets[index + 2].hidden = true;
                    chart.data.datasets[index + 3].hidden = true;
                    //for 95
                    chart.data.datasets[index + 4].hidden = true;
                    chart.data.datasets[index + 5].hidden = true;
                } else { // if show hist. data
                    // for predicted data
                    chart.data.datasets[index + 1].hidden = false;
                    var show = sessionStorage.getItem("showConfidence");
                    if (show == "true") {
                        // for 80
                        chart.data.datasets[index + 2].hidden = false;
                        chart.data.datasets[index + 3].hidden = false;
                        //for 95
                        chart.data.datasets[index + 4].hidden = false;
                        chart.data.datasets[index + 5].hidden = false;
                    } else if (show == "false") {
                        // for 80
                        chart.data.datasets[index + 2].hidden = true;
                        chart.data.datasets[index + 3].hidden = true;
                        //for 95
                        chart.data.datasets[index + 4].hidden = true;
                        chart.data.datasets[index + 5].hidden = true;
                    }
                }
            } else { // hide & show logic for DSIs and Factors
                var num = chart.data.datasets.length/2;
                chart.data.datasets[index].hidden = !chart.data.datasets[index].hidden;
                chart.data.datasets[index + num].hidden = !chart.data.datasets[index + num].hidden;
            }
            chart.update();
        };

        chart.update();
    });
}


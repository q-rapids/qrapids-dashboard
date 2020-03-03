var url = "/api/strategicIndicators/qualityModel";
var serverUrl = sessionStorage.getItem("serverUrl");
if (serverUrl) {
    url = serverUrl + url;
}

var ids = [""];
var labels = [""];
var parents = [""];
var values = [null];
var hovertext = [""];

function loadData() {
    jQuery.ajax({
        dataType: "json",
        type: "GET",
        url : url,
        async: true,
        success: function (data) {
            makeChart(data);
        }});
}

function makeChart(strategicIndicators) {
    console.log("Hi, let's work!");
    console.log(strategicIndicators);
    for (var i = 0; i < strategicIndicators.length; i++) {
        var strategicIndicator = strategicIndicators[i];
        ids.push(strategicIndicator.id);
        labels.push(strategicIndicator.name);
        parents.push(" ");
        values.push(parseFloat(strategicIndicator.value));
        // make tooltip for SI
        hovertext.push('<b>' + strategicIndicator.name + ": " + '</b>' + strategicIndicator.valueDescription);
        for (var j = 0; j < strategicIndicator.factors.length; j++) {
            var factor = strategicIndicator.factors[j];
            ids.push(strategicIndicator.id + '/' + factor.id);
            labels.push(factor.name);
            parents.push(strategicIndicator.id);
            // define weighted value for factor
            var w = parseFloat(factor.weight);
            if ( w == 0 || w == 1) {
                var factorValue = factor.value * 1/strategicIndicator.factors.length;
                values.push(factorValue);
                // make tooltip for Factor
                hovertext.push('<b>' + factor.name + ": " + '</b>' + parseFloat(factor.value).toFixed(2) +
                    " (" + (1/strategicIndicator.factors.length*100).toFixed(0) + "%" +
                        " over " + strategicIndicator.name + ")");
            }
            else {
                values.push(parseFloat(factor.value));
                // make tooltip for Factor
                hovertext.push('<b>' + factor.name + ": " + '</b>' + parseFloat(factor.value/factor.weight).toFixed(2) +
                    " (" + (factor.weight*100).toFixed(0) + "%" +
                        " over " + strategicIndicator.name + ")");
            }
            // sum all metrics weights for factor
            var metricsWeights = sumMetricsWeights(factor.metrics);
            for (var k = 0; k < factor.metrics.length; k++) {
                var metric = factor.metrics[k];
                ids.push(strategicIndicator.id + '/' + factor.id + '/' + metric.id);
                labels.push(metric.name);
                parents.push(strategicIndicator.id + '/' + factor.id);
                if ( w == 0 || w == 1) {
                    /*if (metric.value == 0) {
                        values.push(0.0000002);
                    } else {*/
                        values.push(metric.value * 1/metricsWeights * 1/strategicIndicator.factors.length);
                    //}
                    // make tooltip for Metric
                    hovertext.push('<b>' + metric.name + ": " + '</b>' + parseFloat(metric.value).toFixed(2) +
                        " (" + (1/metricsWeights*100).toFixed(0) + "%" +
                        " over " + factor.name + ")");
                }
                else {
                    /*if (metric.value == 0) {
                        values.push(0.0000002);
                    } else {*/
                        values.push(metric.value * metric.weight/metricsWeights * factor.weight);
                    //}
                    // make tooltip for Metric
                    hovertext.push('<b>' + metric.name + ": " + '</b>' + parseFloat(metric.value).toFixed(2) +
                        " (" + (metric.weight/metricsWeights*100).toFixed(0) + "%" +
                        " over " + factor.name + ")");
                }

            }
        }
    }

    console.log(ids);
    console.log(labels);
    console.log(parents);
    console.log(values);

    var data = [{
        type: "sunburst",
        ids: ids,
        labels: labels,
        parents: parents,
        values:  values,
        hovertext: hovertext,
        name: "Quality Model",
        hoverinfo: "text",
        hoverlabel : {
            font: {
                family: 'Courier New, monospace',
                size: 10,
                color: '#000000'
            },
        },
        outsidetextfont: {size: 14, color: "#000000"},
        insidetextfont: {size: 14, color: "#000000"},
        leaf: {opacity: 0.4},
        marker: {line: {width: 2}},
        textposition: 'inside',
        insidetextorientation: 'radial',
        branchvalues: 'total'
    }];
    var layout = {
        showlegend: true,
        margin: {l: 0, r: 0, b: 0, t: 0},
        width: 600, height: 600,
        sunburstcolorway:d3.scaleOrdinal(d3.quantize(d3.interpolateRainbow, data.length + 1)),
        font: {
            family: 'Courier New, monospace',
            size: 10,
            color: '#7f7f7f'
        },
        extendsunburstcolorway: true
    };

    /* Legends HTML
    var legendContainer = d3.select("#legend").append("div").classed('legends clearfix', true);

    var legend = legendContainer.selectAll('.legend') // selecting elements with class 'legend'
        .data(color) // refers to an array of labels from our dataset
        .enter() // creates placeholder
        .append('div') // replace placeholders with g elements
        .attr('class', 'legend');// each g is given a legend class

// adding colored squares to legend
    legend.append('div').classed('rect', true) // append rectangle squares to legend
        .style('background-color', function (d) { return color[d.key]; }) // each fill is passed a color
        .style('border', function (d) { return '1px solid ' + color[d.key]; }) // each fill is passed a color
        .on('click', function(d) {
            if(d3.select(this).classed('clicked')) {
                d3.select(this).classed('clicked', false)
                    .style('background-color', function(d) { return color[d.key]; });
                // filter data and rerender
            } else {
                d3.select(this).classed('clicked', true)
                    .style('background-color', 'transparent');
                // filter data and rerender
            }
        })

// adding text to legend
    legend.append('span')
        .text(function(d) { return d.key; }); // return label

*/

    Plotly.newPlot('SunburstChart', data, layout, {displaylogo: false, responsive: true});
}

function sumMetricsWeights(elements){
    var totalWeight = 0;
    for (var i = 0; i < elements.length; i++){
        totalWeight += parseFloat(elements[i].weight);
    }
    return totalWeight;
}

loadData();
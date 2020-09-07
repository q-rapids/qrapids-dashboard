console.log("sessionStorage: profile_id");
console.log(sessionStorage.getItem("profile_id"));
var profileId = sessionStorage.getItem("profile_id");

var url = "/api/strategicIndicators/qualityModel?profile="+profileId;
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
            if (w == -1) {
                // if the factor value is zero we have to change it to 0.01 and sum this for all upper levels
                if (parseFloat(factor.weightedValue) == 0) {
                    values.push(0.01);
                    var ind_si =ids.indexOf(strategicIndicator.id);
                    values[ind_si] = values[ind_si] + 0.01;
                } else {
                    values.push(parseFloat(factor.weightedValue));
                }
                // make tooltip for Factor
                hovertext.push('<b>' + factor.name + ": " + '</b>' + parseFloat(factor.assessmentValue).toFixed(2) +
                    "(NA)");
            } else {
                // if the factor value is zero we have to change it to 0.01 and sum this for all upper levels
                if (parseFloat(factor.weightedValue) == 0) {
                    values.push(0.01);
                    var ind_si =ids.indexOf(strategicIndicator.id);
                    values[ind_si] = values[ind_si] + 0.01;
                } else {
                    values.push(parseFloat(factor.weightedValue));
                }
                // make tooltip for Factor
                hovertext.push('<b>' + factor.name + ": " + '</b>' + parseFloat(factor.assessmentValue).toFixed(2) +
                    "&#10;&#13;(" + (factor.weight * 100).toFixed(0) + "%" +
                    " over " + strategicIndicator.name + ")");
            }
            for (var k = 0; k < factor.metrics.length; k++) {
                var metric = factor.metrics[k];
                ids.push(strategicIndicator.id + '/' + factor.id + '/' + metric.id);
                labels.push(metric.name);
                parents.push(strategicIndicator.id + '/' + factor.id);
                // if the metric value is zero we have to change it to 0.01 and sum this for all upper levels
                if (parseFloat(metric.weightedValue) == 0) {
                    values.push(0.01 * factor.weight);
                    var ind_f =ids.indexOf(strategicIndicator.id + '/' + factor.id);
                    values[ind_f] = values[ind_f] + 0.01*factor.weight;
                    var ind_si =ids.indexOf(strategicIndicator.id);
                    values[ind_si] = values[ind_si] + 0.01*factor.weight;
                } else {
                    values.push(parseFloat(metric.weightedValue * factor.weight));
                }
                // make tooltip for Metric
                hovertext.push('<b>' + metric.name + ": " + '</b>' + parseFloat(metric.assessmentValue).toFixed(2) +
                    "&#10;&#13;(" + (metric.weight * 100).toFixed(0) + "%" +
                    " over " + factor.name + ")");
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
            bgcolor: '#ffffff',
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

    Plotly.newPlot('SunburstChart', data, layout, {displaylogo: false, responsive: true});
}



window.onload = function() {
    if (sessionStorage.getItem("profile_qualitylvl") != "ALL") {
        window.open("../QualityModelGraph","_self");
    }
    loadData();
}
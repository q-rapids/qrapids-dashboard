var profileId = sessionStorage.getItem("profile_id");
var url = "/api/strategicIndicators/qualityModel?profile="+profileId;
var serverUrl = sessionStorage.getItem("serverUrl");
if (serverUrl) {
    url = serverUrl + url;
}

var isdsi = false;
var colorList = ['rgba(1, 119, 166, 0.6)', 'rgba(255, 153, 51, 0.6)', 'rgba(51, 204, 51, 0.6)', 'rgba(255, 80, 80, 0.6)', 'rgba(204, 201, 53, 0.6)', 'rgba(192, 96, 201, 0.6)'];

//initialize data vectors
var ids = [];
var colorsForPolar = [];
var titles = [];
var labels = [];
var weights = [];
var weightedValues = [];
var assessmentValues = [];

var categories = [];
var categoriesForPolar = [];

var metrics = true;

function getData() {
    //empty previous data
    ids = [];
    colorsForPolar = [];
    titles = [];
    labels = [];
    weights = [];
    weightedValues = [];
    assessmentValues = [];

    getCategories();
    getMetricsCategories();

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(data);
            var url_string = parseURLSimple(window.location.href);
            var url = new URL(url_string);
            var id = url.searchParams.get("id");
            if (!id) { // if all Factors are required
                for(i = 0; i < data.length; i++) { // while DSI
                    for (j = 0; j < data[i].factors.length; ++j) { // while factors
                        var t = data[i].factors[j].name + ": &nbsp;" + parseFloat(data[i].factors[j].assessmentValue).toFixed(2);
                        if (!titles.includes(t)) {
                            titles.push(t);
                            ids.push(data[i].factors[j].id);
                            var c = colorsForPolar.push([]);
                            var l = labels.push([]);
                            var w = weights.push([]);
                            var wv = weightedValues.push([]);
                            var av = assessmentValues.push([]);
                            for (k = 0; k < data[i].factors[j].metrics.length; ++k) { // while metrics
                                if (data[i].factors[j].metrics[k].name.length < 27)
                                    labels[l - 1].push(data[i].factors[j].metrics[k].name);
                                else
                                    labels[l - 1].push(data[i].factors[j].metrics[k].name.slice(0, 23) + "...");
                                weights[w - 1].push(data[i].factors[j].metrics[k].weight);
                                weightedValues[wv - 1].push(data[i].factors[j].metrics[k].weightedValue);
                                assessmentValues[av - 1].push(data[i].factors[j].metrics[k].assessmentValue);
                                colorsForPolar[c - 1].push(colorList[k%colorList.length]);
                            }
                        }
                    }
                }
            } else { // if individual DSI's Factors are required
                console.log("else");
                console.log(data.find(obj => {
                    return obj.id === id
                }));
                var d = data.find(obj => {
                    return obj.id === id
                });
                for (i = 0; i < d.factors.length; ++i) {
                    ids.push(d.factors[i].id);
                    titles.push(d.factors[i].name + ": &nbsp;" + parseFloat(d.factors[i].assessmentValue).toFixed(2));
                    var c = colorsForPolar.push([]);
                    var l = labels.push([]);
                    var w = weights.push([]);
                    var wv = weightedValues.push([]);
                    var av = assessmentValues.push([]);
                    for (j = 0; j < d.factors[i].metrics.length; ++j) {
                        //for each factor save name to labels vector and value to values vector
                        if (d.factors[i].metrics[j].name < 27)
                            labels[l-1].push(d.factors[i].metrics[j].name);
                        else
                            labels[l-1].push(d.factors[i].metrics[j].name.slice(0, 23) + "...");
                        weights[w-1].push(d.factors[i].metrics[j].weight);
                        weightedValues[wv-1].push(d.factors[i].metrics[j].weightedValue);
                        assessmentValues[av-1].push(d.factors[i].metrics[j].assessmentValue);
                        colorsForPolar[c - 1].push(colorList[j%colorList.length]);
                    }
                }

            }
            drawChart();
        }
    });
}

function getMetricsCategories() {
    jQuery.ajax({
        url: "../api/metrics/categories",
        type: "GET",
        async: true,
        success: function (response) {
            categoriesForPolar = response;
        }
    });
}

function getCategories() {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/qualityFactors/categories";
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url).then (function(cat) {
        categories.push({
            color: cat[0].color, // high category
            pos: cat[1].upperThreshold,
        });
        categories.push({
            color: cat[cat.length-1].color, // low category
            pos: cat[cat.length-1].upperThreshold,
        });
    });
}
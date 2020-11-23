var profileId = sessionStorage.getItem("profile_id");
var url = "/api/strategicIndicators/qualityModel?profile="+profileId;
var serverUrl = sessionStorage.getItem("serverUrl");
if (serverUrl) {
    url = serverUrl + url;
}

var isdsi = true;
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

var metrics = false;

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
    getFactorsCategories();

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
            console.log(id);
            if (!id) { // if all DSI are required
                for(i = 0; i < data.length; i++) { // while DSI
                    titles.push(data[i].name + ": &nbsp;" + data[i].valueDescription);
                    ids.push(data[i].id);
                    colorsForPolar.push([]);
                    labels.push([]);
                    weights.push([]);
                    weightedValues.push([]);
                    assessmentValues.push([]);
                    for (j = 0; j < data[i].factors.length; j++) { // while factors
                        //for each factor save name to labels vector and value to values vector
                        if (data[i].factors[j].name.length < 27)
                            labels[i].push(data[i].factors[j].name);
                        else
                            labels[i].push(data[i].factors[j].name.slice(0, 23) + "...");
                        weights[i].push(data[i].factors[j].weight);
                        weightedValues[i].push(data[i].factors[j].weightedValue);
                        assessmentValues[i].push(data[i].factors[j].assessmentValue);
                        colorsForPolar[i].push(colorList[j%colorList.length]);
                    }
                }
            } else { // if individual DSI is required
                console.log("else");
                console.log(data.find(obj => {
                    return obj.id === id
                }));
                var d = data.find(obj => {
                    return obj.id === id
                });
                titles.push(d.name + ": &nbsp;" + d.valueDescription);
                ids.push(d.id);
                colorsForPolar.push([]);
                labels.push([]);
                weights.push([]);
                weightedValues.push([]);
                assessmentValues.push([]);
                for (j = 0; j < d.factors.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (d.factors[j].name.length < 27)
                        labels[0].push(d.factors[j].name);
                    else
                        labels[0].push(d.factors[j].name.slice(0, 23) + "...");
                    weights[0].push(d.factors[j].weight);
                    weightedValues[0].push(d.factors[j].weightedValue);
                    assessmentValues[0].push(d.factors[j].assessmentValue);
                    colorsForPolar[0].push(colorList[j%colorList.length]);
                }
            }
            // TODO make navigation link
            navTextSimple();
            drawChart();
        }
    });
}

function getFactorsCategories() {
    jQuery.ajax({
        url: "../api/qualityFactors/categories",
        type: "GET",
        async: true,
        success: function (response) {
            categoriesForPolar = response;
        }
    });
}

function getCategories() {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/strategicIndicators/categories";
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url).then (function(cat) {
        categories.push({
            color: cat[0].color, // high category
            pos: 1 - 1/cat.length,
        });
        categories.push({
            color: cat[cat.length-1].color, // low category
            pos: 1/cat.length,
        });
    });
}
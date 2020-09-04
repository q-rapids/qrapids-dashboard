var isdsi = false;
var id = false;

var url;
if (getParameterByName('id').length !== 0) {
    url = parseURLComposed("../api/qualityFactors/metrics/current");
    id = true;
} else {
    url = parseURLComposed("../api/qualityFactors/metrics/current");
}

//initialize data vectors
var titles = [];
var labels = [];
var ids = [];
var values = [];
var categories = [];

function getData() {
    titles = [];
    labels = [];
    ids = [];
    values = [];
    categories = [];

    getCategories();

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(url);
            console.log("getData() in QF Current");
            console.log(data);
            if (id) { // in case we show one detailed factor
                titles.push(getParameterByName('name'));
                ids.push(getParameterByName('id'));
                labels.push([]);
                values.push([]);
                for (j = 0; j < data.length; ++j) {
                    //for each metric save name to labels vector and value to values vector
                    if (data[j].name.length < 27)
                        labels[0].push(data[j].name);
                    else
                        labels[0].push(data[j].name.slice(0, 23) + "...");
                    values[0].push(data[j].value);
                }
                drawChart();
            } else { // in case we show all detailed factors
                for (i = 0; i < data.length; ++i) {
                    //for each qf save name to titles vector and id to ids vector
                    titles.push(data[i].name);
                    ids.push(data[i].id);
                    labels.push([]);
                    values.push([]);
                    for (j = 0; j < data[i].metrics.length; ++j) {
                        //for each metric save name to labels vector and value to values vector
                        if (data[i].metrics[j].name.length < 27)
                            labels[i].push(data[i].metrics[j].name);
                        else
                            labels[i].push(data[i].metrics[j].name.slice(0, 23) + "...");
                        values[i].push(data[i].metrics[j].value);
                    }
                }
                drawChart();
            }
        }
    });

    console.log(titles);
    console.log(labels);
    console.log(values);
}

function getCategories() {
    var serverUrl = sessionStorage.getItem("serverUrl");
    var url = "/api/metrics/categories";
    if (serverUrl) {
        url = serverUrl + url;
    }
    $.getJSON(url).then (function(cat) {
        for (var i = 0; i < cat.length; i++) {
            categories.push({
                name: cat[i].name,
                color: cat[i].color,
                upperThreshold: cat[i].upperThreshold,
            });
        }
        console.log(categories);
    });
}
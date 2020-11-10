var isdsi = false;
var id = false;

var url;
if (getParameterByName('id').length !== 0) {
    url = parseURLComposed("../api/qualityFactors/metrics/current");
    id = true;
} else {
    var profileId = sessionStorage.getItem("profile_id");
    url = parseURLComposed("../api/qualityFactors/metrics/current?profile="+profileId);
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
            sortDataAlphabetically(data);
            console.log(data);
            for (i = 0; i < data.length; ++i) {
                //for each qf save name to titles vector and id to ids vector
                titles.push(data[i].name + "<br/>" + data[i].value_description);
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
            getMetricsCategories();
        }
    });

    console.log(titles);
    console.log(labels);
    console.log(values);
}

function sortDataAlphabetically (data) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    data.sort(compare);
}

function getMetricsCategories() {
    jQuery.ajax({
        url: "../api/metrics/categories",
        type: "GET",
        async: true,
        success: function (response) {
            categories = response;
            drawChart();
        }
    });
}
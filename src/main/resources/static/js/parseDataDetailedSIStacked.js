var url = "/api/strategicIndicators/qualityModel";
var serverUrl = sessionStorage.getItem("serverUrl");
if (serverUrl) {
    url = serverUrl + url;
}

//initialize data vectors
var titles = [];
var labels = [];
var weights = [];
var values = [];

function getData() {
    //empty previous data
    titles = [];
    labels = [];
    weights = [];
    values = [];

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(data);
            for(i = 0; i < data.length; i++) {
                titles.push(data[i].name + ": &nbsp;" + data[i].valueDescription);
                labels.push([]);
                weights.push([]);
                values.push([]);
                for (j = 0; j < data[i].factors.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (data[i].factors[j].name.length < 27)
                        labels[i].push(data[i].factors[j].name);
                    else
                        labels[i].push(data[i].factors[j].name.slice(0, 23) + "...");
                    weights[i].push(data[i].factors[j].weight);
                    values[i].push(data[i].factors[j].value);
                }
            }
            drawChart();
        }
    });
}
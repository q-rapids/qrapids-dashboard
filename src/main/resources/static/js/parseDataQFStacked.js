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

var metrics = true;

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
            var url_string = window.location.href;
            var url = new URL(url_string);
            var id = url.searchParams.get("id");
            console.log(id);
            if (!id) { // if all Factors are required
                for(i = 0; i < data.length; i++) { // while DSI
                    for (j = 0; j < data[i].factors.length; ++j) { // while factors
                        titles.push(data[i].factors[j].name + ": &nbsp;" + parseFloat(data[i].factors[j].value).toFixed(2));
                        var l = labels.push([]);
                        var w = weights.push([]);
                        var v = values.push([]);
                        for (k = 0; k < data[i].factors[j].metrics.length; ++k) { // while metrics
                            console.log(data[i].factors[j].metrics[k].name);
                            if (data[i].factors[j].metrics[k].name.length < 27)
                                labels[l-1].push(data[i].factors[j].metrics[k].name);
                            else
                                labels[l-1].push(data[i].factors[j].metrics[k].name.slice(0, 23) + "...");
                            weights[w-1].push(data[i].factors[j].metrics[k].weight);
                            values[v-1].push(data[i].factors[j].metrics[k].value);
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
                    titles.push(d.factors[i].name + ": &nbsp;" + parseFloat(d.factors[i].value).toFixed(2));
                    var l = labels.push([]);
                    var w = weights.push([]);
                    var v = values.push([]);
                    for (j = 0; j < d.factors[i].metrics.length; ++j) {
                        //for each factor save name to labels vector and value to values vector
                        if (d.factors[i].metrics[j].name < 27)
                            labels[l-1].push(d.factors[i].metrics[j].name);
                        else
                            labels[l-1].push(d.factors[i].metrics[j].name.slice(0, 23) + "...");
                        weights[w-1].push(d.factors[i].metrics[j].weight);
                        values[v-1].push(d.factors[i].metrics[j].value);
                    }
                }

            }
            drawChart();
        }
    });
}
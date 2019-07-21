var url = parseURLSimple("../api/strategicIndicators/qualityFactors/current");

var isdsi = true;

//initialize data vectors
var titles = [];
var ids = [];
var labels = [];
var values = [];

function getData() {
    //empty previous data
    titles = [];
    ids = [];
    labels = [];
    values = [];

    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            for (i = 0; i < data.length; ++i) {
                //for each dsi save name to titles vector and id to ids vector
                titles.push(data[i].name);
                ids.push(data[i].id);
                labels.push([]);
                values.push([]);
                for (j = 0; j < data[i].factors.length; ++j) {
                    //for each factor save name to labels vector and value to values vector
                    if (data[i].factors[j].name.length < 27)
                        labels[i].push(data[i].factors[j].name);
                    else
                        labels[i].push(data[i].factors[j].name.slice(0, 23) + "...");
                    values[i].push(data[i].factors[j].value);
                }
            }
            drawChart();
        }
    });
}
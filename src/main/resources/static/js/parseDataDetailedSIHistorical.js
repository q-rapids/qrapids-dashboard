var isdsi = true;

var url = parseURLSimple("../api/DetailedStrategicIndicators/HistoricalData");

//initialize data vectors
var texts = [];
var ids = [];
var labels = [];
var value = [];

function getData() {
    texts = [];
    ids = [];
    labels = [];
    value = [];
    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: url,
        data: {
            "from": $('#datepickerFrom').val(),
            "to": $('#datepickerTo').val()
        },
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(data);
            for (i = 0; i < data.length; ++i) {
                for (i = 0; i < data.length; ++i) {
                    //for each dsi save name to texts vector and id to ids vector
                    if (data[i].factors.length > 0) {
                        texts.push(data[i].name);
                        ids.push(data[i].id);

                        value.push([[]]);
                        last = data[i].factors[0].id;
                        labels.push([data[i].factors[0].name]);
                        k = 0;
                        for (j = 0; j < data[i].factors.length; ++j) {
                            //check if we are still on the same factor
                            if (last != data[i].factors[j].id) {
                                labels[i].push(data[i].factors[j].name);
                                last = data[i].factors[j].id;
                                ++k;
                                value[i].push([]);
                            }
                            //push date and value to values vector
                            if (!isNaN(data[i].factors[j].value))
                            {
                                value[i][k].push(
                                    {
                                        x: data[i].factors[j].date.year + "-" + data[i].factors[j].date.monthValue + "-" + data[i].factors[j].date.dayOfMonth,
                                        y: data[i].factors[j].value
                                    }
                                );
                            }
                        }
                    } else {
                        data.splice(i, 1);
                        --i;
                    }
                }
            }
            drawChart();
        }
    });
    console.log(texts);
    console.log(labels);
    console.log(value);
}

window.onload = function() {
    getData();
};
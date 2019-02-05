var isSi = false;

var url = parseURLMetrics("../api/Metrics/HistoricalData");

//initialize data vectors
var text = [];
var dades = [];

function getData() {
    text = [];
    dades = [];
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
            j = 0;
            var line = [];
            if (data[j]) {
                last = data[j].id;
                text.push(data[j].name);
            }
            while (data[j]) {
                //check if we are still on the same metric
                if (data[j].id != last) {
                    dades.push(line);
                    line = [];
                    last = data[j].id;
                    text.push(data[j].name);
                }
                //push date and value to line vector
                if (!isNaN(data[j].value))
                {
                    line.push({
                        x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                        y: data[j].value
                    });

                }
                ++j;
            }
            //push line vector to values vector for the last metric
            if (data[j - 1])
                dades.push(line);
            drawChart();
        }
    });
    console.log(dades);
    console.log(text);

}

window.onload = function() {
    getData();
};
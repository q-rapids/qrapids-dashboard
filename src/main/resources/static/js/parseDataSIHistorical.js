var isSi = true;

//initialize data vectors
var text = [];
var dades = [];
var ids = [];

function getData() {
    text = [];
    dades = [];
    ids = [];
    //get data from API
    jQuery.ajax({
        dataType: "json",
        url: "../api/StrategicIndicators/HistoricalData",
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
                ids.push(data[j].id);
            }
            while (data[j]) {
                //check if we are still on the same Strategic Indicator
                if (data[j].id != last) {
                    dades.push(line);
                    line = [];
                    last = data[j].id;
                    text.push(data[j].name);
                    ids.push(data[j].id);
                }
                //push date and value to line vector
                if (!isNaN(data[j].value.first)) {
                    line.push({
                        x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                        y: data[j].value.first
                    });
                }
                ++j;
            }
            //push line vector to values vector for the last metric
            if (data[j - 1])
                dades.push(line);

            drawChart();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                alert("Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400)
                alert("Datasource connection failed.");
        }
    });
    console.log(dades);
    console.log(text);
}

window.onload = function() {
    getData();
};
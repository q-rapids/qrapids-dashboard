var isSi = true;

//initialize data vectors
var text = [];
var dades = [];
var ids = [];
var errors = [];

function getData() {
    document.getElementById("loader").style.display = "block";
    document.getElementById("chartContainer").style.display = "none";
    text = [];
    dades = [];
    ids = [];
    errors = [];
    var technique = $("#selectedTechnique").text();
    var date1 = new Date($('#datepickerFrom').val());
    var date2 = new Date($('#datepickerTo').val());
    var timeDiff = date2.getTime() - date1.getTime();
    var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
    if (diffDays < 1) {
        alert('To date has to be bigger than from date');
    } else {
        //get data from API
        jQuery.ajax({
            dataType: "json",
            url: "../api/StrategicIndicators/PredictionData",
            data: {
                "technique": technique,
                "horizon": diffDays
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
                    errors.push(data[j].forecastingError);
                }
                while (data[j]) {
                    //check if we are still on the same Strategic Indicator
                    if (data[j].id !== last) {
                        dades.push(line);
                        line = [];
                        last = data[j].id;
                        text.push(data[j].name);
                        ids.push(data[j].id);
                        errors.push(data[j].forecastingError);
                    }
                    //push date and value to line vector
                    if (data[j].value !== null) {
                        if (!isNaN(data[j].value.first)) {
                            line.push({
                                x: data[j].date,
                                y: data[j].value.first
                            });
                        }
                    }
                    ++j;
                }
                //push line vector to values vector for the last metric
                if (data[j - 1])
                    dades.push(line);
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                drawChart();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 409)
                    alert("Your datasource and DB categories IDs do not match.");
                else if (jqXHR.status == 400)
                    alert("Datasource connection failed.");
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                document.getElementById("chartContainer").innerHTML = "Error " + xhr.status;
            }
        });
    }
    console.log(dades);
    console.log(text);
}

window.onload = function() {
    getData();
};
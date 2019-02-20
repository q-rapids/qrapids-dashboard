var isSi = false;

var url = parseURLMetrics("../api/Metrics/PredictionData");

//initialize data vectors
var text = [];
var dades = [];
var lower80 = [];
var lower95 = [];
var upper80 = [];
var upper95 = [];
var errors = [];

function getData() {
    document.getElementById("loader").style.display = "block";
    document.getElementById("chartContainer").style.display = "none";
    text = [];
    dades = [];
    lower80 = [];
    lower95 = [];
    upper80 = [];
    upper95 = [];
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
            url: url,
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
                var line80l = [];
                var line80u = [];
                var line95l = [];
                var line95u = [];
                if (data[j]) {
                    last = data[j].id;
                    text.push(data[j].name);
                    errors.push(data[j].forecastingError);
                }
                while (data[j]) {
                    //check if we are still on the same metric
                    if (data[j].id !== last) {
                        dades.push(line);
                        lower80.push(line80l);
                        upper80.push(line80u);
                        lower95.push(line95l);
                        upper95.push(line95u);
                        errors.push(data[j].forecastingError);
                        line = [];
                        line80l = [];
                        line80u = [];
                        line95l = [];
                        line95u = [];
                        last = data[j].id;
                        text.push(data[j].name);
                    }
                    //push date and value to line vector
                    if (!isNaN(data[j].value)) {
                        if (data[j].value !== null) {
                            line.push({
                                x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                                y: data[j].value
                            });
                            line80l.push({
                                x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                                y: data[j].confidence80.second
                            });
                            line80u.push({
                                x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                                y: data[j].confidence80.first
                            });
                            line95l.push({
                                x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                                y: data[j].confidence95.second
                            });
                            line95u.push({
                                x: data[j].date.year + "-" + data[j].date.monthValue + "-" + data[j].date.dayOfMonth,
                                y: data[j].confidence95.first
                            });
                        }
                    }
                    ++j;
                }
                //push line vector to values vector for the last metric
                if (data[j - 1]) {
                    dades.push(line);
                    lower80.push(line80l);
                    upper80.push(line80u);
                    lower95.push(line95l);
                    upper95.push(line95u);
                }
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                drawChart();
            },
            error: function (xhr, ajaxOptions, thrownError) {
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                document.getElementById("chartContainer").innerHTML = "Error " + xhr.status;
            }
        });
    }
    console.log(dades);
    console.log(text);

}
var isSi = true;
var isdsi = false;
var isqf = false;

//initialize data vectors
var texts = [];
var ids = [];
var labels = [];
var value = [];
var errors = [];

var categories = [];

function getData() {
    document.getElementById("loader").style.display = "block";
    document.getElementById("chartContainer").style.display = "none";
    texts = [];
    labels = [];
    value = [];
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
            url: "../api/strategicIndicators/prediction",
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
                    texts.push(data[j].name);
                    labels.push([data[j].name]);
                    ids.push(data[j].id);
                    errors.push([data[j].forecastingError]);

                    data[j].probabilities.forEach(function (category) {
                        categories.push({
                            name: category.label,
                            color: category.color,
                            upperThreshold: category.upperThreshold
                        });
                    });
                }
                while (data[j]) {
                    //check if we are still on the same Strategic Indicator
                    if (data[j].id !== last) {
                        value.push([line]);
                        line = [];
                        last = data[j].id;
                        texts.push(data[j].name);
                        labels.push([data[j].name]);
                        ids.push(data[j].id);
                        errors.push([data[j].forecastingError]);
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
                    value.push([line]);
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
    console.log(value);
    console.log(texts);
}

window.onload = function() {
    getData();
};
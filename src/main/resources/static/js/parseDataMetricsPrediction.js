var isSi = false;
var isdsi = false;
var isqf = false;

var urlpred; // to get prediction data
var urlhist; // to get historical data
if (getParameterByName('id').length !== 0) {
    urlpred = parseURLMetrics("../api/qualityFactors/metrics/prediction");
    urlhist = parseURLMetrics("../api/qualityFactors/metrics/historical");
} else {
    urlpred = parseURLMetrics("../api/metrics/prediction");
    urlhist = parseURLMetrics("../api/metrics/historical");
}

//initialize data vectors
var texts = [];
var labels = [];
var value = [];
var errors = [];
var categories = [];

function getCurrentDate() {
    // get current data and prepare datapickers
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (metrics) {
            sessionStorage.setItem("currentDate", metrics[0].date);
            getData();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 409)
                alert("Your datasource and DB categories IDs do not match.");
            else if (jqXHR.status == 400)
                alert("Datasource connection failed.");
        }
    });
}

function getData() {
    document.getElementById("loader").style.display = "block";
    document.getElementById("chartContainer").style.display = "none";
    texts = [];
    labels = [];
    value = [];
    errors = [];
    // use configured datapickers for forecast
    var technique = $("#selectedTechnique").text();
    var date1 = new Date($('#datepickerFrom').val());
    var date2 = new Date($('#datepickerTo').val());
    var timeDiff = date2.getTime() - date1.getTime();
    var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
    if (diffDays < 1) {
        alert('To date has to be bigger than from date');
    } else {
        //get predicted data from API
        jQuery.ajax({
            dataType: "json",
            url: urlpred,
            data: {
                "technique": technique,
                "horizon": diffDays
            },
            cache: false,
            type: "GET",
            async: true,
            success: function (data) {
                console.log("Data Prediction M");
                console.log(data);
                //get historical data from API
                /*jQuery.ajax({
                    dataType: "json",
                    url: urlhist,
                    data: {
                        "from": $('#datepickerFrom').val(),
                        "to": parseDate(d.setDate(d.getDate()-1)),
                    },
                    cache: false,
                    type: "GET",
                    async: true,
                    success: function (data) {
                        console.log("Historical Data M");
                        console.log(data);
                    }});*/

                j = 0;
                var line = [];
                var line80l = [];
                var line80u = [];
                var line95l = [];
                var line95u = [];
                if (data[j]) {
                    last = data[j].id;
                    texts.push(data[j].name);
                    labels.push([data[j].name, "80", "80", "95", "95"]);
                    errors.push([data[j].forecastingError]);
                }
                while (data[j]) {
                    //check if we are still on the same metric
                    if (data[j].id !== last) {
                        value.push([line, line80l, line80u, line95l, line95u]);
                        line = [];
                        line80l = [];
                        line80u = [];
                        line95l = [];
                        line95u = [];
                        last = data[j].id;
                        texts.push(data[j].name);
                        labels.push([data[j].name, "80", "80", "95", "95"]);
                        errors.push([data[j].forecastingError]);
                    }
                    //push date and value to line vector
                    if (!isNaN(data[j].value)) {
                        if (data[j].value !== null) {
                            line.push({
                                x: data[j].date,
                                y: data[j].value
                            });
                            line80l.push({
                                 x: data[j].date,
                                 y: data[j].confidence80.second
                            });
                            line80u.push({
                                 x: data[j].date,
                                 y: data[j].confidence80.first
                            });
                            line95l.push({
                                 x: data[j].date,
                                 y: data[j].confidence95.second
                            });
                            line95u.push({
                                 x: data[j].date,
                                 y: data[j].confidence95.first
                            });
                        }
                    }
                    ++j;
                }
                //push line vector to values vector for the last metric
                if (data[j - 1]) {
                    value.push([line, line80l, line80u, line95l, line95u]);
                }
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                getMetricsCategories();
                //drawChart();
            },
            error: function (xhr, ajaxOptions, thrownError) {
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                document.getElementById("chartContainer").innerHTML = "Error " + xhr.status;
            }
        });
    }
    console.log(value);
    console.log(texts);

}

function getMetricsCategories () {
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

function parseDate(date) {
    var date = new Date(date);
    var dd = date.getDate();
    var mm = date.getMonth() + 1; //January is 0!
    var yyyy = date.getFullYear();

    if(dd < 10) {
        dd = '0' + dd;
    }
    if(mm < 10) {
        mm = '0' + mm;
    }

    var stringDate = yyyy + '-' + mm + '-' + dd;
    return stringDate
}

window.onload = function() {
    getData();
};
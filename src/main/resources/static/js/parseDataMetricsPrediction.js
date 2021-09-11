var isSi = false;
var isdsi = false;
var isqf = false;
var isdqf = false;

var urlpred; // to get prediction data
var urlhist; // to get historical data
if (getParameterByName('id').length !== 0) {
    urlpred = parseURLComposed("../api/qualityFactors/metrics/prediction");
    urlhist = parseURLComposed("../api/qualityFactors/metrics/historical");
} else {
    var profileId = sessionStorage.getItem("profile_id");
    urlpred = parseURLComposed("../api/metrics/prediction?profile="+profileId);
    urlhist = parseURLComposed("../api/metrics/historical?profile="+profileId);
}

//initialize data vectors
var texts = [];
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
    errors = [];
    // use configured datapickers for forecast
    var technique = $("#selectedTechnique").text();
    var dateFrom = new Date($('#datepickerFrom').val());
    var dateC = new Date($('#datepickerCurrentDate').val());
    var dateTo = new Date($('#datepickerTo').val());
    var timeDiff = dateTo.getTime() - dateC.getTime();
    var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
    if (diffDays < 1) {
        warningUtils("Warning", "To date has to be bigger than from date");
        //alert('To date has to be bigger than from date');
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
            success: function (response) {
                var data = response;
                if (getParameterByName('id').length !== 0) {
                    data = response[0].metrics;
                }
                sortDataAlphabetically(data);
                //get historical data from API
                jQuery.ajax({
                    dataType: "json",
                    url: urlhist,
                    data: {
                        "from": parseDate(dateFrom),
                        "to": parseDate(dateC)
                    },
                    cache: false,
                    type: "GET",
                    async: true,
                    success: function (response) {
                        var data_hist = response;
                        if (getParameterByName('id').length !== 0) {
                            data_hist = response[0].metrics;
                        }
                        sortDataAlphabetically(data_hist);
                        j = 0;
                        var line_hist = [];
                        // generate historical serie of values
                        if (data_hist[j]) {
                            last = data_hist[j].id;
                            texts.push(data_hist[j].name);
                            labels.push([data_hist[j].name]);
                        }
                        while (data_hist[j]) {
                            //check if we are still on the same metric
                            if (data_hist[j].id != last) {
                                var val = [line_hist];
                                value.push(val);
                                line_hist = [];
                                last = data_hist[j].id;
                                texts.push(data_hist[j].name);
                                var labelsForOneChart = [];
                                labelsForOneChart.push(data_hist[j].name);
                                labels.push(labelsForOneChart);
                            }
                            //push date and value to line vector
                            if (!isNaN(data_hist[j].value)) {
                                line_hist.push({
                                    x: data_hist[j].date,
                                    y: data_hist[j].value
                                });
                            }
                            ++j;
                        }
                        //push line vector to values vector for the last metric
                        if (data_hist[j - 1]) {
                            var val = [line_hist];
                            value.push(val);
                        }
                        // add prediction series generated
                        j = 0;
                        x = 0;
                        var line = [];
                        var line80l = [];
                        var line80u = [];
                        var line95l = [];
                        var line95u = [];
                        if (data[j]) {
                            last = data[j].id;
                            labels[x].push("Predicted data", "80", "80", "95", "95");
                            errors.push([data[j].forecastingError]);
                        }
                        while (data[j]) {
                            //check if we are still on the same metric
                            if (data[j].id !== last) {
                                value[x].push(line, line80l, line80u, line95l, line95u);
                                line = [];
                                line80l = [];
                                line80u = [];
                                line95l = [];
                                line95u = [];
                                last = data[j].id;
                                x++;
                                labels[x].push("Predicted data", "80", "80", "95", "95");
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
                            value[x].push(line, line80l, line80u, line95l, line95u);
                        }
                        document.getElementById("loader").style.display = "none";
                        document.getElementById("chartContainer").style.display = "block";
                        getMetricsCategories();
                    }});
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    document.getElementById("loader").style.display = "none";
                    document.getElementById("chartContainer").style.display = "block";
                    document.getElementById("chartContainer").innerHTML = "Error " + xhr.status;
                }
            });
        }
    }

function sortDataAlphabetically (data) {
    function compare (a, b) {
        if (a.name < b.name) return -1;
        else if (a.name > b.name) return 1;
        else return 0;
    }
    data.sort(compare);
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
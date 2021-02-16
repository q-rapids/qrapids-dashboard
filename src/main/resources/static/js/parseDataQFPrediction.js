var isSi = false;
var isdsi = false;
var isqf = true;
var isdqf = false;

var urlpred; // to get prediction data
var urlhist; // to get historical data
if (getParameterByName('id').length !== 0) {
    urlpred = parseURLSimple("../api/strategicIndicators/qualityFactors/prediction");
    urlhist =parseURLSimple("../api/strategicIndicators/qualityFactors/historical");
} else {
    var profileId = sessionStorage.getItem("profile_id");
    urlpred = parseURLSimple("../api/qualityFactors/prediction?profile="+profileId);
    urlhist = parseURLSimple("../api/qualityFactors/historical?profile="+profileId);
}

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
    var dateFrom = new Date($('#datepickerFrom').val());
    var dateC = new Date($('#datepickerCurrentDate').val());
    var dateTo = new Date($('#datepickerTo').val());
    var timeDiff = dateTo.getTime() - dateC.getTime();
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
            success: function (response) {
                var data = response;
                if (getParameterByName('id').length !== 0) {
                    data = response[0].factors;
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
                            data_hist = response[0].factors;
                        }
                        sortDataAlphabetically(data_hist);
                        j = 0;
                        var line_hist = [];
                        // generate historical serie of values
                        if (data_hist[j]) {
                            last = data_hist[j].id;
                            texts.push(data_hist[j].name);
                            labels.push([data_hist[j].name]);
                            ids.push(data_hist[j].id);
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
                                ids.push(data_hist[j].id);
                            }
                            //push date and value to line vector
                            if (!isNaN(data_hist[j].value.first)) {
                                line_hist.push({
                                    x: data_hist[j].date,
                                    y: data_hist[j].value.first
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
                            if (!isNaN(data[j].value.first)) {
                                if (data[j].value.first !== null) {
                                    line.push({
                                        x: data[j].date,
                                        y: data[j].value.first
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
                        getFactorsCategories();
                    }
                });
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 409)
                    alert("Your datasource and DB categories IDs do not match.");
                else if (jqXHR.status == 400)
                    alert("Datasource connection failed.");
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                document.getElementById("chartContainer").innerHTML = "Error " + jqXHR.status;
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

function getFactorsCategories () {
    jQuery.ajax({
        url: "../api/qualityFactors/categories",
        type: "GET",
        async: true,
        success: function (response) {
            categories = response;
            drawChart();
        }
    });
}

window.onload = function() {
    getData();
};
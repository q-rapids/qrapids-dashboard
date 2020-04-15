var isdsi = true;
var isqf = false;

var url_hist = parseURLSimple("../api/strategicIndicators/qualityFactors/historical");
var url_pred = parseURLSimple("../api/strategicIndicators/qualityFactors/prediction");

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
    ids = [];
    labels = [];
    value = [];
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
            url: url_pred,
            data: {
                "technique": technique,
                "horizon": diffDays
            },
            cache: false,
            type: "GET",
            async: true,
            success: function (data) {
                //get historical data from API
                jQuery.ajax({
                    dataType: "json",
                    url: url_hist,
                    data: {
                        "from": parseDate(dateFrom),
                        "to": parseDate(dateC)
                    },
                    cache: false,
                    type: "GET",
                    async: true,
                    success: function (data_hist) {
                        // generate historical serie of values
                        for (var i = 0; i < data_hist.length; ++i) {
                            // order data
                            data_hist[i].factors.sort((a,b) => (a.name > b.name) ? 1 : ((b.name > a.name) ? -1 : 0));
                            //for each dsi save name to texts vector and id to ids vector
                            if (data_hist[i].factors.length > 0) {
                                texts.push(data_hist[i].name);
                                ids.push(data_hist[i].id);

                                value.push([[]]);
                                last = data_hist[i].factors[0].id;
                                labels.push([data_hist[i].factors[0].name]);
                                k = 0;
                                for (j = 0; j < data_hist[i].factors.length; ++j) {
                                    //check if we are still on the same factor
                                    if (last != data_hist[i].factors[j].id) {
                                        labels[i].push(data_hist[i].factors[j].name);
                                        last = data_hist[i].factors[j].id;
                                        ++k;
                                        value[i].push([]);
                                    }
                                    //push date and value to values vector
                                    if (!isNaN(data_hist[i].factors[j].value))
                                    {
                                        value[i][k].push(
                                            {
                                                x: data_hist[i].factors[j].date,
                                                y: data_hist[i].factors[j].value
                                            }
                                        );
                                    }
                                }
                            } else {
                                data.splice(i, 1);
                                --i;
                            }
                        }
                        // add prediction series generated
                        for (i = 0; i < data.length; ++i) {
                            // order data
                            data[i].factors.sort((a,b) => (a.name > b.name) ? 1 : ((b.name > a.name) ? -1 : 0));
                            //for each dsi save name to texts vector and id to ids vector
                            if (data[i].factors.length > 0) {
                                value[i].push([]);
                                last = data[i].factors[0].id;
                                labels[i].push("Predicted "+data[i].factors[0].name);
                                errors.push([data[i].factors[0].forecastingError]);
                                k = value[i].length-1;
                                for (j = 0; j < data[i].factors.length; ++j) {
                                    //check if we are still on the same factor
                                    if (last != data[i].factors[j].id) {
                                        labels[i].push("Predicted "+data[i].factors[j].name);
                                        last = data[i].factors[j].id;
                                        ++k;
                                        value[i].push([]);
                                        errors[i].push(data[i].factors[j].forecastingError);
                                    }
                                    //push date and value to values vector
                                    if (!isNaN(data[i].factors[j].value)) {
                                        if (data[i].factors[j].value !== null) {
                                            value[i][k].push(
                                                {
                                                    x: data[i].factors[j].date,
                                                    y: data[i].factors[j].value
                                                }
                                            );
                                        }
                                    }
                                }
                            } else {
                                data.splice(i, 1);
                                --i;
                            }
                        }
                        document.getElementById("loader").style.display = "none";
                        document.getElementById("chartContainer").style.display = "block";
                        getFactorsCategories();
                    }
                });
            },
            error: function (xhr, ajaxOptions, thrownError) {
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                document.getElementById("chartContainer").innerHTML = "Error " + xhr.status;
            }
        });
    }
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
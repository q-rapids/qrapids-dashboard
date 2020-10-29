var isSi = false;
var isdsi = false;
var isqf = false;
var isdqf = true;

var url = parseURLComposed("../api/qualityFactors/metrics/prediction");


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
                sortDataAlphabetically(data);
                if (getParameterByName('id').length !== 0) {
                    texts.push(getParameterByName('name'));
                    ids.push(getParameterByName('id'));

                    value.push([[]]);
                    last = data[0].id;
                    labels.push([data[0].name]);
                    errors.push([data[0].forecastingError]);
                    k = 0;
                    for (j = 0; j < data.length; ++j) {
                        //check if we are still on the same metric
                        if (last != data[j].id) {
                            labels[0].push(data[j].name);
                            last = data[j].id;
                            ++k;
                            value[0].push([]);
                            errors[0].push(data[j].forecastingError);
                        }
                        //push date and value to values vector
                        if (!isNaN(data[j].value)) {
                            if (data[j].value !== null) {
                                value[0][k].push(
                                    {
                                        x: data[j].date,
                                        y: data[j].value
                                    }
                                );
                            }
                        }
                    }
                } else {

                    for (i = 0; i < data.length; ++i) {
                        //for each qf save name to texts vector and id to ids vector
                        if (data[i].metrics.length > 0) {
                            texts.push(data[i].name);
                            ids.push(data[i].id);

                            value.push([[]]);
                            last = data[i].metrics[0].id;
                            labels.push([data[i].metrics[0].name]);
                            errors.push([data[i].metrics[0].forecastingError]);
                            k = 0;
                            for (j = 0; j < data[i].metrics.length; ++j) {
                                //check if we are still on the same metric
                                if (last != data[i].metrics[j].id) {
                                    labels[i].push(data[i].metrics[j].name);
                                    last = data[i].metrics[j].id;
                                    ++k;
                                    value[i].push([]);
                                    errors[i].push(data[i].metrics[j].forecastingError);
                                }
                                //push date and value to values vector
                                if (!isNaN(data[i].metrics[j].value)) {
                                    if (data[i].metrics[j].value !== null) {
                                        value[i][k].push(
                                            {
                                                x: data[i].metrics[j].date,
                                                y: data[i].metrics[j].value
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

                }


                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                getMetricsCategories();
            },
            error: function (xhr, ajaxOptions, thrownError) {
                document.getElementById("loader").style.display = "none";
                document.getElementById("chartContainer").style.display = "block";
                document.getElementById("chartContainer").innerHTML = "Error " + xhr.status;
            }
        });
    }
    console.log(texts);
    console.log(labels);
    console.log(value);
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

window.onload = function() {
    getData();
};
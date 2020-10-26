var isSi = false;
var isdsi = false;
var isqf = false;
var isdqf = false;

var url;
if (getParameterByName('id').length !== 0) {
    url = parseURLComposed("../api/qualityFactors/metrics/prediction");
} else {
    url = parseURLComposed("../api/metrics/prediction");
}

//initialize data vectors
var texts = [];
var labels = [];
var value = [];
//var lower80 = [];
//var lower95 = [];
//var upper80 = [];
//var upper95 = [];
var errors = [];
var categories = [];

function getData() {
    document.getElementById("loader").style.display = "block";
    document.getElementById("chartContainer").style.display = "none";
    texts = [];
    labels = [];
    value = [];
    //lower80 = [];
    //lower95 = [];
    //upper80 = [];
    //upper95 = [];
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
                j = 0;
                var line = [];
                //var line80l = [];
                //var line80u = [];
                //var line95l = [];
                //var line95u = [];
                if (data[j]) {
                    last = data[j].id;
                    texts.push(data[j].name);
                    labels.push([data[j].name]);
                    errors.push([data[j].forecastingError]);
                }
                while (data[j]) {
                    //check if we are still on the same metric
                    if (data[j].id !== last) {
                        value.push([line]);
                        //lower80.push(line80l);
                        //upper80.push(line80u);
                        //lower95.push(line95l);
                        //upper95.push(line95u);
                        line = [];
                        //line80l = [];
                        //line80u = [];
                        //line95l = [];
                        //line95u = [];
                        last = data[j].id;
                        texts.push(data[j].name);
                        labels.push([data[j].name]);
                        errors.push([data[j].forecastingError]);
                    }
                    //push date and value to line vector
                    if (!isNaN(data[j].value)) {
                        if (data[j].value !== null) {
                            line.push({
                                x: data[j].date,
                                y: data[j].value
                            });
                            // line80l.push({
                            //     x: data[j].date,
                            //     y: data[j].confidence80.second
                            // });
                            // line80u.push({
                            //     x: data[j].date,
                            //     y: data[j].confidence80.first
                            // });
                            // line95l.push({
                            //     x: data[j].date,
                            //     y: data[j].confidence95.second
                            // });
                            // line95u.push({
                            //     x: data[j].date,
                            //     y: data[j].confidence95.first
                            // });
                        }
                    }
                    ++j;
                }
                //push line vector to values vector for the last metric
                if (data[j - 1]) {
                    value.push([line]);
                    // lower80.push(line80l);
                    // upper80.push(line80u);
                    // lower95.push(line95l);
                    // upper95.push(line95u);
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
    console.log(value);
    console.log(texts);

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
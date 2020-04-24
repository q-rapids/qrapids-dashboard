var today = new Date();

var param_from = getParameterByName('from');
var param_to = getParameterByName('to');

var config = {
    format: 'yyyy-mm-dd',
    weekStartDay: 1,
    calendarWeeks: true,
    uiLibrary: 'bootstrap',
    iconsLibrary: 'fontawesome'
};

var currentURL = window.location.href;
if (currentURL.search("/Historic") !== -1) {
    configureHistoric();
} else if (currentURL.search("/Prediction") !== -1) {
    configurePrediction();
}

function configureHistoric () {
    config.maxDate = today;
    $('#datepickerFrom').datepicker(config);
    $('#datepickerTo').datepicker(config);

    $('#intervalsDropdown').append('<li><a onclick="thisWeek();$(\'#chartContainer\').empty();getData()" href="#">This week</a></li>');
    $('#intervalsDropdown').append('<li><a onclick="last7Days();$(\'#chartContainer\').empty();getData()" href="#">Last 7 days</a></li>');
    $('#intervalsDropdown').append('<li><a onclick="last14Days();$(\'#chartContainer\').empty();getData()" href="#">Last 14 days</a></li>');
    $('#intervalsDropdown').append('<li><a onclick="thisMonth();$(\'#chartContainer\').empty();getData()" href="#">This month</a></li>');
    $('#intervalsDropdown').append('<li><a onclick="thisYear();$(\'#chartContainer\').empty();getData()" href="#">This year</a></li>');

    var historicFrom;
    var historicTo;
    // we are navegating -> no params
    if (param_from.length == 0 && param_to.length==0) {
        historicFrom=sessionStorage.getItem("historicFromDate");
        historicTo=sessionStorage.getItem("historicToDate");
        if (!historicFrom || !historicTo)
            last14Days(); // Default
    }
    else if (param_from.length > 0 && param_to.length>0) {
        historicFrom=param_from;
        historicTo=param_to;
    }
    else if (param_to.length==0) { // from filled, to empty
        historicFrom=param_from;
        historicTo=parseDate(today);
    }
    else {// from empty, to filled --> 14 days before, like in the default case when it shows 14 days
        var d = param_to.split("-");
        var date_to = new Date(d[0], d[1]-1, d[2]); // January is 0!
        var dateOffset = (24*60*60*1000) * 14; //14 days
        var date = new Date().setTime(date_to.getTime() - dateOffset);
        historicFrom=parseDate(date);
        historicTo=param_to;
    }

    $('#datepickerFrom').datepicker().value(historicFrom);
    $('#datepickerTo').datepicker().value(historicTo);

    // hide prediction related parts
    $('#techniqueDropdownDiv').hide();
    $('#current_dateDiv').hide();
    $('#showConfidenceDiv').hide();
}

async function configurePrediction () {
    getPredictionCurrentDate();

    config.maxDate = parseDate(sessionStorage.getItem("predictionCurrentDate"));
    $('#datepickerFrom').datepicker(config);
    config.maxDate = null; // necessary to config min date correctly
    var d = new Date(sessionStorage.getItem("predictionCurrentDate"));
    var dateMin = d.setDate(d.getDate() + 1);
    config.minDate = parseDate(dateMin);
    $('#datepickerCurrentDate').datepicker(config);
    $('#datepickerTo').datepicker(config);

    $('#intervalsDropdown').append('<li><a onclick="next7Days();$(\'#chartContainer\').empty();getData()" href="#">Next 7 days</a></li>');
    $('#intervalsDropdown').append('<li><a onclick="next14Days();$(\'#chartContainer\').empty();getData()" href="#">Next 14 days</a></li>');


    var predictionFrom = sessionStorage.getItem("predictionFromDate");
    if (!predictionFrom)
        back14Days();
    else $('#datepickerFrom').datepicker().value(predictionFrom);

    var predictionCurrent = sessionStorage.getItem("predictionCurrentDate");
    $('#datepickerCurrentDate').datepicker().value(predictionCurrent);
    $('#current_dateDiv').show();
    $('#datepickerCurrentDate').prop("disabled",true);
    $('#current_dateDiv').find("span").css("pointer-events", "none");

    var predictionTo = sessionStorage.getItem("predictionToDate");
    if (!predictionTo)
        next7Days();
    else $('#datepickerTo').datepicker().value(predictionTo);

    $('#techniqueDropdownDiv').show();
    loadTechniques();

    $('#showConfidenceDiv').show();
}

function thisWeek() {
    var todayTextDate = parseDate(today);
    $('#datepickerTo').datepicker().value(todayTextDate);
    sessionStorage.setItem("historicToDate", todayTextDate);

    var monday = getPreviousMonday();
    var textDate = parseDate(monday);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("historicFromDate", textDate);
}

function getPreviousMonday() {
    var day = today.getDay();
    var prevMonday;
    if(today.getDay() === 1){
        prevMonday = today;
    }
    else{
        prevMonday = new Date().setDate(today.getDate() - day + 1);
    }
    return prevMonday;
}

function loadTechniques () {
    jQuery.ajax({
        dataType: "json",
        url: "../api/forecastTechniques",
        cache: false,
        type: "GET",
        async: true,
        success: function (techniques) {
            for (i = 0; i < techniques.length; i++) {
                $("#techniqueDropdown").append('<li><a onclick="setTechnique(\''+techniques[i]+'\')" href="#">'+ techniques[i] +'</a></li>');
            }
        }
    });

    var lastUsedTechnique = sessionStorage.getItem("forecastingTechnique");
    if (lastUsedTechnique != null) {
        setTechnique(lastUsedTechnique);
    }
}

function setTechnique(technique) {
    sessionStorage.setItem("forecastingTechnique", technique);
    $("#selectedTechnique").text(technique);
}

function getPredictionCurrentDate() {
    var saved = sessionStorage.getItem("predictionCurrentDate"); // current actual saved in sessionStorage
    // set current datapicker for each case
    if (currentURL.match("/Metrics/PredictionChart")) {
        jQuery.ajax({
            dataType: "json",
            url: "../api/metrics/currentDate",
            cache: false,
            type: "GET",
            async: false,
            success: function (date) {
                console.log(date);
                if (saved !== date) {
                    sessionStorage.setItem("predictionCurrentDate", date);
                    sessionStorage.removeItem("predictionToDate");
                    sessionStorage.removeItem("predictionFromDate");
                }
            }
        });
    } else if (currentURL.match("/QualityFactors/PredictionChart")) {
        jQuery.ajax({
            dataType: "json",
            url: "../api/qualityFactors/currentDate",
            cache: false,
            type: "GET",
            async: false,
            success: function (date) {
                console.log(date);
                if (saved !== date) {
                    sessionStorage.setItem("predictionCurrentDate", date);
                    sessionStorage.removeItem("predictionToDate");
                    sessionStorage.removeItem("predictionFromDate");
                }
            }
        });
    } else if (currentURL.match("/StrategicIndicators/PredictionChart") || currentURL.match("/DetailedStrategicIndicators/PredictionChart")) {
        jQuery.ajax({
            dataType: "json",
            url: "../api/strategicIndicators/currentDate",
            cache: false,
            type: "GET",
            async: false,
            success: function (date) {
                console.log(date);
                if (saved !== date) {
                    sessionStorage.setItem("predictionCurrentDate", date);
                    sessionStorage.removeItem("predictionToDate");
                    sessionStorage.removeItem("predictionFromDate");
                }
            }
        });
    }
}

$("#applyButton").click(function () {
    $('#chartContainer').empty();
    getData();
    var currentURL = window.location.href;
    if (currentURL.match("/PredictionChart")) {
        // set from and to datapickers
        var predictionTo = $('#datepickerTo').datepicker().val();
        sessionStorage.setItem("predictionToDate", predictionTo);
        var predictionFrom = $('#datepickerFrom').datepicker().val();
        sessionStorage.setItem("predictionFromDate", predictionFrom);
    } else {
        var historicFrom = $('#datepickerFrom').datepicker().val();
        sessionStorage.setItem("historicFromDate", historicFrom);

        var historicTo = $('#datepickerTo').datepicker().val();
        sessionStorage.setItem("historicToDate", historicTo);
    }
});

//Historic intervals

function last7Days() {
    var todayTextDate = parseDate(today);
    $('#datepickerTo').datepicker().value(todayTextDate);
    sessionStorage.setItem("historicToDate", todayTextDate);

    var date = new Date().setDate(today.getDate() - 7);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("historicFromDate", textDate);
}

function last14Days() {
    var todayTextDate = parseDate(today);
    $('#datepickerTo').datepicker().value(todayTextDate);
    sessionStorage.setItem("historicToDate", todayTextDate);

    var date = new Date().setDate(today.getDate() - 14);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("historicFromDate", textDate);
}

function thisMonth() {
    var todayTextDate = parseDate(today);
    $('#datepickerTo').datepicker().value(todayTextDate);
    sessionStorage.setItem("historicToDate", todayTextDate);

    var date = new Date(today.getFullYear(), today.getMonth(), 1);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("historicFromDate", textDate);
}

function thisYear() {
    var todayTextDate = parseDate(today);
    $('#datepickerTo').datepicker().value(todayTextDate);
    sessionStorage.setItem("historicToDate", todayTextDate);

    var date = new Date(today.getFullYear(), 0, 1);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("historicFromDate", textDate);
}

// Prediction intervals

function next7Days () {
    var d = new Date(sessionStorage.getItem("predictionCurrentDate"));
    var date = d.setDate(d.getDate() + 7);
    var textDate = parseDate(date);
    $('#datepickerTo').datepicker().value(textDate);
    sessionStorage.setItem("predictionToDate", textDate);
    back14Days ();
}

function back14Days () {
    var d = new Date(sessionStorage.getItem("predictionCurrentDate"));
    var date = d.setDate(d.getDate() - 14);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("predictionFromDate", textDate);
}

function next14Days () {
    var d = new Date(sessionStorage.getItem("predictionCurrentDate"));
    var date = d.setDate(d.getDate() + 14);
    var textDate = parseDate(date);
    $('#datepickerTo').datepicker().value(textDate);
    sessionStorage.setItem("predictionToDate", textDate);
    back28Days ();
}

function back28Days () {
    var d = new Date(sessionStorage.getItem("predictionCurrentDate"));
    var date = d.setDate(d.getDate() - 28);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
    sessionStorage.setItem("predictionFromDate", textDate);
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



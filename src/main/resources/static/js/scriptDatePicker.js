var today = new Date();

var from = getParameterByName('from');
var to = getParameterByName('to');

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

    if (from.length == 0)
        last14Days();
    else $('#datepickerFrom').datepicker().value(from);

    if (to.length == 0)
        to = parseDate(today);
    $('#datepickerTo').datepicker().value(to);
}

function configurePrediction () {
    config.minDate = today;
    $('#datepickerFrom').datepicker(config);
    $('#datepickerTo').datepicker(config);

    $('#intervalsDropdown').append('<li><a onclick="next7Days();$(\'#chartContainer\').empty();getData()" href="#">Next 7 days</a></li>');
    $('#intervalsDropdown').append('<li><a onclick="next14Days();$(\'#chartContainer\').empty();getData()" href="#">Next 14 days</a></li>');

    from = parseDate(today);
    $('#datepickerFrom').datepicker().value(from);
    $('#datepickerFrom').prop("disabled",true);
    $('#fromDiv').find("span").css("pointer-events", "none");

    next7Days()
}

function thisWeek() {
    var monday = getPreviousMonday();
    var textDate = parseDate(monday);
    $('#datepickerFrom').datepicker().value(textDate);
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

//Historic intervals

function last7Days() {
    var date = new Date().setDate(today.getDate() - 7);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
}

function last14Days() {
    var date = new Date().setDate(today.getDate() - 14);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
}

function thisMonth() {
    var date = new Date(today.getFullYear(), today.getMonth(), 1);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
}

function thisYear() {
    var date = new Date(today.getFullYear(), 0, 1);
    var textDate = parseDate(date);
    $('#datepickerFrom').datepicker().value(textDate);
}

// Prediction intervals

function next7Days () {
    var date = new Date().setDate(today.getDate() + 7);
    var textDate = parseDate(date);
    $('#datepickerTo').datepicker().value(textDate);
}

function next14Days () {
    var date = new Date().setDate(today.getDate() + 14);
    var textDate = parseDate(date);
    $('#datepickerTo').datepicker().value(textDate);
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



// $('#datepickerFrom').datepicker().on('changeDate', function (ev) {
//     $('#datepickerFrom').change();
// });
//
// $('#datepickerTo').datepicker().on('changeDate', function (ev) {
//     $('#datepickerTo').change();
// });
//
// $('#apply').change(function () {
//     $('#chartContainer').empty();
//     getData();
//     drawChart();
// });
//
// $('#datepickerTo').change(function () {
//     $('#chartContainer').empty();
//     getData();
//     drawChart();
// });

// $('#datepickerFrom').prop('readonly', true);
// $('#datepickerTo').prop('readonly', true);

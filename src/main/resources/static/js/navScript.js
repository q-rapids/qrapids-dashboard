var currentURL = window.location.href;
var viewMode, time, assessment, prediction;

var serverUrl = null;
if (!(serverUrl = sessionStorage.getItem("serverUrl"))) {
    jQuery.ajax({
        dataType: "json",
        url: "../api/serverUrl",
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            serverUrl = data.serverUrl;
            sessionStorage.setItem("serverUrl", serverUrl);
            checkAlertsPending();
        }
    })
}
else {
    connect();
    checkAlertsPending();
}

// Load state from sessionStorage
// If missing, set default values
if (!(viewMode = sessionStorage.getItem("viewMode"))) {
    viewMode = "Chart";
}
if (!(time = sessionStorage.getItem("time"))) {
    time = "Current";
}
if (!(assessment = sessionStorage.getItem("assessment"))) {
    assessment = "StrategicIndicators";
}
if (!(prediction = sessionStorage.getItem("prediction"))) {
    prediction = "StrategicIndicators";
}

///////////////////////////////////////////////////////////////////
///// Customising View Mode options ///////////////////////////////
///////////////////////////////////////////////////////////////////

// 1.- Customising the ViewMode buttons depending on the selected option in the main menu
// This part also fills the variables viewMode and Time used bellow
if (currentURL.search("/HistoricTable") !== -1) {
    viewMode = "Table";
    time = "Historic";
} else if (currentURL.search("/CurrentTable") !== -1) {
    viewMode = "Table";
    time = "Current";
} else if (currentURL.search("/HistoricChart") !== -1) {
    viewMode = "Chart";
    time = "Historic";
} else if (currentURL.search("/CurrentChart") !== -1){
    viewMode = "Chart";
    time = "Current";
}

//Store state in sessionStorage
sessionStorage.setItem("viewMode", viewMode);
sessionStorage.setItem("time", time);


// 2.- Highlighting the enabled options depending on the View Mode and Time options selected
$("#" + viewMode).css("background-color", "#ffc380");
$("#" + time).css("background-color", "#ffc380");


// 3.- Checking the enabled options depending on the selected option on the main menu
var id;
if (currentURL.search("/StrategicIndicators/") !== -1 || currentURL.search("/EditStrategicIndicators/") !== -1) {
    id = "StrategicIndicators";
    if (currentURL.search("/Prediction") !== -1)
        highlightAndSaveCurrentPrediction(id);
    else
        highlightAndSaveCurrentAssessment(id);
} else if (currentURL.search("/DetailedStrategicIndicators/") !== -1) {
    id = "DetailedStrategicIndicators";
    if (currentURL.search("/Prediction") !== -1)
        highlightAndSaveCurrentPrediction(id);
    else
        highlightAndSaveCurrentAssessment(id);
} else if (currentURL.search("/QualityFactors/") !== -1) {
    id = "QualityFactors";
    if (currentURL.search("/Prediction") !== -1)
        highlightAndSaveCurrentPrediction(id);
    else
        highlightAndSaveCurrentAssessment(id);
} else if (currentURL.search("/Metrics/") !== -1) {
    id = "Metrics";
    if (currentURL.search("/Prediction") !== -1)
        highlightAndSaveCurrentPrediction(id);
    else
        highlightAndSaveCurrentAssessment(id);
} else if (currentURL.search("/Simulation") !== -1) {
    id = "Simulation";
    highlight(id);
} else if (currentURL.search("/QualityAlerts") !== -1) {
    id = "QualityAlerts";
    highlight(id);
} else if (currentURL.search("/Products") !== -1) {
    id = "Products";
    highlight(id);
} else if (currentURL.search("/Products/evaluation") !== -1) {
    id = "ProductsAssess";
    highlight(id);
}

function highlightAndSaveCurrentAssessment (id) {
    var assessmentButton = $("#Assessment");
    assessmentButton.css("background-color", "#eeeeee");
    assessmentButton.css("color", "black");
    highlight(id+"Assessment");
    sessionStorage.setItem("assessment", id);
    assessment = id;
}

function highlightAndSaveCurrentPrediction (id) {
    var predictionButton = $("#Prediction");
    predictionButton.css("background-color", "#eeeeee");
    predictionButton.css("color", "black");
    highlight(id+"Prediction");
    sessionStorage.setItem("prediction", id);
    prediction = id;
}

function highlight (id) {
    var menuOption = $("#" + id);
    menuOption.css("background-color", "#eeeeee");
    menuOption.css("color", "black");
}


///////////////////////////////////////////////////////////////////
// Defining URLs for the navigation menu from Dashboard Template
// This check should be done after checking the View Mode options
//         --> time and viewMode variables filled
///////////////////////////////////////////////////////////////////

$("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);

$("#Prediction").attr("href", serverUrl + "/" + prediction + "/" + "PredictionChart");

$("#StrategicIndicatorsAssessment").attr("href", serverUrl + "/StrategicIndicators/" + time + viewMode);

$("#StrategicIndicatorsPrediction").attr("href", serverUrl + "/StrategicIndicators/PredictionChart");

$("#DetailedStrategicIndicatorsAssessment").attr("href", serverUrl + "/DetailedStrategicIndicators/" + time + viewMode);

$("#DetailedStrategicIndicatorsPrediction").attr("href", serverUrl + "/DetailedStrategicIndicators/PredictionChart");

$("#QualityFactorsAssessment").attr("href", serverUrl + "/QualityFactors/" + time + viewMode);

$("#QualityFactorsPrediction").attr("href", serverUrl + "/QualityFactors/PredictionChart");

$("#MetricsAssessment").attr("href", serverUrl + "/Metrics/" + time + viewMode);

$("#MetricsPrediction").attr("href", serverUrl + "/Metrics/PredictionChart");

$("#Simulation").attr("href", serverUrl + "/Simulation");

$("#QualityAlerts").attr("href", serverUrl + "/QualityAlerts");

$("#Products").attr("href", serverUrl+"/Products");

$("#ProductsAssess").attr("href", serverUrl+"/Products/evaluation");

$("#ProjectsAssess").attr("href", serverUrl+"/Products/detailedEvaluation");


function menuNav (urlNav) {
    var parameters = false;
    //add id and name to url if found
    var id = getParameterByName('id');
    if (id.length !== 0) {
        urlNav = urlNav + "?id=" + id;
        parameters = true;
        var name = getParameterByName('name');
        if (name.length !== 0) {
            urlNav = urlNav + "&name=" + name;
        }
        var si = getParameterByName('si');
        if (si.length !== 0) {
            urlNav = urlNav + "&si=" + si;
        }
        var siid = getParameterByName('siid');
        if (siid.length !== 0) {
            urlNav = urlNav + "&siid=" + siid;
        }
    }
    addDatesAndGo(urlNav, parameters);
}

function parseURLSimple(url) {
    //if url has parameters, set new url and set navegaibility text
    var id = getParameterByName('id');
    var name = getParameterByName('name');
    if (id.length !== 0) {
        url = url + "/" + id;
        if (currentURL.search("/Detailed") !== -1) {
            if (currentURL.match("/PredictionChart")) $('a#originSIQF').attr("href", "../StrategicIndicators/PredictionChart");
            $('a#originSIQF').text(name + ' (SI)');
            $('h1#title').text('Detailed ' + name + ' Strategic Indicator');
        }
        else {
            if (currentURL.match("/PredictionChart")) $('a#originSIQF').attr("onclick", "menuNav('../DetailedStrategicIndicators/PredictionChart')");
            $('a#originSIQF').text(name + ' (DSI)');
            $('h1#title').text('Factors for ' + name + ' Strategic Indicator');
        }
    }
    return url;
}

function parseURLMetrics(url) {
    //if url has parameters, set new url and set navegaibility text
    var id = getParameterByName('id');
    var name = getParameterByName('name');
    var si = getParameterByName('si');
    if (id.length !== 0) {
        url = url + "/" + id;
        if (si.length === 0)
            $('a#origin').text(name + ' (QF)');
        else {
            $('a#originSI').text(si + ' (DSI)');
            $('span#arrow').text('>');
            $('a#origin').text(name + ' (QF)');
        }
        $('h1#title').text('Metrics for ' + name + ' Factor');
    }
    return url;
}

function addDatesAndGo(urlNav, parameters) {
    //add from and to to url if found
    var from = getParameterByName('from');
    var to = getParameterByName('to');
    if (($('#datepickerFrom').length || (from.length !== 0 && to.length !== 0)) && time !== "Prediction") {
        //check if any previous parameters were added
        if (parameters)
            urlNav = urlNav + '&';
        else
            urlNav = urlNav + '?';

        //if possible use data from datepicker
        if ($('#datepickerFrom').length)
            urlNav = urlNav + "from=" + $('#datepickerFrom').val() + "&to=" + $('#datepickerTo').val();
        else
            urlNav = urlNav + "from=" + from + "&to=" + to;
    }
    location.href = urlNav;
}

function navBack(toDetailed) {
    var siid = getParameterByName('siid');
    var si = getParameterByName('si');
    if (currentURL.match("/PredictionChart")) {
        if (toDetailed)
            var urlNav = "../DetailedStrategicIndicators/PredictionChart";
        else
            var urlNav = "../QualityFactors/PredictionChart";
    }
    else {
        if (toDetailed)
            var urlNav = "../DetailedStrategicIndicators/" + time + viewMode;
        else
            var urlNav = "../QualityFactors/" + time + viewMode;
    }
    var parameters = false;
    if (siid.length !== 0 && si.length !== 0) {
        var urlNav = urlNav + "?id=" + siid + "&name=" + si;
        parameters = true;
    }
    addDatesAndGo(urlNav, parameters);
}
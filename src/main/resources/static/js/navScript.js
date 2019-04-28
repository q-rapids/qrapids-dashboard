var currentURL = window.location.href;
var viewMode, time, assessment, prediction, products, simulation, configuration, userName;

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

function getUserName () {
    jQuery.ajax({
        dataType: "json",
        url: serverUrl + "/api/me",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            sessionStorage.setItem("userName", data.userName);
            $("#MyProfile").text(data.userName);
        },
        error: function () {
            sessionStorage.setItem("userName", "undefined");
        }
    });
}
if (!(userName = sessionStorage.getItem("userName")))
    getUserName();
else if (userName !== "undefined")
    $("#MyProfile").text(userName);

function checkProducts () {
    jQuery.ajax({
        dataType: "json",
        url: serverUrl + "/api/products",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            if (data.length > 0)
                $("#Products").show();
            else
                $("#Products").hide();
        }
    });
}
checkProducts();

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
if (!(products = sessionStorage.getItem("products"))) {
    products = "Evaluation";
}
if (!(configuration = sessionStorage.getItem("configuration"))) {
    configuration = "Products";
}
if (!(simulation = sessionStorage.getItem("simulation"))) {
    simulation = "Factors";
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

// Highlighting the enabled options depending on the View Mode and Time options selected
$("#" + viewMode).css("background-color", "#ffc380");
$("#" + time).css("background-color", "#ffc380");


// Checking the enabled options depending on the selected option on the main menu
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
} else if (currentURL.search("/Simulation/Factors") !== -1) {
    id = "Factors";
    highlightAndSaveCurrentSimulation(id);
} else if (currentURL.search("/Simulation/Metrics") !== -1) {
    id = "Metrics";
    highlightAndSaveCurrentSimulation(id);
} else if (currentURL.search("/Simulation/QR") !== -1) {
    id = "QR";
    highlightAndSaveCurrentSimulation(id);
} else if (currentURL.search("/QualityAlerts") !== -1) {
    id = "QualityAlerts";
    highlight(id);
} else if (currentURL.search("/QualityRequirements") !== -1) {
    id = "QualityRequirements";
    highlight(id);
} else if (currentURL.search("/Decisions") !== -1) {
    id = "Decisions";
    highlight(id);
} else if (currentURL.search("/QualityModel") !== -1) {
    id = "QualityModel";
    highlightAndSaveCurrentAssessment(id);
    disableViewModeAndTimeOption();
} else if (currentURL.search("/Products/Evaluation") !== -1) {
    id = "Evaluation";
    highlightandSaveCurrentProducts(id);
} else if (currentURL.search("/Products/DetailedEvaluation") !== -1) {
    id = "DetailedEvaluation";
    highlightandSaveCurrentProducts(id);
} else {
    if (currentURL.match("/Products"))
        id = "Products";
    else if (currentURL.match("/profile"))
        id = "profile";
    else if (currentURL.match("/users"))
        id = "users";
    else if (currentURL.match("/usergroups"))
        id = "usergroups";
    highlightAndSaveCurrentConfiguration(id);
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

function highlightandSaveCurrentProducts (id) {
    var productsButton = $("#Products");
    productsButton.css("background-color", "#eeeeee");
    productsButton.css("color", "black");
    highlight("Products" + id);
    sessionStorage.setItem("products", id);
    products = id;
}

function highlightAndSaveCurrentConfiguration (id) {
    var profileButton = $("#Configuration");
    profileButton.css("background-color", "#eeeeee");
    profileButton.css("color", "black");
    highlight(id + "Config");
    sessionStorage.setItem("configuration", id);
    configuration = id;
}

function highlightAndSaveCurrentSimulation (id) {
    var simulationButton = $("#Simulation");
    simulationButton.css("background-color", "#eeeeee");
    simulationButton.css("color", "black");
    highlight(id+"Simulation");
    sessionStorage.setItem("simulation", id);
    simulation = id;
}

function highlight (id) {
    var menuOption = $("#" + id);
    menuOption.css("background-color", "#eeeeee");
    menuOption.css("color", "black");
}

function disableViewModeAndTimeOption () {
    $("#Chart").prop("disabled", true);
    $("#Table").prop("disabled", true);
    $("#Current").prop("disabled", true);
    $("#Historic").prop("disabled", true);
}


///////////////////////////////////////////////////////////////////
// Defining URLs for the navigation menu from Dashboard Template
// This check should be done after checking the View Mode options
//         --> time and viewMode variables filled
///////////////////////////////////////////////////////////////////

if (assessment === "QualityModel") $("#Assessment").attr("href", serverUrl + "/" + assessment);
else $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);

$("#Prediction").attr("href", serverUrl + "/" + prediction + "/" + "PredictionChart");

$("#StrategicIndicatorsAssessment").attr("href", serverUrl + "/StrategicIndicators/" + time + viewMode);

$("#StrategicIndicatorsPrediction").attr("href", serverUrl + "/StrategicIndicators/PredictionChart");

$("#DetailedStrategicIndicatorsAssessment").attr("href", serverUrl + "/DetailedStrategicIndicators/" + time + viewMode);

$("#DetailedStrategicIndicatorsPrediction").attr("href", serverUrl + "/DetailedStrategicIndicators/PredictionChart");

$("#QualityFactorsAssessment").attr("href", serverUrl + "/QualityFactors/" + time + viewMode);

$("#QualityFactorsPrediction").attr("href", serverUrl + "/QualityFactors/PredictionChart");

$("#MetricsAssessment").attr("href", serverUrl + "/Metrics/" + time + viewMode);

$("#MetricsPrediction").attr("href", serverUrl + "/Metrics/PredictionChart");

$("#Simulation").attr("href", serverUrl + "/Simulation/" + simulation);

$("#FactorsSimulation").attr("href", serverUrl + "/Simulation/Factors");

$("#MetricsSimulation").attr("href", serverUrl + "/Simulation/Metrics");

$("#QRSimulation").attr("href", serverUrl + "/Simulation/QR");

$("#QualityAlerts").attr("href", serverUrl + "/QualityAlerts");

$("#QualityRequirements").attr("href", serverUrl + "/QualityRequirements");

$("#Decisions").attr("href", serverUrl + "/Decisions");

$("#QualityModelAssessment").attr("href", serverUrl + "/QualityModel");

$("#Products").attr("href", serverUrl + "/Products/" + products);

$("#ProductsEvaluation").attr("href", serverUrl+"/Products/Evaluation");

$("#ProductsDetailedEvaluation").attr("href", serverUrl+"/Products/DetailedEvaluation");

$("#Configuration").attr("href", serverUrl + "/" + configuration);

$("#ProductsConfig").attr("href", serverUrl + "/Products");

$("#profileConfig").attr("href", serverUrl + "/profile");

$("#usersConfig").attr("href", serverUrl + "/users");

$("#usergroupsConfig").attr("href", serverUrl + "/usergroups");

$("#LogoutProfileConfig").attr("href", serverUrl + "/logout_user");
$("#LogoutProfileConfig").click(function () {
    sessionStorage.removeItem("userName");
});


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
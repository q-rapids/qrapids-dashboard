var currentURL = window.location.href;
var viewMode, representationMode, qmMode, time, assessment, prediction, products, simulation, configuration, userName;

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

function checkPhases () {
    jQuery.ajax({
        dataType: "json",
        url: serverUrl + "/api/phases",
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            if (data.length > 0)
                $("#PhasesAssessment").show();
            else
                $("#PhasesAssessment").hide();
        }
    });
}

checkProducts();
checkPhases();

// Load state from sessionStorage
// If missing, set default values
if (!(viewMode = sessionStorage.getItem("viewMode"))) {
    viewMode = "Chart";
}
if (!(representationMode = sessionStorage.getItem("representationMode"))) {
    representationMode = "Radar";
}
if (!(qmMode = sessionStorage.getItem("qmMode"))) {
    qmMode = "Graph";
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
    configuration = "StrategicIndicators";
}
if (!(simulation = sessionStorage.getItem("simulation"))) {
    simulation = "Factors";
}

//Store state in sessionStorage
sessionStorage.setItem("viewMode", viewMode);
sessionStorage.setItem("representationMode", representationMode);
sessionStorage.setItem("qmMode", qmMode);
sessionStorage.setItem("time", time);

////////////////////////////////////////////////////////////////////////////////////////
///// Customising View, Representation and Quality Model Mode options ////////////
///////////////////////////////////////////////////////////////////////////////////////

// 1.- Customising the ViewMode, RepresentationMode and qmMode buttons depending on the selected option in the main menu
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
}  else if (currentURL.search("/CurrentChartRadar") !== -1){
    viewMode = "Chart";
    representationMode = "Radar";
    time = "Current";
} else if (currentURL.search("/CurrentChartStacked") !== -1){
    viewMode = "Chart";
    representationMode = "Stacked";
    time = "Current";
} else if (currentURL.search("/CurrentChart") !== -1){
    viewMode = "Chart";
    representationMode = sessionStorage.getItem("representationMode");
    time = "Current";
}

if (currentURL.search("/QualityModelGraph") !== -1) {
    qmMode = "Graph";
} else if (currentURL.search("/QualityModelSunburst") !== -1) {
    qmMode = "Sunburst";
}

//Store state in sessionStorage
sessionStorage.setItem("viewMode", viewMode);
sessionStorage.setItem("representationMode", representationMode);
sessionStorage.setItem("qmMode", qmMode);
sessionStorage.setItem("time", time);

// Highlighting the enabled options depending on the View Mode and Time options selected
$("#" + viewMode).css("background-color", "#ffc380");
$("#" + representationMode).css("background-color", "#ffc380");
$("#" + qmMode).css("background-color", "#ffc380");
$("#" + time).css("background-color", "#ffc380");


// Checking the enabled options depending on the selected option on the main menu
var id;
if ((currentURL.search("/StrategicIndicators/") !== -1 || currentURL.search("/EditStrategicIndicators/") !== -1) && !currentURL.match("Configuration")) {
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
} else if (currentURL.search("/QualityFactors/") !== -1 && !currentURL.match("Configuration")) {
    id = "QualityFactors";
    if (currentURL.search("/Prediction") !== -1)
        highlightAndSaveCurrentPrediction(id);
    else
        highlightAndSaveCurrentAssessment(id);
} else if (currentURL.search("/DetailedQualityFactors/") !== -1) {
    id = "DetailedQualityFactors";
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
} else if (currentURL.search("/Phases") !== -1) {
    id = "Phases";
    highlightAndSaveCurrentAssessment(id);
    disableViewModeAndTimeOption();
} else if (currentURL.search("/Products/Evaluation") !== -1) {
    id = "Evaluation";
    highlightandSaveCurrentProducts(id);
} else if (currentURL.search("/Products/DetailedEvaluation") !== -1) {
    id = "DetailedEvaluation";
    highlightandSaveCurrentProducts(id);
} else {
    if (currentURL.match("/StrategicIndicators"))
        id = "StrategicIndicators";
    else if (currentURL.match("/Products"))
        id = "Products";
    else if (currentURL.match("/QualityFactors"))
        id = "QualityFactors";
    else if (currentURL.match("/Categories"))
        id = "Categories";
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

if (assessment === "QualityModel") $("#Assessment").attr("href", serverUrl + "/" + assessment + qmMode);
else if ( assessment === "Phases" ) $("#Assessment").attr("href", serverUrl + "/" + assessment);
else if (assessment === "DetailedStrategicIndicators" || assessment === "QualityFactors" ) $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode + representationMode);
else $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);

$("#Prediction").attr("href", serverUrl + "/" + prediction + "/" + "PredictionChart");

$("#StrategicIndicatorsAssessment").attr("href", serverUrl + "/StrategicIndicators/" + time + viewMode);

$("#StrategicIndicatorsPrediction").attr("href", serverUrl + "/StrategicIndicators/PredictionChart");

if (time == "Current" && viewMode == "Chart") {
    $("#DetailedStrategicIndicatorsAssessment").attr("href", serverUrl + "/DetailedStrategicIndicators/" + time + viewMode + representationMode);
} else {
    $("#DetailedStrategicIndicatorsAssessment").attr("href", serverUrl + "/DetailedStrategicIndicators/" + time + viewMode);
}

$("#DetailedStrategicIndicatorsPrediction").attr("href", serverUrl + "/DetailedStrategicIndicators/PredictionChart");

$("#QualityFactorsAssessment").attr("href", serverUrl + "/QualityFactors/" + time + viewMode);

$("#QualityFactorsPrediction").attr("href", serverUrl + "/QualityFactors/PredictionChart");

if (time == "Current" && viewMode == "Chart") {
    $("#DetailedQualityFactorsAssessment").attr("href", serverUrl + "/DetailedQualityFactors/" + time + viewMode + representationMode);
} else {
    $("#DetailedQualityFactorsAssessment").attr("href", serverUrl + "/DetailedQualityFactors/" + time + viewMode);}

$("#DetailedQualityFactorsPrediction").attr("href", serverUrl + "/DetailedQualityFactors/PredictionChart");

$("#MetricsAssessment").attr("href", serverUrl + "/Metrics/" + time + viewMode);

$("#MetricsPrediction").attr("href", serverUrl + "/Metrics/PredictionChart");

$("#Simulation").attr("href", serverUrl + "/Simulation/" + simulation);

$("#FactorsSimulation").attr("href", serverUrl + "/Simulation/Factors");

$("#MetricsSimulation").attr("href", serverUrl + "/Simulation/Metrics");

$("#QRSimulation").attr("href", serverUrl + "/Simulation/QR");

$("#QualityAlerts").attr("href", serverUrl + "/QualityAlerts");

$("#QualityRequirements").attr("href", serverUrl + "/QualityRequirements");

$("#Decisions").attr("href", serverUrl + "/Decisions");

$("#QualityModelAssessment").attr("href", serverUrl + "/QualityModel" + qmMode);

$("#PhasesAssessment").attr("href", serverUrl + "/Phases");

$("#Products").attr("href", serverUrl + "/Products/" + products);

$("#ProductsEvaluation").attr("href", serverUrl+"/Products/Evaluation");

$("#ProductsDetailedEvaluation").attr("href", serverUrl+"/Products/DetailedEvaluation");

$("#Configuration").attr("href", serverUrl + "/" + configuration + "/Configuration");

$("#StrategicIndicatorsConfig").attr("href", serverUrl + "/StrategicIndicators/Configuration");

$("#QualityFactorsConfig").attr("href", serverUrl + "/QualityFactors/Configuration");

$("#ProductsConfig").attr("href", serverUrl + "/Products/Configuration");

$("#CategoriesConfig").attr("href", serverUrl + "/Categories/Configuration");

$("#profileConfig").attr("href", serverUrl + "/profile");

$("#usersConfig").attr("href", serverUrl + "/users");

$("#usergroupsConfig").attr("href", serverUrl + "/usergroups");

$("#LogoutProfileConfig").attr("href", serverUrl + "/logout_user");
$("#LogoutProfileConfig").click(function () {
    sessionStorage.removeItem("userName");
});


function menuNav (urlNav) {
    //add id and name to url if found
    var id = getParameterByName('id');
    if (id.length !== 0) {
        urlNav = urlNav + "?id=" + id;
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
    location.href = urlNav;
}

function parseURLSimple(url) {
    //if url has parameters, set new url and set navegaibility text
    var id = getParameterByName('id');
    var name = getParameterByName('name');
    if (id.length !== 0) {
        url = addStrategicIndicatorIdToUrl(url, id);
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

function parseURLComposed(url) {
    //if url has parameters, set new url and set navegaibility text
    var id = getParameterByName('id');
    var name = getParameterByName('name');
    var si = getParameterByName('si');
    if (id.length !== 0) {
        url = addFactorIdToUrl(url, id);
        if (si.length === 0) {
            $('a#origin').text(name + ' (QF)'); // in DQF view
            $('a#originDQF').text(name + ' (DQF)'); // in Metric view
        } else {
            // in DQF view
            $('a#originSI').text(si + ' (DSI)');
            $('span#arrow').text('>');
            $('a#origin').text(name + ' (QF)');
            // in Metric view
            $('a#originDSI').text(si + ' (DSI)');
            $('span#arrow1').text('>');
            $('a#originQF').text(name + ' (QF)');
            $('span#arrow2').text('>');
            $('a#originDQF').text(name + ' (DQF)');
        }
        if (currentURL.search("/Detailed") !== -1) {
            $('h1#title').text('Detailed ' + name + ' Factor');
        } else {
            $('h1#title').text('Metrics for ' + name + ' Factor');
        }
    }

    var metricId = getParameterByName('metricId');
    if (metricId.length > 0) {
        url = '../api/metrics/'+metricId+"/historical";
    }

    return url;
}

function addStrategicIndicatorIdToUrl (url, id) {
    var splits = url.split('/');
    var i;
    for (i = 0; i < splits.length && splits[i] !== "strategicIndicators"; i++) {}
    splits.splice(i + 1, 0, id);
    return splits.join('/');
}

function addFactorIdToUrl (url, id) {
    var splits = url.split('/');
    var i;
    for (i = 0; i < splits.length && splits[i] !== "qualityFactors"; i++) {}
    splits.splice(i + 1, 0, id);
    return splits.join('/');
}

function navBack(toDetailed, factor) {
    var urlNav;
    var id = getParameterByName('id');
    var name = getParameterByName('name');
    var siid = getParameterByName('siid');
    var si = getParameterByName('si');
    if (currentURL.match("/PredictionChart")) {
        if (toDetailed)
            urlNav = "../DetailedStrategicIndicators/PredictionChart";
        else
            urlNav = "../QualityFactors/PredictionChart";
    }
    else {
        if (toDetailed)
            if (factor)
                if (time == "Current" && viewMode == "Chart")
                    urlNav = "../DetailedQualityFactors/" + time + viewMode + representationMode + "?id=" + id + "&name=" + name;
                else
                    urlNav = "../DetailedQualityFactors/" + time + viewMode + "?id=" + id + "&name=" + name;
            else
                if (time == "Current" && viewMode == "Chart")
                    urlNav = "../DetailedStrategicIndicators/" + time + viewMode + representationMode;
                else
                    urlNav = "../DetailedStrategicIndicators/" + time + viewMode;
        else
            urlNav = "../QualityFactors/" + time + viewMode;
    }
    if (siid.length !== 0 && si.length !== 0) {
        if (factor)
            urlNav = urlNav + "&siid=" + siid + "&si=" + si;
        else
            urlNav = urlNav + "?id=" + siid + "&name=" + si;
    }
    location.href = urlNav;
}
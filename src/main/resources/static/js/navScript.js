var currentURL = window.location.href;
var viewMode, DSIRepresentationMode, DQFRepresentationMode, metRepresentationMode, qmMode, time, assessment, prediction, products, simulation, configuration, userName;

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
            // Set Metrics view checkbox
            //sessionStorage.setItem("groupByFactor", "false");
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
            if ((data.length > 0) && (sessionStorage.getItem("profile_qualitylvl") == "ALL"))
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
            if ((data.length > 0) && (sessionStorage.getItem("profile_qualitylvl") == "ALL"))
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
if (!(DSIRepresentationMode = sessionStorage.getItem("DSIRepresentationMode"))) {
    DSIRepresentationMode = "Radar";
}
if (!(DQFRepresentationMode = sessionStorage.getItem("DQFRepresentationMode"))) {
    DQFRepresentationMode = "Radar";
}
if (!(metRepresentationMode = sessionStorage.getItem("metRepresentationMode"))) {
    metRepresentationMode = "Gauge";
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
sessionStorage.setItem("DSIRepresentationMode", DSIRepresentationMode);
sessionStorage.setItem("DQFRepresentationMode", DQFRepresentationMode);
sessionStorage.setItem("metRepresentationMode", metRepresentationMode)
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
} else if (currentURL.search("/DetailedStrategicIndicators/CurrentChartRadar") !== -1){
    viewMode = "Chart";
    DSIRepresentationMode = "Radar";
    time = "Current";
} else if (currentURL.search("/DetailedStrategicIndicators/CurrentChartStacked") !== -1){
    viewMode = "Chart";
    DSIRepresentationMode = "Stacked";
    time = "Current";
} else if (currentURL.search("/DetailedStrategicIndicators/CurrentChartPolar") !== -1){
    viewMode = "Chart";
    DSIRepresentationMode = "Polar";
    time = "Current";
} else if (currentURL.search("/DetailedStrategicIndicators/CurrentChart") !== -1){
    viewMode = "Chart";
    DSIRepresentationMode = sessionStorage.getItem("DSIRepresentationMode");
    time = "Current";
} else if (currentURL.search("/DetailedQualityFactors/CurrentChartRadar") !== -1){
    viewMode = "Chart";
    DQFRepresentationMode = "Radar";
    time = "Current";
} else if (currentURL.search("/DetailedQualityFactors/CurrentChartStacked") !== -1){
    viewMode = "Chart";
    DQFRepresentationMode = "Stacked";
    time = "Current";
} else if (currentURL.search("/DetailedQualityFactors/CurrentChartPolar") !== -1){
    viewMode = "Chart";
    DQFRepresentationMode = "Polar";
    time = "Current";
} else if (currentURL.search("/DetailedQualityFactors/CurrentChart") !== -1){
    viewMode = "Chart";
    DQFRepresentationMode = sessionStorage.getItem("DQFRepresentationMode");
    time = "Current";
} else if (currentURL.search("/Metrics/CurrentChartGauge") !== -1){
    viewMode = "Chart";
    metRepresentationMode = "Gauge";
    time = "Current";
} else if (currentURL.search("/Metrics/CurrentChartSlider") !== -1){
    viewMode = "Chart";
    metRepresentationMode = "Slider";
    time = "Current";
} else if (currentURL.search("/Metrics/CurrentChart") !== -1) {
    viewMode = "Chart";
    metRepresentationMode = sessionStorage.getItem("metRepresentationMode");
    time = "Current";
} else if (currentURL.search("/CurrentChart") !== -1) {
    viewMode = "Chart";
    metRepresentationMode = sessionStorage.getItem("metRepresentationMode");
    DQFRepresentationMode = sessionStorage.getItem("DQFRepresentationMode");
    DSIRepresentationMode = sessionStorage.getItem("DSIRepresentationMode");
    time = "Current";
}

if (currentURL.search("/QualityModelGraph") !== -1) {
    qmMode = "Graph";
} else if (currentURL.search("/QualityModelSunburst") !== -1) {
    qmMode = "Sunburst";
}

//Store state in sessionStorage
sessionStorage.setItem("viewMode", viewMode);
sessionStorage.setItem("DSIRepresentationMode", DSIRepresentationMode);
sessionStorage.setItem("DQFRepresentationMode", DQFRepresentationMode);
sessionStorage.setItem("metRepresentationMode", metRepresentationMode);
sessionStorage.setItem("qmMode", qmMode);
sessionStorage.setItem("time", time);

console.log("AFTER store in sessionStorage");
console.log("time value: " + time);
console.log("viewMode value: " + viewMode);

// Highlighting the enabled options depending on the View Mode and Time options selected
$("#" + viewMode).css("background-color", "#ffc380");
if (currentURL.search("/DetailedStrategicIndicators/CurrentChart") !== -1) {
    $("#" + DSIRepresentationMode).css("background-color", "#ffc380");
} else if(currentURL.search("/DetailedQualityFactors/CurrentChart") !== -1) {
    $("#" + DQFRepresentationMode).css("background-color", "#ffc380");
}
$("#" + metRepresentationMode).css("background-color", "#ffc380");
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
} else if (currentURL.search("/Metrics/") !== -1 && !currentURL.match("Configuration")) {
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
} else if (currentURL.search("/Reporting") !== -1) {
    id = "Reporting";
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
} else { // related to config views
    if (currentURL.match("/StrategicIndicators"))
        id = "StrategicIndicators";
    else if (currentURL.match("/Products"))
        id = "Products";
    else if (currentURL.match("/QualityFactors"))
        id = "QualityFactors";
    else if (currentURL.match("/Metrics"))
        id = "Metrics";
    else if (currentURL.match("/Profiles"))
        id = "Profiles";
    else if (currentURL.match("/Categories"))
        id = "Categories";
    else if (currentURL.match("/QRPatterns"))
        id = "QRPatterns";
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
else if (assessment === "DetailedStrategicIndicators") {
    if (time == "Current" && viewMode == "Chart") {
        $("#Assessment").attr("href", serverUrl + "/" + assessment + "/" + time + viewMode + DSIRepresentationMode);
    } else {
        $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);
    }
} else if (assessment === "DetailedQualityFactors") {
    if (time == "Current" && viewMode == "Chart") {
        $("#Assessment").attr("href", serverUrl + "/" + assessment + "/" + time + viewMode + DQFRepresentationMode);
    } else {
        $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);
    }
} else if (assessment === "Metrics") {
    if (time == "Current" && viewMode == "Chart") {
        $("#Assessment").attr("href", serverUrl + "/" + assessment + "/" + time + viewMode + metRepresentationMode);
    } else {
        $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);
    }
} else $("#Assessment").attr("href", serverUrl + "/" + assessment  + "/" + time + viewMode);

$("#Prediction").attr("href", serverUrl + "/" + prediction + "/" + "PredictionChart");

$("#StrategicIndicatorsAssessment").attr("href", serverUrl + "/StrategicIndicators/" + time + viewMode);

$("#StrategicIndicatorsPrediction").attr("href", serverUrl + "/StrategicIndicators/PredictionChart");

if ((time == "Current") && (viewMode == "Chart")) {
    console.log("DSIRepresentationMode " + DSIRepresentationMode);
    $("#DetailedStrategicIndicatorsAssessment").attr("href", serverUrl + "/DetailedStrategicIndicators/" + time + viewMode + DSIRepresentationMode);
} else {
    $("#DetailedStrategicIndicatorsAssessment").attr("href", serverUrl + "/DetailedStrategicIndicators/" + time + viewMode);
}

$("#DetailedStrategicIndicatorsPrediction").attr("href", serverUrl + "/DetailedStrategicIndicators/PredictionChart");

$("#QualityFactorsAssessment").attr("href", serverUrl + "/QualityFactors/" + time + viewMode);

$("#QualityFactorsPrediction").attr("href", serverUrl + "/QualityFactors/PredictionChart");

if ((time == "Current") && (viewMode == "Chart")) {
    console.log("DQFRepresentationMode " + DQFRepresentationMode);
    $("#DetailedQualityFactorsAssessment").attr("href", serverUrl + "/DetailedQualityFactors/" + time + viewMode + DQFRepresentationMode);
} else {
    $("#DetailedQualityFactorsAssessment").attr("href", serverUrl + "/DetailedQualityFactors/" + time + viewMode);}

$("#DetailedQualityFactorsPrediction").attr("href", serverUrl + "/DetailedQualityFactors/PredictionChart");

if ((time == "Current") && (viewMode == "Chart")) {
    console.log("metRepresentationMode " + metRepresentationMode);
    $("#MetricsAssessment").attr("href", serverUrl + "/Metrics/" + time + viewMode + metRepresentationMode);
} else {
    $("#MetricsAssessment").attr("href", serverUrl + "/Metrics/" + time + viewMode);
}

$("#MetricsPrediction").attr("href", serverUrl + "/Metrics/PredictionChart");

$("#Simulation").attr("href", serverUrl + "/Simulation/" + simulation);

$("#FactorsSimulation").attr("href", serverUrl + "/Simulation/Factors");

$("#MetricsSimulation").attr("href", serverUrl + "/Simulation/Metrics");

$("#QRSimulation").attr("href", serverUrl + "/Simulation/QR");

$("#QualityAlerts").attr("href", serverUrl + "/QualityAlerts");

$("#QualityRequirements").attr("href", serverUrl + "/QualityRequirements");

$("#Decisions").attr("href", serverUrl + "/Decisions");

console.log("qmMode " + qmMode);
$("#QualityModelAssessment").attr("href", serverUrl + "/QualityModel" + qmMode);

$("#PhasesAssessment").attr("href", serverUrl + "/Phases");

$("#Products").attr("href", serverUrl + "/Products/" + products);

$("#ProductsEvaluation").attr("href", serverUrl+"/Products/Evaluation");

$("#ProductsDetailedEvaluation").attr("href", serverUrl+"/Products/DetailedEvaluation");

$("#Configuration").attr("href", serverUrl + "/" + configuration + "/Configuration");

$("#StrategicIndicatorsConfig").attr("href", serverUrl + "/StrategicIndicators/Configuration");

$("#QualityFactorsConfig").attr("href", serverUrl + "/QualityFactors/Configuration");

$("#MetricsConfig").attr("href", serverUrl + "/Metrics/Configuration");

$("#ProductsConfig").attr("href", serverUrl + "/Products/Configuration");

$("#ProfilesConfig").attr("href", serverUrl + "/Profiles/Configuration");

$("#CategoriesConfig").attr("href", serverUrl + "/Categories/Configuration");

$("#QRPatternsConfig").attr("href", serverUrl + "/QRPatterns/Configuration");

$("#profileConfig").attr("href", serverUrl + "/profile");

$("#usersConfig").attr("href", serverUrl + "/users");

$("#usergroupsConfig").attr("href", serverUrl + "/usergroups");

$("#Reporting").attr("href", serverUrl + "/Reporting");

$("#LogoutProfileConfig").attr("href", serverUrl + "/logout_user");
$("#LogoutProfileConfig").click(function () {
    sessionStorage.removeItem("userName");
});


function menuNav (urlNav) {

    console.log("IN menuNav: ");
    console.log(urlNav);
    console.log("time value: " + time);
    console.log("viewMode value: " + viewMode);

    console.log(location);

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
    if (location.href.includes("/Metrics") && urlNav == "CurrentChart"){
        location.href = urlNav + metRepresentationMode;
    } else if (location.href.includes("/DetailedQualityFactors") && urlNav == "CurrentChart") {
        location.href = urlNav + DQFRepresentationMode;
    } else if (location.href.includes("/DetailedStrategicIndicators") && urlNav == "CurrentChart") {
        location.href = urlNav + DSIRepresentationMode;
    } else {
        location.href = urlNav;
    }
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
            if (factor)
                urlNav = "../DetailedQualityFactors/PredictionChart" + "?id=" + id + "&name=" + name;
            else
                urlNav = "../DetailedStrategicIndicators/PredictionChart";
        else
            urlNav = "../QualityFactors/PredictionChart";
    }
    else {
        if (toDetailed)
            if (factor)
                if (time == "Current" && viewMode == "Chart") {
                    urlNav = "../DetailedQualityFactors/" + time + viewMode + DQFRepresentationMode + "?id=" + id + "&name=" + name;
                } else
                    urlNav = "../DetailedQualityFactors/" + time + viewMode + "?id=" + id + "&name=" + name;
            else
                if (time == "Current" && viewMode == "Chart") {
                    urlNav = "../DetailedStrategicIndicators/" + time + viewMode + DSIRepresentationMode;
                } else
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

// profile quality level filtering
function profileQualityLevelFilter() {
    var profileId = sessionStorage.getItem("profile_id");
    jQuery.ajax({
        dataType: "json",
        url: "../api/profiles/"+profileId,
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            sessionStorage.setItem("profile_qualitylvl", data.qualityLevel);
            if (data.qualityLevel == "METRICS") { // hide some menu options from navBar
                $("#Products").hide();
                $("#Simulation").hide();

                $("#StrategicIndicatorsAssessment").hide();
                $("#DetailedStrategicIndicatorsAssessment").hide();
                $("#QualityFactorsAssessment").hide();
                $("#DetailedQualityFactorsAssessment").hide();

                $("#QualityModelAssessment").hide();

                $("#StrategicIndicatorsPrediction").hide();
                $("#DetailedStrategicIndicatorsPrediction").hide();
                $("#QualityFactorsPrediction").hide();
                $("#DetailedQualityFactorsPrediction").hide();

                $("#ProductsConfig").hide();
                $("#StrategicIndicatorsConfig").hide();
                $("#QualityFactorsConfig").hide();
            } else if (data.qualityLevel == "METRICS_FACTORS") {
                $("#Products").hide();
                $("#FactorsSimulation").hide();

                $("#StrategicIndicatorsAssessment").hide();
                $("#DetailedStrategicIndicatorsAssessment").hide();
                $("#PhasesAssessment").hide();

                $("#StrategicIndicatorsPrediction").hide();
                $("#DetailedStrategicIndicatorsPrediction").hide();

                $("#ProductsConfig").hide();
                $("#StrategicIndicatorsConfig").hide();
            }
        }
    });
}

profileQualityLevelFilter();

window.onload = function() {
    if(!window.location.hash) {
        window.location = window.location + '#loaded';
        if (!window.location.href.match("/QualityAlerts"))  // correct alerts new status bug
            window.location.reload();
    }
}

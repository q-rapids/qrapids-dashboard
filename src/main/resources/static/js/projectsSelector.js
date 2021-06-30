var projects = sessionStorage.getItem("projects");
projects = (projects) ? JSON.parse(projects) : [];

for (i = 0; i < projects.length; i++) {
    $("#projectsDropdownItems").append('<li><a onclick="setProject(\''+projects[i]+'\')" href="#">'+ projects[i] +'</a></li>');
}

var profiles = sessionStorage.getItem("profiles");
profiles = (profiles) ? JSON.parse(profiles) : [];

for (i = 0; i < profiles.length; i+=2) {
    $("#profilesDropdownItems").append('<li><a onclick="setProfile(\''+profiles[i]+','+profiles[i+1]+'\')" href="#">'+ profiles[i+1] +'</a></li>');
}

var prj = sessionStorage.getItem("prj");
if (prj) {
    $("#projectsDropdownText").text(prj);
}


function setProject(project, url) {
    sessionStorage.setItem("prj", project);
    if (url && (url != window.location.href))
        window.open(url,"_self");
    else
        window.location.reload();
}

// HTTP interceptor
XMLHttpRequest.prototype.open = (function(open) {
    return function(method,url,async) {
        if (url.search("/api") !== -1 && url.search("/api/projects") === -1 && url.search("/api/profiles") === -1
            && url.search("/serverUrl") === -1) {
            var prj = sessionStorage.getItem("prj");
            console.log(url+" Project: "+prj);
            var prf = sessionStorage.getItem("profile_id");
            if (!prf || prf === " ") {
                getProfiles();
            } if (!prj || prj === " ") {
                getProjects(prf);
            } else {
                console.log("else from if in HTTP interceptor");
                url = setQueryStringParameter(url, "prj", prj);
                open.apply(this, arguments);
            }
        }
        else {
            console.log("else in HTTP interceptor");
            open.apply(this, arguments);
        }
    };
})(XMLHttpRequest.prototype.open);

function getProjects(profileID) {
    console.log("in getProjects()");
    var url;
    if (profileID && profileID != "null") { // if profileID not null --> show specific projects
        url = "../api/projects?profile_id=" + profileID;
    } else { // if profileID is null --> show all projects
        url = "../api/projects";
    }
    // make API Rest call
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            var prj_externalId = [];
            for (i = 0; i < data.length; i++) {
                prj_externalId.push(data[i].externalId);
            }
            sessionStorage.setItem("projects", JSON.stringify(prj_externalId));
            if (data.length === 0) { //For testing purposes
                setProject(" ");
            } else {
                showProjectSelector(prj_externalId);
            }
        }
    });
}

function getProfiles() {
    jQuery.ajax({
        dataType: "json",
        url: "../api/profiles",
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            var profiles = [];
            profiles.push(null);
            profiles.push("Without Profile");
            $("#profilesDropdownItems").append('<li><a onclick="setProfile(\'' + null +','+ "Without Profile" + '\')" href="#">' + "Without Profile" + '</a></li>');
            for (i = 0; i < data.length; i++) {
                profiles.push(data[i].id);
                profiles.push(data[i].name);
                $("#profilesDropdownItems").append('<li><a onclick="setProfile(\'' + data[i].id + ','+ data[i].name + '\')" href="#">' + data[i].name + '</a></li>');
            }
            sessionStorage.setItem("profiles", JSON.stringify(profiles));
            // set default profile
            sessionStorage.setItem("profile_id", null);
            sessionStorage.setItem("profile_name", "Without Profile");
            $("#profilesDropdownText").text("Without Profile");
        }
    });
}

function setProfile(input) {
    var input = input.split(",");
    // set profile_id and profile_name in sessionStorage
    sessionStorage.setItem("profile_id", input[0]); // input[0] = profile id
    sessionStorage.setItem("profile_name", input[1]); // input[1] = profile name
    //set profile name in selector
    $("#profilesDropdownText").text(input[1]);
    // refresh projects list
    getProjects(input[0]);
}

function showProjectSelector (projects) {
    // clear old project list
    $("#projectsModalItems").empty()
    // create new project list
    for (var i = 0; i < projects.length; i++) {
        $("#projectsModalItems").append('<button class="list-group-item">' + projects[i] + '</button>');
    }

    $('.list-group-item').on('click', function () {
        var $this = $(this);

        $('.active').removeClass('active');
        $this.toggleClass('active');

        $("#projectsModal").modal('hide');

        var url = window.location.href;
        var profileId = sessionStorage.getItem("profile_id");
        if (!profileId || profileId == "null"){
            sessionStorage.setItem("profile_qualitylvl", "ALL");
            // if without profile select representationMode and qmMode to default values
            sessionStorage.setItem("DSIRepresentationMode", "Radar");
            sessionStorage.setItem("DQFRepresentationMode", "Radar");
            sessionStorage.setItem("metRepresentationMode", "Gauge");
            sessionStorage.setItem("qmMode", "Graph");
        } else {
            jQuery.ajax({
                dataType: "json",
                url: "../api/profiles/" + profileId,
                cache: false,
                type: "GET",
                async: false,
                success: function (data) {
                    // select representationMode and qmMode by profile
                    sessionStorage.setItem("DSIRepresentationMode", data.dsiView);
                    sessionStorage.setItem("DQFRepresentationMode", data.dqfView);
                    sessionStorage.setItem("metRepresentationMode", data.mView);
                    sessionStorage.setItem("qmMode", data.qmView);
                    // specific cases: redirect to correct visualization
                    if (currentURL.search("/QualityModel") !== -1) // qm
                        url = serverUrl + "/QualityModel" + sessionStorage.getItem("qmMode");
                    else if (currentURL.search("/Metrics/CurrentChart") !== -1) {// metrics
                        console.log("else if metrics");
                        console.log(sessionStorage.getItem("metRepresentationMode"));
                        url = serverUrl + "/Metrics/CurrentChart" + sessionStorage.getItem("metRepresentationMode");
                    } else if (currentURL.search("/DetailedStrategicIndicators/CurrentChart") !== -1) // dsi
                        url = serverUrl + "/DetailedStrategicIndicators/CurrentChart" + sessionStorage.getItem("DSIRepresentationMode");
                    else if (currentURL.search("/DetailedQualityFactors/CurrentChart") !== -1) // dqf
                        url = serverUrl + "/DetailedQualityFactors/CurrentChart" + sessionStorage.getItem("DQFRepresentationMode");
                    sessionStorage.setItem("profile_qualitylvl", data.qualityLevel);
                    if (data.qualityLevel == "METRICS") {
                        sessionStorage.setItem("prediction", "Metrics");
                        sessionStorage.setItem("configuration", "Categories");
                        sessionStorage.setItem("assessment", "Metrics");
                        if (currentURL.search("/Prediction") !== -1)
                            url = serverUrl + "/Metrics/PredictionChart";
                        else
                            url = serverUrl + "/Metrics/" + time + viewMode + sessionStorage.getItem("metRepresentationMode");
                    } else if (data.qualityLevel == "METRICS_FACTORS") {
                        sessionStorage.setItem("prediction", "QualityFactors");
                        sessionStorage.setItem("configuration", "Categories");
                        sessionStorage.setItem("assessment", "QualityFactors");
                        sessionStorage.setItem("simulation", "Metrics");
                        sessionStorage.setItem("qmMode", "Graph");
                        if (currentURL.search("/Prediction") !== -1)
                            url = serverUrl + "/QualityFactors/PredictionChart";
                        else if (currentURL.search("/Simulation") !== -1)
                            url = serverUrl + "/Simulation/Metrics";
                        else {
                            url = serverUrl + "/QualityFactors/" + time + viewMode;
                        }
                    }
                }
            });
        }
        setProject($this.text(), url);
    });

    $("#projectsModal").modal();
}

function setQueryStringParameter(uri, key, value) {
    var separator = uri.indexOf('?') !== -1 ? "&" : "?";
    return uri + separator + key + "=" + value;
}

$("#projectModalButton").click(function () {
    var profileID = sessionStorage.getItem("profile_id");
    var profileName = sessionStorage.getItem("profile_name");
    console.log(profileID);
    console.log(profileName);
    setProfile(profileID+','+profileName);
    $("#projectsModal").modal();
});

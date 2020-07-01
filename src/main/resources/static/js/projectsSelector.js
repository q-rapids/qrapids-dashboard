var projects = sessionStorage.getItem("projects");
projects = (projects) ? JSON.parse(projects) : [];

for (i = 0; i < projects.length; i++) {
    $("#projectsDropdownItems").append('<li><a onclick="setProject(\''+projects[i]+'\')" href="#">'+ projects[i] +'</a></li>');
}

var prj = sessionStorage.getItem("prj");
if (prj) {
    $("#projectsDropdownText").text(prj);
}
/*
var prf = sessionStorage.getItem("profile_name");
if (prf) {
    $("#profilesDropdownText").text(prf);
    getProjects(sessionStorage.getItem("profile_id"));
}
*/

function setProject(project) {
    sessionStorage.setItem("prj", project);
    window.location.reload();
}

// HTTP interceptor
XMLHttpRequest.prototype.open = (function(open) {
    return function(method,url,async) {
        var profileID = sessionStorage.getItem("profile_id");
        if (url.search("/api") !== -1 && url.search("/api/projects/profile") === -1 && url.search("/api/profiles") === -1
            && url.search("/api/projects/profile?profile_id="+profileID) === -1 && url.search("/serverUrl") === -1) {

            var prj = sessionStorage.getItem("prj");
            console.log(url+" Project: "+prj);

            var prf = sessionStorage.getItem("profile_id");
            if (!prf || prf === " ") {
                getProfiles();
            } else {
                url = setQueryStringParameter(url, "prj", prj);
                open.apply(this, arguments);
            }
        }
        else {
            open.apply(this, arguments);
        }
    };
})(XMLHttpRequest.prototype.open);

function getProjects(profileID) {
    if (profileID && profileID != "null") { // if profileID not null --> show specific projects
        jQuery.ajax({
            dataType: "json",
            url: "../api/projects/profile?profile_id=" + profileID,
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
    } else { // if profileID is null --> show all projects
        jQuery.ajax({
            dataType: "json",
            url: "../api/projects/profile",
            cache: false,
            type: "GET",
            async: false,
            success: function (data) {
                var prj_externalId = [];
                for (i = 0; i < data.length; i++) {
                    prj_externalId.push(data[i].externalId)
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
}

function getProfiles() {
    if (sessionStorage.getItem("profiles") && sessionStorage.getItem("profiles").length > 0) {
        // to avoid extra api calls
        sessionStorage.setItem("profile_id", null);
        sessionStorage.setItem("profile_name", "Without Profile");
        $("#profilesDropdownText").text("Without Profile");
        getProjects(null);
    } else {
        jQuery.ajax({
            dataType: "json",
            url: "../api/profiles",
            cache: false,
            type: "GET",
            async: false,
            success: function (data) {
                var prfNames = [];
                $("#profilesDropdownItems").append('<li><a onclick="setProfile(\'' + null +','+ "Without Profile" + '\')" href="#">' + "Without Profile" + '</a></li>');
                for (i = 0; i < data.length; i++) {
                    prfNames.push(data[i].name);
                    $("#profilesDropdownItems").append('<li><a onclick="setProfile(\'' + data[i].id + ','+ data[i].name + '\')" href="#">' + data[i].name + '</a></li>');
                }
                sessionStorage.setItem("profiles", prfNames);
                // set default profile
                //sessionStorage.setItem("profile_id", null);
                //sessionStorage.setItem("profile_name", "Without Profile");
                //$("#profilesDropdownText").text("Without Profile");
                //getProjects(null);
            }
        });
    }
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

        setProject($this.text());
    });

    $("#projectsModal").modal();
}

function setQueryStringParameter(uri, key, value) {
    var separator = uri.indexOf('?') !== -1 ? "&" : "?";
    return uri + separator + key + "=" + value;
}
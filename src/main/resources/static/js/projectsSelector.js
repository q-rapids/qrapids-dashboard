var projects = sessionStorage.getItem("projects");
projects = (projects) ? JSON.parse(projects) : [];

for (i = 0; i < projects.length; i++) {
    $("#projectsDropdownItems").append('<li><a onclick="setProject(\''+projects[i]+'\')" href="#">'+ projects[i] +'</a></li>');
}

var prj = sessionStorage.getItem("prj");
if (prj) {
    $("#projectsDropdownText").text(prj);
}

function setProject(project) {
    sessionStorage.setItem("prj", project);
    window.location.reload();
}

// HTTP interceptor
XMLHttpRequest.prototype.open = (function(open) {
    return function(method,url,async) {
        if (url.search("/api") !== -1 && url.search("/api/projects/import") === -1 && url.search("/serverUrl") === -1) {
            var prj = sessionStorage.getItem("prj");
            console.log(url+" Project: "+prj);
            if (!prj || prj === " ")
                getProjects();
            else {
                url = setQueryStringParameter(url, "prj", prj);
                open.apply(this, arguments);
            }
        }
        else {
            open.apply(this, arguments);
        }
    };
})(XMLHttpRequest.prototype.open);

function getProjects() {
    if (sessionStorage.getItem("projects") && sessionStorage.getItem("projects").length > 0) {
        showProjectSelector(projects);
    } else {
        jQuery.ajax({
            dataType: "json",
            url: "../api/projects/import",
            cache: false,
            type: "GET",
            async: false,
            success: function (data) {
                sessionStorage.setItem("projects", JSON.stringify(data));
                if (data.length === 0) { //For testing purposes
                    setProject(" ");
                }
                else if (data.length === 1) {
                    setProject(data[0]);
                }
                else {
                    showProjectSelector(data);
                }
            }
        });
    }
}

function showProjectSelector (projects) {
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

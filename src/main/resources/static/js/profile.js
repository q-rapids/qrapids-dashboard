var projects; // all projects
var profileProjects; // only selected profile projects (unsaved profile modification)
var currentProfileID;
var currentProfile;

function getProjects() {
    var url = "/api/projects";
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            projects = data;
        }
    });
};

function getProjectByID(id){
    var url = "/api/projects/"+id;
    var result = null;
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            result = data;
        }
    });
    return result;
};

function buildProfileList() {
    var url = "/api/profiles";
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            var profileList = document.getElementById('profileList');
            for (var i = 0; i < data.length; i++) {
                var profile = document.createElement('li');
                profile.classList.add("list-group-item");
                profile.classList.add("profile");
                profile.setAttribute("id", (data[i].id));
                profile.appendChild(document.createTextNode(data[i].name));
                profile.addEventListener("click", clickOnTree);

                profileList.appendChild(profile);
            }
            document.getElementById('profileTree').appendChild(profileList);
        }
    });
}

function clickOnTree(e){
    currentProfileID = e.target.id.replace("profile", "");
    var url = "/api/profiles/" + currentProfileID;
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            // set current profile
            currentProfile = data;

            // set profileProjects variable
            profileProjects = data.projects;

            // create show profile view
            var profileForm = document.createElement('div');
            profileForm.setAttribute("id", "profileForm");

            var title1Row = document.createElement('div');
            title1Row.classList.add("profileInfoRow");
            var title1P = document.createElement('p');
            title1P.appendChild(document.createTextNode("Profile Information"));
            title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            title1Row.appendChild(title1P);
            profileForm.appendChild(title1Row);

            var nameRow = document.createElement('div');
            nameRow.classList.add("profileInfoRow");
            var nameP = document.createElement('p');
            nameP.appendChild(document.createTextNode("Name*: "));
            nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            nameRow.appendChild(nameP);
            var inputName = document.createElement("input");
            inputName.setAttribute('id', 'profileName');
            inputName.setAttribute('type', 'text');
            inputName.setAttribute('value', data.name);
            inputName.setAttribute('style', 'width: 100%;');
            inputName.setAttribute('placeholder', 'Write the profile name here');
            nameRow.appendChild(inputName);
            profileForm.appendChild(nameRow);

            var descriptionRow = document.createElement('div');
            descriptionRow.classList.add("profileInfoRow");
            descriptionRow.setAttribute('style', 'resize: vertical;');
            var descriptionP = document.createElement('p');
            descriptionP.appendChild(document.createTextNode("Description: "));
            descriptionP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            descriptionRow.appendChild(descriptionP);
            var inputDescription = document.createElement("textarea");
            inputDescription.setAttribute('id', 'profileDescription');
            inputDescription.value= data.description;
            inputDescription.setAttribute('style', 'width: 100%;');
            inputDescription.setAttribute('rows', '3');
            inputDescription.setAttribute('placeholder', 'Write the profile description here');
            descriptionRow.appendChild(inputDescription);
            profileForm.appendChild(descriptionRow);


            var title2Row = document.createElement('div');
            title2Row.classList.add("profileInfoRow");
            var title2P = document.createElement('p');
            title2P.appendChild(document.createTextNode("Profile Permissions"));
            title2P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            title2Row.appendChild(title2P);
            profileForm.appendChild(title2Row);

            // row with allowed projects and their SIs
            var allowedRow = document.createElement('div');
            allowedRow.classList.add("profileInfoRow");
            var allowedProjectsCol = document.createElement('div');
            allowedProjectsCol.classList.add("selectionColumn");
            allowedProjectsCol.setAttribute('style', 'width: 100%');
            var allowedProjectsP = document.createElement('p');
            allowedProjectsP.appendChild(document.createTextNode("Allowed Projects: "));
            allowedProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            allowedProjectsCol.appendChild(allowedProjectsP);
            // TODO create edit button (modify css)
            var selProjectsBtn = document.createElement('button');
            selProjectsBtn.setAttribute('id', 'selProjectsBtn');
            selProjectsBtn.appendChild(document.createTextNode("..."));
            selProjectsBtn.onclick = selectProjectsModal;
            allowedProjectsCol.appendChild(selProjectsBtn);

            var allowedProjectsBox = document.createElement('select');
            allowedProjectsBox.setAttribute('id', 'allowedProjectsBox');
            allowedProjectsBox.setAttribute('multiple', 'multiple');
            allowedProjectsBox.setAttribute('style', 'height: 150px;');
            // projects from current profile
            for (var i = 0; i < data.projects.length; i++) {
                var opt = document.createElement("option");
                opt.value = data.projects[i].id;
                opt.innerHTML = data.projects[i].name;
                allowedProjectsBox.appendChild(opt);
            }
            // TODO onclick show SIs list
            allowedProjectsBox.onclick = showSIsList;
            allowedProjectsCol.appendChild(allowedProjectsBox);

            var allowedSIsCol = document.createElement('div');
            allowedSIsCol.classList.add("selectionColumn");
            allowedSIsCol.setAttribute('style', 'width: 100%');
            var allowedSIsP = document.createElement('p');
            allowedSIsP.appendChild(document.createTextNode("Allowed Strategic Indicators: "));
            allowedSIsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            allowedSIsCol.appendChild(allowedSIsP);

            // TODO create edit button (modify css)
            var selSIsBtn = document.createElement('button');
            selSIsBtn.setAttribute('id', 'selSIsBtn');
            selSIsBtn.appendChild(document.createTextNode("..."));
            selSIsBtn.onclick = selectSIsModal;
            allowedSIsCol.appendChild(selSIsBtn);

            var allowedSIsBox = document.createElement('select');
            allowedSIsBox.setAttribute('id', 'allowedSIsBox');
            allowedSIsBox.setAttribute('multiple', 'multiple');
            allowedSIsBox.setAttribute('style', 'height: 150px;');
            allowedSIsCol.appendChild(allowedSIsBox);

            // TODO
            allowedRow.appendChild(allowedProjectsCol);
            allowedRow.appendChild(allowedSIsCol);
            profileForm.appendChild(allowedRow);

            var saveBtnRow = document.createElement('div');
            saveBtnRow.classList.add("profileInfoRow");
            saveBtnRow.setAttribute('style', 'justify-content: space-between');
            var deleteBtn = document.createElement('button');
            deleteBtn.classList.add("btn");
            deleteBtn.classList.add("btn-primary");
            deleteBtn.classList.add("btn-danger");
            deleteBtn.setAttribute("id", "deleteBtn");
            deleteBtn.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            deleteBtn.appendChild(document.createTextNode("Delete Profile"));
            deleteBtn.onclick = deleteProfile;
            saveBtnRow.appendChild(deleteBtn);
            var saveBtn = document.createElement('button');
            saveBtn.classList.add("btn");
            saveBtn.classList.add("btn-primary");
            saveBtn.setAttribute("id", "saveBtn");
            saveBtn.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            saveBtn.appendChild(document.createTextNode("Save Profile"));
            saveBtn.onclick = saveProfile;
            saveBtnRow.appendChild(saveBtn);
            profileForm.appendChild(saveBtnRow);

            document.getElementById('profileInfo').innerHTML = "";
            document.getElementById('profileInfo').appendChild(profileForm);
        }
    });
}

function newProfile() {
    var profileForm = document.createElement('div');
    profileForm.setAttribute("id", "profileForm");

    var title1Row = document.createElement('div');
    title1Row.classList.add("profileInfoRow");
    var title1P = document.createElement('p');
    title1P.appendChild(document.createTextNode("Step 1 - Fill your profile information"));
    title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    title1Row.appendChild(title1P);
    profileForm.appendChild(title1Row);

    var nameRow = document.createElement('div');
    nameRow.classList.add("profileInfoRow");
    var nameP = document.createElement('p');
    nameP.appendChild(document.createTextNode("Name*: "));
    nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
    nameRow.appendChild(nameP);
    var inputName = document.createElement("input");
    inputName.setAttribute('id', 'profileName');
    inputName.value = "";
    inputName.setAttribute('rows', '1');
    inputName.setAttribute('style', 'width: 100%;');
    inputName.setAttribute('placeholder', 'Write the profile name here');
    nameRow.appendChild(inputName);
    profileForm.appendChild(nameRow);

    var descriptionRow = document.createElement('div');
    descriptionRow.classList.add("profileInfoRow");
    descriptionRow.setAttribute('style', 'resize: vertical;');
    var descriptionP = document.createElement('p');
    descriptionP.appendChild(document.createTextNode("Description: "));
    descriptionP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
    descriptionRow.appendChild(descriptionP);
    var inputDescription = document.createElement("textarea");
    inputDescription.setAttribute('id', 'profileDescription');
    inputDescription.value= "";
    inputDescription.setAttribute('style', 'width: 100%;');
    inputDescription.setAttribute('rows', '3');
    inputDescription.setAttribute('placeholder', 'Write the profile description here');
    descriptionRow.appendChild(inputDescription);
    profileForm.appendChild(descriptionRow);

    var title2Row = document.createElement('div');
    title2Row.classList.add("profileInfoRow");
    var title2P = document.createElement('p');
    title2P.appendChild(document.createTextNode("Step 2 - Select the corresponding projects"));
    title2P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    title2Row.appendChild(title2P);
    profileForm.appendChild(title2Row);

    var selProjectsCol = document.createElement('div');
    selProjectsCol.classList.add("selectionColumn");
    selProjectsCol.setAttribute('style', 'width: 100%');
    var selProjectsP = document.createElement('p');
    selProjectsP.appendChild(document.createTextNode("Allowed Projects*: "));
    selProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    selProjectsCol.appendChild(selProjectsP);
    var selProjectsBox = document.createElement('select');
    selProjectsBox.setAttribute('id', 'selProjectsBox');
    selProjectsBox.setAttribute('multiple', 'multiple');
    selProjectsBox.setAttribute('style', 'height: 150px;');
    selProjectsCol.appendChild(selProjectsBox);
    var projectsRow = document.createElement('div');
    projectsRow.classList.add("profileInfoRow");
    var avProjectsCol = document.createElement('div');
    avProjectsCol.classList.add("selectionColumn");
    avProjectsCol.setAttribute('style', 'width: 100%');
    var avProjectsP = document.createElement('p');
    avProjectsP.appendChild(document.createTextNode("Available Projects: "));
    avProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    avProjectsCol.appendChild(avProjectsP);
    var avProjectsBox = document.createElement('select');
    avProjectsBox.setAttribute('id', 'avProjectsBox');
    avProjectsBox.setAttribute('multiple', 'multiple');
    avProjectsBox.setAttribute('style', 'height: 150px;');
    for (var i = 0; i < projects.length; i++) {
        var opt = document.createElement("option");
        opt.setAttribute('id', ('opt' + projects[i].name));
        opt.value = projects[i].id;
        opt.innerHTML = projects[i].name;
        avProjectsBox.appendChild(opt);
    }
    avProjectsCol.appendChild(avProjectsBox);
    var arrowsCol = document.createElement('div');
    arrowsCol.classList.add("selectionColumn");
    arrowsCol.setAttribute('style', 'padding-top:30px;');
    var arrowLeft = document.createElement('button');
    arrowLeft.classList.add("btn");
    arrowLeft.classList.add("btn-default");
    arrowLeft.classList.add("top-and-bottom-margin");
    arrowLeft.setAttribute('id', 'oneLeft');
    arrowLeft.appendChild(document.createTextNode("<"));
    arrowLeft.onclick = moveItemsLeft;
    arrowsCol.appendChild(arrowLeft);
    var arrowRight = document.createElement('button');
    arrowRight.classList.add("btn");
    arrowRight.classList.add("btn-default");
    arrowRight.classList.add("top-and-bottom-margin");
    arrowRight.setAttribute('id', 'right');
    arrowRight.appendChild(document.createTextNode(">"));
    arrowRight.onclick = moveItemsRight;
    arrowRight.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowRight);
    var arrowAllRight = document.createElement('button');
    arrowAllRight.classList.add("btn");
    arrowAllRight.classList.add("btn-default");
    arrowAllRight.classList.add("top-and-bottom-margin");
    arrowAllRight.setAttribute('id', 'allRight');
    arrowAllRight.appendChild(document.createTextNode(">>"));
    arrowAllRight.onclick = moveAllItemsRight;
    arrowAllRight.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowAllRight);
    var arrowAllLeft = document.createElement('button');
    arrowAllLeft.classList.add("btn");
    arrowAllLeft.classList.add("btn-default");
    arrowAllLeft.classList.add("top-and-bottom-margin");
    arrowAllLeft.setAttribute('id', 'allLeft');
    arrowAllLeft.appendChild(document.createTextNode("<<"));
    arrowAllLeft.onclick = moveAllItemsLeft;
    arrowAllLeft.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowAllLeft);

    projectsRow.appendChild(avProjectsCol);
    projectsRow.appendChild(arrowsCol);
    projectsRow.appendChild(selProjectsCol);
    profileForm.appendChild(projectsRow);

    var saveBtnRow = document.createElement('div');
    saveBtnRow.classList.add("profileInfoRow");
    saveBtnRow.setAttribute('style', 'justify-content: flex-end');
    var saveBtn = document.createElement('button');
    saveBtn.classList.add("btn");
    saveBtn.classList.add("btn-primary");
    saveBtn.setAttribute("id", "saveBtn");
    saveBtn.setAttribute('style', 'font-size: 18px; max-width: 30%;');
    saveBtn.appendChild(document.createTextNode("Save Profile"));
    saveBtn.onclick = saveNewProfile;
    saveBtnRow.appendChild(saveBtn);
    profileForm.appendChild(saveBtnRow);

    document.getElementById('profileInfo').innerHTML = "";
    document.getElementById('profileInfo').appendChild(profileForm);
}

function selectProjectsModal() {
    var projectsRow = document.getElementById("projectsRow");
    // clean all projectsRow div child
    projectsRow.innerHTML = "";
    //create projectsRow div content
    projectsRow.classList.add("profileInfoRow");
    var selProjectsCol = document.createElement('div');
    selProjectsCol.classList.add("selectionColumn");
    selProjectsCol.setAttribute('style', 'width: 100%');
    var selProjectsP = document.createElement('p');
    selProjectsP.appendChild(document.createTextNode("Allowed Projects*: "));
    selProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    selProjectsCol.appendChild(selProjectsP);
    var selProjectsBox = document.createElement('select');
    selProjectsBox.setAttribute('id', 'selProjectsBox');
    selProjectsBox.setAttribute('multiple', 'multiple');
    selProjectsBox.setAttribute('style', 'height: 150px;');
    var projectsNames = [];
    for (var i = 0; i < profileProjects.length; i++) {
        var opt = document.createElement("option");
        opt.value = profileProjects[i].id;
        opt.innerHTML = profileProjects[i].name;
        selProjectsBox.appendChild(opt);
        projectsNames.push(profileProjects[i].name);
    }
    selProjectsCol.appendChild(selProjectsBox);
    var avProjectsCol = document.createElement('div');
    avProjectsCol.classList.add("selectionColumn");
    avProjectsCol.setAttribute('style', 'width: 100%');
    var avProjectsP = document.createElement('p');
    avProjectsP.appendChild(document.createTextNode("Available Projects: "));
    avProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    avProjectsCol.appendChild(avProjectsP);
    var avProjectsBox = document.createElement('select');
    avProjectsBox.setAttribute('id', 'avProjectsBox');
    avProjectsBox.setAttribute('multiple', 'multiple');
    avProjectsBox.setAttribute('style', 'height: 150px;');
    for (var i = 0; i < projects.length; i++) {
        if(!projectsNames.includes(projects[i].name)) {
            var opt = document.createElement("option");
            opt.setAttribute('id', ('opt' + projects[i].name));
            opt.value = projects[i].id;
            opt.innerHTML = projects[i].name;
            avProjectsBox.appendChild(opt);
        }
    }
    avProjectsCol.appendChild(avProjectsBox);
    var arrowsCol = document.createElement('div');
    arrowsCol.classList.add("selectionColumn");
    arrowsCol.setAttribute('style', 'padding-top:30px;');
    var arrowLeft = document.createElement('button');
    arrowLeft.classList.add("btn");
    arrowLeft.classList.add("btn-default");
    arrowLeft.classList.add("top-and-bottom-margin");
    arrowLeft.setAttribute('id', 'oneLeft');
    arrowLeft.appendChild(document.createTextNode("<"));
    arrowLeft.onclick = moveItemsLeft;
    arrowsCol.appendChild(arrowLeft);
    var arrowRight = document.createElement('button');
    arrowRight.classList.add("btn");
    arrowRight.classList.add("btn-default");
    arrowRight.classList.add("top-and-bottom-margin");
    arrowRight.setAttribute('id', 'right');
    arrowRight.appendChild(document.createTextNode(">"));
    arrowRight.onclick = moveItemsRight;
    arrowRight.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowRight);
    var arrowAllRight = document.createElement('button');
    arrowAllRight.classList.add("btn");
    arrowAllRight.classList.add("btn-default");
    arrowAllRight.classList.add("top-and-bottom-margin");
    arrowAllRight.setAttribute('id', 'allRight');
    arrowAllRight.appendChild(document.createTextNode(">>"));
    arrowAllRight.onclick = moveAllItemsRight;
    arrowAllRight.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowAllRight);
    var arrowAllLeft = document.createElement('button');
    arrowAllLeft.classList.add("btn");
    arrowAllLeft.classList.add("btn-default");
    arrowAllLeft.classList.add("top-and-bottom-margin");
    arrowAllLeft.setAttribute('id', 'allLeft');
    arrowAllLeft.appendChild(document.createTextNode("<<"));
    arrowAllLeft.onclick = moveAllItemsLeft;
    arrowAllLeft.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowAllLeft);
    // append created content to projectsRow div
    projectsRow.appendChild(avProjectsCol);
    projectsRow.appendChild(arrowsCol);
    projectsRow.appendChild(selProjectsCol);
    // show modal
    $("#profileSelectProjectsModal").modal();
};

$("#submitProfileSelectProjectsModalBtn").click(function () {
    // obtain selected projects
    var selectedProjects = [];
    $('#selProjectsBox').children().each (function (i, option) {
        selectedProjects.push(option.value);
    });
    if (selectedProjects.length > 0) {
        // obtain previously allowed projects
        var allowedProjects = [];
        $('#allowedProjectsBox').children().each (function (i, option) {
            allowedProjects.push(option.value);
        });
        // compare selected and previously allowed projects
        if (selectedProjects.length === allowedProjects.length && selectedProjects.sort().every(function(value, index) { return value === allowedProjects.sort()[index]})) {
            // no changes —> close modal
            $("#profileSelectProjectsModal").modal('hide');
        } else {
            profileProjects = [];
            var allowedProjectsBox = document.getElementById('allowedProjectsBox')
            // clean old allowedProjectsBox content
            allowedProjectsBox.innerHTML = "";
            for (var i = 0; i < selectedProjects.length; i++) {
                var prj = getProjectByID(selectedProjects[i]);
                // update profileProjects
                profileProjects.push(prj);
                // update allowedProjectsBox
                var opt = document.createElement("option");
                opt.value = prj.id;
                opt.innerHTML = prj.name;
                allowedProjectsBox.appendChild(opt);
            }

            var allowedSIsBox = document.getElementById('allowedSIsBox')
            // clean allowedSIsBox content
            allowedSIsBox.innerHTML = "";

            // close modal
            $("#profileSelectProjectsModal").modal('hide');
        }

    } else alert("Make sure that you have completed all fields marked with an *");
});

function selectSIsModal() {
    // TODO parecido a Creació dels SI con sus factores available i selected
    console.log("open edit SIs modal");
};

function showSIsList() {
    /* TODO
        1. Obtener el external id del project selecionat (value inside profileProjects or getProjectByID)
        2. Mirrar el campo "all si" del profile_project BD table
        2.1 "all si" = true - show all SIs del project
        2.2 "all si" = false - show only specified SIs del project
     */
    var allowedProjectsBox = document.getElementById("allowedProjectsBox");
    var prjID = allowedProjectsBox.options[allowedProjectsBox.selectedIndex].value;
    var prjExternalID = profileProjects.find(x => x.id == prjID).externalId;
    console.log(prjExternalID);
    if (currentProfile.allSIs.find(x => x.key == prjID)) { // saved project from profile
        if (currentProfile.allSIs.find(x => x.key == prjID).value) {
            // "all si" = true - show all SIs del project
            var url = "/api/strategicIndicators?prj=" + prjExternalID;
            console.log("get all SIs");
            fillAllowedSIsBox(url);
        } else {
            // "all si" = false - show only specified SIs del project
        }
    } else { // new added project to profile
        // by default show all si
        var url = "/api/strategicIndicators?prj=" + prjExternalID;
        console.log("get all SIs");
        fillAllowedSIsBox(url);
    }
};

function fillAllowedSIsBox(url){
    if (serverUrl) {
        url = serverUrl + url;
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(data);
            var allowedSIsBox = document.getElementById('allowedSIsBox')
            // clean old allowedSIsBox content
            allowedSIsBox.innerHTML = "";
            for (var i = 0; i < data.length; i++) {
                // update allowedSIsBox
                var opt = document.createElement("option");
                opt.value = data[i].id;
                opt.innerHTML = data[i].name;
                allowedSIsBox.appendChild(opt);
            }

        }
    });
}

function moveItemsLeft() {
    $('#selProjectsBox').find(':selected').appendTo('#avProjectsBox');
};

function moveAllItemsLeft() {
    $('#selProjectsBox').children().appendTo('#avProjectsBox');
};

function moveItemsRight() {
    $('#avProjectsBox').find(':selected').appendTo('#selProjectsBox');
};

function moveAllItemsRight() {
    $('#avProjectsBox').children().appendTo('#selProjectsBox');
};

function saveNewProfile() {
    var selectedProjects = [];

    $('#selProjectsBox').children().each (function (i, option) {
        selectedProjects.push(option.value);
    });

    if ($('#profileName').val() != "" && selectedProjects.length > 0) {
        var formData = new FormData();
        formData.append("name", $('#profileName').val());
        formData.append("description", $('#profileDescription').val());
        formData.append("projects", selectedProjects);
        var url = "/api/profiles";
        if (serverUrl) {
            url = serverUrl + url;
        }

        $.ajax({
            url: url,
            data: formData,
            type: "POST",
            contentType: false,
            processData: false,
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 409)
                    alert("This Profile name is already in use");
                else {
                    alert("Error in the ElasticSearch: contact to the system administrator");
                    location.href = serverUrl + "/Profiles/Configuration";
                }
            },
            success: function() {
                location.href = "../Profiles/Configuration";
            }
        });
    } else alert("Make sure that you have completed all fields marked with an *");
};

function deleteProfile() {
    if (confirm("Are you sure you want to delete this profile?")) {

        var url = "/api/profiles/" + currentProfileID;
        if (serverUrl) {
            url = serverUrl + url;
        }
        $.ajax({
            url: url,
            type: "DELETE",
            contentType: false,
            processData: false,
            error: function(jqXHR, textStatus, errorThrown) {
                alert("Error in the ElasticSearch: contact to the system administrator");
                location.href = serverUrl + "/Profiles/Configuration";
            },
            success: function() {
                location.href = serverUrl + "/Profiles/Configuration";
            }
        });
    }
};

function saveProfile() {
    var allowedProjects = [];

    $('#allowedProjectsBox').children().each (function (i, option) {
        allowedProjects.push(option.value);
    });

    if ($('#profileName').val() != "" && allowedProjects.length > 0) {
        var formData = new FormData();
        formData.append("name", $('#profileName').val());
        formData.append("description", $('#profileDescription').val());
        formData.append("projects", allowedProjects);

        var url = "/api/profiles/" + currentProfileID;
        if (serverUrl) {
            url = serverUrl + url;
        }

        $.ajax({
            url: url,
            data: formData,
            type: "PUT",
            contentType: false,
            processData: false,
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 409)
                    alert("This Profile name is already in use");
                else {
                    alert("Error in the ElasticSearch: contact to the system administrator");
                    location.href = "../Profiles/Configuration";
                }
            },
            success: function() {
                location.href = "../Profiles/Configuration";
            }
        });
    } else alert("Make sure that you have completed all fields marked with an *");
};


window.onload = function() {
    getProjects();
    buildProfileList();
};
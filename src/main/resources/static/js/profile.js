var projects;
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
    currentProfile = e.target.id.replace("profile", "");
    var url = "/api/profiles/" + currentProfile;
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
            title2P.appendChild(document.createTextNode("Profile Projects Permission"));
            title2P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            title2Row.appendChild(title2P);
            profileForm.appendChild(title2Row);

            var projectsRow = document.createElement('div');
            projectsRow.classList.add("profileInfoRow");
            var selProjectsCol = document.createElement('div');
            selProjectsCol.classList.add("selectionColumn");
            selProjectsCol.setAttribute('style', 'width: 100%');
            var selProjectsP = document.createElement('p');
            selProjectsP.appendChild(document.createTextNode("Selected Projects*: "));
            selProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            selProjectsCol.appendChild(selProjectsP);
            var selProjectsBox = document.createElement('select');
            selProjectsBox.setAttribute('id', 'selProjectsBox');
            selProjectsBox.setAttribute('multiple', 'multiple');
            selProjectsBox.setAttribute('style', 'height: 150px;');
            var projectsNames = [];
            for (var i = 0; i < data.projects.length; i++) {
                var opt = document.createElement("option");
                opt.value = data.projects[i].id;
                opt.innerHTML = data.projects[i].name;
                selProjectsBox.appendChild(opt);
                projectsNames.push(data.projects[i].name);
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

            projectsRow.appendChild(avProjectsCol);
            projectsRow.appendChild(arrowsCol);
            projectsRow.appendChild(selProjectsCol);
            profileForm.appendChild(projectsRow);

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
    selProjectsP.appendChild(document.createTextNode("Selected Projects*: "));
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

        var url = "/api/profiles/" + currentProfile;
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
    var selectedProjects = [];

    $('#selProjectsBox').children().each (function (i, option) {
        selectedProjects.push(option.value);
    });

    if ($('#productName').val() != "" && selectedProjects.length > 0) {
        var formData = new FormData();
        formData.append("name", $('#profileName').val());
        formData.append("description", $('#profileDescription').val());
        formData.append("projects", selectedProjects);

        var url = "/api/profiles/" + currentProfile;
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
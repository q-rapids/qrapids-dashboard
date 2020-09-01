var projects; // all projects
var profileProjects = []; // only selected profile projects (unsaved profile modification)
var currentProfileID;
var currentProfile = null;
var allprojectSIs; // all si of selected project
var projectSIs = []; // pairs <prj, si> for all profile projects
var prjExternalID;
var qualityLevel = ""; // specify (ALL, METRICS_FACTORS, METRICS)

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

function getSIByID(id){
    var url = "/api/strategicIndicators/"+id;
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
            var profiles = [];
            profiles.push(null);
            profiles.push("Without Profile");
            var profileList = document.getElementById('profileList');
            for (var i = 0; i < data.length; i++) {
                var profile = document.createElement('li');
                profile.classList.add("list-group-item");
                profile.classList.add("profile");
                profile.setAttribute("id", (data[i].id));
                profile.appendChild(document.createTextNode(data[i].name));
                profile.addEventListener("click", clickOnTree);
                profileList.appendChild(profile);
                // create profiles list for sessionStorage
                profiles.push(data[i].id);
                profiles.push(data[i].name);
            }
            document.getElementById('profileTree').appendChild(profileList);
            // refresh sessionStorage profiles information
            sessionStorage.setItem("profiles", JSON.stringify(profiles));
        }
    });
}

function clickOnTree(e){
    // clean projectSIs
    projectSIs = [];
    // get selected profile info
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
            // row with radio button for quality level selection
            var qualityLevelRow = document.createElement('div');
            qualityLevelRow.classList.add("profileInfoRow");
            qualityLevelRow.setAttribute('style', 'margin-bottom: 1%');
            var qualityLevelCol = document.createElement('div');
            qualityLevelCol.classList.add("selectionColumn");
            qualityLevelCol.setAttribute('style', 'width: 100%');
            var qualityLevelP = document.createElement('p');
            qualityLevelP.appendChild(document.createTextNode(" Quality Level:   "));
            qualityLevelP.setAttribute('style', 'font-size: 19.5px; margin-bottom: 1%');
            var qualityLevelBtn = document.createElement('button');
            qualityLevelBtn.classList.add("btn");
            // ALL case
            var inputAll = document.createElement('input');
            inputAll.setAttribute('id', 'qualityLevel_All');
            inputAll.setAttribute('type', 'radio');
            inputAll.setAttribute('name', 'qualityLevelForm');
            inputAll.setAttribute('value', 'ALL');
            inputAll.addEventListener("change", updateQualityLevel, false);
            qualityLevelBtn.appendChild(inputAll);
            var textAll = document.createElement('span');
            textAll.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            textAll.innerText = "  All  ";
            qualityLevelBtn.appendChild(textAll);
            // METRICS_FACTORS case
            var inputMetricsFactors = document.createElement('input');
            inputMetricsFactors.setAttribute('id', 'qualityLevel_METRICS_FACTORS');
            inputMetricsFactors.setAttribute('type', 'radio');
            inputMetricsFactors.setAttribute('name', 'qualityLevelForm');
            inputMetricsFactors.setAttribute('value', 'METRICS_FACTORS');
            inputMetricsFactors.addEventListener("change", updateQualityLevel, false);
            qualityLevelBtn.appendChild(inputMetricsFactors);
            var textMetricsFactors = document.createElement('span');
            textMetricsFactors.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            textMetricsFactors.innerText = "  Factors & Metrics  ";
            qualityLevelBtn.appendChild(textMetricsFactors);
            // METRICS case
            var inputMetrics = document.createElement('input');
            inputMetrics.setAttribute('id', 'qualityLevel_METRICS');
            inputMetrics.setAttribute('type', 'radio');
            inputMetrics.setAttribute('name', 'qualityLevelForm');
            inputMetrics.setAttribute('value', 'METRICS');
            inputMetrics.addEventListener("change", updateQualityLevel, false);
            qualityLevelBtn.appendChild(inputMetrics);
            var textMetrics = document.createElement('span');
            textMetrics.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            textMetrics.innerText = "  Only Metrics  ";
            qualityLevelBtn.appendChild(textMetrics);
            qualityLevelP.appendChild(qualityLevelBtn);
            qualityLevelCol.appendChild(qualityLevelP);
            qualityLevelRow.appendChild(qualityLevelCol);
            profileForm.appendChild(qualityLevelRow);
            // update qualityLevel and check the correct radio button
            qualityLevel = data.qualityLevel;
            if (qualityLevel == 'ALL') inputAll.checked = true;
            else if (qualityLevel == 'METRICS_FACTORS') inputMetricsFactors.checked = true;
            else inputMetrics.checked = true;
            // row with allowed projects and their SIs
            var allowedRow = document.createElement('div');
            allowedRow.classList.add("profileInfoRow");
            var allowedProjectsCol = document.createElement('div');
            allowedProjectsCol.classList.add("selectionColumn");
            allowedProjectsCol.setAttribute('style', 'width: 100%');
            var allowedProjectsP = document.createElement('p');
            allowedProjectsP.appendChild(document.createTextNode("Allowed Projects: "));
            allowedProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            var selProjectsBtn = document.createElement('button');
            selProjectsBtn.classList.add("btn");
            selProjectsBtn.setAttribute('id', 'selProjectsBtn');
            var editIcon = document.createElement('img');
            editIcon.classList.add("icons");
            editIcon.src = '/icons/edit.png';
            selProjectsBtn.appendChild(editIcon);
            selProjectsBtn.onclick = openSelectProjectsModal;
            allowedProjectsP.appendChild(selProjectsBtn);
            allowedProjectsCol.appendChild(allowedProjectsP);

            var allowedProjectsBox = document.createElement('select');
            allowedProjectsBox.setAttribute('id', 'allowedProjectsBox');
            allowedProjectsBox.setAttribute('multiple', 'multiple');
            allowedProjectsBox.setAttribute('style', 'height: 150px;');
            // all projects shown, only from current profile are available
            for (var i = 0; i < projects.length; i++) {
                var opt = document.createElement("option");
                opt.value = projects[i].id;
                if(data.projects.find(x => x.id == projects[i].id)) {
                    opt.innerHTML = projects[i].name + " (Yes)";
                } else {
                    opt.innerHTML = projects[i].name + " (No)";
                    opt.disabled = true;
                }
                allowedProjectsBox.appendChild(opt);
            }
            allowedProjectsBox.onclick = showSIsList;
            allowedProjectsCol.appendChild(allowedProjectsBox);

            var allowedSIsCol = document.createElement('div');
            allowedSIsCol.classList.add("selectionColumn");
            allowedSIsCol.setAttribute('style', 'width: 100%');
            var allowedSIsP = document.createElement('p');
            allowedSIsP.appendChild(document.createTextNode("Allowed Strategic Indicators: "));
            allowedSIsP.setAttribute('id', 'allowedSIsP');
            if (inputMetrics.checked || inputMetricsFactors.checked) { // put it in grey color if it's not ALL quality level
                allowedSIsP.setAttribute('style', 'color:grey; font-size: 18px; margin-bottom: 1%');
            } else {
                allowedSIsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            }
            var selSIsBtn = document.createElement('button');
            selSIsBtn.classList.add("btn");
            selSIsBtn.setAttribute('id', 'selSIsBtn');
            var editIcon = document.createElement('img');
            editIcon.classList.add("icons");
            editIcon.src = '/icons/edit.png';
            selSIsBtn.appendChild(editIcon);
            selSIsBtn.onclick = openSelectSIsModal;
            selSIsBtn.disabled = true;
            allowedSIsP.appendChild(selSIsBtn);
            allowedSIsCol.appendChild(allowedSIsP);

            var allowedSIsBox = document.createElement('select');
            allowedSIsBox.setAttribute('id', 'allowedSIsBox');
            allowedSIsBox.setAttribute('multiple', 'multiple');
            allowedSIsBox.setAttribute('style', 'height: 150px;');
            allowedSIsCol.appendChild(allowedSIsBox);

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
    // clean temporal var
    profileProjects = [];
    projectSIs = [];
    currentProfile = null;
    qualityLevel = "";

    // make new profile form
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
    title2P.appendChild(document.createTextNode("Step 2 - Select the profile permissions"));
    title2P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    title2Row.appendChild(title2P);
    profileForm.appendChild(title2Row);

    // row with radio button for quality level selection
    var qualityLevelRow = document.createElement('div');
    qualityLevelRow.classList.add("profileInfoRow");
    qualityLevelRow.setAttribute('style', 'margin-bottom: 1%');
    var qualityLevelCol = document.createElement('div');
    qualityLevelCol.classList.add("selectionColumn");
    qualityLevelCol.setAttribute('style', 'width: 100%');
    var qualityLevelP = document.createElement('p');
    qualityLevelP.appendChild(document.createTextNode("Step 2.1 - Quality Level:   "));
    qualityLevelP.setAttribute('style', 'font-size: 19.5px; margin-bottom: 1%');
    var qualityLevelBtn = document.createElement('button');
    qualityLevelBtn.classList.add("btn");
    // ALL case
    var inputAll = document.createElement('input');
    inputAll.setAttribute('id', 'qualityLevel_All');
    inputAll.setAttribute('type', 'radio');
    inputAll.setAttribute('name', 'qualityLevelForm');
    inputAll.setAttribute('value', 'ALL');
    inputAll.addEventListener("change", updateQualityLevel, false);
    inputAll.checked = true;
    qualityLevel = 'ALL'; // default value
    qualityLevelBtn.appendChild(inputAll);
    var textAll = document.createElement('span');
    textAll.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    textAll.innerText = "  All  ";
    qualityLevelBtn.appendChild(textAll);
    // METRICS_FACTORS case
    var inputMetricsFactors = document.createElement('input');
    inputMetricsFactors.setAttribute('id', 'qualityLevel_METRICS_FACTORS');
    inputMetricsFactors.setAttribute('type', 'radio');
    inputMetricsFactors.setAttribute('name', 'qualityLevelForm');
    inputMetricsFactors.setAttribute('value', 'METRICS_FACTORS');
    inputMetricsFactors.addEventListener("change", updateQualityLevel, false);
    qualityLevelBtn.appendChild(inputMetricsFactors);
    var textMetricsFactors = document.createElement('span');
    textMetricsFactors.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    textMetricsFactors.innerText = "  Factors & Metrics  ";
    qualityLevelBtn.appendChild(textMetricsFactors);
    // METRICS case
    var inputMetrics = document.createElement('input');
    inputMetrics.setAttribute('id', 'qualityLevel_METRICS');
    inputMetrics.setAttribute('type', 'radio');
    inputMetrics.setAttribute('name', 'qualityLevelForm');
    inputMetrics.setAttribute('value', 'METRICS');
    inputMetrics.addEventListener("change", updateQualityLevel, false);
    qualityLevelBtn.appendChild(inputMetrics);
    var textMetrics = document.createElement('span');
    textMetrics.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    textMetrics.innerText = "  Only Metrics  ";
    qualityLevelBtn.appendChild(textMetrics);
    qualityLevelP.appendChild(qualityLevelBtn);
    qualityLevelCol.appendChild(qualityLevelP);
    qualityLevelRow.appendChild(qualityLevelCol);
    profileForm.appendChild(qualityLevelRow);

    // row with allowed projects and their SIs
    var allowedRow = document.createElement('div');
    allowedRow.classList.add("profileInfoRow");
    var allowedProjectsCol = document.createElement('div');
    allowedProjectsCol.classList.add("selectionColumn");
    allowedProjectsCol.setAttribute('style', 'width: 100%');
    var allowedProjectsP = document.createElement('p');
    allowedProjectsP.appendChild(document.createTextNode("Step 2.2 - Allowed Projects*: "));
    allowedProjectsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    var selProjectsBtn = document.createElement('button');
    selProjectsBtn.classList.add("btn");
    selProjectsBtn.setAttribute('id', 'qualityLevelBtn');
    var editIcon = document.createElement('img');
    editIcon.classList.add("icons");
    editIcon.src = '/icons/edit.png';
    selProjectsBtn.appendChild(editIcon);
    selProjectsBtn.onclick = openSelectProjectsModal;
    allowedProjectsP.appendChild(selProjectsBtn);
    allowedProjectsCol.appendChild(allowedProjectsP);

    var allowedProjectsBox = document.createElement('select');
    allowedProjectsBox.setAttribute('id', 'allowedProjectsBox');
    allowedProjectsBox.setAttribute('multiple', 'multiple');
    allowedProjectsBox.setAttribute('style', 'height: 150px;');
    // all projects shown, only from current profile are available
    for (var i = 0; i < projects.length; i++) {
        var opt = document.createElement("option");
        opt.value = projects[i].id;
        if(profileProjects.find(x => x.id == projects[i].id)) {
            opt.innerHTML = projects[i].name + " (Yes)";
        } else {
            opt.innerHTML = projects[i].name + " (No)";
            opt.disabled = true;
        }
        allowedProjectsBox.appendChild(opt);
    }
    allowedProjectsBox.onclick = showSIsList;
    allowedProjectsCol.appendChild(allowedProjectsBox);

    var allowedSIsCol = document.createElement('div');
    allowedSIsCol.classList.add("selectionColumn");
    allowedSIsCol.setAttribute('style', 'width: 100%');
    var allowedSIsP = document.createElement('span');
    allowedSIsP.appendChild(document.createTextNode("Step 2.3 - Allowed Strategic Indicators: "));
    allowedSIsP.setAttribute('id', 'allowedSIsP');
    allowedSIsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    var selSIsBtn = document.createElement('button');
    selSIsBtn.classList.add("btn");
    selSIsBtn.setAttribute('id', 'selSIsBtn');
    var editIcon = document.createElement('img');
    editIcon.classList.add("icons");
    editIcon.src = '/icons/edit.png';
    selSIsBtn.appendChild(editIcon);
    selSIsBtn.onclick = openSelectSIsModal;
    selSIsBtn.disabled = true;
    allowedSIsP.appendChild(selSIsBtn);
    allowedSIsCol.appendChild(allowedSIsP);

    var allowedSIsBox = document.createElement('select');
    allowedSIsBox.setAttribute('id', 'allowedSIsBox');
    allowedSIsBox.setAttribute('multiple', 'multiple');
    allowedSIsBox.setAttribute('style', 'height: 150px;');
    allowedSIsCol.appendChild(allowedSIsBox);

    allowedRow.appendChild(allowedProjectsCol);
    allowedRow.appendChild(allowedSIsCol);
    profileForm.appendChild(allowedRow);

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

function openSelectProjectsModal() {
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
    arrowLeft.onclick = moveProjectItemsLeft;
    arrowsCol.appendChild(arrowLeft);
    var arrowRight = document.createElement('button');
    arrowRight.classList.add("btn");
    arrowRight.classList.add("btn-default");
    arrowRight.classList.add("top-and-bottom-margin");
    arrowRight.setAttribute('id', 'right');
    arrowRight.appendChild(document.createTextNode(">"));
    arrowRight.onclick = moveProjectItemsRight;
    arrowRight.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowRight);
    var arrowAllRight = document.createElement('button');
    arrowAllRight.classList.add("btn");
    arrowAllRight.classList.add("btn-default");
    arrowAllRight.classList.add("top-and-bottom-margin");
    arrowAllRight.setAttribute('id', 'allRight');
    arrowAllRight.appendChild(document.createTextNode(">>"));
    arrowAllRight.onclick = moveAllProjectItemsRight;
    arrowAllRight.setAttribute('style', "margin-top:3px;");
    arrowsCol.appendChild(arrowAllRight);
    var arrowAllLeft = document.createElement('button');
    arrowAllLeft.classList.add("btn");
    arrowAllLeft.classList.add("btn-default");
    arrowAllLeft.classList.add("top-and-bottom-margin");
    arrowAllLeft.setAttribute('id', 'allLeft');
    arrowAllLeft.appendChild(document.createTextNode("<<"));
    arrowAllLeft.onclick = moveAllProjectItemsLeft;
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
            if (!option.disabled){
                allowedProjects.push(option.value);
            }
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
            for (var i = 0; i < projects.length; i++) {
                // update allowedProjectsBox
                var opt = document.createElement("option");
                opt.value = projects[i].id;
                var index = selectedProjects.indexOf(projects[i].id.toString());
                if(index != -1) {
                    opt.innerHTML = projects[i].name + " (Yes)";
                    var prj = getProjectByID(selectedProjects[index]);
                    // update profileProjects
                    profileProjects.push(prj);
                    console.log("profileProjects");
                    console.log(profileProjects);
                } else {
                    opt.innerHTML = projects[i].name + " (No)";
                    opt.disabled = true;
                    // delete unused info from projectSIs
                    var idx =projectSIs.findIndex(x => x.prj == projects[i].externalId);
                    projectSIs.splice(idx,1);
                }
                allowedProjectsBox.appendChild(opt);
            }

            var allowedSIsBox = document.getElementById('allowedSIsBox')
            // clean allowedSIsBox content
            allowedSIsBox.innerHTML = "";
            // disable button to edit SIs for project of profile
            document.getElementById('selSIsBtn').disabled = true;

            // close modal
            $("#profileSelectProjectsModal").modal('hide');
        }

    } else alert("Make sure that you have completed all fields marked with an *");
});

function openSelectSIsModal() {

    var projectText = document.getElementById("projectText");
    projectText.setAttribute('style', 'font-size: 18px; margin-bottom: 1%')
    projectText. innerText = "Project: " + prjExternalID; //profileProjects.find(x => x.id == prjID).name;

    var url = "/api/strategicIndicators?prj=" + prjExternalID;
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
            var SIsRow = document.getElementById("SIsRow");
            // clean all SIsRow div child
            SIsRow.innerHTML = "";
            //create SIsRow div content
            SIsRow.classList.add("profileInfoRow");
            var selSIsCol = document.createElement('div');
            selSIsCol.classList.add("selectionColumn");
            selSIsCol.setAttribute('style', 'width: 100%');
            var selSIsP = document.createElement('p');
            selSIsP.appendChild(document.createTextNode("Allowed Indicators*: "));
            selSIsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            selSIsCol.appendChild(selSIsP);
            var selSIsBox = document.createElement('select');
            selSIsBox.setAttribute('id', 'selSIsBox');
            selSIsBox.setAttribute('multiple', 'multiple');
            selSIsBox.setAttribute('style', 'height: 150px;');
            var siNames = [];
            // use concrete SIs list of selected project
            var selSIs = projectSIs.find(x => x.prj == prjExternalID).si;
            for (var i = 0; i < selSIs.length; i++) {
                var opt = document.createElement("option");
                opt.value = selSIs[i].id;
                opt.innerHTML = selSIs[i].name;
                selSIsBox.appendChild(opt);
                siNames.push(selSIs[i].name);
            }
            selSIsCol.appendChild(selSIsBox);
            var avSIsCol = document.createElement('div');
            avSIsCol.classList.add("selectionColumn");
            avSIsCol.setAttribute('style', 'width: 100%');
            var avSIsP = document.createElement('p');
            avSIsP.appendChild(document.createTextNode("Available Indicators: "));
            avSIsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
            avSIsCol.appendChild(avSIsP);
            var avSIsBox = document.createElement('select');
            avSIsBox.setAttribute('id', 'avSIsBox');
            avSIsBox.setAttribute('multiple', 'multiple');
            avSIsBox.setAttribute('style', 'height: 150px;');
            // use all SIs list of selected project
            for (var i = 0; i < data.length; i++) {
                if (!siNames.includes(data[i].name)) {
                    var opt = document.createElement("option");
                    opt.setAttribute('id', ('opt' + data[i].name));
                    opt.value = data[i].id;
                    opt.innerHTML = data[i].name;
                    avSIsBox.appendChild(opt);
                }
            }
            avSIsCol.appendChild(avSIsBox);
            var arrowsCol = document.createElement('div');
            arrowsCol.classList.add("selectionColumn");
            arrowsCol.setAttribute('style', 'padding-top:30px;');
            var arrowLeft = document.createElement('button');
            arrowLeft.classList.add("btn");
            arrowLeft.classList.add("btn-default");
            arrowLeft.classList.add("top-and-bottom-margin");
            arrowLeft.setAttribute('id', 'oneLeft');
            arrowLeft.appendChild(document.createTextNode("<"));
            arrowLeft.onclick = moveSIsItemsLeft;
            arrowsCol.appendChild(arrowLeft);
            var arrowRight = document.createElement('button');
            arrowRight.classList.add("btn");
            arrowRight.classList.add("btn-default");
            arrowRight.classList.add("top-and-bottom-margin");
            arrowRight.setAttribute('id', 'right');
            arrowRight.appendChild(document.createTextNode(">"));
            arrowRight.onclick = moveSIsItemsRight;
            arrowRight.setAttribute('style', "margin-top:3px;");
            arrowsCol.appendChild(arrowRight);
            var arrowAllRight = document.createElement('button');
            arrowAllRight.classList.add("btn");
            arrowAllRight.classList.add("btn-default");
            arrowAllRight.classList.add("top-and-bottom-margin");
            arrowAllRight.setAttribute('id', 'allRight');
            arrowAllRight.appendChild(document.createTextNode(">>"));
            arrowAllRight.onclick = moveAllSIsItemsRight;
            arrowAllRight.setAttribute('style', "margin-top:3px;");
            arrowsCol.appendChild(arrowAllRight);
            var arrowAllLeft = document.createElement('button');
            arrowAllLeft.classList.add("btn");
            arrowAllLeft.classList.add("btn-default");
            arrowAllLeft.classList.add("top-and-bottom-margin");
            arrowAllLeft.setAttribute('id', 'allLeft');
            arrowAllLeft.appendChild(document.createTextNode("<<"));
            arrowAllLeft.onclick = moveAllSIsItemsLeft;
            arrowAllLeft.setAttribute('style', "margin-top:3px;");
            arrowsCol.appendChild(arrowAllLeft);
            // append created content to SIsRow div
            SIsRow.appendChild(avSIsCol);
            SIsRow.appendChild(arrowsCol);
            SIsRow.appendChild(selSIsCol);
            // show modal
            $("#profileProjectSelectSIsModal").modal();
        }
    });
};

$("#submitProfileProjectSelectSIsModalBtn").click(function () {
    // obtain selected SIs
    var selectedSIs = [];
    $('#selSIsBox').children().each (function (i, option) {
        selectedSIs.push(option.value);
    });
    if (selectedSIs.length > 0) {
        // obtain previously allowed SIs
        var allowedSIs = [];
        $('#allowedSIsBox').children().each (function (i, option) {
            if (!option.disabled) allowedSIs.push(option.value);
        });
        // compare selected and previously allowed SIs
        if (selectedSIs.length === allowedSIs.length && selectedSIs.sort().every(function(value, index) { return value === allowedSIs.sort()[index]})) {
            // no changes —> close modal
            $("#profileProjectSelectSIsModal").modal('hide');
        } else {
            var newSIsList = [];
            var allowedSIsBox = document.getElementById('allowedSIsBox')
            // clean old allowedSIsBox content
            allowedSIsBox.innerHTML = "";
            for (var i = 0; i < allprojectSIs.length; i++) {
                // update allowedSIsBox
                var opt = document.createElement("option");
                opt.value = allprojectSIs[i].id;
                var index = selectedSIs.indexOf(allprojectSIs[i].id.toString());
                if(index != -1) {
                    opt.innerHTML = allprojectSIs[i].name + " (Yes)";
                    var si = getSIByID(selectedSIs[index]);
                    // get new SIs for projectSIs
                    newSIsList.push(si);
                } else {
                    opt.innerHTML = allprojectSIs[i].name + " (No)";
                    opt.disabled = true;
                }
                allowedSIsBox.appendChild(opt);
            }
            // update projectSIs
            projectSIs.find(x => x.prj == prjExternalID).si = newSIsList;
            console.log("Accept SI Modal btn: ")
            console.log(projectSIs);
            // close modal
            $("#profileProjectSelectSIsModal").modal('hide');
        }

    } else alert("Make sure that you have completed all fields marked with an *");
});

function showSIsList() {
    if (qualityLevel == 'ALL') {
        var allowedProjectsBox = document.getElementById("allowedProjectsBox");
        var prjID = allowedProjectsBox.options[allowedProjectsBox.selectedIndex].value;
        prjExternalID = profileProjects.find(x => x.id == prjID).externalId;
        if (projectSIs.find(x => x.prj == prjExternalID)) {
            fillAllowedSIsBox();
        } else {
            if (currentProfile) { // saved project from profile
                if (currentProfile.allSIs.find(x => x.key == prjID) && (currentProfile.allSIs.find(x => x.key == prjID).value)) {
                    // "all si" = true - show all SIs del project
                    var url = "/api/strategicIndicators?prj=" + prjExternalID;
                    fillAllowedSIsBox(url);
                } else {
                    // "all si" = false - show only specified SIs del project
                    var url = "/api/strategicIndicators?prj=" + prjExternalID + "&profile=" + currentProfileID;
                    fillAllowedSIsBox(url);
                }
            } else { // new added project to profile
                // by default show all si
                var url = "/api/strategicIndicators?prj=" + prjExternalID;
                fillAllowedSIsBox(url);
            }
        }
        // able button to edit SIs for project of profile
        document.getElementById('selSIsBtn').disabled = false;
    }
};

function fillAllowedSIsBox(url){
    var siList = [];
    // get data to show as (Yes) in allowedSIsBox
    if (url) {
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
                siList = data;
            }
        });
    } else {
        siList = projectSIs.find(x => x.prj == prjExternalID).si;
    }
    var getSIsurl = "/api/strategicIndicators?prj=" + prjExternalID;
    if (serverUrl) {
        getSIsurl = serverUrl + getSIsurl;
    }
    jQuery.ajax({
        dataType: "json",
        url: getSIsurl,
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            allprojectSIs = data;
            var allowedSIsBox = document.getElementById('allowedSIsBox');
            // clean old allowedSIsBox content
            allowedSIsBox.innerHTML = "";
            for (var i = 0; i < data.length; i++) {
                // update allowedSIsBox
                var opt = document.createElement("option");
                opt.value = data[i].id;
                if (siList.find(x => x.id == data[i].id)) {
                    opt.innerHTML = data[i].name + " (Yes)";
                } else {
                    opt.innerHTML = data[i].name + " (No)";
                    opt.disabled = true;
                }
                allowedSIsBox.appendChild(opt);
            }
            // save or update projectSIs
            if (url) {
                projectSIs.push({'prj': prjExternalID, 'si': siList});
            } else {
                projectSIs.find(x => x.prj == prjExternalID).si = siList;
            }
        }
    });
}


function updateQualityLevel() {
    // save selected quality level
    qualityLevel = $("input[name=qualityLevelForm]:checked").val();
    // clean allowedSIsBox content
    var allowedSIsBox = document.getElementById('allowedSIsBox');
    allowedSIsBox.innerHTML = "";
    // clean projects SIs list
    projectSIs = [];
    // disable button to edit SIs for project of profile
    document.getElementById('selSIsBtn').disabled = true;
    // set grey color for title when SI selection is not allowed
    var allowedSIsP = document.getElementById('allowedSIsP');
    if ($("input[name=qualityLevelForm]:checked").val() == "ALL") {
        allowedSIsP.setAttribute('style', 'font-size: 18px; margin-bottom: 1%');
    } else {
        allowedSIsP.setAttribute('style', 'color: grey; font-size: 18px; margin-bottom: 1%');
    }
}

function moveProjectItemsLeft() {
    $('#selProjectsBox').find(':selected').appendTo('#avProjectsBox');
};

function moveAllProjectItemsLeft() {
    $('#selProjectsBox').children().appendTo('#avProjectsBox');
};

function moveProjectItemsRight() {
    $('#avProjectsBox').find(':selected').appendTo('#selProjectsBox');
};

function moveAllProjectItemsRight() {
    $('#avProjectsBox').children().appendTo('#selProjectsBox');
};

function moveSIsItemsLeft() {
    $('#selSIsBox').find(':selected').appendTo('#avSIsBox');
};

function moveAllSIsItemsLeft() {
    $('#selSIsBox').children().appendTo('#avSIsBox');
};

function moveSIsItemsRight() {
    $('#avSIsBox').find(':selected').appendTo('#selSIsBox');
};

function moveAllSIsItemsRight() {
    $('#avSIsBox').children().appendTo('#selSIsBox');
};

function saveNewProfile() {
    var allowedProjects = [];

    $('#allowedProjectsBox').children().each (function (i, option) {
        var prjID;
        var allSI = true;
        var si = [];
        if (!option.disabled) {
            prjID = option.value;
            var prj = projects.find(x => x.id == prjID).externalId;
            if ((projectSIs.length > 0) && (projectSIs.find(x => x.prj == prj))) {
                // if projectSIs isn't empty (maybe we select not all si)
                var getSIsurl = "/api/strategicIndicators?prj=" + prj;
                if (serverUrl) {
                    getSIsurl = serverUrl + getSIsurl;
                }
                jQuery.ajax({
                    dataType: "json",
                    url: getSIsurl,
                    cache: false,
                    type: "GET",
                    async: false,
                    success: function (data) {
                        if (projectSIs.find(x => x.prj == prj).si.length != data.length) {
                            allSI = false;
                            si = projectSIs.find(x => x.prj == prj).si;
                        }
                    }
                });
            }
            // save project info
            allowedProjects.push({
                "prj" : prjID,
                "all_si" : allSI,
                "si" : si
            });
        }
    });

    console.log("SAVE: allowedProjects");
    console.log(allowedProjects);

    if ($('#profileName').val() != "" && allowedProjects.length > 0) {
        var formData = new FormData();
        formData.append("name", $('#profileName').val());
        formData.append("description", $('#profileDescription').val());
        formData.append("quality_level", qualityLevel);
        formData.append("projects_info", JSON.stringify(allowedProjects));

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

function saveProfile() {
    var allowedProjects = [];

    $('#allowedProjectsBox').children().each (function (i, option) {
        var prjID;
        var allSI = true;
        var si = [];
        if (!option.disabled) {
            prjID = option.value;
            console.log("currentProfile");
            console.log(currentProfile);
            var prj = projects.find(x => x.id == prjID).externalId;
            if (projectSIs.length > 0) {
                // if projectSIs isn't empty (maybe we select not all si)
                if (projectSIs.find(x => x.prj == prj)) {
                    var getSIsurl = "/api/strategicIndicators?prj=" + prj;
                    if (serverUrl) {
                        getSIsurl = serverUrl + getSIsurl;
                    }
                    jQuery.ajax({
                        dataType: "json",
                        url: getSIsurl,
                        cache: false,
                        type: "GET",
                        async: false,
                        success: function (data) {
                            if (projectSIs.find(x => x.prj == prj).si.length != data.length) {
                                allSI = false;
                                si = projectSIs.find(x => x.prj == prj).si;
                            }
                        }
                    });
                }
            } else { // if projectSIs is empty
                // may be we have old info about it
                if(currentProfile.allSIs.find(x => x.key == prjID)){
                    allSI = currentProfile.allSIs.find(x => x.key == prjID).value;
                    if (allSI == false){
                        var getSIsurl = "/api/strategicIndicators?prj=" + prj + "&profile=" + currentProfileID;
                        if (serverUrl) {
                            getSIsurl = serverUrl + getSIsurl;
                        }
                        jQuery.ajax({
                            dataType: "json",
                            url: getSIsurl,
                            cache: false,
                            type: "GET",
                            async: false,
                            success: function (data) {
                                si = data;
                            }
                        });
                    }
                }
            }
            // save project info
            allowedProjects.push({
                "prj" : prjID,
                "all_si" : allSI,
                "si" : si
            });
        }
    });

    console.log("SAVE: allowedProjects");
    console.log(allowedProjects);

    if ($('#profileName').val() != "" && allowedProjects.length > 0) {
        var formData = new FormData();
        formData.append("name", $('#profileName').val());
        formData.append("description", $('#profileDescription').val());
        formData.append("quality_level", qualityLevel);
        formData.append("projects_info", JSON.stringify(allowedProjects));

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

function deleteProfile() {
    if (confirm("\t This operation cannot be undone. \t\n Are you sure you want to delete this profile?")) {

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

window.onload = function() {
    getProjects();
    buildProfileList();
};
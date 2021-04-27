var serverUrl = sessionStorage.getItem("serverUrl");
var previousSelectionId;
var currentSelectionId;
var classifiersTree;

function buildTree() {
    var url = "/api/qrPatternsClassifiers";
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
            classifiersTree = data;
            var classifier1List = document.getElementById('patternList');
            classifier1List.innerHTML = "";
            for (var i=0; i<data.length; i++) {
                var classifier1 = document.createElement('li');
                classifier1.classList.add("list-group-item");
                classifier1.classList.add("Classifier");
                classifier1.setAttribute("id", "classifier" + data[i].id);
                classifier1.setAttribute("data-toggle", "collapse");
                classifier1.setAttribute("data-target", ("#sonsOf" + data[i].id));
                //classifier1.appendChild(document.createTextNode(data[i].name));
                classifier1.addEventListener("click", clickOnTree);

                var icon_c1 = document.createElement('img');
                icon_c1.classList.add("icons");
                icon_c1.setAttribute("src", "/icons/folder.png");
                icon_c1.setAttribute("style", "margin-right: 5px;");
                classifier1.appendChild(icon_c1);
                var text_c1 = document.createElement('p');
                text_c1.appendChild(document.createTextNode(data[i].name));
                classifier1.appendChild(text_c1);

                var classifier2List = document.createElement('ul');
                classifier2List.classList.add("collapse");
                classifier2List.setAttribute("id", ("sonsOf" + data[i].id));
                for(var j=0; j<data[i].internalClassifiers.length; j++) {
                    var classifier2 = document.createElement('li');
                    classifier2.classList.add("list-group-item");
                    classifier2.classList.add("Classifier");
                    //classifier2.appendChild(document.createTextNode(data[i].internalClassifiers[j].name));
                    classifier2.setAttribute("id", ("classifier" + data[i].internalClassifiers[j].id + "-childOf" + data[i].name));
                    classifier2.setAttribute("data-toggle", "collapse");
                    classifier2.setAttribute("data-target", ("#sonsOf" + data[i].internalClassifiers[j].id));
                    classifier2.addEventListener("click", clickOnTree);

                    var icon_c2 = document.createElement('img');
                    icon_c2.classList.add("icons");
                    icon_c2.setAttribute("src", "/icons/folder.png");
                    icon_c2.setAttribute("style", "margin-right: 5px;");
                    classifier2.appendChild(icon_c2);
                    var text_c2 = document.createElement('p');
                    text_c2.appendChild(document.createTextNode(data[i].internalClassifiers[j].name));
                    classifier2.appendChild(text_c2);

                    classifier2List.appendChild(classifier2);

                    var patternList = document.createElement('ul');
                    patternList.classList.add("collapse");
                    patternList.setAttribute("id", ("sonsOf" + data[i].internalClassifiers[j].id));
                    for(var k=0; k<data[i].internalClassifiers[j].requirementPatterns.length; k++) {
                        var pattern = document.createElement('li');
                        pattern.classList.add("list-group-item");
                        pattern.classList.add("Pattern");
                        //pattern.appendChild(document.createTextNode(data[i].internalClassifiers[j].requirementPatterns[k].name));
                        pattern.setAttribute("id", ("pattern" + data[i].internalClassifiers[j].requirementPatterns[k].id + "-childOf" + data[i].internalClassifiers[j].name));
                        pattern.addEventListener("click", clickOnTree);

                        var icon_p = document.createElement('img');
                        icon_p.classList.add("icons");
                        icon_p.setAttribute("src", "/icons/document.png");
                        icon_p.setAttribute("style", "margin-right: 5px;");
                        pattern.appendChild(icon_p);
                        var text_p = document.createElement('p');
                        text_p.appendChild(document.createTextNode(data[i].internalClassifiers[j].requirementPatterns[k].name));
                        pattern.appendChild(text_p);

                        patternList.appendChild(pattern);
                    }
                    classifier2List.appendChild(patternList);
                }
                classifier1List.appendChild(classifier1);
                classifier1List.appendChild(classifier2List);
            }
            document.getElementById('patternTree').appendChild(classifier1List);
        }
    });
};

function clickOnTree(e) {
    var target = e.target;
    if (target.parentNode.classList.contains("Pattern") || target.parentNode.classList.contains("Classifier")) target = target.parentNode;
    if (target.classList.contains("Pattern")) {
        //currentSelection = "Project";
        previousSelectionId = currentSelectionId;
        currentSelectionId = target.id;
        if (previousSelectionId != null) {
            document.getElementById(previousSelectionId).setAttribute('style', 'background-color: #ffffff;');
        }
        document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
        var idString = target.id.split("-")[0];
        getChosenPattern(idString.replace("pattern", ""));
    } else if (target.classList.contains("Classifier")) {
        //currentSelection = "Project";
        previousSelectionId = currentSelectionId;
        currentSelectionId = target.id;
        if (previousSelectionId != null) {
            document.getElementById(previousSelectionId).setAttribute('style', 'background-color: #ffffff;');
        }
        document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
        var idString2 = target.id.split("-")[0];
        getChosenClassifier(idString2.replace("classifier", ""));
    }
}

function getChosenPattern(currentPatternId) {
    var url = "/api/qrPatterns/" + currentPatternId;
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
            var patternForm = document.createElement('div');
            patternForm.setAttribute("id", "patternForm");

            var title1Row = document.createElement('div');
            title1Row.classList.add("productInfoRow");
            var title1P = document.createElement('p');
            title1P.appendChild(document.createTextNode("Requirement Pattern Information"))
            title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            title1Row.appendChild(title1P);
            patternForm.appendChild(title1Row);

            var nameRow = document.createElement('div');
            nameRow.classList.add("productInfoRow");
            var nameP = document.createElement('p');
            nameP.appendChild(document.createTextNode("Name: "));
            nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            nameRow.appendChild(nameP);
            var inputName = document.createElement("input");
            inputName.setAttribute('id', 'patternName');
            inputName.setAttribute('type', 'text');
            inputName.setAttribute('value', data.name);
            inputName.setAttribute('style', 'width: 100%;');
            inputName.setAttribute('placeholder', 'Write the pattern name here');
            inputName.setAttribute('readonly', "");
            nameRow.appendChild(inputName);
            patternForm.appendChild(nameRow);

            var goalRow = document.createElement('div');
            goalRow.classList.add("productInfoRow");
            var goalP = document.createElement('p');
            goalP.appendChild(document.createTextNode("Goal: "));
            goalP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            goalRow.appendChild(goalP);
            var inputGoal = document.createElement("input");
            inputGoal.setAttribute('id', 'patternGoal');
            inputGoal.setAttribute('type', 'text');
            inputGoal.setAttribute('value', data.goal);
            inputGoal.setAttribute('style', 'width: 100%;');
            inputGoal.setAttribute('placeholder', 'Write the pattern goal here');
            inputGoal.setAttribute('readonly', "");
            goalRow.appendChild(inputGoal);
            patternForm.appendChild(goalRow);

            var descriptionRow = document.createElement('div');
            descriptionRow.classList.add("productInfoRow");
            var descriptionP = document.createElement('p');
            descriptionP.appendChild(document.createTextNode("Description: "));
            descriptionP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            descriptionRow.appendChild(descriptionP);
            var inputDescription = document.createElement("textarea");
            inputDescription.setAttribute('id', 'patternDescription');
            inputDescription.setAttribute('type', 'text');
            inputDescription.value = data.forms[0].description;
            inputDescription.setAttribute('style', 'width: 100%;');
            inputDescription.setAttribute('rows', '3');
            inputDescription.setAttribute('placeholder', 'Write the pattern description here');
            inputDescription.setAttribute('readonly', "");
            descriptionRow.appendChild(inputDescription);
            patternForm.appendChild(descriptionRow);

            var requirementRow = document.createElement('div');
            requirementRow.classList.add("productInfoRow");
            var requirementP = document.createElement('p');
            requirementP.appendChild(document.createTextNode("Requirement: "));
            requirementP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            requirementRow.appendChild(requirementP);
            var inputRequirement = document.createElement("input");
            inputRequirement.setAttribute('id', 'patternRequirement');
            inputRequirement.setAttribute('type', 'text');
            inputRequirement.setAttribute('value', data.forms[0].fixedPart.formText);
            inputRequirement.setAttribute('style', 'width: 100%;');
            inputRequirement.setAttribute('placeholder', 'Write the pattern requirement here');
            inputRequirement.setAttribute('readonly', "");
            requirementRow.appendChild(inputRequirement);
            patternForm.appendChild(requirementRow);

            document.getElementById('patternInfo').innerHTML = "";
            document.getElementById('patternInfo').appendChild(patternForm);
        }
    })
}

function getChosenClassifier(currentClassifierId) {
    var url = "/api/qrPatternsClassifiers/" + currentClassifierId;
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
            var classifierForm = document.createElement('div');
            classifierForm.setAttribute("id", "classifierForm");

            var title1Row = document.createElement('div');
            title1Row.classList.add("productInfoRow");
            var title1P = document.createElement('p');
            title1P.appendChild(document.createTextNode("Classifier Information"))
            title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            title1Row.appendChild(title1P);
            classifierForm.appendChild(title1Row);

            var nameRow = document.createElement('div');
            nameRow.classList.add("productInfoRow");
            var nameP = document.createElement('p');
            nameP.appendChild(document.createTextNode("Name: "));
            nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            nameRow.appendChild(nameP);
            var inputName = document.createElement("input");
            inputName.setAttribute('id', 'classifierName');
            inputName.setAttribute('type', 'text');
            inputName.setAttribute('value', data.name);
            inputName.setAttribute('style', 'width: 100%;');
            inputName.setAttribute('placeholder', 'Write the classifier name here');
            nameRow.appendChild(inputName);
            classifierForm.appendChild(nameRow);

            var parentClassifierRow = document.createElement('div');
            parentClassifierRow.classList.add("productInfoRow");
            var parentP = document.createElement('p');
            parentP.appendChild(document.createTextNode("Parent classifier: "));
            parentP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            parentClassifierRow.appendChild(parentP);
            var parentSelect = document.createElement('select');
            parentSelect.setAttribute('id', "parentSelect");
            var optionRoot = document.createElement('option');
            optionRoot.appendChild(document.createTextNode("(Root)"));
            optionRoot.setAttribute('value', "root"); //---> revisar <---
            parentSelect.appendChild(optionRoot);
            parentSelect.value = "root";
            for (var i=0; i<classifiersTree.length; i++) {
                var option = document.createElement('option');
                option.appendChild(document.createTextNode(classifiersTree[i].name));
                option.setAttribute('value', classifiersTree[i].id);
                parentSelect.appendChild(option);
                classifiersTree[i].internalClassifiers.forEach(function(c) {
                    if (c.id == data.id) {
                        option.setAttribute('selected', 'selected');
                    }
                });
            }
            parentClassifierRow.appendChild(parentSelect);
            classifierForm.appendChild(parentClassifierRow);

            document.getElementById('patternInfo').innerHTML = "";
            document.getElementById('patternInfo').appendChild(classifierForm);
        }
    })
}

function newRequirement() {
    var requirementForm = document.createElement('div');
    requirementForm.setAttribute("id", "requirementForm");

    var randomText = document.createElement('h1');
    randomText.appendChild(document.createTextNode("Not implemented yet :("));
    requirementForm.appendChild(randomText);

    document.getElementById('patternInfo').innerHTML = "";
    document.getElementById('patternInfo').appendChild(requirementForm);
};

window.onload = function() {
    buildTree();
};
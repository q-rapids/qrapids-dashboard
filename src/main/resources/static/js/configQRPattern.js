var serverUrl = sessionStorage.getItem("serverUrl");
var previousSelectionId;
var currentSelectionId;
var classifierOfCurrentPattern;
var classifiersTree;
var currentPatternData;
var saveMethod;

var previousSelectionId_metric;
var currentSelectionId_metric;
var saveMethod_metric;

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
        async: false,
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
                    classifier2.setAttribute("id", ("classifier" + data[i].internalClassifiers[j].id));
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
                        pattern.setAttribute("id", ("pattern" + data[i].internalClassifiers[j].requirementPatterns[k].id));
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
}

function clickOnTree(e) {
    var target = e.target;
    if (target.parentNode.classList.contains("Pattern") || target.parentNode.classList.contains("Classifier")) target = target.parentNode;
    previousSelectionId = currentSelectionId;
    currentSelectionId = target.id;
    if (previousSelectionId != null) {
        document.getElementById(previousSelectionId).setAttribute('style', 'background-color: #ffffff;');
    }
    document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
    if (target.classList.contains("Pattern")) {
        getChosenPattern(currentSelectionId.replace("pattern", ""));
    } else if (target.classList.contains("Classifier")) {
        getChosenClassifier(currentSelectionId.replace("classifier", ""));
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
            currentPatternData = data;
            var patternForm = document.createElement('div');
            patternForm.setAttribute("id", "patternForm");

            var title1Row = document.createElement('div');
            title1Row.classList.add("productInfoRow");
            var title1P = document.createElement('p');
            title1P.appendChild(document.createTextNode("Requirement Pattern Information"));
            title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            title1Row.appendChild(title1P);
            patternForm.appendChild(title1Row);

            var nameRow = document.createElement('div');
            nameRow.classList.add("productInfoRow");
            var nameP = document.createElement('p');
            nameP.appendChild(document.createTextNode("Name*: "));
            nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
            nameRow.appendChild(nameP);
            var inputName = document.createElement("input");
            inputName.setAttribute('id', 'patternName');
            inputName.setAttribute('type', 'text');
            inputName.setAttribute('value', data.name);
            inputName.setAttribute('style', 'width: 100%;');
            inputName.setAttribute('placeholder', 'Write the pattern name here');
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
            requirementRow.appendChild(inputRequirement);
            patternForm.appendChild(requirementRow);

            var classifierRow = document.createElement('div');
            classifierRow.classList.add("productInfoRow");
            var classifierP = document.createElement('p');
            classifierP.appendChild(document.createTextNode("Save pattern into classifier: "));
            classifierP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
            classifierRow.appendChild(classifierP);
            var classifierSelect = document.createElement('select');
            classifierSelect.setAttribute('id', "classifierSelect");
            classifierSelect.setAttribute('style', 'width: 100%;');
            for (var i=0; i<classifiersTree.length; i++) {
                var optgroup = document.createElement('optgroup');
                optgroup.setAttribute('label', classifiersTree[i].name);
                for (var j=0; j<classifiersTree[i].internalClassifiers.length; j++) {
                    var option = document.createElement('option');
                    option.appendChild(document.createTextNode(classifiersTree[i].internalClassifiers[j].name));
                    option.setAttribute('value', classifiersTree[i].internalClassifiers[j].id);
                    for (var k=0; k<classifiersTree[i].internalClassifiers[j].requirementPatterns.length; k++) {
                        if (classifiersTree[i].internalClassifiers[j].requirementPatterns[k].id == data.id) {
                            option.setAttribute('selected', 'selected');
                            classifierOfCurrentPattern = classifiersTree[i].internalClassifiers[j].id;
                        }
                    }
                    optgroup.appendChild(option);
                }
                classifierSelect.appendChild(optgroup);
            }
            classifierRow.appendChild(classifierSelect);
            patternForm.appendChild(classifierRow);

            //Parameter
            var parameterTitleRow = document.createElement('div');
            parameterTitleRow.classList.add("productInfoRow");
            var parameterTitleP = document.createElement('p');
            parameterTitleP.appendChild(document.createTextNode("Requirement Parameter"));
            parameterTitleP.setAttribute('style', 'font-size: 36px; margin-right: 1%');
            parameterTitleRow.appendChild(parameterTitleP);
            patternForm.appendChild(parameterTitleRow);

            var parameterNameRow = document.createElement('div');
            parameterNameRow.classList.add("productInfoRow");
            parameterNameRow.setAttribute('style', 'align-items: center');
            var parameterNameP = document.createElement('p');
            parameterNameP.appendChild(document.createTextNode("Parameter name: "));
            parameterNameP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
            parameterNameRow.appendChild(parameterNameP);
            var parameterNameSymbol = document.createElement('p');
            parameterNameSymbol.appendChild(document.createTextNode("%"));
            parameterNameSymbol.setAttribute('style', 'color: grey');
            parameterNameRow.appendChild(parameterNameSymbol);
            var parameterNameInput = document.createElement("input");
            parameterNameInput.setAttribute('id', 'parameterName');
            parameterNameInput.setAttribute('type', 'text');
            parameterNameInput.setAttribute('value', data.forms[0].fixedPart.parameters[0].name);
            parameterNameInput.setAttribute('style', 'width: 100%;');
            parameterNameInput.setAttribute('placeholder', 'Write the parameter name here');
            parameterNameRow.appendChild(parameterNameInput);
            parameterNameRow.appendChild(parameterNameSymbol.cloneNode(true));
            patternForm.appendChild(parameterNameRow);

            var parameterDescriptionRow = document.createElement('div');
            parameterDescriptionRow.classList.add("productInfoRow");
            var parameterDescriptionP = document.createElement('p');
            parameterDescriptionP.appendChild(document.createTextNode("Parameter description: "));
            parameterDescriptionP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
            parameterDescriptionRow.appendChild(parameterDescriptionP);
            var parameterDescriptionInput = document.createElement("textarea");
            parameterDescriptionInput.setAttribute('id', 'parameterDescription');
            parameterDescriptionInput.setAttribute('type', 'text');
            parameterDescriptionInput.value = data.forms[0].fixedPart.parameters[0].description;
            parameterDescriptionInput.setAttribute('style', 'width: 100%;');
            parameterDescriptionInput.setAttribute('rows', '3');
            parameterDescriptionInput.setAttribute('placeholder', 'Write the parameter description here');
            parameterDescriptionRow.appendChild(parameterDescriptionInput);
            patternForm.appendChild(parameterDescriptionRow);

            var parameterCorrectnessConditionRow = document.createElement('div');
            parameterCorrectnessConditionRow.classList.add("productInfoRow");
            var parameterCorrectnessCondP = document.createElement('p');
            parameterCorrectnessCondP.appendChild(document.createTextNode("Parameter correctness condition: "));
            parameterCorrectnessCondP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
            parameterCorrectnessConditionRow.appendChild(parameterCorrectnessCondP);
            var parameterCorrectnessCondInput = document.createElement("input");
            parameterCorrectnessCondInput.setAttribute('id', 'parameterCorrectnessCondition');
            parameterCorrectnessCondInput.setAttribute('type', 'text');
            parameterCorrectnessCondInput.setAttribute('value', data.forms[0].fixedPart.parameters[0].correctnessCondition);
            parameterCorrectnessCondInput.setAttribute('style', 'width: 100%;');
            parameterCorrectnessCondInput.setAttribute('placeholder', 'Write the parameter correctness condition here');
            parameterCorrectnessConditionRow.appendChild(parameterCorrectnessCondInput);
            patternForm.appendChild(parameterCorrectnessConditionRow);

            var parameterMetricRow = document.createElement('div');
            parameterMetricRow.classList.add("productInfoRow");
            var parameterMetricP = document.createElement('p');
            parameterMetricP.appendChild(document.createTextNode("Parameter metric: "));
            parameterMetricP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
            parameterMetricRow.appendChild(parameterMetricP);
            var parameterMetricSelect = document.createElement('select');
            parameterMetricSelect.setAttribute('id', "parameterMetricSelect");
            parameterMetricSelect.setAttribute('style', 'width: 100%;');
            fillParameterMetricSelect(parameterMetricSelect, data.forms[0].fixedPart.parameters[0].metricId);
            parameterMetricRow.appendChild(parameterMetricSelect);

            var manageMetricsButton = document.createElement('button');
            manageMetricsButton.classList.add("btn");
            manageMetricsButton.classList.add("btn-default");
            manageMetricsButton.setAttribute('style', 'margin-left: 1%; padding: 2px 12px;');
            manageMetricsButton.appendChild(document.createTextNode("Manage metrics"));
            manageMetricsButton.addEventListener("click", openMetricsModal);
            parameterMetricRow.appendChild(manageMetricsButton);
            patternForm.appendChild(parameterMetricRow);
            //Parameter end

            var buttonsRow = document.createElement('div');
            buttonsRow.classList.add("productInfoRow");
            buttonsRow.setAttribute('id', 'buttonsRow');
            buttonsRow.setAttribute('style', 'justify-content: space-between;');
            var deleteButton = document.createElement('button');
            deleteButton.classList.add("btn");
            deleteButton.classList.add("btn-danger");
            deleteButton.setAttribute('id', 'deleteButton');
            deleteButton.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            deleteButton.appendChild(document.createTextNode("Delete Pattern"));
            deleteButton.addEventListener("click", deletePattern);
            buttonsRow.appendChild(deleteButton);
            var saveButton = document.createElement('button');
            saveButton.classList.add("btn");
            saveButton.classList.add("btn-primary");
            saveButton.setAttribute('id', 'saveButton');
            saveButton.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            saveButton.appendChild(document.createTextNode("Save Pattern"));
            saveButton.addEventListener("click", savePattern);
            saveMethod = "PUT";
            buttonsRow.appendChild(saveButton);
            patternForm.appendChild(buttonsRow);

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
            nameP.appendChild(document.createTextNode("Name*: "));
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
            fillParentClassifierSelect(parentSelect, data.id);
            parentClassifierRow.appendChild(parentSelect);
            classifierForm.appendChild(parentClassifierRow);

            var buttonsRow = document.createElement('div');
            buttonsRow.classList.add("productInfoRow");
            buttonsRow.setAttribute('id', 'buttonsRow');
            buttonsRow.setAttribute('style', 'justify-content: space-between;');
            var deleteButton = document.createElement('button');
            deleteButton.classList.add("btn");
            deleteButton.classList.add("btn-danger");
            deleteButton.setAttribute('id', 'deleteButton');
            deleteButton.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            deleteButton.appendChild(document.createTextNode("Delete Classifier"));
            deleteButton.addEventListener("click", deleteClassifier);
            buttonsRow.appendChild(deleteButton);
            var saveButton = document.createElement('button');
            saveButton.classList.add("btn");
            saveButton.classList.add("btn-primary");
            saveButton.setAttribute('id', 'saveButton');
            saveButton.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            saveButton.appendChild(document.createTextNode("Save Classifier"));
            saveButton.addEventListener("click", saveClassifier);
            saveMethod = "PUT";
            buttonsRow.appendChild(saveButton);
            classifierForm.appendChild(buttonsRow);

            document.getElementById('patternInfo').innerHTML = "";
            document.getElementById('patternInfo').appendChild(classifierForm);
        }
    })
}

function newPattern() {
    if (currentSelectionId != null) {
        document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #ffffff;');
        currentSelectionId = null;
    }

    var patternForm = document.createElement('div');
    patternForm.setAttribute("id", "patternForm");

    var title1Row = document.createElement('div');
    title1Row.classList.add("productInfoRow");
    var title1P = document.createElement('p');
    title1P.appendChild(document.createTextNode("Step 1 - Fill the pattern information"));
    title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    title1Row.appendChild(title1P);
    patternForm.appendChild(title1Row);

    var nameRow = document.createElement('div');
    nameRow.classList.add("productInfoRow");
    var nameP = document.createElement('p');
    nameP.appendChild(document.createTextNode("Name*: "));
    nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
    nameRow.appendChild(nameP);
    var inputName = document.createElement("input");
    inputName.setAttribute('id', 'patternName');
    inputName.setAttribute('type', 'text');
    inputName.setAttribute('style', 'width: 100%;');
    inputName.setAttribute('placeholder', 'Write the pattern name here');
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
    inputGoal.setAttribute('style', 'width: 100%;');
    inputGoal.setAttribute('placeholder', 'Write the pattern goal here');
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
    inputDescription.setAttribute('style', 'width: 100%;');
    inputDescription.setAttribute('rows', '3');
    inputDescription.setAttribute('placeholder', 'Write the pattern description here');
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
    inputRequirement.setAttribute('style', 'width: 100%;');
    inputRequirement.setAttribute('placeholder', 'Write the pattern requirement here');
    requirementRow.appendChild(inputRequirement);
    patternForm.appendChild(requirementRow);

    var classifierRow = document.createElement('div');
    classifierRow.classList.add("productInfoRow");
    var classifierP = document.createElement('p');
    classifierP.appendChild(document.createTextNode("Save pattern into classifier: "));
    classifierP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
    classifierRow.appendChild(classifierP);
    var classifierSelect = document.createElement('select');
    classifierSelect.setAttribute('id', "classifierSelect");
    for (var i=0; i<classifiersTree.length; i++) {
        var optgroup = document.createElement('optgroup');
        optgroup.setAttribute('label', classifiersTree[i].name);
        for (var j=0; j<classifiersTree[i].internalClassifiers.length; j++) {
            var option = document.createElement('option');
            option.appendChild(document.createTextNode(classifiersTree[i].internalClassifiers[j].name));
            option.setAttribute('value', classifiersTree[i].internalClassifiers[j].id);
            optgroup.appendChild(option);
        }
        classifierSelect.appendChild(optgroup);
    }
    classifierRow.appendChild(classifierSelect);
    patternForm.appendChild(classifierRow);

    //Parameter
    var parameterTitleRow = document.createElement('div');
    parameterTitleRow.classList.add("productInfoRow");
    var parameterTitleP = document.createElement('p');
    parameterTitleP.appendChild(document.createTextNode("Step 2 - Fill the parameter information"));
    parameterTitleP.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    parameterTitleRow.appendChild(parameterTitleP);
    patternForm.appendChild(parameterTitleRow);

    var parameterNameRow = document.createElement('div');
    parameterNameRow.classList.add("productInfoRow");
    parameterNameRow.setAttribute('style', 'align-items: center');
    var parameterNameP = document.createElement('p');
    parameterNameP.appendChild(document.createTextNode("Parameter name: "));
    parameterNameP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
    parameterNameRow.appendChild(parameterNameP);
    var parameterNameSymbol = document.createElement('p');
    parameterNameSymbol.appendChild(document.createTextNode("%"));
    parameterNameSymbol.setAttribute('style', 'color: grey');
    parameterNameRow.appendChild(parameterNameSymbol);
    var parameterNameInput = document.createElement("input");
    parameterNameInput.setAttribute('id', 'parameterName');
    parameterNameInput.setAttribute('type', 'text');
    parameterNameInput.setAttribute('style', 'width: 100%;');
    parameterNameInput.setAttribute('placeholder', 'Write the parameter name here');
    parameterNameRow.appendChild(parameterNameInput);
    parameterNameRow.appendChild(parameterNameSymbol.cloneNode(true));
    patternForm.appendChild(parameterNameRow);

    var parameterDescriptionRow = document.createElement('div');
    parameterDescriptionRow.classList.add("productInfoRow");
    var parameterDescriptionP = document.createElement('p');
    parameterDescriptionP.appendChild(document.createTextNode("Parameter description: "));
    parameterDescriptionP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
    parameterDescriptionRow.appendChild(parameterDescriptionP);
    var parameterDescriptionInput = document.createElement("textarea");
    parameterDescriptionInput.setAttribute('id', 'parameterDescription');
    parameterDescriptionInput.setAttribute('type', 'text');
    parameterDescriptionInput.setAttribute('style', 'width: 100%;');
    parameterDescriptionInput.setAttribute('rows', '3');
    parameterDescriptionInput.setAttribute('placeholder', 'Write the parameter description here');
    parameterDescriptionRow.appendChild(parameterDescriptionInput);
    patternForm.appendChild(parameterDescriptionRow);

    var parameterCorrectnessConditionRow = document.createElement('div');
    parameterCorrectnessConditionRow.classList.add("productInfoRow");
    var parameterCorrectnessCondP = document.createElement('p');
    parameterCorrectnessCondP.appendChild(document.createTextNode("Parameter correctness condition: "));
    parameterCorrectnessCondP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
    parameterCorrectnessConditionRow.appendChild(parameterCorrectnessCondP);
    var parameterCorrectnessCondInput = document.createElement("input");
    parameterCorrectnessCondInput.setAttribute('id', 'parameterCorrectnessCondition');
    parameterCorrectnessCondInput.setAttribute('type', 'text');
    parameterCorrectnessCondInput.setAttribute('style', 'width: 100%;');
    parameterCorrectnessCondInput.setAttribute('placeholder', 'Write the parameter correctness condition here');
    parameterCorrectnessConditionRow.appendChild(parameterCorrectnessCondInput);
    patternForm.appendChild(parameterCorrectnessConditionRow);

    var parameterMetricRow = document.createElement('div');
    parameterMetricRow.classList.add("productInfoRow");
    var parameterMetricP = document.createElement('p');
    parameterMetricP.appendChild(document.createTextNode("Parameter metric: "));
    parameterMetricP.setAttribute('style', 'font-size: 18px; margin-right: 1%; white-space: nowrap');
    parameterMetricRow.appendChild(parameterMetricP);
    var parameterMetricSelect = document.createElement('select');
    parameterMetricSelect.setAttribute('id', "parameterMetricSelect");
    parameterMetricSelect.setAttribute('style', 'width: 100%;');
    fillParameterMetricSelect(parameterMetricSelect, 0);
    parameterMetricRow.appendChild(parameterMetricSelect);

    var manageMetricsButton = document.createElement('button');
    manageMetricsButton.classList.add("btn");
    manageMetricsButton.classList.add("btn-default");
    manageMetricsButton.setAttribute('style', 'margin-left: 1%; padding: 2px 12px;');
    manageMetricsButton.appendChild(document.createTextNode("Manage metrics"));
    manageMetricsButton.addEventListener("click", openMetricsModal);
    parameterMetricRow.appendChild(manageMetricsButton);
    patternForm.appendChild(parameterMetricRow);
    //Parameter end

    var buttonsRow = document.createElement('div');
    buttonsRow.classList.add("productInfoRow");
    buttonsRow.setAttribute('id', 'buttonsRow');
    buttonsRow.setAttribute('style', 'justify-content: space-between;');
    var saveButton = document.createElement('button');
    saveButton.classList.add("btn");
    saveButton.classList.add("btn-primary");
    saveButton.setAttribute('id', 'saveButton');
    saveButton.setAttribute('style', 'font-size: 18px; max-width: 30%;');
    saveButton.appendChild(document.createTextNode("Save Pattern"));
    saveButton.addEventListener("click", savePattern);
    saveMethod = "POST";
    buttonsRow.appendChild(saveButton);
    patternForm.appendChild(buttonsRow);

    document.getElementById('patternInfo').innerHTML = "";
    document.getElementById('patternInfo').appendChild(patternForm);
}

function savePattern() {
    var saved = document.getElementById("savedText")
    if (saved != null) {
        saved.remove();
    }
    if ($('#patternName').val() !== "") {
        var formData = new FormData();
        formData.append("name", $('#patternName').val());
        formData.append("goal", $('#patternGoal').val());
        formData.append("description", $('#patternDescription').val());
        formData.append("requirement", $('#patternRequirement').val());

        var classifierId = $('#classifierSelect').val();
        var i, j=0, found = false;
        for (i=0; i<classifiersTree.length && !found; i++) {
            for (j=0; j<classifiersTree[i].internalClassifiers.length && !found; j++) {
                found = (classifiersTree[i].internalClassifiers[j].id == classifierId);
            }
        }
        i--; j--; //correct increment of value

        var classifierPatterns = "";
        classifiersTree[i].internalClassifiers[j].requirementPatterns.forEach(function(p) {
            classifierPatterns += p.id + ",";
        });
        if (saveMethod == "PUT" && classifiersTree[i].internalClassifiers[j].id != classifierOfCurrentPattern) {
            classifierPatterns += currentSelectionId.replace("pattern", "");
        }

        formData.append("classifierId", classifierId);
        formData.append("classifierName", classifiersTree[i].internalClassifiers[j].name);
        formData.append("classifierPos", j);
        formData.append("classifierPatterns", classifierPatterns);

        formData.append("parameterName", $('#parameterName').val());
        formData.append("parameterDescription", $('#parameterDescription').val());
        formData.append("parameterCorrectnessCondition", $('#parameterCorrectnessCondition').val());
        formData.append("metricId", $('#parameterMetricSelect').val());

        var url = "/api/qrPatterns";
        if (saveMethod == "PUT"){ //Edit pattern: add id to URL
            url += "/" + currentSelectionId.replace("pattern", "");
        }
        if (serverUrl) {
            url = serverUrl + url;
        }

        document.getElementById("saveButton").innerHTML = "Saving...";

        $.ajax({
            url: url,
            data: formData,
            type: saveMethod,
            contentType: false,
            processData: false,
            error: function (jqXHR, textStatus, errorThrown) {
                document.getElementById("saveButton").innerHTML = "Save Pattern";
                if (jqXHR.status == 400)
                    alert("Error: Missing parameters");
                else if (jqXHR.status == 404)
                    alert("Error: This pattern does not exist");
                else if (jqXHR.status == 409)
                    alert("Error: Pattern name already exists");
                else {
                    alert("Internal server error");
                }
            },
            success: function() {
                classifierOfCurrentPattern = formData.get("classifierId");
                buildTree();
                if (saveMethod == "PUT") {
                    document.getElementById(currentSelectionId).parentNode.parentNode.classList.add("in");
                    document.getElementById(currentSelectionId).parentNode.classList.add("in");
                    document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
                    document.getElementById("saveButton").innerHTML = "Save Pattern";
                    var savedText = document.createElement('p');
                    savedText.setAttribute("id", "savedText");
                    savedText.setAttribute("style", "color: #286090; text-align: center;");
                    savedText.appendChild(document.createTextNode("Pattern saved successfully"));
                    document.getElementById("patternForm").appendChild(savedText);
                } else {
                    document.getElementById("sonsOf" + formData.get("classifierId")).parentNode.classList.add("in");
                    document.getElementById("sonsOf" + formData.get("classifierId")).classList.add("in");
                    document.getElementById('patternInfo').innerHTML = "";
                }
            }
        });
    }
    else {
        alert("Make sure that you have completed all fields marked with an *");
    }
}

function deletePattern() {
    if (confirm("Are you sure you want to delete this pattern?")) {
        var url = "/api/qrPatterns/" + currentSelectionId.replace("pattern", "");
        if (serverUrl) {
            url = serverUrl + url;
        }

        document.getElementById("deleteButton").innerHTML = "Deleting...";

        $.ajax({
            url: url,
            type: "DELETE",
            contentType: false,
            processData: false,
            error: function (jqXHR, textStatus, errorThrown) {
                $('#deleteButton').text("Delete Pattern");
                if (jqXHR.status == 404)
                    alert("Error: This pattern does not exist");
                else {
                    alert("Internal server error");
                }
            },
            success: function () {
                var parentClassifierId = document.getElementById(currentSelectionId).parentNode.id;
                buildTree();
                document.getElementById(parentClassifierId).parentNode.classList.add("in");
                document.getElementById(parentClassifierId).classList.add("in");
                document.getElementById('patternInfo').innerHTML = "";
                currentSelectionId = null;
            }
        });
    }
}

function newClassifier() {
    if (currentSelectionId != null) {
        document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #ffffff;');
        currentSelectionId = null;
    }

    var classifierForm = document.createElement('div');
    classifierForm.setAttribute("id", "classifierForm");

    var title1Row = document.createElement('div');
    title1Row.classList.add("productInfoRow");
    var title1P = document.createElement('p');
    title1P.appendChild(document.createTextNode("Step 1 - Fill the classifier information"));
    title1P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    title1Row.appendChild(title1P);
    classifierForm.appendChild(title1Row);

    var nameRow = document.createElement('div');
    nameRow.classList.add("productInfoRow");
    var nameP = document.createElement('p');
    nameP.appendChild(document.createTextNode("Name*: "));
    nameP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
    nameRow.appendChild(nameP);
    var inputName = document.createElement("input");
    inputName.setAttribute('id', 'classifierName');
    inputName.setAttribute('type', 'text');
    inputName.setAttribute('style', 'width: 100%;');
    inputName.setAttribute('placeholder', 'Write the classifier name here');
    nameRow.appendChild(inputName);
    classifierForm.appendChild(nameRow);

    var step2Row = document.createElement('div');
    step2Row.classList.add("productInfoRow");
    var step2P = document.createElement('p');
    step2P.appendChild(document.createTextNode("Step 2 - Select the parent classifier"));
    step2P.setAttribute('style', 'font-size: 36px; margin-right: 1%');
    step2Row.appendChild(step2P);
    classifierForm.appendChild(step2Row);

    var parentClassifierRow = document.createElement('div');
    parentClassifierRow.classList.add("productInfoRow");
    var parentP = document.createElement('p');
    parentP.appendChild(document.createTextNode("Parent classifier: "));
    parentP.setAttribute('style', 'font-size: 18px; margin-right: 1%');
    parentClassifierRow.appendChild(parentP);
    var parentSelect = document.createElement('select');
    parentSelect.setAttribute('id', "parentSelect");
    fillParentClassifierSelect(parentSelect, "-1");
    parentClassifierRow.appendChild(parentSelect);
    classifierForm.appendChild(parentClassifierRow);

    var buttonsRow = document.createElement('div');
    buttonsRow.classList.add("productInfoRow");
    buttonsRow.setAttribute('id', 'buttonsRow');
    buttonsRow.setAttribute('style', 'justify-content: space-between;');
    var saveButton = document.createElement('button');
    saveButton.classList.add("btn");
    saveButton.classList.add("btn-primary");
    saveButton.setAttribute('id', 'saveButton');
    saveButton.setAttribute('style', 'font-size: 18px; max-width: 30%;');
    saveButton.appendChild(document.createTextNode("Save Classifier"));
    saveButton.addEventListener("click", saveClassifier);
    saveMethod = "POST";
    buttonsRow.appendChild(saveButton);
    classifierForm.appendChild(buttonsRow);

    document.getElementById('patternInfo').innerHTML = "";
    document.getElementById('patternInfo').appendChild(classifierForm);
}

function saveClassifier() {
    var saved = document.getElementById("savedText")
    if (saved != null) {
        saved.remove();
    }
    if ($('#classifierName').val() !== "") {
        var formData = new FormData();
        formData.append("name", $('#classifierName').val());
        formData.append("parentClassifier", $('#parentSelect').val());

        var url = "/api/qrPatternsClassifiers";
        var emptyOrNoMove = true;
        if (saveMethod == "PUT") { //Edit classifier: add id to URL
            var classifierId = currentSelectionId.replace("classifier", "");
            url += "/" + classifierId;

            var i, j, found = false;
            for (i=0; i<classifiersTree.length && !found; i++) {
                found = (classifiersTree[i].id == classifierId);
                if (found) {
                    formData.append("oldParentClassifier", "-1");
                    emptyOrNoMove = (classifiersTree[i].internalClassifiers.length == 0);
                }
                for (j=0; j<classifiersTree[i].internalClassifiers.length && !found; j++) {
                    found = (classifiersTree[i].internalClassifiers[j].id == classifierId);
                    if (found) {
                        formData.append("oldParentClassifier", classifiersTree[i].id);
                        emptyOrNoMove = (classifiersTree[i].internalClassifiers[j].requirementPatterns.length == 0);
                    }
                }
            }
            emptyOrNoMove = emptyOrNoMove || (formData.get("oldParentClassifier") == formData.get("parentClassifier"));
        }

        if (emptyOrNoMove) {
            if (serverUrl) {
                url = serverUrl + url;
            }

            document.getElementById("saveButton").innerHTML = "Saving...";

            $.ajax({
                url: url,
                data: formData,
                type: saveMethod,
                contentType: false,
                processData: false,
                error: function (jqXHR, textStatus, errorThrown) {
                    document.getElementById("saveButton").innerHTML = "Save Classifier";
                    if (jqXHR.status == 400)
                        alert("Error: Missing parameters");
                    else if (jqXHR.status == 404)
                        alert("Error: This classifier does not exist");
                    else {
                        alert("Internal server error");
                    }
                },
                success: function () {
                    document.getElementById("saveButton").innerHTML = "Save Classifier";
                    var classifiersTree_before = classifiersTree;
                    buildTree();
                    if (saveMethod == "PUT") {
                        var newParent = document.getElementById("parentSelect").value;
                        var oldParent = formData.get("oldParentClassifier");
                        if (newParent == "-1") {
                            if (oldParent == newParent) {
                                document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
                            } else {
                                var classifiersLevel1 = document.getElementById("patternList").querySelectorAll("li");
                                classifiersLevel1[classifiersLevel1.length-1].setAttribute('style', 'background-color: #efeff8;');
                                currentSelectionId = classifiersLevel1[classifiersLevel1.length-1].id;
                            }
                        } else {
                            if (oldParent == newParent) {
                                document.getElementById(currentSelectionId).parentNode.classList.add("in");
                                document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
                            } else if (oldParent == "-1") {
                                //when root -> noRoot parentId could change due to PABRE limitations
                                //before buildTree
                                var found_newParent = false, i_newParent;
                                for (i_newParent=0; i_newParent<classifiersTree_before.length && !found_newParent; i_newParent++) {
                                    found_newParent = (classifiersTree_before[i_newParent].id == newParent);
                                }
                                if (i < i_newParent) {
                                    i_newParent--;
                                }
                                //after buildTree
                                var newParent_newId = classifiersTree[i_newParent-1].id;
                                var positionInsideNewParent = classifiersTree[i_newParent-1].internalClassifiers.length-1;
                                document.getElementById("sonsOf" + newParent_newId).classList.add("in");
                                currentSelectionId = "classifier" + classifiersTree[i_newParent-1].internalClassifiers[positionInsideNewParent].id;
                                document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
                            } else {
                                document.getElementById("sonsOf" + newParent).classList.add("in");
                                var classifiersInside = document.getElementById("sonsOf" + newParent).querySelectorAll("li");
                                classifiersInside[classifiersInside.length-1].setAttribute('style', 'background-color: #efeff8;');
                                currentSelectionId = classifiersInside[classifiersInside.length-1].id;
                            }
                        }
                        var classifierNewId = currentSelectionId.replace("classifier", "");
                        var parentSelect = document.getElementById("parentSelect");
                        parentSelect.innerHTML = "";
                        fillParentClassifierSelect(parentSelect, classifierNewId);
                        var savedText = document.createElement('p');
                        savedText.setAttribute("id", "savedText");
                        savedText.setAttribute("style", "color: #286090; text-align: center;");
                        savedText.appendChild(document.createTextNode("Classifier saved successfully"));
                        document.getElementById("classifierForm").appendChild(savedText);
                    } else {
                        var parent = document.getElementById("parentSelect").value;
                        if (parent != "-1") {
                            document.getElementById("sonsOf" + parent).classList.add("in");
                        }
                        document.getElementById('patternInfo').innerHTML = "";
                    }
                }
            });
        }
        else {
            alert("You could not move a classifier that contains patterns or other classifiers");
        }
    }
    else {
        alert("Make sure that you have completed all fields marked with an *");
    }
}

function deleteClassifier() {
    if (confirm("Are you sure you want to delete this classifier?")) {
        var classifierId = currentSelectionId.replace("classifier", "");
        var i, j, found = false, empty = false;
        for (i = 0; i < classifiersTree.length && !found; i++) {
            found = (classifiersTree[i].id == classifierId);
            if (found) empty = (classifiersTree[i].internalClassifiers.length == 0);
            for (j = 0; j < classifiersTree[i].internalClassifiers.length && !found; j++) {
                found = (classifiersTree[i].internalClassifiers[j].id == classifierId);
                if (found) empty = (classifiersTree[i].internalClassifiers[j].requirementPatterns.length == 0);
            }
        }
        if (empty) {
            var url = "/api/qrPatternsClassifiers/" + classifierId;
            if (serverUrl) {
                url = serverUrl + url;
            }

            document.getElementById("deleteButton").innerHTML = "Deleting...";

            $.ajax({
                url: url,
                type: "DELETE",
                contentType: false,
                processData: false,
                error: function (jqXHR, textStatus, errorThrown) {
                    $('#deleteButton').text("Delete Classifier");
                    if (jqXHR.status == 404)
                        alert("Error: This classifier does not exist");
                    else {
                        alert("Internal server error");
                    }
                },
                success: function () {
                    var parentClassifierId = document.getElementById(currentSelectionId).parentNode.id;
                    buildTree();
                    if (parentClassifierId != "patternList") {
                        document.getElementById(parentClassifierId).classList.add("in");
                    }
                    document.getElementById('patternInfo').innerHTML = "";
                    currentSelectionId = null;
                }
            });
        } else {
            alert("You could not delete a classifier that contains patterns or other classifiers");
        }
    }
}

function fillParentClassifierSelect(parentSelect, selectedClassifierId) {
    var optionRoot = document.createElement('option');
    optionRoot.appendChild(document.createTextNode("(Root)"));
    optionRoot.setAttribute('value', "-1");
    parentSelect.appendChild(optionRoot);
    for (var i=0; i<classifiersTree.length; i++) {
        if (classifiersTree[i].id != selectedClassifierId) {
            var option = document.createElement('option');
            option.appendChild(document.createTextNode(classifiersTree[i].name));
            option.setAttribute('value', classifiersTree[i].id);
            parentSelect.appendChild(option);
            classifiersTree[i].internalClassifiers.forEach(function (c) {
                if (c.id == selectedClassifierId) {
                    option.setAttribute('selected', 'selected');
                }
            });
        }
    }
}

function fillParameterMetricSelect(parameterMetricSelect, selectedMetricId) {
    var urlMetrics = "/api/qrPatternsMetrics";
    if (serverUrl) {
        urlMetrics = serverUrl + urlMetrics;
    }
    jQuery.ajax({
        dataType: "json",
        url: urlMetrics,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            for (var i=0; i<data.length; i++) {
                var metricOption = document.createElement('option');
                metricOption.appendChild(document.createTextNode(data[i].name));
                metricOption.setAttribute('value', data[i].id);
                if (data[i].id == selectedMetricId) {
                    metricOption.setAttribute('selected', 'selected');
                }
                parameterMetricSelect.appendChild(metricOption);
            }
        }
    });
}

// Metrics modal
function openMetricsModal() {
    buildTreeMetrics();
    $("#manageMetricsModal").modal();
}

function closeMetricsModal() {
    var select = document.getElementById("parameterMetricSelect");
    select.innerHTML = "";
    if (currentSelectionId == null) {
        fillParameterMetricSelect(select, 0);
    } else {
        fillParameterMetricSelect(select, currentPatternData.forms[0].fixedPart.parameters[0].metricId);
    }
    $("#manageMetricsModal").modal('hide');
    document.getElementById("metricInfo").setAttribute('style', "display: none");
}

function buildTreeMetrics() {
    var url = "/api/qrPatternsMetrics";
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
            var metricList = document.getElementById('metricList');
            metricList.innerHTML = "";
            for (var i=0; i<data.length; i++) {
                var metric = document.createElement('li');
                metric.classList.add("list-group-item");
                metric.setAttribute("id", "metric" + data[i].id);
                metric.appendChild(document.createTextNode(data[i].name));
                metric.addEventListener("click", clickOnTreeMetrics);
                metricList.appendChild(metric);
            }
        }
    });
}

function clickOnTreeMetrics(e) {
    previousSelectionId_metric = currentSelectionId_metric;
    currentSelectionId_metric = e.target.id;
    if (previousSelectionId_metric != null) {
        document.getElementById(previousSelectionId_metric).classList.remove("active");
    }
    document.getElementById(currentSelectionId_metric).classList.add("active");
    getChosenMetric(currentSelectionId_metric.replace("metric", ""));
}

function getChosenMetric(currentMetricId) {
    var saved = document.getElementById("savedText_metric")
    if (saved != null) {
        saved.remove();
    }
    document.getElementById("metricInfo").removeAttribute('style');
    var metricInfoTitle = document.getElementById("metricInfoTitle");
    metricInfoTitle.innerHTML = "";
    metricInfoTitle.appendChild(document.createTextNode("Metric Information"));
    var url = "/api/qrPatternsMetrics/" + currentMetricId;
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
            document.getElementById("metricName").value = data.name;
            document.getElementById("metricDescription").value = data.description;
            var typeSelect = document.getElementById("typeSelect");
            typeSelect.value = data.type;
            typeSelect.setAttribute("disabled", "");
            changeTypeMetric(data.type);

            if (data.type == "integer" || data.type == "float") {
                document.getElementById("metricMinValue").value = data.minValue.toString();
                document.getElementById("metricMaxValue").value = data.maxValue.toString();
            }
            else if (data.type == "domain") {
                var stringPossibleValues = "";
                for (var i=0; i<data.possibleValues.length; i++) {
                    if (i>0) {
                        stringPossibleValues += "\n";
                    }
                    stringPossibleValues += data.possibleValues[i];
                }
                document.getElementById("metricPossibleValues").value = stringPossibleValues;
            }

            var buttonsRowMetric = document.getElementById("buttonsRowMetric");
            buttonsRowMetric.innerHTML = "";
            var deleteButtonMetric = document.createElement('button');
            deleteButtonMetric.classList.add("btn");
            deleteButtonMetric.classList.add("btn-danger");
            deleteButtonMetric.setAttribute('id', 'deleteButtonMetric');
            deleteButtonMetric.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            deleteButtonMetric.appendChild(document.createTextNode("Delete Metric"));
            deleteButtonMetric.addEventListener("click", deleteMetric);
            buttonsRowMetric.appendChild(deleteButtonMetric);
            var saveButtonMetric = document.createElement('button');
            saveButtonMetric.classList.add("btn");
            saveButtonMetric.classList.add("btn-primary");
            saveButtonMetric.setAttribute('id', 'saveButtonMetric');
            saveButtonMetric.setAttribute('style', 'font-size: 18px; max-width: 30%;');
            saveButtonMetric.appendChild(document.createTextNode("Save Metric"));
            saveButtonMetric.addEventListener("click", saveMetric);
            saveMethod_metric = "PUT";
            buttonsRowMetric.appendChild(saveButtonMetric);
        }
    })
}

function newMetric() {
    var saved = document.getElementById("savedText_metric")
    if (saved != null) {
        saved.remove();
    }
    document.getElementById("metricInfo").removeAttribute('style');
    var metricInfoTitle = document.getElementById("metricInfoTitle");
    metricInfoTitle.innerHTML = "";
    metricInfoTitle.appendChild(document.createTextNode("Step 1 - Fill the metric information"));

    if (currentSelectionId_metric != null) {
        document.getElementById(currentSelectionId_metric).classList.remove("active");
        currentSelectionId_metric = null;
    }
    document.getElementById("metricName").value = "";
    document.getElementById("metricDescription").value = "";
    var typeSelect = document.getElementById("typeSelect");
    typeSelect.value = "integer";
    typeSelect.removeAttribute("disabled");
    changeTypeMetric("integer");
    document.getElementById("metricPossibleValues").value = "";

    var buttonsRowMetric = document.getElementById("buttonsRowMetric");
    buttonsRowMetric.innerHTML = "";
    var saveButtonMetric = document.createElement('button');
    saveButtonMetric.classList.add("btn");
    saveButtonMetric.classList.add("btn-primary");
    saveButtonMetric.setAttribute('id', 'saveButtonMetric');
    saveButtonMetric.setAttribute('style', 'font-size: 18px; max-width: 30%;');
    saveButtonMetric.appendChild(document.createTextNode("Save Metric"));
    saveButtonMetric.addEventListener("click", saveMetric);
    saveMethod_metric = "POST";
    buttonsRowMetric.appendChild(saveButtonMetric);
}

function saveMetric() {
    var saved = document.getElementById("savedText_metric")
    if (saved != null) {
        saved.remove();
    }
    if ($('#metricName').val() !== "") {
        var formData = new FormData();
        formData.append("name", $('#metricName').val());
        formData.append("description", $('#metricDescription').val());
        var typeSelectValue = $('#typeSelect').val();
        formData.append("type", typeSelectValue);
        if (typeSelectValue == "integer" || typeSelectValue == "float") {
            var minValue = $('#metricMinValue').val();
            var maxValue = $('#metricMaxValue').val();
            if (minValue == "" || maxValue == "") {
                alert("Minimum value or maximum value are not numbers");
                return;
            }
            if (minValue > maxValue) {
                alert("Minimum value must be equal or smaller than maximum value");
                return;
            }
            formData.append("minValue", minValue);
            formData.append("maxValue", maxValue);
        } else if (typeSelectValue == "domain") {
            formData.append("possibleValues", $('#metricPossibleValues').val());
        }

        var url = "/api/qrPatternsMetrics";
        if (saveMethod_metric == "PUT"){ //Edit metric: add id to URL
            url += "/" + currentSelectionId_metric.replace("metric", "");
        }
        if (serverUrl) {
            url = serverUrl + url;
        }

        $.ajax({
            url: url,
            data: formData,
            type: saveMethod_metric,
            contentType: false,
            processData: false,
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 400)
                    alert("Error: Missing parameters");
                else if (jqXHR.status == 404)
                    alert("Error: This metric does not exist");
                else if (jqXHR.status == 409)
                    alert("Error: Metric name already exists");
                else {
                    alert("Internal server error");
                }
            },
            success: function() {
                buildTreeMetrics();
                if (saveMethod_metric == "PUT") {
                    document.getElementById(currentSelectionId_metric).classList.add("active");
                    var savedText = document.createElement('p');
                    savedText.setAttribute("id", "savedText_metric");
                    savedText.setAttribute("style", "color: #286090; text-align: center;");
                    savedText.appendChild(document.createTextNode("Metric saved successfully"));
                    document.getElementById("metricForm").appendChild(savedText);
                } else {
                    document.getElementById("metricInfo").setAttribute('style', "display: none");
                }
            }
        });
    }
    else {
        alert("Make sure that you have completed all fields marked with an *");
    }
}

function deleteMetric() {
    if (confirm("Are you sure you want to delete this metric?")) {
        var url = "/api/qrPatternsMetrics/" + currentSelectionId_metric.replace("metric", "");
        if (serverUrl) {
            url = serverUrl + url;
        }

        $.ajax({
            url: url,
            type: "DELETE",
            contentType: false,
            processData: false,
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 404)
                    alert("Error: This pattern does not exist");
                else if (jqXHR.status == 409)
                    alert("Error: This metric is used in one or more patterns");
                else {
                    alert("Internal server error");
                }
            },
            success: function () {
                buildTreeMetrics();
                document.getElementById("metricInfo").setAttribute("style", "display: none");
                currentSelectionId_metric = null;
            }
        });
    }
}

function changeTypeMetric(type) {
    if (type == 'integer' || type == 'float') {
        document.getElementById("minValueSection").style.display = null;
        document.getElementById("maxValueSection").style.display = null;
        document.getElementById("possibleValuesSection").style.display = "none";

        var minValue = document.getElementById("metricMinValue");
        var maxValue = document.getElementById("metricMaxValue");
        if (type == 'float') {
            minValue.setAttribute("step", "0.01");
            maxValue.setAttribute("step", "0.01");
        } else {
            minValue.removeAttribute("step");
            maxValue.removeAttribute("step");
        }
        minValue.value = "";
        maxValue.value = "";
    }
    else if (type == 'domain') {
        document.getElementById("minValueSection").style.display = "none";
        document.getElementById("maxValueSection").style.display = "none";
        document.getElementById("possibleValuesSection").style.display = null;
    }
    else {
        document.getElementById("minValueSection").style.display = "none";
        document.getElementById("maxValueSection").style.display = "none";
        document.getElementById("possibleValuesSection").style.display = "none";
    }
}

window.onload = function() {
    buildTree();
};
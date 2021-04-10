var serverUrl = sessionStorage.getItem("serverUrl");

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
            var classifier1List = document.getElementById('patternList');
            classifier1List.innerHTML = "";
            for (var i=0; i<data.length; i++) {
                var classifier1 = document.createElement('li');
                classifier1.classList.add("list-group-item");
                classifier1.classList.add("Classifier");
                classifier1.setAttribute("id", "classifier1" + data[i].id);
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
                    classifier2.setAttribute("id", ("classifier2" + data[i].internalClassifiers[j].id + "-childOf" + data[i].name));
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
    //show pattern or classifier information
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
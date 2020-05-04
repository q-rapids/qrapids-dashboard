var projects = []
var serverUrl = sessionStorage.getItem("serverUrl");

function readProjects(){
    $.ajax({
        dataType: "json",
        url: "http://localhost:8082/jasperserver/rest_v2/resources?type=reportUnit",
        type: "GET",
        contentType: "application/x-www-form-urlencoded",
        data: {
            j_username:"joeuser",
            j_password:"joeuser"
        },
        success: function (data){
            for (i=0; i < data.resourceLookup.length; i++){
                if(data.resourceLookup[i].uri.includes("QRapids")){
                    tmp = data.resourceLookup[i].uri.split("/");
                    if (!projects.includes(tmp[3])) projects.push(tmp[3]);
                }
            }
            if (projects && projects.length){
                for (i = 0; i < projects.length; i++) {
                    var opt = document.createElement("li");
                    opt.setAttribute('id', (projects[i] + 'Button'));
                    opt.setAttribute('class', 'list-group-item category-element');
                    opt.setAttribute('onclick', 'showRInfo($(this), \"' + projects[i] + '\" )');
                    opt.innerHTML = projects[i];
                    document.getElementById("ElementList").appendChild(opt);
                }
            }
            else{
                var opt = document.createElement("li");
                opt.setAttribute('class', 'list-group-item category-element');
                opt.innerHTML = "No QRapids' projects available in the server.";
                document.getElementById("ElementList").appendChild(opt);
                $("#DefaultReport").hide();
                $("#NoReport").show();
            }

        }
    });
};

function showRInfo(selectedElement, prjName){
    selectElement(selectedElement);
    linkWithJasper(prjName);
}

function selectElement (selectedElement) {
    selectedElement.addClass("active");
    $(".category-element").each(function () {
        if (selectedElement.attr("id") !== $(this).attr("id"))
            $(this).removeClass("active");
    });
}

function linkWithJasper(prjName){
    $.ajax({
        url: "http://localhost:8082/jasperserver/rest_v2/resources?type=reportUnit",
        type: "GET",
        contentType: "application/x-www-form-urlencoded",
        data: {
            j_username:"joeuser",
            j_password:"joeuser"
        },
        dataType: "json",
        success: function (data) {
            $("#DefaultReport").hide();
            $("#NoReport").hide();
            while(document.getElementById("tableREP").hasChildNodes())
                document.getElementById("tableREP").removeChild(document.getElementById("tableREP").firstChild);

            var headerText = document.createElement("th");
            headerText.innerText = "Report Name";
            var row = document.getElementById("tableREP").insertRow(-1);
            row.appendChild(headerText);
            row.appendChild(document.createElement("th"));
            row.appendChild(document.createElement("th"));
            row.appendChild(document.createElement("th"));

            for (i=0; i < data.resourceLookup.length; i++){
                if(data.resourceLookup[i].uri.includes("QRapids/" + prjName)){
                    var row = document.getElementById("tableREP").insertRow(-1);
                    var reportName = document.createElement("td");
                    reportName.appendChild(document.createTextNode(data.resourceLookup[i].label));
                    row.appendChild(reportName);

                    var buttonViewTd = document.createElement("td");
                    var buttonView = document.createElement("button");
                    buttonView.setAttribute("class", "btn btn-link");
                    buttonView.setAttribute('onclick', 'onClickBt(\"' + prjName + '\", \"' + data.resourceLookup[i].label + '\", \"html\" )');
                    buttonView.innerText = "View as HTML";
                    buttonViewTd.appendChild(buttonView);
                    row.appendChild(buttonViewTd);

                    var buttonPDFtd = document.createElement("td");
                    var buttonPDF = document.createElement("button");
                    buttonPDF.setAttribute("class", "btn btn-link");
                    buttonPDF.setAttribute('onclick', 'onClickBt(\"' + prjName + '\", \"' + data.resourceLookup[i].label + '\", \"pdf\" )');
                    buttonPDF.innerText = "Save as PDF";
                    buttonPDFtd.appendChild(buttonPDF);
                    row.appendChild(buttonPDFtd);

                    var buttonPPTtd = document.createElement("td");
                    var buttonPPT = document.createElement("button");
                    buttonPPT.setAttribute("class", "btn btn-link");
                    buttonPPT.setAttribute('onclick', 'onClickBt(\"' + prjName + '\", \"' + data.resourceLookup[i].label + '\", \"pptx\" )');
                    buttonPPT.innerText = "Save as PowerPoint";
                    buttonPPTtd.appendChild(buttonPPT);
                    row.appendChild(buttonPPTtd);
                }
            }
            $("#tableREPdiv").show();
        },
        error: function() {
            alert("Error loading reports");
        }
    });
}

function onClickBt(prjName, reportName, repType){
    var urlV = "http://localhost:8082/jasperserver/rest_v2/reports/reports/QRapids/" + prjName + "/" + reportName + "." + repType;
    while (urlV.includes(" "))
        urlV = urlV.replace(" ","_");
    while(urlV.includes("-"))
        urlV = urlV.replace("-","_")
    window.open(urlV);
}

window.onload = function() {
    readProjects();
};

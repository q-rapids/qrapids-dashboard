var dirs = []
var serverUrl = sessionStorage.getItem("serverUrl");

var jasperserverURL;
var jasperserverUser;
var jasperserverPassword;

function getJasperserverInfo() {
    var url = "/api/jasperserverInfo";
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
            jasperserverURL = data[0];
            jasperserverUser = data[1];
            jasperserverPassword = data[2];
            // we login before make other requests
            $.ajax({
                type: "POST",
                url: jasperserverURL + "/rest/login",
                data: {
                    j_username: jasperserverUser,
                    j_password: jasperserverPassword
                },
                success: function (data, textStatus, xhr) {
                    console.log("login response status: " + xhr.status);
                    readDirectories();
                }
            });

        }
    });
}

function readDirectories(){
    $.ajax({
        dataType: "json",
        url: jasperserverURL + "/rest_v2/resources?type=reportUnit",
        type: "GET",
        contentType: "application/x-www-form-urlencoded",
        data: {
            j_username:jasperserverUser,
            j_password:jasperserverPassword
        },
        success: function (data){
            for (i=0; i < data.resourceLookup.length; i++){
                // choose resource where in uri there is a Q-Rapids directory!
                if(data.resourceLookup[i].uri.includes("/Q_Rapids/")){
                    tmp = data.resourceLookup[i].uri.split("/");
                    // the last one is a report, one before is its directory
                    if (!dirs.includes(tmp[tmp.length-2])) dirs.push(tmp[tmp.length-2]);
                }
            }
            if (dirs && dirs.length){
                // first we need to sort directories in a case-insensitive way
                dirs.sort((a,b) => (a.toLowerCase() > b.toLowerCase()) ? 1 : ((b.toLowerCase() > a.toLowerCase()) ? -1 : 0));
                // then make a list of buttons
                var dirsList = dirs.slice();
                for (i = 0; i < dirsList.length; i++) {
                    // change _ to space or -
                    if (dirsList[i] == "Q_Rapids") dirsList[i] = dirsList[i].replace("_","-");
                    else {
                        while (dirsList[i].includes("_"))
                            dirsList[i] = dirsList[i].replace("_"," ");
                    }
                    var opt = document.createElement("li");
                    opt.setAttribute('id', (dirs[i] + 'Button'));
                    opt.setAttribute('class', 'list-group-item category-element');
                    opt.setAttribute('onclick', 'showRInfo($(this), \"' + dirs[i] + '\" )');
                    opt.innerHTML = dirsList[i];
                    document.getElementById("ElementList").appendChild(opt);
                }
            }
            else{
                var opt = document.createElement("li");
                opt.setAttribute('class', 'list-group-item category-element');
                opt.innerHTML = "No Q-Rapids' reports available in the server.";
                document.getElementById("ElementList").appendChild(opt);
            }
        }
    });
};

function showRInfo(selectedElement, dirName){
    selectElement(selectedElement);
    linkWithJasper(dirName);
}

function selectElement (selectedElement) {
    selectedElement.addClass("active");
    $(".category-element").each(function () {
        if (selectedElement.attr("id") !== $(this).attr("id"))
            $(this).removeClass("active");
    });
}

function linkWithJasper(dirName){
    $.ajax({
        dataType: "json",
        url: jasperserverURL + "/rest_v2/resources?type=reportUnit",
        type: "GET",
        contentType: "application/x-www-form-urlencoded",
        data: {
            j_username:jasperserverUser,
            j_password:jasperserverPassword
        },
        dataType: "json",
        success: function (data) {
            while(document.getElementById("tableREP").hasChildNodes())
                document.getElementById("tableREP").removeChild(document.getElementById("tableREP").firstChild);
            // add header to table --> made in html
            // add data rows to table
            for (i=0; i < data.resourceLookup.length; i++){
                // find reports for specified directory
                if(data.resourceLookup[i].uri.includes("/Q_Rapids/")) {
                    tmp = data.resourceLookup[i].uri.split("/");
                    // take a look is resource directory is correspond with selected one
                    if (tmp[tmp.length - 2] == dirName) {
                        var row = document.getElementById("tableREP").insertRow(-1);
                        var reportName = document.createElement("td");
                        reportName.appendChild(document.createTextNode(data.resourceLookup[i].label));
                        row.appendChild(reportName);

                        var buttonViewTd = document.createElement("td");
                        var buttonView = document.createElement("button");
                        buttonView.setAttribute("class", "btn btn-link");
                        buttonView.setAttribute('onclick', 'onClickBt(\"' + data.resourceLookup[i].uri + '\", \"html\" )');
                        buttonView.innerText = "View as HTML";
                        buttonViewTd.appendChild(buttonView);
                        row.appendChild(buttonViewTd);

                        var buttonPDFtd = document.createElement("td");
                        var buttonPDF = document.createElement("button");
                        buttonPDF.setAttribute("class", "btn btn-link");
                        buttonPDF.setAttribute('onclick', 'onClickBt(\"' + data.resourceLookup[i].uri + '\", \"pdf\" )');
                        buttonPDF.innerText = "Save as PDF";
                        buttonPDFtd.appendChild(buttonPDF);
                        row.appendChild(buttonPDFtd);

                        var buttonPPTtd = document.createElement("td");
                        var buttonPPT = document.createElement("button");
                        buttonPPT.setAttribute("class", "btn btn-link");
                        buttonPPT.setAttribute('onclick', 'onClickBt(\"' + data.resourceLookup[i].uri + '\", \"pptx\" )');
                        buttonPPT.innerText = "Save as PowerPoint";
                        buttonPPTtd.appendChild(buttonPPT);
                        row.appendChild(buttonPPTtd);
                    }
                }
            }
            $("#tableREPdiv").show();
        },
        error: function() {
            alert("Error loading reports");
        }
    });
}

function onClickBt(uriReport, repType){
     // get project id from project selector
    var p = sessionStorage.getItem("prj");
    var urlV = jasperserverURL + "/rest_v2/reports" + uriReport + "." + repType + "?projID=" + p
        + "&from=" + $('#datepickerFrom').val() + "&to=" + $('#datepickerTo').val()
        +"&j_username=" + jasperserverUser + "&j_password="+jasperserverPassword; // remove credentials from url
    console.log(urlV);
    window.open(urlV);
}

// search function
$(document).ready(function(){
    $("#searchInput").on("keyup", function() {
        var value = $(this).val().toLowerCase();
        $("#tableREP tr").filter(function() {
            $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
    });
});

window.onload = function() {
    getJasperserverInfo();
    // remove projectSelectorDiv old css class and put new one
    const el = document.querySelector('#projectSelectorDiv');
    if (el.classList.contains("col-xs-4")) {
        el.classList.remove("col-xs-4");
        el.classList.add("col-reporting");
        $('.col-reporting').css('padding-right', '15px');
        $('.col-reporting').css('padding-left', '15px');
        // also correct css of datapicker
        $('.well').css('margin-top', '10px');
        $('.well').css('margin-bottom', '10px');
    }
};

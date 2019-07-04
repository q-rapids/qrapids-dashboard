var serverUrl = sessionStorage.getItem("serverUrl");

function buildSIList() {
    var url = "/api/StrategicIndicators";
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
            var SIList = document.getElementById('SIList');
            for (var i = 0; i < data.length; i++) {
                var SI = document.createElement('li');
                SI.classList.add("list-group-item");
                SI.classList.add("Product");
                SI.setAttribute("id", ("SI" + data[i].id));
                SI.appendChild(document.createTextNode(data[i].name));
                SI.addEventListener("click", clickOnTree);

                SIList.appendChild(SI);
            }
            document.getElementById('SITree').appendChild(SIList);
        }
    });
};

function clickOnTree(e){
    console.log(currentSelectionId);
    if (e.target.classList.contains("Project")) {
        currentSelection = "Project";
        previousSelectionId = currentSelectionId;
        currentSelectionId = e.target.id;
        if (previousSelectionId != null) {
            document.getElementById(previousSelectionId).setAttribute('style', 'background-color: #ffffff;');
        }
        document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
        var idString = e.target.id.split("-")[0];
        console.log(idString.replace("project", ""));
        getChosenProject(idString.replace("project", ""));
    } else if (e.target.classList.contains("Product")) {
        currentSelection = "Project";
        previousSelectionId = currentSelectionId;
        currentSelectionId = e.target.id;
        if (previousSelectionId != null) {
            document.getElementById(previousSelectionId).setAttribute('style', 'background-color: #ffffff;');
        }
        document.getElementById(currentSelectionId).setAttribute('style', 'background-color: #efeff8;');
        getChosenProduct(e.target.id.replace("product", ""));
    }
}

window.onload = function() {
    buildSIList();
};
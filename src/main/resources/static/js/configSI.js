var serverUrl = sessionStorage.getItem("serverUrl");

function newSI() {
    $("#SIInfo").show();
    $.ajax({
        url: "../api/QualityFactors/CurrentEvaluation",
        type: "GET",
        success: function(data) {
            for(i = 0; i < data.length; ++i) {
                $('#avFactorsBox').append($('<option>', {
                    value: data[i].id,
                    text: data[i].name
                }));
            }
        }
    });
}

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

}

function moveItemsLeft() {
    $('#selFactorsBox').find(':selected').appendTo('#avFactorsBox');
};

function moveAllItemsLeft() {
    $('#selFactorsBox').children().appendTo('#avFactorsBox');
};

function moveItemsRight() {
    $('#avFactorsBox').find(':selected').appendTo('#selFactorsBox');
};

function moveAllItemsRight() {
    $('#avFactorsBox').children().appendTo('#selFactorsBox');
};

window.onload = function() {
    buildSIList();
};
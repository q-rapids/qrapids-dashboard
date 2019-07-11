var serverUrl = sessionStorage.getItem("serverUrl");

var factors = [];

var postUrl;
var deleteUrl;

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
                SI.setAttribute("id", (data[i].id));
                SI.appendChild(document.createTextNode(data[i].name));
                SI.addEventListener("click", clickOnTree);

                SIList.appendChild(SI);
            }
            document.getElementById('SITree').appendChild(SIList);
        }
    });
}

function clickOnTree(e){
    postUrl = "/api/EditStrategicIndicator/" + e.target.id;
    deleteUrl = "/api/StrategicIndicators/" + e.target.id;
    if (serverUrl) {
        postUrl = serverUrl + postUrl;
    }
    jQuery.ajax({
        dataType: "json",
        url: postUrl,
        cache: false,
        type: "GET",
        async: true,
        success: function (si) {
            $("#SIInfo").show();
            $("#SIInfoTitle").text("Strategic Indicator Information");
            $("#SIName").val(si.name);
            $("#SIDescription").val(si.description);
            $("#SINetworkLabel").html("Assessment Model: <br/>(leave empty if unchanged)");
            $("#SINetwork").val("");
            $("#SICompositionTitle").text("Strategic Indicator Composition");
            $("#deleteSI").show();
            if (factors.length > 0) {
                showFactors();
                si.quality_factors.forEach(function (factor) {
                    $('#avFactorsBox').find("option[value='" + factor + "']").appendTo('#selFactorsBox');
                });
            }
        }
    });
}

function newSI() {
    $("#SIInfo").show();
    $("#SIInfoTitle").text("1. Strategic Indicator Information");
    $("#SIName").val("");
    $("#SIDescription").val("");
    $("#SINetworkLabel").html("Assessment Model: ");
    $("#SINetwork").val("");
    $("#SICompositionTitle").text("2. Strategic Indicator Composition");
    $("#deleteSI").hide();
    if (factors.length > 0)
        showFactors();
    else {
        loadFactors(true);
    }
    postUrl="/api/newStrategicIndicator";
}

function showFactors () {
    $('#avFactorsBox').empty();
    $('#selFactorsBox').empty();
    factors.forEach(function (factor) {
        $('#avFactorsBox').append($('<option>', {
            value: factor.id,
            text: factor.name
        }));
    });
}

function loadFactors (show) {
    $.ajax({
        url: "../api/QualityFactors/CurrentEvaluation",
        type: "GET",
        async: true,
        success: function(data) {
            data.forEach(function (factor) {
                factors.push(factor);
            });
            if (show)
                showFactors();
        }
    });
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

$("#saveSI").click(function () {
    var qualityFactors = [];

    $('#selFactorsBox').children().each (function (i, option) {
        qualityFactors.push(option.value);
    });

    if ($('#SIName').val() !== "" && qualityFactors.length > 0) {

        var formData = new FormData();
        formData.append("name", $('#SIName').val());
        formData.append("description", $('#SIDescription').val());
        formData.append("network", $('#SINetwork')[0].files[0]);
        formData.append("quality_factors", qualityFactors);

        $.ajax({
            url: postUrl,
            data: formData,
            type: "POST",
            contentType: false,
            processData: false,
            //ToDo: the service produces more than one error, the current message does not fit all of them
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status === 409)
                    alert("This Strategic Indicator name is already in use");
                else {
                    alert("Error in the ElasticSearch: contact to the system administrator");
                    location.href = "../StrategicIndicators/Configuration";
                }
            },
            success: function() {
                location.href = "../StrategicIndicators/Configuration";
            }
        });
    } else alert("Make sure that you have completed all fields marked with an *");
});

$("#deleteSI").click(function () {
    if (confirm("Are you sure you want to delete this Strategic Indicator?")) {
        jQuery.ajax({
            url: deleteUrl,
            cache: false,
            type: "DELETE",
            async: true,
            success: function () {
                location.href = "../StrategicIndicators/Configuration";
            }
        });
    }
});

window.onload = function() {
    loadFactors(false);
    buildSIList();
};
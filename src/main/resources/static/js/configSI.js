var serverUrl = sessionStorage.getItem("serverUrl");

var factors = [];

var postUrl;
var deleteUrl;
var httpMethod = "POST";

function buildSIList() {
    var url = "/api/strategicIndicators";
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
                SI.classList.add("SI");
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
    e.target.classList.add("active");
    $(".SI").each(function () {
        if (e.target.id !== $(this).attr('id'))
            $(this).removeClass("active");
    });

    postUrl = "/api/strategicIndicators/" + e.target.id;
    httpMethod = "PUT";
    deleteUrl = "/api/strategicIndicators/" + e.target.id;
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
            console.log ("SI concret:");
            console.log(si);
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
                si.qualityFactors.forEach(function (factor) {
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
    postUrl="../api/strategicIndicators";
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
        url: "../api/qualityFactors",
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

var checkbox = document.getElementById('weight');
checkbox.addEventListener("change", validaCheckbox, false);
function validaCheckbox(){
    var checked = checkbox.checked;
    if(checked){
        var qualityFactors = getSelectedFactors(false);
        console.log(qualityFactors);
        if (qualityFactors.length > 0) {
            $("#weightsItems").empty();
            var i = 0;
            qualityFactors.forEach(function (qf) {
                var id = "editor"+i;
                $("#weightsItems").append('<tr class="phaseItem"><td>' + qf + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
                //$('"#'+id+'"').inputmask({"mask": "999"});
                i++;
            });
            $("#weightsModal").modal();
        } else alert('You have no selected factors.');
    }
}
function uncheck() {
    document.getElementById("weight").checked = false;
}
uncheck();

function openEdit() {  // This function is called by the checkbox click
    if (document.getElementById('weight').checked == true) { // If it is checked
        document.getElementById('weightEditButton').disabled = false; // Then we remove the disable attribute
    }
}

$("#weightEditButton").click(function () {
    alert("Open a modal with corresponding values.");
});

var weightsForFactors = [];

$("#submitWeightsButton").click(function () {
    var qualityFactors = getSelectedFactors(false);
    var i = 0;
    var totalSum = 0;
    weightsForFactors = [];
    qualityFactors.forEach(function (qf) {
        var id = "editor"+i;
        var cell = document.getElementById(id);
        var cellValue = cell.innerText;
        var weightValue = parseFloat(cellValue);
        if (isNaN(weightValue)) {
            alert("The value " + cellValue + " is not a number");
        } else {
            totalSum += weightValue;
            weightsForFactors.push([qf, weightValue]);
        }
        i++;
    });
    if (totalSum != 100) alert("Total sum is not equals to 100.");
    else $("#weightsModal").modal('hide');
    console.log(weightsForFactors);
});

function getSelectedFactors(final) {
    var qualityFactors = [];

    if (final) {
        $('#selFactorsBox').children().each (function (i, option) {
            qualityFactors.push([option.value, -1]);
        });
    } else {
        $('#selFactorsBox').children().each (function (i, option) {
            qualityFactors.push(option.value);
        });
    }

    return qualityFactors;
}

$("#saveSI").click(function () {
    var qualityFactors;
    if (weightsForFactors.length == 0){
        qualityFactors = getSelectedFactors(true);
    } else {
        qualityFactors = weightsForFactors;
    }

    if ($('#SIName').val() !== "" && qualityFactors.length > 0) {
        var formData = new FormData();
        formData.append("name", $('#SIName').val());
        formData.append("description", $('#SIDescription').val());
        var file = $('#SINetwork').prop('files')[0];
        if (file)
            formData.append("network", file);
        formData.append("quality_factors", qualityFactors);
        $.ajax({
            url: postUrl,
            data: formData,
            type: httpMethod,
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
var serverUrl = sessionStorage.getItem("serverUrl");

var factors = [];
var weightsForFactors = [];

var postUrl;
var deleteUrl;
var httpMethod = "POST";

var assessSI_url;

function getAssessSIUrl () {
    if (serverUrl) {
        var url = serverUrl + "/api/assessSIUrl";
    }
    jQuery.ajax({
        dataType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            console.log(data.assessSIUrl);
            assessSI_url = data.assessSIUrl;
        }
    });
}

function buildSIList() {
    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));
    var profileId = sessionStorage.getItem("profile_id");
    var url = "/api/strategicIndicators?profile=" + profileId;
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
            console.log("buildSIList");
            console.log(data);
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
        deleteUrl = serverUrl + deleteUrl;
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
            $("div.SIInfoRowID").show();
            $("#SIAssessmentID").val(si.externalId);
            $("#SIName").attr("placeholder", "Write the strategic indicator name here");
            $("#SIName").val(si.name);
            $("#SIDescription").attr("placeholder", "Write the strategic indicator description here");
            $("#SIDescription").val(si.description);
            $("#SIThreshold").attr("placeholder", "Specify minimum acceptable value for the strategic indicator here");
            $("#SIThreshold").val(si.threshold);
            if (assessSI_url != '') {
                $("#SINetworkLabel").html("Assessment Model: <br/>(leave empty if unchanged)");
                $("#SINetwork").val("");
            } else {
                $("#SINetworkLabel").hide();
                $("#SINetwork").hide();
            }
            $("#SICompositionTitle").text("Strategic Indicator Composition");
            $("#SICompositionWarning").text("Warning: Changing the composition of Strategic Indicator will affect its historical data interpretation."); // add warning
            $("#deleteSI").show();
            if (factors.length > 0) {
                showFactors();
                si.qualityFactors.forEach(function (factor) {
                    $('#avFactorsBox').find("option[value='" + factor + "']").appendTo('#selFactorsBox');
                });
            }
            document.getElementById('weightCheckbox').checked = si.weighted;
            document.getElementById('weightEditButton').disabled = !si.weighted;
            console.log(si.qualityFactorsWeights);
            if (si.weighted) weightsForFactors = si.qualityFactorsWeights;
            else weightsForFactors = [];
            console.log("clickOnTree: weightsForFactors");
            console.log(weightsForFactors);
        }
    });
}

function newSI() {
    // clean temporal var and remove active list item
    weightsForFactors = [];
    httpMethod = "POST";
    // clean selected items on SI list
    $(".SI").each(function () {
        $(this).removeClass("active");
    });
    // make new Strategic Indicator form
    $("#SIInfo").show();
    $("#SIInfoTitle").text("Step 1 - Fill the strategic indicator information");
    $("div.SIInfoRowID").hide();
    $("#SIAssessmentID").val("");
    $("#SIName").attr("placeholder", "Write the strategic indicator name here");
    $("#SIName").val("");
    $("#SIDescription").attr("placeholder", "Write the strategic indicator description here");
    $("#SIDescription").val("");
    $("#SIThreshold").attr("placeholder", "Specify minimum acceptable value for the strategic indicator here");
    $("#SIThreshold").val("");
    if (assessSI_url != '') {
        $("#SINetworkLabel").html("Assessment Model: ");
        $("#SINetwork").val("");
    } else {
        $("#SINetworkLabel").hide();
        $("#SINetwork").hide();
    }
    $("#SICompositionTitle").text("Step 2 - Select the corresponding factors");
    $("#SICompositionWarning").text(""); // clean warning
    $("#deleteSI").hide();
    if (factors.length > 0)
        showFactors();
    else {
        loadFactors(true);
    }
    document.getElementById('weightCheckbox').checked = false;
    document.getElementById('weightEditButton').disabled = true;
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
    // get factors from DB
    $.ajax({
        url: "../api/qualityFactors",
        type: "GET",
        async: true,
        success: function(data) {
            console.log("loadFactors");
            console.log(data);
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

var checkbox = document.getElementById('weightCheckbox');
checkbox.addEventListener("change", validaCheckbox, false);
function validaCheckbox(){
    var checked = checkbox.checked;
    if(checked){
        var qualityFactors = getSelectedFactors(false);
        if (qualityFactors.length > 0) {
            $("#SIweightsItems").empty();
            var i = 0;
            qualityFactors.forEach(function (qf) {
                var selectedFactor;
                var found = false;
                var j = 0;
                while (j < factors.length && !found) {
                    if (factors[j].id == qf) {
                        found = true;
                        selectedFactor = factors[j].name;
                    }
                    j++;
                }
                var id = "editor"+i;
                $("#SIweightsItems").append('<tr class="weightItem"><td>' + selectedFactor + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
                // add listeners which control if we try to input letters, floats, negative values or zero
                var cell = document.getElementById(id);
                cell.addEventListener('keydown', onlyNumbers);
                i++;
            });
            $("#SIweightsModal").modal();
        } else {
            alert('You have no selected factors.');
            document.getElementById('weightCheckbox').checked = false;
        }
    }
    if (!checked) {
        if (weightsForFactors.length > 0) {
            var c = confirm('You will lose the values of factors weights for this Strategic Indicator. Do you want to continue?');
            if (c) {
                weightsForFactors = [];
            } else {
                document.getElementById('weightCheckbox').checked = true;
            }
        }
    }
}

function onlyNumbers(e){
    var key = window.Event ? e.which : e.keyCode;
    // numbers from 0 to 9 and not
    if ((key < 48 || key > 57) && (key!==8))
        e.preventDefault();
}

function openEdit() {  // This function is called by the checkbox click
    if (document.getElementById('weightCheckbox').checked == true) { // If it is checked
        document.getElementById('weightEditButton').disabled = false; // Then we remove the disabled attribute
    }
}


$("#weightEditButton").click(function () {
    var wff = String(weightsForFactors).split(",");
    var selector = getSelectedFactors(false);
    if (selector.length > 0) {
        $("#SIweightsItems").empty();
        var i = 0;
        selector.forEach(function (qf) {
            var id = "editor"+i;
            var selectedFactor;
            var found = false;
            var j = 0;
            while (j < factors.length && !found) {
                if (factors[j].id == qf) {
                    found = true;
                    selectedFactor = factors[j].name;
                }
                j++;
            }
            if (wff.includes(qf)) {
                $("#SIweightsItems").append('<tr class="weightItem"><td>' + selectedFactor + '</td><td id="' + id + '" contenteditable="true">' + Math.floor(wff[wff.indexOf(qf)+1]) +'</tdid>');
            } else {
                $("#SIweightsItems").append('<tr class="weightItem"><td>' + selectedFactor + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
            }
            // add listeners which control if we try to input letters, floats, negative values or zero
            var cell = document.getElementById(id);
            cell.addEventListener('keydown', onlyNumbers);
            i++;
        });
        $("#SIweightsModal").modal();
    } else {
        alert('You have no selected factors.');
        document.getElementById('weightCheckbox').checked = false;
        document.getElementById('weightEditButton').disabled = true;
    }
    return false;
});

$("#SIsubmitWeightsButton").click(function () {
    var qualityFactors = getSelectedFactors(false);
    var i = 0;
    var totalSum = 0;
    aux = [];
    var ok = true;
    while (i < qualityFactors.length && ok) {
        var id = "editor"+i;
        var cell = document.getElementById(id);
        var cellValue = cell.innerText;
        var weightValue = Math.floor(cellValue);
        if (isNaN(weightValue) || weightValue <= 0) {
            ok = false;
        } else {
            totalSum += weightValue;
            aux.push(qualityFactors[i], weightValue);
        }
        i++;
    }
    if (!ok) alert ("Check the form fields, there may be one of the following errors:\n" +
        "- Empty fields\n" +
        "- Zero value");
    else {
        if (totalSum != 100) alert("Total sum is not equals to 100.");
        else {
            weightsForFactors = aux;
            $("#SIweightsModal").modal('hide');
        }
    }
});

$("#SIcloseWeightsButton").click(function () {
    if (!weightsForFactors.length) {
        document.getElementById('weightCheckbox').checked = false;
        document.getElementById('weightEditButton').disabled = true;
    }
    $("#SIweightsModal").modal('hide');
});

function getSelectedFactors(final) {
    var qualityFactors = [];

    if (final) {
        $('#selFactorsBox').children().each (function (i, option) {
            qualityFactors.push(option.value, -1);
        });
    } else {
        $('#selFactorsBox').children().each (function (i, option) {
            qualityFactors.push(option.value);
        });
    }

    return qualityFactors;
}

$('#weightCheckbox').change(function(){
    if($(this).is(':checked')) {
        // Checkbox is checked..
        document.getElementById('weightEditButton').disabled = false;
    } else {
        // Checkbox is not checked..
        document.getElementById('weightEditButton').disabled = true;
    }
});

function checkTotalSum () {
    var qualityFactors = getSelectedFactors(false);
    var wff = String(weightsForFactors).split(",");
    console.log(wff);
    var totalSum = 0;
    for (var i = 0; i < qualityFactors.length; i++){
        if (wff.includes(qualityFactors[i])) totalSum += parseFloat(wff[wff.indexOf(qualityFactors[i])+1]);
    }
    console.log(totalSum);
    return totalSum == 100 && (qualityFactors.length == wff.length/2);
}

$("#saveSI").click(function () {
    var qualityFactors;
    var totalSum = true;
    // when quality factors a not weighted
    if ((document.getElementById('weightCheckbox').checked == false) || (weightsForFactors.length == 0)){
        qualityFactors = getSelectedFactors(true);
    } else { // when quality factors a weighted
        if (!checkTotalSum()) {
            totalSum = false;
            alert("Total sum is not equals to 100.");
        }
        qualityFactors = weightsForFactors;
    }

    if ($('#SIName').val() !== "" && qualityFactors.length > 0 && totalSum) {

        console.log("QF que envio:");
        console.log(qualityFactors);

        var formData = new FormData();
        formData.append("name", $('#SIName').val());
        formData.append("description", $('#SIDescription').val());
        formData.append("threshold", $('#SIThreshold').val());
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
    } else alert("Make sure that you have completed correctly all fields marked with an *");
});

$("#deleteSI").click(function () {
    if (confirm("\t\t This operation cannot be undone. \t\n Are you sure you want to delete this strategic indicator?")) {
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
    getAssessSIUrl();
};
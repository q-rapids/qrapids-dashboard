var serverUrl = sessionStorage.getItem("serverUrl");

var metrics = [];
var weightsForMetrics = [];

var postUrl;
var deleteUrl;
var httpMethod = "POST";

function buildQFList() {
    var profileId = sessionStorage.getItem("profile_id");
    var url = "/api/qualityFactors?profile=" + profileId;
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
            var QFList = document.getElementById('QFList');
            for (var i = 0; i < data.length; i++) {
                var QF = document.createElement('li');
                QF.classList.add("list-group-item");
                QF.classList.add("QF");
                QF.setAttribute("id", (data[i].id));
                QF.appendChild(document.createTextNode(data[i].name));
                QF.addEventListener("click", clickOnTree);
                QFList.appendChild(QF);
            }
            document.getElementById('QFTree').appendChild(QFList);
        }
    });
}

function clickOnTree(e){
    e.target.classList.add("active");
    $(".QF").each(function () {
        if (e.target.id !== $(this).attr('id'))
            $(this).removeClass("active");
    });
    postUrl = "/api/qualityFactors/" + e.target.id;
    httpMethod = "PUT";
    deleteUrl = "/api/qualityFactors/" + e.target.id;
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
        success: function (qf) {
            $("#QFInfo").show();
            $("#QFInfoTitle").text("Quality Factor Information");
            $("div.QFInfoRowID").show();
            $("#QFAssessmentID").val(qf.externalId);
            $("#QFName").attr("placeholder", "Write the quality factor name here");
            $("#QFName").val(qf.name);
            $("#QFDescription").attr("placeholder", "Write the quality factor description here");
            $("#QFDescription").val(qf.description);
            $("#QFThreshold").attr("placeholder", "Specify minimum acceptable value for the quality factor here");
            $("#QFThreshold").val(qf.threshold);
            $("#QFCompositionTitle").text("Quality Factor Composition");
            $("#QFCompositionWarning").text("Warning: Changing the composition of Quality Factor will affect its historical data interpretation."); // add warning
            $("#deleteQF").show();
            if (metrics.length > 0) {
                showMetrics();
                qf.metrics.forEach(function (metric) {
                    // value use metric id (because we have metric in DB)
                    $('#avMetricsBox').find("option[value='" + metric + "']").appendTo('#selMetricsBox');
                });
            }
            document.getElementById('QFweightCheckbox').checked = qf.weighted;
            document.getElementById('QFweightEditButton').disabled = !qf.weighted;
            console.log("clickOnTree: qf.metricsWeights")
            console.log(qf.metricsWeights);
            if (qf.weighted) weightsForMetrics = qf.metricsWeights;
            else weightsForMetrics = [];
            console.log("clickOnTree: weightsForMetrics");
            console.log(weightsForMetrics);
        }
    });
}

function newQF() {
    // clean temporal var and remove active list item
    weightsForMetrics = [];
    httpMethod = "POST";
    $(".QF").each(function () {
        $(this).removeClass("active");
    });
    // make new Quality Factor form
    $("#QFInfo").show();
    $("#QFInfoTitle").text("Step 1 - Fill the quality factor information");
    $("div.QFInfoRowID").hide();
    $("#QFAssessmentID").val("");
    $("#QFName").attr("placeholder", "Write the quality factor name here");
    $("#QFName").val("");
    $("#QFDescription").attr("placeholder", "Write the quality factor description here");
    $("#QFDescription").val("");
    $("#QFThreshold").attr("placeholder", "Specify minimum acceptable value for the quality factor here");
    $("#QFThreshold").val("");
    $("#QFCompositionTitle").text("Step 2 - Select the corresponding metrics");
    $("#QFCompositionWarning").text(""); // clean warning
    $("#deleteQF").hide();
    if (metrics.length > 0)
        showMetrics();
    else {
        loadMetrics(true);
    }
    document.getElementById('QFweightCheckbox').checked = false;
    document.getElementById('QFweightEditButton').disabled = true;
    postUrl="../api/qualityFactors";
}

function showMetrics () {
    $('#avMetricsBox').empty();
    $('#selMetricsBox').empty();
    metrics.forEach(function (metric) {
        $('#avMetricsBox').append($('<option>', {
            value: metric.id,
            text: metric.name
        }));
    });
}

function loadMetrics (show) {
    // get metrics from DB
    $.ajax({
        url: "../api/metrics",
        type: "GET",
        async: true,
        success: function(data) {
            data.forEach(function (metric) {
                metrics.push(metric);
            });
            if (show)
                showMetrics();
        }
    });
}

function moveItemsLeft() {
    $('#selMetricsBox').find(':selected').appendTo('#avMetricsBox');
};

function moveAllItemsLeft() {
    $('#selMetricsBox').children().appendTo('#avMetricsBox');
};

function moveItemsRight() {
    $('#avMetricsBox').find(':selected').appendTo('#selMetricsBox');
};

function moveAllItemsRight() {
    $('#avMetricsBox').children().appendTo('#selMetricsBox');
};

var checkbox = document.getElementById('QFweightCheckbox');
checkbox.addEventListener("change", validaCheckbox, false);
function validaCheckbox(){
    var checked = checkbox.checked;
    if(checked){
        var qualityMetrics = getSelectedMetrics(false);
        if (qualityMetrics.length > 0) {
            $("#QFweightsItems").empty();
            var i = 0;
            qualityMetrics.forEach(function (qm) {
                var selectedMetric;
                var j = 0;
                var found = false;
                while (j < metrics.length && !found) {
                    if (metrics[j].id == qm) {
                        found = true;
                        selectedMetric = metrics[j].name;
                    }
                    j++;
                }
                var id = "editor"+i;
                $("#QFweightsItems").append('<tr class="weightItem"><td>' + selectedMetric + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
                // add listeners which control if we try to input letters, floats, negative values or zero
                var cell = document.getElementById(id);
                cell.addEventListener('keydown', onlyNumbers);
                i++;
            });
            $("#QFweightsModal").modal();
        } else {
            warningUtils("Warning", "You have no selected metrics.");
            //alert('You have no selected metrics.');
            document.getElementById('QFweightCheckbox').checked = false;
            document.getElementById('QFweightCheckbox').checked = false;
        }
    }
    if (!checked) {
        if (weightsForMetrics.length > 0) {
            var c = confirm('You will lose the values of metrics weights for this Quality Factor. Do you want to continue?');
            if (c) {
                weightsForMetrics = [];
            } else {
                document.getElementById('QFweightCheckbox').checked = true;
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
    if (document.getElementById('QFweightCheckbox').checked == true) { // If it is checked
        document.getElementById('QFweightEditButton').disabled = false; // Then we remove the disabled attribute
    }
}


$("#QFweightEditButton").click(function () { // (...) btn
    var selector = getSelectedMetrics(false);
    if (selector.length > 0) {
        $("#QFweightsItems").empty();
        var i = 0; // used for id counter
        selector.forEach(function (m) {
            var id = "editor"+i;
            var selectedMetric;
            var j = 0;
            var found = false;
            while (j < metrics.length && !found) {
                if (metrics[j].id == m) {
                    found = true;
                    selectedMetric = metrics[j].name;
                }
                j++;
            }
            if (weightsForMetrics.includes(m)) {
                $("#QFweightsItems").append('<tr class="weightItem"><td>' + selectedMetric + '</td><td id="' + id + '" contenteditable="true">' + Math.floor(weightsForMetrics[weightsForMetrics.indexOf(m)+1]) +'</tdid>');
            } else {
                $("#QFweightsItems").append('<tr class="weightItem"><td>' + selectedMetric + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
            }
            // add listeners which control if we try to input letters, floats, negative values or zero
            var cell = document.getElementById(id);
            cell.addEventListener('keydown', onlyNumbers);
            i++;
        });
        $("#QFweightsModal").modal();
    } else {
        warningUtils("Warning", "You have no selected metrics.");
        //alert('You have no selected metrics.');
        document.getElementById('QFweightCheckbox').checked = false;
        document.getElementById('QFweightEditButton').disabled = true;
    }
    return false;
});

$("#QFsubmitWeightsButton").click(function () {
    var qualityMetrics = getSelectedMetrics(false);
    var i = 0;
    var totalSum = 0;
    aux = [];
    var ok = true;
    while (i < qualityMetrics.length && ok) {
        var id = "editor"+i;
        var cell = document.getElementById(id);
        var cellValue = cell.innerText;
        var weightValue = Math.floor(cellValue);
        if (isNaN(weightValue) || weightValue <= 0) {
            ok = false;
        } else {
            totalSum += weightValue;
            aux.push(qualityMetrics[i], weightValue);
        }
        i++;
    }
    if (!ok) alert ("Check the form fields, there may be one of the following errors:\n" +
        "- Empty fields\n" +
        "- Zero value");
    else {
        if (totalSum != 100) {
            warningUtils("Warning", "Total sum is not equals to 100.");
            //alert("Total sum is not equals to 100.");
        }
        else {
            weightsForMetrics = aux;
            $("#QFweightsModal").modal('hide');
            console.log("submitWeightsButton: weightsForMetrics");
            console.log(weightsForMetrics);
        }
    }
});

$("#QFcloseWeightsButton").click(function () {
    if (!weightsForMetrics.length) {
        document.getElementById('QFweightCheckbox').checked = false;
        document.getElementById('QFweightEditButton').disabled = true;
    }
    $("#QFweightsModal").modal('hide');
});

function getSelectedMetrics(final) {
    var metrics = [];

    if (final) {
        $('#selMetricsBox').children().each (function (i, option) {
            metrics.push(option.value, -1);
        });
    } else {
        $('#selMetricsBox').children().each (function (i, option) {
            metrics.push(option.value);
        });
    }

    return metrics;
}

$('#QFweightCheckbox').change(function(){
    if($(this).is(':checked')) {
        // Checkbox is checked..
        document.getElementById('QFweightEditButton').disabled = false;
    } else {
        // Checkbox is not checked..
        document.getElementById('QFweightEditButton').disabled = true;
    }
});

function checkTotalSum () {
    var qualityMetrics = getSelectedMetrics(false);
    var totalSum = 0;
    for (var i = 0; i < qualityMetrics.length; i++){
        if (weightsForMetrics.includes(qualityMetrics[i])) totalSum += parseFloat(weightsForMetrics[weightsForMetrics.indexOf(qualityMetrics[i])+1]);
    }
    return totalSum == 100 && (qualityMetrics.length == weightsForMetrics.length/2);
}

$("#saveQF").click(function () {
    var qualityMetrics;
    var totalSum = true;
    // when quality factors a not weighted
    if ((document.getElementById('QFweightCheckbox').checked == false) || (weightsForMetrics.length == 0)){
        qualityMetrics = getSelectedMetrics(true);
    } else { // when quality factors a weighted
        if (!checkTotalSum()) {
            totalSum = false;
            warningUtils( "Warning", "Total sum is not equals to 100.");
            //alert("Total sum is not equals to 100.");
        }
        qualityMetrics = weightsForMetrics;
    }


    if ($('#QFName').val() !== "" && qualityMetrics.length > 0 && totalSum) {

        console.log("QM que envio:");
        console.log(qualityMetrics);
        console.log(httpMethod);
        console.log(postUrl);

        var formData = new FormData();
        formData.append("name", $('#QFName').val());
        formData.append("description", $('#QFDescription').val());
        formData.append("threshold", $('#QFThreshold').val());
        formData.append("metrics", qualityMetrics);
        $.ajax({
            url: postUrl,
            data: formData,
            type: httpMethod,
            contentType: false,
            processData: false,
            //ToDo: the service produces more than one error, the current message does not fit all of them
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status === 409)
                    warningUtils("Error", "This Quality Factor name is already in use");
                    //alert("This Quality Factor name is already in use");
                else {
                    warningUtils("Error","Error in the ElasticSearch: contact to the system administrator");
                    //alert("Error in the ElasticSearch: contact to the system administrator");
                    location.href = "../QualityFactors/Configuration";
                }
            },
            success: function() {
                // after edit factor we need to assess si, in order to update its information
                $.ajax({
                    url: "../api/strategicIndicators/assess?train=NONE",
                    type: "GET",
                    contentType: false,
                    processData: false,
                    //ToDo: the service produces more than one error, the current message does not fit all of them
                    error: function (jqXHR, textStatus, errorThrown) {
                        warningUtils("Error","Error in the ElasticSearch: contact to the system administrator");
                        //alert("Error in the ElasticSearch: contact to the system administrator");
                        location.href = "../QualityFactors/Configuration";
                    },
                    success: function () {
                        location.href = "../QualityFactors/Configuration";
                    }
                });
            }
        });
    } else warningUtils("Warning","Make sure that you have completed correctly all fields marked with an *");
        //alert("Make sure that you have completed correctly all fields marked with an *");
});

$("#deleteQF").click(function () {
    console.log("click on Delete Button");
    if (confirm("\t This operation cannot be undone. \t\n Are you sure you want to delete this factor?")) {
        jQuery.ajax({
            url: deleteUrl,
            cache: false,
            type: "DELETE",
            async: true,
            success: function () {
                location.href = "../QualityFactors/Configuration";
            },
            error: function (error) {
                if (error.status === 403) {
                    warningUtils("Error","This factor can't be deleted, it's involved in Strategic Indicators computation.");
                    //alert("This factor can't be deleted, it's involved in Strategic Indicators computation.");
                }
            }
        });
    }
});

window.onload = function() {
    loadMetrics(false);
    buildQFList();
};
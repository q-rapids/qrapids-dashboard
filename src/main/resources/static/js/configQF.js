var serverUrl = sessionStorage.getItem("serverUrl");

var metrics = [];
var weightsForMetrics = [];

var postUrl;
var deleteUrl;
var httpMethod = "POST";

function buildQFList() {
    /* TODO: get quality factors from DB and present them as a list
        ! decidir que api utilizar para las cridas de BD
     */
    var url = "/api/qualityFactors";
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
                QF.classList.add("SI");
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
    $(".SI").each(function () {
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
            $("#QFAssessmentID").val(qf.id);
            $("#QFName").attr("placeholder", "Write the quality factor name here");
            $("#QFName").val(qf.name);
            $("#QFDescription").attr("placeholder", "Write the quality factor description here");
            $("#QFDescription").val(qf.description);
            $("#QFCompositionTitle").text("Quality Factor Composition");
            $("#deleteQF").show();
            if (metrics.length > 0) {
                showMetrics();
                //qf.metrics.forEach(function (metric) {
                //    $('#avMetricsBox').find("option[value='" + metric + "']").appendTo('#selMetricsBox');
                //});
            }
            // TODO weighted
            //document.getElementById('weightCheckbox').checked = si.weighted;
            //document.getElementById('weightEditButton').disabled = !si.weighted;
            //console.log(si.qualityFactorsWeights);
            //if (si.weighted) weightsForMetrics = si.qualityFactorsWeights;
            //else weightsForMetrics = [];
        }
    });
}

function newQF() {
    console.log("newQF function");
    $("#QFInfo").show();
    $("#QFInfoTitle").text("Step 1 - Fill the quality factor information");
    $("div.QFInfoRowID").hide();
    $("#QFAssessmentID").val("");
    $("#QFName").attr("placeholder", "Write the quality factor name here");
    $("#QFName").val("");
    $("#QFDescription").attr("placeholder", "Write the quality factor description here");
    $("#QFDescription").val("");
    $("#QFCompositionTitle").text("Step 2 - Select the corresponding metrics");
    $("#deleteQF").hide();
    if (metrics.length > 0)
        showMetrics();
    else {
        loadMetrics(true);
    }
    document.getElementById('QFweightCheckbox').checked = false;
    document.getElementById('QFweightEditButton').disabled = true;
    // TODO Quality Factors POST URL
    postUrl="../api/strategicIndicators";
}

function showMetrics () {
    $('#avMetricsBox').empty();
    $('#selMetricsBox').empty();

    console.log("metrics in showMetrics");
    console.log(metrics);

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

//TODO
var checkbox = document.getElementById('QFweightCheckbox');
checkbox.addEventListener("change", validaCheckbox, false);
function validaCheckbox(){
    var checked = checkbox.checked;
    if(checked){
        var metrics = getSelectedMetrics(false);
        if (metrics.length > 0) {
            $("#weightsItems").empty();
            var i = 0;
            metrics.forEach(function (qf) {
                var selectedMetric;
                var found = false;
                var j = 0;
                while (j < metrics.length && !found) {
                    if (metrics[j].id == qf) {
                        selectedMetric = metrics[j].name;
                    }
                    j++;
                }
                var id = "editor"+i;
                $("#weightsItems").append('<tr class="weightItem"><td>' + selectedMetric + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
                // add listeners which control if we try to input letters, floats, negative values or zero
                var cell = document.getElementById(id);
                cell.addEventListener('keydown', onlyNumbers);
                i++;
            });
            $("#weightsModal").modal();
        } else {
            alert('You have no selected metrics.');
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


$("#weightEditButton").click(function () {
    var wff = String(weightsForMetrics).split(",");
    var selector = getSelectedMetrics(false);
    if (selector.length > 0) {
        $("#weightsItems").empty();
        var i = 0;
        selector.forEach(function (qf) {
            var id = "editor"+i;
            var selectedFactor;
            var found = false;
            var j = 0;
            while (j < factors.length && !found) {
                if (factors[j].id == qf) {
                    selectedFactor = factors[j].name;
                }
                j++;
            }
            if (wff.includes(qf)) {
                $("#weightsItems").append('<tr class="weightItem"><td>' + selectedFactor + '</td><td id="' + id + '" contenteditable="true">' + Math.floor(wff[wff.indexOf(qf)+1]) +'</tdid>');
            } else {
                $("#weightsItems").append('<tr class="weightItem"><td>' + selectedFactor + '</td><td id="' + id + '" contenteditable="true">' + " " +'</tdid>');
            }
            // add listeners which control if we try to input letters, floats, negative values or zero
            var cell = document.getElementById(id);
            cell.addEventListener('keydown', onlyNumbers);
            i++;
        });
        $("#weightsModal").modal();
    } else {
        alert('You have no selected factors.');
        document.getElementById('weightCheckbox').checked = false;
        document.getElementById('weightEditButton').disabled = true;
    }
    return false;
});

$("#submitWeightsButton").click(function () {
    var qualityFactors = getSelectedMetrics(false);
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
            aux.push([qualityFactors[i], weightValue]);
        }
        i++;
    }
    if (!ok) alert ("Check the form fields, there may be one of the following errors:\n" +
        "- Empty fields\n" +
        "- Zero value");
    else {
        if (totalSum != 100) alert("Total sum is not equals to 100.");
        else {
            weightsForMetrics = aux;
            $("#weightsModal").modal('hide');
        }
    }
});

$("#closeWeightsButton").click(function () {
    if (!weightsForMetrics.length) {
        document.getElementById('weightCheckbox').checked = false;
        document.getElementById('weightEditButton').disabled = true;
    }
    $("#weightsModal").modal('hide');
});

function getSelectedMetrics(final) {
    var metrics = [];

    if (final) {
        $('#selMetricsBox').children().each (function (i, option) {
            metrics.push([option.value, -1]);
        });
    } else {
        $('#selMetricsBox').children().each (function (i, option) {
            metrics.push(option.value);
        });
    }

    return metrics;
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
    var qualityFactors = getSelectedMetrics(false);
    var wff = String(weightsForMetrics).split(",");
    console.log(wff);
    var totalSum = 0;
    for (var i = 0; i < qualityFactors.length; i++){
        if (wff.includes(qualityFactors[i])) totalSum += parseFloat(wff[wff.indexOf(qualityFactors[i])+1]);
    }
    console.log(totalSum);
    return totalSum == 100 && (qualityFactors.length == wff.length/2);
}

// TODO save function for factors
$("#saveQF").click(function () {
    var qualityFactors;
    var totalSum = true;
    // when quality factors a not weighted
    if ((document.getElementById('weightCheckbox').checked == false) || (weightsForMetrics.length == 0)){
        qualityFactors = getSelectedMetrics(true);
    } else { // when quality factors a weighted
        if (!checkTotalSum()) {
            totalSum = false;
            alert("Total sum is not equals to 100.");
        }
        qualityFactors = weightsForMetrics;
    }

    if ($('#SIName').val() !== "" && qualityFactors.length > 0 && totalSum) {

        console.log("QF que envio:");
        console.log(qualityFactors);

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
    } else alert("Make sure that you have completed correctly all fields marked with an *");
});

$("#deleteQF").click(function () {
    /* TODO delete function for factors
        1. Mirrar si el factor no esta incolucrado en ningun SI (strategic_indicator_quality_factors)
        1.1 Si esta incolucrado no se puede borrar (mensaje correspondiente)
        1.2 Si NO est involucrado se puede borrar (seguente codi commentado)
    if (confirm("Are you sure you want to delete this Quality Factor?")) {
        jQuery.ajax({

            url: deleteUrl,
            cache: false,
            type: "DELETE",
            async: true,
            success: function () {
                location.href = "../QualityFactors/Configuration";
            }
        });
    }
    */
});

window.onload = function() {
    loadMetrics(false);
    //buildQFList();
};
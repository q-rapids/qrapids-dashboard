var postUrl = "../api/strategicIndicators";
var httpMethod = "POST";
var id;
var si_qf;
var factorsLoaded = false;
var siDataLoaded = false;

function moveFactors() {
    for(i = 0; i < si_qf.length; ++i)  {
        $('#sbOne').find("option[value='" + si_qf[i] + "']").appendTo('#sbTwo');
    }
}

var profileId = sessionStorage.getItem("profile_id");
$.ajax({
    url: "../api/qualityFactors/metrics/current?profile="+profileId,
    type: "GET",
    success: function(data) {
        for(i = 0; i < data.length; ++i) {
            $('#sbOne').append($('<option>', {
                value: data[i].id,
                text: data[i].name
            }));
        }
        factorsLoaded = true;
        if (siDataLoaded) moveFactors();
    }
});

if (window.location.href.includes("/EditStrategicIndicators/")) {

    $("#title").text("Edit Strategic Indicator");
    $("#networkLabel").html("Assessment Model: <br/>(leave empty if unchanged)");
    $('#newSI').text("Save Strategic Indicator");
    window.location.href.split('/').forEach(function(element, i, arr) {
        if (element == "EditStrategicIndicators")
            id = arr[i+1];
    });
    postUrl = "../api/strategicIndicators/" + id;
    httpMethod = "PUT";

    $.ajax({
        url: "../api/strategicIndicators/" + id,
        type: "GET",
        success: function(data) {
            console.log(data);
            $('#SIName').val(data.name);
            $('#SIDescription').val(data.description);
            si_qf = data.quality_factors;
            siDataLoaded = true;
            if (factorsLoaded) moveFactors();
        }
    });
}


$(function () {

    function moveItems(origin, dest) {
        $(origin).find(':selected').appendTo(dest);
    }

    function moveAllItems(origin, dest) {
        $(origin).children().appendTo(dest);
    }

    $('#left').click(function () {
        moveItems('#sbTwo', '#sbOne');
    });

    $('#right').on('click', function () {
        moveItems('#sbOne', '#sbTwo');
    });

    $('#leftall').on('click', function () {
        moveAllItems('#sbTwo', '#sbOne');
    });

    $('#rightall').on('click', function () {
        moveAllItems('#sbOne', '#sbTwo');
    });
});

$('#resetCategories').click(function () {
    $.ajax({
        url: '../api/categories',
        type: "DELETE",
        success: function() {
            location.reload();
        }
    });
});

$('#newSI').click(function () {

    var qualityFactors = [];

    $('#sbTwo').children().each (function (i, option) {
        qualityFactors.push(option.value);
    });

    if ($('#SIName').val() != "" && qualityFactors.length > 0) {

        var formData = new FormData();
        formData.append("name", $('#SIName').val());
        formData.append("description", $('#SIDescription').val());
        formData.append("network", $('#network')[0].files[0]);
        formData.append("quality_factors", qualityFactors);
        formData.append("historical_assessment", $('#historical_assessment').val())

        $.ajax({
            url: postUrl,
            data: formData,
            type: httpMethod,
            contentType: false,
            processData: false,
            //ToDo: the service produces more than one error, the current message does not fit all of them
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 409)
                    warningUtils("Error", "This Strategic Indicator name is already in use");
                else {
                    warningUtils("Error", "Error in the ElasticSearch: contact to the system administrator");
                    location.href = "../StrategicIndicators/CurrentChart";
                }
            },
            success: function() {
                location.href = "../StrategicIndicators/CurrentChart";
            }
        });
    } else warningUtils("Warning","Make sure that you have completed all fields marked with an *");
});
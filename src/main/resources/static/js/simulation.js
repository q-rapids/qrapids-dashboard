function getAllQualityFactors(){
    var url = "api/QualityFactors/getAll?prj="+sessionStorage.getItem("prj");
    $.ajax({
        url : url,
        type: "GET",
        success: function (response) {
            showQualityFactorSliders(response);
        }
    });
}

function showQualityFactorSliders (qualityFactors) {
    var qualityFactorsDiv = $("#qualityFactors");
    qualityFactors.forEach(function (qualityFactor) {
        var div = document.createElement('div');
        div.style.marginTop = "1em";
        div.style.marginBottom = "1em";

        var label = document.createElement('label');
        label.id = qualityFactor.id;
        label.textContent = qualityFactor.name;
        div.appendChild(label);

        div.appendChild(document.createElement('br'));

        var slider = document.createElement("input");
        slider.id = "sliderValue" + qualityFactor.id;
        slider.style.width = "50%";
        var sliderConfig = {
            id: "slider" + qualityFactor.id,
            min: 0,
            max: 1,
            step: 0.01,
            value: qualityFactor.value
        };
        // Add original value
        var start, end;
        if (qualityFactor.value === 0) {
            start = 0;
            end = 0.03;
        }
        else if (qualityFactor.value === 1) {
            start = 0.97;
            end = 1;
        }
        else {
            start = qualityFactor.value - 0.015;
            end = qualityFactor.value + 0.015;
        }
        sliderConfig.rangeHighlights = [{
            start: start,
            end: end
        }];
        div.appendChild(slider);
        qualityFactorsDiv.append(div);
        $("#"+slider.id).slider(sliderConfig);
        $(".slider-rangeHighlight").css("background", "#000000");
    });
}

$('#apply').click(function () {

    var qualityFactors = [];

    Array.from($("#qualityFactors").children()).forEach(function(element) {
        qualityFactors.push({
            id: element.children[0].id,
            name: element.children[0].textContent,
            value: element.children[3].value
        });
    });

    var formData = new FormData();
    formData.append("factors", JSON.stringify(qualityFactors));

    $.ajax({
        url: 'api/Simulate?prj='+sessionStorage.getItem("prj"),
        data: formData,
        type: "POST",
        contentType: false,
        processData: false,
        error: function(jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 405)
                alert(textStatus);
        },
        success: function(result) {
            data = result;
            $('#simulationResult').empty();
            var title = document.createElement("h3");
            title.textContent = "Simulated Assessment";
            $('#simulationResult').append(title);
            drawChart("simulationResult", 200, 200, false, false);
        }
    });
});

$('#restore').click(function () {
    $('#simulationResult').empty();
    $('#qualityFactors').empty();
    getAllQualityFactors();
});


window.onload = function() {
    getData(200, 200, false, false);
    getAllQualityFactors();
};
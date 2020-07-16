function getStrategicIndicators() {

    console.log("sessionStorage: profile_id");
    console.log(sessionStorage.getItem("profile_id"));

    var profileId = sessionStorage.getItem("profile_id");

    jQuery.ajax({
        dataType: "json",
        url: '../api/strategicIndicators/current?profile='+profileId,
        cache: false,
        type: "GET",
        async: true,
        success: function (strategicIndicators) {
            getFactors(strategicIndicators)
        }
    });
}

function getFactors(strategicIndicators) {
    jQuery.ajax({
        dataType: "json",
        url: '../api/strategicIndicators/qualityFactors/current',
        cache: false,
        type: "GET",
        async: true,
        success: function (detailedStrategicIndicators) {
            for (i = 0; i < detailedStrategicIndicators.length; ++i) {
                var ids = [];
                var names = [];
                var values = [];
                var evaluationDates = [];
                for (j = 0; j < detailedStrategicIndicators[i].factors.length; ++j) {
                    ids.push(detailedStrategicIndicators[i].factors[j].id);
                    names.push(detailedStrategicIndicators[i].factors[j].name);
                    values.push(detailedStrategicIndicators[i].factors[j].value);
                    evaluationDates.push(detailedStrategicIndicators[i].factors[j].formattedDate);
                }
                // search for the strategic indicator object that matches the detailed strategic indicator
                var strategicIndicator = strategicIndicators.find(function (element) {
                    return element.id === detailedStrategicIndicators[i].id;
                });
                strategicIndicator.factorIds = ids;
                strategicIndicator.factorNames = names;
                strategicIndicator.factorValues = values;
                strategicIndicator.factorEvaluationDates = evaluationDates;
            }
            console.log(strategicIndicators);
            drawChart(strategicIndicators);
        }
    });
}

function drawChart(strategicIndicators) {
    for (i = 0; i < strategicIndicators.length; ++i) {
        var div = document.createElement('div');
        div.style.display = "inline-block";
        div.style.margin = "5px 5px 100px 5px";

        var canvas = document.createElement('canvas');
        canvas.id = 'canvas' + i;
        canvas.width = 400;
        canvas.style.display = "inline";
        canvas.getContext("2d");

        document.getElementById("radarChart").appendChild(div).appendChild(canvas);

        if (strategicIndicators[i].factorNames.length === 2) {
            strategicIndicators[i].factorNames.push(null);
        }

        // Draw chart
        window.myLine = new Chart(canvas, {
            type: 'radar',
            data: {
                labels: strategicIndicators[i].factorNames,
                datasets: [{
                    label: strategicIndicators[i].name,
                    backgroundColor: 'rgba(1, 119, 166, 0.2)',
                    borderColor: 'rgb(1, 119, 166)',
                    pointBackgroundColor: 'rgb(1, 119, 166)',
                    pointBorderColor: 'rgb(1, 119, 166)',
                    data: strategicIndicators[i].factorValues,
                    fill: true
                }]
            },
            options: {
                title: {
                    display: false,
                    fontSize: 16,
                    text: strategicIndicators[i].name
                },
                responsive: false,
                legend: {
                    display: false
                },
                scale: {    //make y axis scale 0 to 1 and set maximum number of axis lines
                    ticks: {
                        min: 0,
                        max: 1,
                        maxTicksLimit: 5
                    }
                }
            }
        });

        // SI name
        var name = document.createElement('a');
        name.innerHTML = strategicIndicators[i].name;
        name.style.fontSize = "16px";
        name.style.textDecoration = "none";
        var nameParagraph = document.createElement('p');
        div.appendChild(nameParagraph).appendChild(name);

        // SI current value
        var currentValueParagraph = document.createElement("p");
        currentValueParagraph.innerHTML = "Current value: ";
        var currentValue = document.createElement("span");
        currentValue.style.fontWeight = "bold"
        currentValue.innerHTML = strategicIndicators[i].value.first.toFixed(2);
        currentValueParagraph.appendChild(currentValue);
        div.appendChild(currentValueParagraph);

        div.appendChild(document.createElement("br"));

        // Slider
        var slider = document.createElement("input");
        slider.id = "sliderValue" + strategicIndicators[i].id;

        var sliderConfig = {
            id: "slider" + strategicIndicators[i].id,
            min: 0,
            max: 1,
            step: 0.01,
            value: strategicIndicators[i].value.first
        };
        var rangeHighlights = [];
        var ranges = strategicIndicators[i].probabilities;
        ranges.sort(function (a, b) {
            return a.upperThreshold - b.upperThreshold;
        });
        var start = 0;
        for (j = 0; j < ranges.length; j++) {
            var end = 1/ranges.length * (j+1);
            var offset = 0;
            if (end < 1) offset = 0.02;
            var range = {
                start: start,
                end: end + offset,
                class: ranges[j].label
            };
            rangeHighlights.push(range);
            start = end;
        }
        sliderConfig.rangeHighlights = rangeHighlights;

        div.appendChild(slider);
        $("#"+slider.id).slider(sliderConfig);

        for (j = 0; j < ranges.length; j++) {
            $("#"+sliderConfig.id+" .slider-rangeHighlight."+ranges[j].label).css("background", ranges[j].color)
        }

        div.appendChild(document.createElement("br"));
        div.appendChild(document.createElement("br"));

        // Save feedback button
        var saveFeedbackButton = document.createElement("button");
        saveFeedbackButton.className = "btn btn-primary";
        saveFeedbackButton.id = strategicIndicators[i].id + "button";
        saveFeedbackButton.innerHTML = "Save feedback";
        if (strategicIndicators[i].dbId == null) saveFeedbackButton.disabled = true;
        if (!strategicIndicators[i].hasBN) saveFeedbackButton.disabled = true;
        div.appendChild(saveFeedbackButton);
        $("#" + strategicIndicators[i].id + "button").on("click", {strategicIndicator: strategicIndicators[i]}, function (event) {
            newFeedback(event.data.strategicIndicator, $("#sliderValue"+event.data.strategicIndicator.id).prop("value"))
        });
    }
}

function newFeedback(strategicIndicator, newvalue){
    var formData = new FormData();
    formData.append("newvalue", newvalue);
    formData.append("oldvalue", strategicIndicator.value.first);
    var factorIds = JSON.stringify(strategicIndicator.factorIds);
    formData.append("factorIds", factorIds);
    var factorNames = JSON.stringify(strategicIndicator.factorNames);
    formData.append("factorNames", factorNames);
    var factorValues = JSON.stringify(strategicIndicator.factorValues);
    formData.append("factorValues", factorValues);
    var factorEvaluationDates = JSON.stringify(strategicIndicator.factorEvaluationDates);
    formData.append("factorEvaluationDates", factorEvaluationDates);

    $.ajax({
        url: '../api/strategicIndicators/' + strategicIndicator.dbId + "/feedback",
        data: formData,
        type: "POST",
        contentType: false,
        processData: false,
        success: function() {
            alert("Your feedback has been stored successfully");
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
        }
    });


}

window.onload = function() {
    getStrategicIndicators();
};
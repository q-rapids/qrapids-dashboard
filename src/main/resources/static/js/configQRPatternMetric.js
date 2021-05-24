var serverUrl = sessionStorage.getItem("serverUrl");
var previousSelectionId;
var currentSelectionId;
var saveMethod;

function buildTree() {
    var url = "/api/qrPatternsMetrics";
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
            var metricList = document.getElementById('metricList');
            metricList.innerHTML = "";
            for (var i=0; i<data.length; i++) {
                var metric = document.createElement('li');
                metric.classList.add("list-group-item");
                metric.setAttribute("id", "metric" + data[i].id);
                metric.appendChild(document.createTextNode(data[i].name));
                metric.addEventListener("click", clickOnTree);
                metricList.appendChild(metric);
            }
            //document.getElementById('patternTree').appendChild(metricList);
        }
    });
}

function clickOnTree(e) {
    previousSelectionId = currentSelectionId;
    currentSelectionId = e.target.id;
    if (previousSelectionId != null) {
        document.getElementById(previousSelectionId).classList.remove("active")
    }
    document.getElementById(currentSelectionId).classList.add("active");
    getChosenMetric(e.target.id.replace("metric", ""));
}

function getChosenMetric(currentMetricId) {
    document.getElementById("metricInfo").removeAttribute('style');
    var url = "/api/qrPatternsMetrics/" + currentMetricId;
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
            document.getElementById("metricName").setAttribute("value", data.name);
            document.getElementById("metricDescription").value = data.description;
            document.getElementById("typeSelect").value = data.type;
            changeType(data.type);

            if (data.type == "integer" || data.type == "float") {
                document.getElementById("metricMinValue").value = data.minValue.toString();
                document.getElementById("metricMaxValue").value = data.maxValue.toString();
            }
            else if (data.type == "domain") {
                var stringPossibleValues = "";
                for (var i=0; i<data.possibleValues.length; i++) {
                    if (i>0) {
                        stringPossibleValues += "\n";
                    }
                    stringPossibleValues += data.possibleValues[i];
                }
                document.getElementById("metricPossibleValues").value = stringPossibleValues;
            }
        }
    })
}

function newMetric() {
}

function saveMetric() {
}

function deleteMetric() {
}

function changeType(type) {
    if (type == 'integer' || type == 'float') {
        document.getElementById("minValueSection").style.display = null;
        document.getElementById("maxValueSection").style.display = null;
        document.getElementById("possibleValuesSection").style.display = "none";

        var minValue = document.getElementById("metricMinValue");
        var maxValue = document.getElementById("metricMaxValue");
        if (type == 'float') {
            minValue.setAttribute("step", "0.01");
            maxValue.setAttribute("step", "0.01");
        } else {
            minValue.removeAttribute("step");
            maxValue.removeAttribute("step");
        }
        minValue.value = "";
        maxValue.value = "";
    }
    else if (type == 'domain') {
        document.getElementById("minValueSection").style.display = "none";
        document.getElementById("maxValueSection").style.display = "none";
        document.getElementById("possibleValuesSection").style.display = null;
    }
    else {
        document.getElementById("minValueSection").style.display = "none";
        document.getElementById("maxValueSection").style.display = "none";
        document.getElementById("possibleValuesSection").style.display = "none";
    }
}

window.onload = function() {
    buildTree();
};
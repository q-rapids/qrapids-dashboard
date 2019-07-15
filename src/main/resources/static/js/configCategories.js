$("#SICategoriesButton").click(function () {
    selectElement($(this));
    $("#SICategories").show();
    $("#FactorsCategories").hide();
});

$("#FactorsCategoriesButton").click(function () {
    selectElement($(this));
    $("#SICategories").hide();
    $("#FactorsCategories").show();
});

function selectElement (selectedElement) {
    selectedElement.addClass("active");
    $(".category-element").each(function () {
        if (selectedElement.attr("id") !== $(this).attr("id"))
            $(this).removeClass("active");
    });
}

function checkFirst() {
    $('input[name=upperThres][class!="hide"]').each(function (i) {
        if (i != 0) {
            $(this).prop('readonly', false);
        } else {
            $(this).val(100);
            $(this).prop('readonly', true);
        }
    });
}

function loadSICategories () {
    $.ajax({
        url: '../api/strategicIndicators/categories',
        type: "GET",
        success: function(categories) {
            if (categories.length > 0) {
                categories.forEach(function (category) {
                    buildCategoryRow(category, "tableSI", false);
                });
            } else {
                buildDefaultSITable();
            }
        }
    });
}

function loadFactorCategories () {
    $.ajax({
        url: '../api/qualityFactors/categories',
        type: "GET",
        success: function(categories) {
            if (categories.length > 0) {
                categories.forEach(function (category) {
                    buildCategoryRow(category, "tableQF", true);
                });
            } else {
                buildDefaultFactorTable();
            }
        }
    });
}

function buildCategoryRow (category, tableId, isFactor) {
    var table = document.getElementById(tableId);
    var row = table.insertRow(-1);

    var categoryName = document.createElement("td");
    categoryName.setAttribute("contenteditable", "true");
    categoryName.appendChild(document.createTextNode(category.name));
    row.appendChild(categoryName);

    var categoryColorPicker = document.createElement("input");
    categoryColorPicker.setAttribute("value", category.color);
    categoryColorPicker.setAttribute("type", "color");
    var categoryColor = document.createElement("td");
    categoryColor.appendChild(categoryColorPicker);
    row.appendChild(categoryColor);

    if (isFactor) {
        var thresholdSelector = document.createElement("input");
        thresholdSelector.setAttribute("value", category.upperThreshold * 100);
        thresholdSelector.setAttribute("name", "upperThres");
        thresholdSelector.setAttribute("min", "1");
        thresholdSelector.setAttribute("max", "100");
        thresholdSelector.setAttribute("type", "number");
        var threshold = document.createElement("td");
        threshold.appendChild(thresholdSelector);
        row.appendChild(threshold);
    }

    var arrowUp = document.createElement("span");
    arrowUp.classList.add("glyphicon", "glyphicon-arrow-up");
    arrowUp.addEventListener("click", function () {
        var $row = $(this).parents('tr');
        if ($row.index() === 1) return; // Don't go above the header
        $row.prev().before($row.get(0));
        checkFirst();
    });
    var arrowDown = document.createElement("span");
    arrowDown.classList.add("glyphicon", "glyphicon-arrow-down");
    arrowDown.addEventListener("click", function () {
        var $row = $(this).parents('tr');
        $row.next().after($row.get(0));
        checkFirst();
    });
    var arrows = document.createElement("td");
    arrows.appendChild(arrowUp);
    arrows.appendChild(arrowDown);
    row.appendChild(arrows);

    var removeIcon = document.createElement("span");
    removeIcon.classList.add("glyphicon", "glyphicon-remove");
    var remove = document.createElement("td");
    remove.addEventListener("click", function () {
        $(this).parents('tr').detach();
        checkFirst();
    });
    remove.appendChild(removeIcon);
    row.appendChild(remove);
}

function buildDefaultSITable () {
    var goodCategory = {
        name: "Good",
        color: "#00ff00"
    };
    buildCategoryRow(goodCategory, "tableSI", false);

    var neutralCategory = {
        name: "Neutral",
        color: "#ff8000"
    };
    buildCategoryRow(neutralCategory, "tableSI", false);

    var badCategory = {
        name: "Bad",
        color: "#ff0000"
    };
    buildCategoryRow(badCategory, "tableSI", false);
}

function buildDefaultFactorTable () {
    var goodCategory = {
        name: "Good",
        color: "#00ff00",
        upperThreshold: 0.67
    };
    buildCategoryRow(goodCategory, "tableQF", true);

    var neutralCategory = {
        name: "Neutral",
        color: "#ff8000",
        upperThreshold: 0.33
    };
    buildCategoryRow(neutralCategory, "tableQF", true);

    var badCategory = {
        name: "Bad",
        color: "#ff0000",
        upperThreshold: 0
    };
    buildCategoryRow(badCategory, "tableQF", true);
}

function addButtonBehaviour () {
    $('.table-addSI').click(function () {
        var goodCategory = {
            name: "Good",
            color: "#00ff00"
        };
        buildCategoryRow(goodCategory, "tableSI", false);
    });

    $('.table-addQF').click(function () {
        var goodCategory = {
            name: "Good",
            color: "#00ff00",
            upperThreshold: 0
        };
        buildCategoryRow(goodCategory, "tableQF", true);
    });
}

function getData() {
    var $rows = $('#tableSI').find('tr:not(:hidden)');
    var headers = ["name", "color"];
    var data = [];

    // Turn all existing rows into a loopable array
    $rows.slice(1).each(function () {
        var $td = $(this).find('td');
        var h = {};

        // Use the headers from earlier to name our hash keys
        headers.forEach(function (header, i) {
            if (i%2 == 0)
                h[header] = $td.eq(i).text();
            else
                h[header] = $td.eq(i).children()[0].value;
        });

        data.push(h);
    });
    return data;
}

function getDataQF() {
    var $rows = $('#tableQF').find('tr:not(:hidden)');
    var headers = ["name", "color", "upperThreshold"];
    var data = [];

    // Turn all existing rows into a loopable array
    $rows.slice(1).each(function () {
        var $td = $(this).find('td');
        var h = {};

        // Use the headers from earlier to name our hash keys
        headers.forEach(function (header, i) {
            if (i%3 == 0)
                h[header] = $td.eq(i).text();
            else if (i%3 == 1)
                h[header] = $td.eq(i).children()[0].value;
            else
                h[header] = $td.eq(i).children()[0].value;
        });

        data.push(h);
    });
    console.log(data);
    return data;
}

$('#saveSICategories').click(function () {
    var formData = new FormData();
    var dataSI = getData();
    formData.append("SICat", JSON.stringify(dataSI));

    if (dataSI.length < 2)
        alert("There has to be at least 2 categories for each indicator");
    else {
        $.ajax({
            url: '../api/strategicIndicators/categories',
            data: formData,
            type: "POST",
            contentType: false,
            processData: false,
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 405)
                    alert("You can't have two categories with the same name");
                else
                    alert("Error on saving categories");
            },
            success: function() {
                alert("Strategic Indicator Categories saved successfully");
            }
        });

    }
});

$('#saveFactorCategories').click(function () {
    var formData = new FormData();
    var dataQF = getDataQF();
    formData.append("QFCat", JSON.stringify(dataQF));

    if (dataQF.length < 2)
        alert("There has to be at least 2 categories for each factor");
    else {
        $.ajax({
            url: '../api/qualityFactors/categories',
            data: formData,
            type: "POST",
            contentType: false,
            processData: false,
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 405)
                    alert("You can't have two categories with the same name");
                else
                    alert("Error on saving categories");
            },
            success: function() {
                alert("Quality Factor Categories saved successfully");
            }
        });

    }
});


size = $('input[name=upperThres][class!="hide"]').length;
$('input[name=upperThres][class!="hide"]').each(function (i) {
    $(this).val(Math.round((size-i)*100/size));
});
checkFirst();
loadSICategories();
loadFactorCategories();
addButtonBehaviour();
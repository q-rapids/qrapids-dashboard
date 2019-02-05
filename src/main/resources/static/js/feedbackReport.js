function getFeedFact(){
    var id = getParameterByName('id');
    var url =  "../api/FeedbackReport/" + id;
    jQuery.ajax({
        feedbackType: "json",
        url: url,
        cache: false,
        type: "GET",
        async: true,
        success: function (feedback) {
            console.log(feedback);
            createTable(feedback);
        }
    });
}

function createTable(feedback){
    $("#title").text(feedback[0].siName + " Strategic Indicator Feedback");

    var Table = document.getElementById("myTable");
    var header = Table.createTHead();
    var headerRow = header.insertRow(0);

    var siHeaderCell = headerRow.insertCell(0);
    siHeaderCell.innerHTML = "<b>Strategic Indicator</b>";

    var dateHeaderCell = headerRow.insertCell(1);
    dateHeaderCell.innerHTML = "<b>Date</b>";

    var newValueHeaderCell = headerRow.insertCell(2);
    newValueHeaderCell.innerHTML = "<b>New Value</b>";

    var newCategoryHeaderCell = headerRow.insertCell(3);
    newCategoryHeaderCell.innerHTML = "<b>New Category</b>";

    var oldValueHeaderCell = headerRow.insertCell(4);
    oldValueHeaderCell.innerHTML = "<b>Old Value</b>";

    var oldCategoryHeaderCell = headerRow.insertCell(5);
    oldCategoryHeaderCell.innerHTML = "<b>Old Category</b>";

    for (var i = 0; i < feedback[0].fact.length; ++i){
        var factorHeaderCell = headerRow.insertCell(i + 6);
        factorHeaderCell.innerHTML = "<b>" + feedback[0].fact[i] + "</b>";
    }

    var authorHeaderCell = headerRow.insertCell(feedback[0].fact.length + 6);
    authorHeaderCell.innerHTML = "<b>Author</b>";

    var body = Table.createTBody();
    for (var j = 0; j < feedback.length; ++j){
        var row = body.insertRow(j);

        var siCell = row.insertCell(0);
        siCell.innerHTML = feedback[j].siName;

        var dateCell = row.insertCell(1);
        dateCell.innerHTML = feedback[j].date;

        var newValueCell = row.insertCell(2);
        newValueCell.innerHTML = feedback[j].newvalue;
        newValueCell.style.backgroundColor = feedback[j].newCategoryColor;

        var newCategoryCell = row.insertCell(3);
        newCategoryCell.innerHTML = feedback[j].newCategory;
        newCategoryCell.style.backgroundColor = feedback[j].newCategoryColor;

        var oldValueCell =  row.insertCell(4);
        oldValueCell.innerHTML = feedback[j].oldvalue;
        oldValueCell.style.backgroundColor = feedback[j].oldCategoryColor;

        var oldCategoryCell =  row.insertCell(5);
        oldCategoryCell.innerHTML = feedback[j].oldCategory;
        oldCategoryCell.style.backgroundColor = feedback[j].oldCategoryColor;

        for (var k = 0; k < feedback[j].factVal.length; ++k){
            var factorCell = row.insertCell(k + 6);
            factorCell.innerHTML = feedback[j].factVal[k];
        }

        var authorCell = row.insertCell(feedback[j].factVal.length + 6);
        if (feedback[j].author !== "-1") authorCell.innerHTML = feedback[j].author;
    }
}

function exportTableToCSV(filename) {
    var csv = [];
    var rows = document.querySelectorAll("table tr");

    for (var i = 0; i < rows.length; i++) {
        var row = [], cols = rows[i].querySelectorAll("td, th");

        for (var j = 0; j < cols.length; j++)
            row.push(cols[j].innerText);

        csv.push(row.join(", "));
    }

    // Download CSV file
    downloadCSV(csv.join("\r\n"), filename);
}

function downloadCSV(csv, filename) {
    var csvFile;
    var downloadLink;

    // CSV file
    csvFile = new Blob([csv], {type: "text/csv"});

    // Download link
    downloadLink = document.createElement("a");
    downloadLink.download = filename;
    downloadLink.href = window.URL.createObjectURL(csvFile);
    downloadLink.style.display = "none";
    document.body.appendChild(downloadLink);
    downloadLink.click();
}
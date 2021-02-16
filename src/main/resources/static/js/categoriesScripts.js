function checkFirst() {
    var arrSize = $('input[name=upperThres][class!="hide"]').length - 1;
    $('input[name=upperThres][class!="hide"]').each(function (i) {
        if (i != 0) {
            $(this).prop('readonly', false);
        } else {
            $(this).val(100);
            $(this).prop('readonly', true);
        }
    });
}

$('.table-addSI').click(function () {
    var $clone = $('#tableSI').find('tr.hide').clone(true).removeClass('hide table-line');
    $('#tableSI').find('table').append($clone);
});

$('.table-addQF').click(function () {
    var $clone = $('#tableQF').find('tr.hide').clone(true).removeClass('hide table-line');
    $clone.find('input.hide').removeClass('hide')
    $('#tableQF').find('table').append($clone);
    checkFirst();
});

$('.table-remove').click(function () {
    $(this).parents('tr').detach();
    checkFirst();
});

$('.table-up').click(function () {
    var $row = $(this).parents('tr');
    if ($row.index() === 1) return; // Don't go above the header
    $row.prev().before($row.get(0));
    checkFirst();
});

$('.table-down').click(function () {
    var $row = $(this).parents('tr');
    $row.next().after($row.get(0));
    checkFirst();
});

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
                h[header] = $td.eq(i+1).children()[0].value;
        });

        data.push(h);
    });
    return data;
}

$('#accept').click(function () {

    var formData = new FormData();
    var dataSI = getData();
    var dataQF = getDataQF();
    formData.append("SICat", JSON.stringify(dataSI));
    formData.append("QFCat", JSON.stringify(dataQF));

    if (dataSI.length < 2 || dataQF.length < 2)
        alert("There has to be at least 2 categories for each indicator.")
    else {
        $.ajax({
            url: '../api/categories',
            data: formData,
            type: "POST",
            contentType: false,
            processData: false,
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 500)
                    alert("You can't have two categories with the same name.");
            },
            success: function() {
                location.reload();
            }
        });

    }
});


size = $('input[name=upperThres][class!="hide"]').length;
$('input[name=upperThres][class!="hide"]').each(function (i) {
    $(this).val(Math.round((size-i)*100/size));
});
checkFirst();
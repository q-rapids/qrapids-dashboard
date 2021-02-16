var decisions = new Map();

function getDecisions () {
    jQuery.ajax({
        dataType: "json",
        url: "../api/decisions?qrs=true",
        data: {
            "from": $('#datepickerFrom').val(),
            "to": $('#datepickerTo').val()
        },
        cache: false,
        type: "GET",
        async: false,
        success: function (data) {
            decisions = new Map();
            for(var i = 0; i < data.length; i++) {
                var decision = {
                    type: data[i].type,
                    requirement: data[i].requirement,
                    comments: data[i].rationale,
                    date: data[i].date
                };
                if (decisions.has(data[i].elementId))
                    decisions.get(data[i].elementId).push(decision);
                else
                    decisions.set(data[i].elementId, [decision])
            }
        }
    })
}
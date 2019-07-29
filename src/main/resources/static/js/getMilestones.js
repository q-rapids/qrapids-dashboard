function getNextMilestones () {
    var today = new Date();
    jQuery.ajax({
        url: "../api/milestones?date=" + today.toISOString().substring(0, 10),
        type: "GET",
        async: true,
        success: function (data) {
            if (data.length > 0) {
                var milestone = data[0];
                var milestoneDate = new Date(milestone.date);
                var daysDiff = Math.round((milestoneDate - today) / (1000 * 60 * 60 * 24));
                var message = "<b>" + daysDiff + "</b> days for the next <b>" + milestone.type + "</b>: " + milestone.description;
                $("#milestone").html(message);
            }
        }
    })
}

getNextMilestones();
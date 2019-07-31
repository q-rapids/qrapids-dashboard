function getNextMilestones () {
    var today = new Date();
    jQuery.ajax({
        url: "../api/milestones?date=" + today.toISOString().substring(0, 10),
        type: "GET",
        async: true,
        success: function (milestones) {
            if (milestones.length > 0) {
                $("#postIt").show();
                var milestone = milestones[0];
                var milestoneDate = new Date(milestone.date);
                var daysDiff = Math.round((milestoneDate - today) / (1000 * 60 * 60 * 24));
                var message = "<b>" + daysDiff + "</b> days for the next <b>" + milestone.type + "</b>: " + milestone.description;
                $("#milestone").html(message);
            }
        }
    })
}

function getMilestonesList () {
    jQuery.ajax({
        url: "../api/milestones",
        type: "GET",
        async: true,
        success: function (milestones) {
            if (milestones.length > 0) {
                $("#milestonesItems").empty();
                milestones.forEach(function (milestone) {
                    $("#milestonesItems").append('<tr class="milestoneItem"><td>' + milestone.date + '</td><td>' + milestone.type + '</td><td>' + milestone.name + '</td><td>' + milestone.description + '</td></tr>');
                });
                $("#milestonesModal").modal();
            }
        }
    })
}

getNextMilestones();
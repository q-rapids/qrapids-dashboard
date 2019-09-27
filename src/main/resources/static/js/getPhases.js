function getPhasesList () {
    jQuery.ajax({
        url: "../api/phases",
        type: "GET",
        async: true,
        success: function (phases) {
            if (phases.length > 0) {
                $("#phasesItems").empty();
                phases.forEach(function (phase) {
                    $("#phasesItems").append('<tr class="phaseItem"><td>' + phase.date_from + '</td><td>' + phase.date_to + '</td><td>' + phase.name + '</td><td>' + phase.description + '</td></tr>');
                });
                $("#phasesModal").modal();
            }
        }
    })
}
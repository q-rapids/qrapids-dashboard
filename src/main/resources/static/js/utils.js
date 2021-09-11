function warningUtils(type, text) {
    //alert(type +": " + text);
    $("#warningModalType").empty();
    $("#warningModalBody").empty();
    if(type=="Ok") {
        $("#warningModalBody").append('<img class="icons" src="../icons/ok.jpg" style="padding-left:15px;">');
        //$("#warningModalType").append('<h4 style="color:limegreen">' + type + '</h4>');
    }
    else if(type=="Warning") {
        //$("#warningModalType").append('<h4 style="color:darkorange">' + type + '</h4>');
        $("#warningModalBody").append('<img class="icons" src="../icons/warning.jpg" style="padding-left:15px;" >');
    }
    else {
        //$("#warningModalType").append('<h4 style="color:red">' + type + '</h4>');
        $("#warningModalBody").append('<img class="icons" src="../icons/error.jpg" style="padding-left:15px;" >');
    }

    $("#warningModalBody").append('<span style="padding-left:15px;font-size:15px">' + "&nbsp;" + text + '</span>');
    $("#warningModal").modal();
    //$("#SIweightsModal").modal();
    //$("#SIweightsModal").modal();
}
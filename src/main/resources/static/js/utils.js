function warningUtils(type, text) {
    $("#warningModalType").empty();
    $("#warningModalBody").empty();
    if(type=="Ok") {
        $("#warningModalBody").append('<img class="icons" src="../icons/ok.jpg" style="padding-left:15px;">');
    }
    else if(type=="Warning") {
        $("#warningModalBody").append('<img class="icons" src="../icons/warning.jpg" style="padding-left:15px;" >');
    }
    else {
        $("#warningModalBody").append('<img class="icons" src="../icons/error.jpg" style="padding-left:15px;" >');
    }

    $("#warningModalBody").append('<span style="padding-left:15px;font-size:15px">' + "&nbsp;" + text + '</span>');
    $("#warningModal").modal();
}
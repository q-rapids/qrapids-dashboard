var stompClient = null;

function connect() {
    // Create and init the SockJS object
    var serverUrl = sessionStorage.getItem("serverUrl");
    var socket = new SockJS(serverUrl+'/ws');
    stompClient = Stomp.over(socket);
    // Subscribe the '/notify' channell
    stompClient.connect({}, function(frame) {
        stompClient.subscribe('/queue/notify', function(notification) {
            // Call the notify function when receive a notification
            showNotification(JSON.parse(notification.body).message);
        });
    });
} // function connect

function checkAlertsPending(){
    var serverUrl = sessionStorage.getItem("serverUrl");
    jQuery.ajax({
        dataType: "json",
        url: serverUrl+'/api/alerts/countNew',
        cache: false,
        type: "GET",
        async: true,
        success: function (data) {
            var newAlerts = data.newAlerts;
            var newAlertsWithQR = data.newAlertsWithQR;
            var newAlertsText = "";
            var newAlertsWithQRText = "";
            if (newAlerts > 0) {
                if (newAlerts == 1) newAlertsText = "There is " + newAlerts + " new alert";
                else newAlertsText = "There are " + newAlerts + " new alerts";

                if(newAlertsWithQR > 0) {
                    if (newAlertsWithQR == 1) newAlertsWithQRText += newAlertsWithQR + " of them has QR associated";
                    else newAlertsWithQRText += newAlertsWithQR + " of them have QR associated";
                }
            }
            $("#alertsPending").text(newAlertsText);
            $("#qrAlertsPending").text(newAlertsWithQRText);
            $("#AlertsBanner").attr("href", serverUrl+"/QualityAlerts")
        }
    });
}

function clearAlertsPendingBanner() {
    $("#alertsPending").text("");
    $("#qrAlertsPending").text("");
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function showNotification(message) {
    // $.notify({
    //     message: "New notification",
    //     url: "../QualityAlerts",
    //     target: "_self"
    // },{
    //     // settings
    //     type: "warning",
    //     delay: 0,
    // });
    checkAlertsPending();
}

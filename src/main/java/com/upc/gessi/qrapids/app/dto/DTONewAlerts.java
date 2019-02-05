package com.upc.gessi.qrapids.app.dto;

public class DTONewAlerts {
    private long newAlerts;
    private long newAlertsWithQR;

    public DTONewAlerts(long newAlerts, long newAlertsWithQR) {
        this.newAlerts = newAlerts;
        this.newAlertsWithQR = newAlertsWithQR;
    }

    public long getNewAlerts() {
        return newAlerts;
    }

    public void setNewAlerts(long newAlerts) {
        this.newAlerts = newAlerts;
    }

    public long getNewAlertsWithQR() {
        return newAlertsWithQR;
    }

    public void setNewAlertsWithQR(long newAlertsWithQR) {
        this.newAlertsWithQR = newAlertsWithQR;
    }
}

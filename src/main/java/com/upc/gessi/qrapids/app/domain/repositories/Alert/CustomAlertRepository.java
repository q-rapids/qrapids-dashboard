package com.upc.gessi.qrapids.app.domain.repositories.Alert;

import com.upc.gessi.qrapids.app.dto.DTOAlert;
import com.upc.gessi.qrapids.app.domain.models.Alert;

import java.io.Serializable;
import java.util.List;

public interface CustomAlertRepository extends Serializable {
    List<DTOAlert> getAlerts();
    List<Alert> getAlertByName(String name);
}

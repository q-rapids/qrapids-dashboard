package com.upc.gessi.qrapids.app.domain.models;

import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOFactor;
import util.FormattedDates;

import java.time.LocalDate;
import java.util.*;

public class Factors {
    private List<DTOFactor> elements;

    public void Factors() {
        this.elements = new ArrayList<>();
    }
    public void Factors (List <DTOFactor> factors) {
        setFactors(factors);
    }

    public List <DTOFactor> getFactors() {
        return elements;
    }
    public void setFactors(List <DTOFactor> factors) {
        this.elements = factors;
    }

    public void clearStrategicIndicatorsRelations(LocalDate date) {
        String qma_date = FormattedDates.formatDate(date);
        List <String> si_IDs;

        for  (DTOFactor factor: elements){
            si_IDs = factor.getStrategicIndicators();
            factor.getStrategicIndicators().removeIf((String si_id)  ->  si_id.contains(qma_date));
        }
    }
    public void clearStrategicIndicatorsRelations(LocalDate date, String strategicIndicatorName) {
        List <String> si_IDs;
        String si_hardID=strategicIndicatorName + "-" + FormattedDates.formatDate(date);

        for  (DTOFactor factor: elements){
            si_IDs = factor.getStrategicIndicators();
            factor.getStrategicIndicators().removeIf((String si_id)  ->  si_id.contains(si_hardID));
        }
    }

}

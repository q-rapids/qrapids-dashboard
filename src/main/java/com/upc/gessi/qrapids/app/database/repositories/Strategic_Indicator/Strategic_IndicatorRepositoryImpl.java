package com.upc.gessi.qrapids.app.database.repositories.Strategic_Indicator;

import com.upc.gessi.qrapids.app.domain.repositories.Strategic_Indicator.CustomStrategic_IndicatorRepository;
import com.upc.gessi.qrapids.app.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.domain.models.StrategicIndicatorEvaluation;


import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
public class Strategic_IndicatorRepositoryImpl implements CustomStrategic_IndicatorRepository {


    @PersistenceContext
    private EntityManager entityManager;


    public List<DTOStrategicIndicatorEvaluation> CurrentEvaluation(){
        List<DTOStrategicIndicatorEvaluation> dtoSIE = new ArrayList<>();
        TypedQuery<StrategicIndicatorEvaluation> siquery = this.entityManager.createQuery("SELECT si FROM Strategic_Indicator si WHERE date = (select max(si2.date) from Strategic_Indicator si2 where si2.id = si.id AND si2.date <= current_date)", StrategicIndicatorEvaluation.class);
        List<StrategicIndicatorEvaluation> SIList = siquery.getResultList();

        //for(int i = 0; i < SIList.size(); ++i) {
        //    dtoSIE.add(new DTOStrategicIndicatorEvaluation(SIList.get(i).getId(), SIList.get(i).getName(), SIList.get(i).getTarget(),SIList.get(i).getLowerthreshold(),SIList.get(i).getUpperthreshold(), SIList.get(i).getValue(), SIList.get(i).getDate().toLocalDate(), "Database"));
        //}
        return dtoSIE;
    }

    public List<DTOStrategicIndicatorEvaluation> getSIData(){
        List<DTOStrategicIndicatorEvaluation> dtoSIE = new ArrayList<>();
        TypedQuery<StrategicIndicatorEvaluation> siquery = this.entityManager.createQuery("SELECT si FROM StrategicIndicatorEvaluation si", StrategicIndicatorEvaluation.class);
        List<StrategicIndicatorEvaluation> SIList = siquery.getResultList();

        //for(int i = 0; i < SIList.size(); ++i) {
        //    dtoSIE.add(new DTOStrategicIndicatorEvaluation(SIList.get(i).getId(), SIList.get(i).getName(), SIList.get(i).getTarget(),SIList.get(i).getLowerthreshold(),SIList.get(i).getUpperthreshold(), 0.0f, null, null));
        //}
        return dtoSIE;
    }

    public List<DTOStrategicIndicatorEvaluation> HistoricalData(){
        List<DTOStrategicIndicatorEvaluation> dtoSIE = new ArrayList<>();
        TypedQuery<StrategicIndicatorEvaluation> siquery = this.entityManager.createQuery("SELECT si FROM StrategicIndicatorEvaluation si", StrategicIndicatorEvaluation.class);
        List<StrategicIndicatorEvaluation> SIList = siquery.getResultList();
        StrategicIndicatorEvaluation id = SIList.get(0);
        //for(int i = 0; i < SIList.size(); ++i) {
        //    dtoSIE.add(new DTOStrategicIndicatorEvaluation(id.getId(), id.getName(), id.getTarget(), id.getLowerthreshold(), id.getUpperthreshold(), SIList.get(i).getValue(), SIList.get(i).getDate().toLocalDate(), "Database"));
        //}
        return dtoSIE;
    }

}

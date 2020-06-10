package com.upc.gessi.qrapids.app.presentation.rest.services;

import com.upc.gessi.qrapids.app.domain.controllers.ProjectsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import com.upc.gessi.qrapids.app.domain.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.exceptions.ProjectNotFoundException;
import com.upc.gessi.qrapids.app.domain.models.Project;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOSIAssessment;
import com.upc.gessi.qrapids.app.presentation.rest.dto.DTOStrategicIndicatorEvaluation;
import com.upc.gessi.qrapids.app.presentation.rest.dto.reporting.DTOStrategicIndicatorReportInfo;
import com.upc.gessi.qrapids.app.presentation.rest.services.helpers.Messages;
import org.elasticsearch.ElasticsearchStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Reporting {

    @Autowired
    private ProjectsController projectsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    private Logger logger = LoggerFactory.getLogger(Reporting.class);

    @GetMapping("/api/reporting/strategicIndicators")
    @ResponseStatus(HttpStatus.OK)
    public List<DTOStrategicIndicatorReportInfo> getStrategicIndicatorsReportInfo(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to) {
        try {
            Project project = projectsController.findProjectByExternalId(prj);
            List<DTOStrategicIndicatorEvaluation> current_data = strategicIndicatorsController.getAllStrategicIndicatorsCurrentEvaluation(prj);
            List<DTOStrategicIndicatorEvaluation> historic_data = strategicIndicatorsController.getAllStrategicIndicatorsHistoricalEvaluation(prj, LocalDate.parse(from), LocalDate.parse(to));
            List<DTOStrategicIndicatorReportInfo> result = new ArrayList<>();
            int j = 0;
            for (int i = 0; i < current_data.size(); i++) {
                DTOStrategicIndicatorEvaluation aux = current_data.get(i);
                DTOStrategicIndicatorReportInfo si_info = new DTOStrategicIndicatorReportInfo(aux.getId(),project.getName(),aux.getName(),aux.getDescription(),
                        aux.getValue(), aux.getDbId(),aux.getRationale(),aux.getProbabilities(),aux.getDate());
                List<DTOStrategicIndicatorReportInfo.DTOHistoricalData> si_hist_info = new ArrayList<>();
                while (j < historic_data.size() && aux.getId().equals(historic_data.get(j).getId())) {
                    DTOStrategicIndicatorEvaluation hist_aux = historic_data.get(j);
                    DTOStrategicIndicatorReportInfo.DTOHistoricalData hist_info = new DTOStrategicIndicatorReportInfo.DTOHistoricalData(hist_aux.getValue(),hist_aux.getRationale(),hist_aux.getDate());
                    si_hist_info.add(hist_info);
                    j++;
                }
                si_info.setHistoricalDataList(si_hist_info);
                result.add(si_info);
            }
            return result;
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.PROJECT_NOT_FOUND);
        } catch (CategoriesException | ProjectNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.CATEGORIES_DO_NOT_MATCH);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR + e.getMessage());
        }
    }
}

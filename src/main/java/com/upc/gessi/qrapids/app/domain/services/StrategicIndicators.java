package com.upc.gessi.qrapids.app.domain.services;

import com.upc.gessi.qrapids.app.domain.adapters.Forecast;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAFakedata;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMAStrategicIndicators;
import com.upc.gessi.qrapids.app.exceptions.CategoriesException;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.database.repositories.Strategic_Indicator.Strategic_IndicatorRepositoryImpl;
import com.upc.gessi.qrapids.app.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


@RestController
public class StrategicIndicators {

    @Autowired
    private Strategic_IndicatorRepositoryImpl kpirep;

    @Autowired
    private QMAStrategicIndicators qmasi;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private QMAFakedata qmafake;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private Forecast qmaf;

    @RequestMapping("/api/StrategicIndicators/CurrentEvaluation")
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsEvaluation(@RequestParam(value = "prj", required=false) String prj, HttpServletRequest request, HttpServletResponse response) {
        if (qmafake.usingFakeData()) {
            return kpirep.CurrentEvaluation();
        } else {
            try {
                return qmasi.CurrentEvaluation(prj);
            } catch (CategoriesException e) {
                System.err.println(e.getMessage());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return null;
            } catch (IOException e) {
                System.err.println(e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }

        }
    }

    @RequestMapping("/api/StrategicIndicators/HistoricalData")
    public @ResponseBody
    List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsHistoricalData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to, HttpServletResponse response) {
        if (qmafake.usingFakeData()) {
            return kpirep.HistoricalData();
        } else {
            try {
                return qmasi.HistoricalData(LocalDate.parse(from), LocalDate.parse(to), prj);
            } catch (CategoriesException e) {
                System.err.println(e.getMessage());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return null;
            } catch (IOException e) {
                System.err.println(e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
        }
    }

    @RequestMapping("/api/DetailedStrategicIndicators/CurrentEvaluation")
    public List<DTODetailedStrategicIndicator> getDetailedSI(@RequestParam(value = "prj", required=false) String prj, HttpServletResponse response) {
        try {
            return qmadsi.CurrentEvaluation(null, prj);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @RequestMapping("/api/DetailedStrategicIndicators/CurrentEvaluation/{id}")
    public List<DTODetailedStrategicIndicator> getDetailedSIbyID(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id, HttpServletResponse response) {
        try {
            return qmadsi.CurrentEvaluation(id, prj);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @RequestMapping("/api/DetailedStrategicIndicators/HistoricalData")
    public @ResponseBody
    List<DTODetailedStrategicIndicator> getDetailedSIHistorical(@RequestParam(value = "prj", required=false) String prj, @RequestParam("from") String from, @RequestParam("to") String to, HttpServletResponse response) {
        try {
            return qmadsi.HistoricalData(null, LocalDate.parse(from), LocalDate.parse(to), prj);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @RequestMapping("/api/DetailedStrategicIndicators/HistoricalData/{id}")
    public @ResponseBody
    List<DTODetailedStrategicIndicator> getDetailedSIHistorical(@RequestParam(value = "prj", required=false) String prj, @PathVariable String id, @RequestParam("from") String from, @RequestParam("to") String to, HttpServletResponse response) {
        try {
            return qmadsi.HistoricalData(id, LocalDate.parse(from), LocalDate.parse(to), prj);
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @RequestMapping("/api/DetailedStrategicIndicators/PredictionData/{id}")
    public @ResponseBody
    List<DTODetailedStrategicIndicator> getQualityFactorsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("horizon") String horizon, @PathVariable String id) throws IOException {
        return qmaf.ForecastDSI(qmadsi.CurrentEvaluation(id, prj), "7", horizon, prj);
    }

    @RequestMapping("/api/DetailedStrategicIndicators/PredictionData")
    public @ResponseBody
    List<DTODetailedStrategicIndicator> getQualityFactorsPredicitionData(@RequestParam(value = "prj", required=false) String prj, @RequestParam("horizon") String horizon) throws IOException {
        return qmaf.ForecastDSI(qmadsi.CurrentEvaluation(null, prj), "7", horizon, prj);
    }

    @RequestMapping("/api/StrategicIndicators/PredictionData")
    public List<DTOStrategicIndicatorEvaluation> getStrategicIndicatorsPrediction(@RequestParam(value = "prj", required=false) String prj, @RequestParam("horizon") String horizon) throws IOException {
        return qmaf.ForecastSI("7", horizon, prj);
    }

    /*private List<DTOStrategicIndicatorEvaluation> mergeData(List<DTOStrategicIndicatorEvaluation> apiEval, List<Strategic_Indicator> dbEval) {
        boolean found = false;
        String lastSIid = "";
        for (Iterator<DTOStrategicIndicatorEvaluation> itAPI = apiEval.iterator(); itAPI.hasNext();) {
            DTOStrategicIndicatorEvaluation itemAPI = itAPI.next();
            found = false;
            if (lastSIid.equals(itemAPI.getId())) {
                found = true;
            } else {
                lastSIid = itemAPI.getId();
            }
            for (Iterator<Strategic_Indicator> itDB = dbEval.iterator(); itDB.hasNext() && !found;) {
                Strategic_Indicator itemDB = itDB.next();
                if (itemAPI.getId().equals(itemDB.getName().replaceAll("\\s+","").toLowerCase())) {
                    itemAPI.setLowerThreshold(0.33f);
                    itemAPI.setUpperThreshold(0.66f);
                    itemAPI.setTarget(0.5f);
                    itDB.remove();
                    found = true;
                }
            }
        }
        return apiEval;
    }*/
}

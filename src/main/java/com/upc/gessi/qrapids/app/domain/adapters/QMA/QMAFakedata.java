package com.upc.gessi.qrapids.app.domain.adapters.QMA;

import com.upc.gessi.qrapids.app.database.repositories.Strategic_Indicator.Strategic_IndicatorRepositoryImpl;
import com.upc.gessi.qrapids.app.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
*    This class has been created for testing purposes. It generates some data to simulate the connection to the QMA API
*    Data created for the period 2017-10-22 to 2017-10-24
*    The Quality model simulated corresponds to...
*
*    - Blocking
*       |--Blocking Code
*       |   |--%Passed Quality Rules
*       |--Testing Status
*       |   |--Test coverage
*       |   |--%Test Done
*       |   |--%Passed Tests
*       |--Testing Performance
*       |   |--Unit test duration
*       |   |--%Unit test run
*       |--Delayed Issues                   <REMOVED!>
*       |   |--%Delayed tasks               <REMOVED!>
*       |   |--#Blocking tasks              <REMOVED!>
*       |   |--#High priority not started   <REMOVED!>
*       |--Definition Readiness
*           |--%Complete defined issues
*           |--%New issues
*           |--%Modified issues
*    - Product Quality
*       |--Code Quality
*       |   |-- % files without duplications
*       |   |--% non-complex files
*       |   |--% commented files
*       |--Testing Status
*       |   |--Test coverage
*       |   |--%Test Done
*       |   |--%Passed Tests
*       |--Usage <REMOVED!!!>
*       |   |--%Heavily used features
*       |   |--Median usage time
*       |   |--Median frequency
*       |--Stability
*           |--#Crashes
*           |--Mean time between failues
*           |--#Customer Bugs"
*    - Time-to-Market
*       |--Code Quality
*       |   |-- % files without duplications
*       |   |--% non-complex files
*       |   |--% commented files
*       |--Testing Status
*       |   |--Test coverage
*       |   |--%Test Done
*       |   |--%Passed Tests
*       |--Testing Performance
*       |   |--Unit test duration
*       |   |--%Unit test run
*       |--Delayed Issues                   <REMOVED!>
*       |   |--%Delayed tasks               <REMOVED!>
*       |   |--#Blocking tasks              <REMOVED!>
*       |   |--#High priority not started   <REMOVED!>
*       |--Definition Readiness
*       |   |--%Complete defined issues
*       |   |--%New issues
*       |   |--%Modified issues
*       |--Issues Velocity
*           |--%Unsolved issues
*           |--%New issues
*           |--%Removed issues
*           |--%Modified issues
*    - Customer Satisfaction
*       |--Popularity
*       |   |--Customer Raking
*       |   |--Positive Feedback
*       |--Usage
*       |   |--%Heavily used features
*       |   |--Median usage time
*       |   |--Median frequency
*       |--Stability
*           |--#Crashes
*           |--Mean time between failues
*           |--#Customer Bugs
* */
@Component
public class QMAFakedata {

    @Autowired
    private Strategic_IndicatorRepositoryImpl kpirep;

    @Value("${fakeData}")
    private boolean fake=false;

    public boolean usingFakeData() {
        return fake;
    }

    // Fake Data for the Strategic Indicators Views is in the database
    public List <DTOStrategicIndicatorEvaluation> getSIs(){
        // The fake data for the strategic indicators is in the database
        return kpirep.CurrentEvaluation();
    }
    public List <DTOStrategicIndicatorEvaluation> getHistoricalSIs(){
        // The fake data for the strategic indicators is in the database
        return kpirep.HistoricalData();
    }

    // Fake Data for the Detailed Strategic  Indicators Views
    public List<DTODetailedStrategicIndicator> getDetailedSIs(String siID) {
        List<DTODetailedStrategicIndicator> dsi;
        dsi = new ArrayList<DTODetailedStrategicIndicator>();
        /*
        if (siID == null || siID.equals("BLK")|| siID.equals("blocking")) {
            dsi.add(new DTODetailedStrategicIndicator("blocking", "Blocking", new ArrayList<DTOFactor>() {{
                add(new DTOFactor("BC", "Blocking Code", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.75f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("TS", "Testing Status", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.33f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("TP", "Testing Performance", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.82f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("DR", "Definition Readiness", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.30f, LocalDate.of(2017,10,24), "fake"));
                }}));
            }}));
        }
        if (siID == null || siID.equals("PQ") || siID.equals("productquality")) {
            dsi.add(new DTODetailedStrategicIndicator("productquality", "Product Quality", new ArrayList<DTOFactor>() {{
                add(new DTOFactor("CQ", "Code Quality", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("TS", "Testing Status", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.73f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("S", "Stability", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.46f, LocalDate.of(2017,10,24), "fake"));
                }}));
            }}));
        }
        if (siID == null || siID.equals("TTM") || siID.equals("ontimedelivery")) {
            dsi.add(new DTODetailedStrategicIndicator("ontimedelivery", "On-time Delivery", new ArrayList<DTOFactor>() {{
                add(new DTOFactor("CQ", "Code Quality", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("TS", "Testing Status", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.83f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("TP", "Testing Performance", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.52f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("DR", "Definition Readiness", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.82f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("IV", "Issues Velocity", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.42f, LocalDate.of(2017,10,24), "fake"));
                }}));
            }}));
        }
        if (siID == null || siID.equals("CS")  || siID.equals("customersatisfaction")) {
            dsi.add(new DTODetailedStrategicIndicator("customersatisfaction", "Customer Satisfaction", new ArrayList<DTOFactor>() {{
                add(new DTOFactor("U", "Usage", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.64f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("S", "Stability", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.46f, LocalDate.of(2017,10,24), "fake"));
                }}));
                add(new DTOFactor("Pop", "Popularity", new ArrayList<DTOEvaluation>() {{
                    add(new DTOEvaluation(0.7f, LocalDate.of(2017,10,24), "fake"));
                }}));
            }}));
        }*/
        return dsi;
    }

    public List<DTODetailedStrategicIndicator> getHistoricalDetailedSIs(String siID) {
        List<DTODetailedStrategicIndicator> dsi = new ArrayList<DTODetailedStrategicIndicator>();/*
        // Filling the list with fake data
        if (siID == null || siID.equals("BLK") || siID.equals("blocking")) {
            dsi.add(new DTODetailedStrategicIndicator("blocking", "Blocking",
                    new ArrayList<DTOFactor>() {{
                        add(new DTOFactor("BC", "Blocking Code", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.42f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.75f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("TS", "Testing Status", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.1f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.5f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.33f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("TP", "Testing Performance", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.12f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.82f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("DR", "Definition Readiness", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.96f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.36f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.22f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                    }}
            ));
        }
        if (siID == null || siID.equals("PQ") || siID.equals("productquality")) {
            dsi.add(new DTODetailedStrategicIndicator("productquality", "Product Quality",
                    new ArrayList<DTOFactor>() {{
                        add(new DTOFactor("CQ", "Code Quality", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.80f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.78f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("TS", "Testing Status", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.1f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.5f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.33f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("S", "Stability", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.87f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.12f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.46f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                    }}
            ));
        }
        if (siID == null || siID.equals("TTM") || siID.equals("ontimedelivery")) {
            dsi.add(new DTODetailedStrategicIndicator("ontimedelivery", "On-time Delivery",
                    new ArrayList<DTOFactor>() {{
                        add(new DTOFactor("BC", "Blocking Code", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.42f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.75f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("TS", "Testing Status", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.1f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.5f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.33f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("TP", "Testing Performance", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.12f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.56f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.82f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("DR", "Definition Readiness", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.96f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.36f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.22f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("IV", "Issues Velocity", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.22f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.72f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.42f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                    }}
            ));
        }
        if (siID == null || siID.equals("CS") || siID.equals("customersatisfaction")) {
            dsi.add(new DTODetailedStrategicIndicator("customersatisfaction", "Customer Satisfaction",
                    new ArrayList<DTOFactor>() {{
                        add(new DTOFactor("U", "Usage", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.14f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.35f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.64f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("S", "Stability", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.87f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.12f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.46f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                        add(new DTOFactor("Pop", "Popularity", new ArrayList<DTOEvaluation>() {{
                            add(new DTOEvaluation(0.5f, LocalDate.of(2017,10,22), "fake"));
                            add(new DTOEvaluation(0.6f, LocalDate.of(2017,10,23), "fake"));
                            add(new DTOEvaluation(0.7f, LocalDate.of(2017,10,24), "fake"));
                        }}));
                    }}
            ));
        }*/
        return dsi;
    }

    // Fake Data for Quality Factors Views
    public List<DTOQualityFactor> getFactors(String siID) {
        List<DTOQualityFactor> qf = new ArrayList<DTOQualityFactor>();

        if (siID == null || siID.equals("PQ") || siID.equals("BLK") || siID.equals("TTM") ||
             siID.equals("productquality") || siID.equals("blocking") || siID.equals("ontimedelivery"))
        {
            qf.add(new DTOQualityFactor("TS", "Testing Status", getMetrics("TS")));
        }

        if (siID == null || siID.equals("BLK") || siID.equals("TTM") ||
                siID.equals("blocking") || siID.equals("ontimedelivery"))
        {
            qf.add(new DTOQualityFactor("BK", "Blocking Code", getMetrics("BC")));
            qf.add(new DTOQualityFactor("TP", "Testing Performance", getMetrics("TP")));
            qf.add(new DTOQualityFactor("DR", "Definition Readiness", getMetrics("DR")));
        }
        if (siID == null || siID.equals("CS") || siID.equals("PQ") || siID.equals("customersatisfaction") || siID.equals("productquality"))
        {
            qf.add(new DTOQualityFactor("S", "Stability", getMetrics("S")));
        }
        if (siID == null || siID.equals("CS") || siID.equals("customersatisfaction")) {
            qf.add(new DTOQualityFactor("Pop", "Popularity", getMetrics("Pop")));
            qf.add(new DTOQualityFactor("U", "Usage", getMetrics("U")));
        }

        if (siID == null || siID.equals("TTM")|| siID.equals("ontimedelivery"))
            qf.add(new DTOQualityFactor("IV", "Issues Velocity", getMetrics("IV")));


        if (siID == null || siID.equals("PQ")|| siID.equals("productquality")){
            qf.add(new DTOQualityFactor("CQ", "Code Quality", getMetrics("CQ")));
        }
        return qf;

    }

    public List<DTOQualityFactor> getHistoricalFactors(String siID) {

        List<DTOQualityFactor> qf = new ArrayList<DTOQualityFactor>();

        if (siID == null || siID.equals("PQ") || siID.equals("BLK") || siID.equals("TTM") ||
                siID.equals("productquality") || siID.equals("blocking") || siID.equals("ontimedelivery")) {
            qf.add(new DTOQualityFactor("TS", "Testing Status", getHistoricalMetrics("TS")));
        }

        if (siID == null || siID.equals("BLK") || siID.equals("TTM") ||
                siID.equals("blocking") || siID.equals("ontimedelivery")) {
            qf.add(new DTOQualityFactor("BC", "Blocking Code", getHistoricalMetrics("BC")));
            qf.add(new DTOQualityFactor("TP", "Testing Performance", getHistoricalMetrics("TP")));
            qf.add(new DTOQualityFactor("DR", "Definition Readiness", getHistoricalMetrics("DR")));
        }
        if (siID == null || siID.equals("CS") ||
                siID.equals("PQ") || siID.equals("customersatisfaction") || siID.equals("productquality"))
        {
            qf.add(new DTOQualityFactor("S", "Stability", getHistoricalMetrics("S")));
        }
        if (siID == null || siID.equals("CS") || siID.equals("customersatisfaction")) {
            qf.add(new DTOQualityFactor("U", "Popularity", getHistoricalMetrics("Pop")));
            qf.add(new DTOQualityFactor("U", "Usage", getHistoricalMetrics("U")));
        }

        if (siID == null || siID.equals("TTM")|| siID.equals("ontimedelivery"))
            qf.add(new DTOQualityFactor("IV", "Issues Velocity", getHistoricalMetrics("IV")));

        if (siID == null || siID.equals("PQ")|| siID.equals("productquality")){
            qf.add(new DTOQualityFactor("CQ", "Code Quality", getHistoricalMetrics("CQ")));
        }
        return qf;
    }

    // Fake Data for Metrics Views
    public List<DTOMetric> getMetrics(String qfID) {
        List<DTOMetric> metrics;
        metrics = new ArrayList<DTOMetric>();

        if (qfID == null || qfID.equals("CQ")) {
            metrics.add(new DTOMetric("DF", "% files without duplications", "","SonarQube", "", LocalDate.parse("2017-10-24"), 0.87f));
            metrics.add(new DTOMetric("CC", "% non-complex files","", "SonarQube", "", LocalDate.parse("2017-10-24"), 0.77f));
            metrics.add(new DTOMetric("CmC", "% commented files", "","SonarQube", "", LocalDate.parse("2017-10-24"), 0.37f));

        }
        if (qfID == null || qfID.equals("BC")) {
            metrics.add(new DTOMetric("PQR", "%Passed Quality Rules","", "SonarQube", "", LocalDate.parse("2017-10-24"), 0.87f));
        }

        if (qfID == null || qfID.equals("TS")) {
            metrics.add(new DTOMetric("TC", "Test Coverage","SonarQube","", "", LocalDate.parse("2017-10-24"), 0.25f));
            metrics.add(new DTOMetric("TD", "%Test Done", "Jenkings","", "",LocalDate.parse("2017-10-24"), 0.9f));
            metrics.add(new DTOMetric("TP", "%Test Passed", "Jenkings", "", "", LocalDate.parse("2017-10-24"), 0.8f));
        }
        if (qfID == null || qfID.equals("TP")) {
            metrics.add(new DTOMetric("UTD", "Unit Test Duration", "SonarQube","", "", LocalDate.parse("2017-10-24"), 0.5f));
            metrics.add(new DTOMetric("UTD", "Unit Test Run", "SonarQube", "","", LocalDate.parse("2017-10-24"), 0.9f));
        }
/*        if (qfID == null || qfID.equals("DI")) {
            metrics.add(new DTOMetric("DT", "Delayed Tasks", "Jira", "2017-10-24", 0.12f));
            metrics.add(new DTOMetric("BT", "Blocking Tasks", "Jira", "2017-10-24", 0.25f));
            metrics.add(new DTOMetric("HNS", "High Priorty Waiting", "Jira", "2017-10-24", 0.0f));
        }*/
        if (qfID == null || qfID.equals("U")) {
            metrics.add(new DTOMetric("HUF", "%Heavily used Features", "","Jira", "", LocalDate.parse("2017-10-24"), 0.9f));
            metrics.add(new DTOMetric("MUT", "Median usage Time", "","Usage Log", "", LocalDate.parse("2017-10-24"), 0.25f));
            metrics.add(new DTOMetric("MF", "Median usage Frecuency", "","Usage Log", "", LocalDate.parse("2017-10-24"), 0.63f));
        }
        if (qfID == null || qfID.equals("S")) {
            metrics.add(new DTOMetric("NC", "#Crashes", "exceptions Log","", "", LocalDate.parse("2017-10-24"), 0.2f));
            metrics.add(new DTOMetric("MT", "Mean time between failues", "","", "exceptions Log", LocalDate.parse("2017-10-24"), 0.75f));
            metrics.add(new DTOMetric("CB", "#Customer Bugs", "","Jira", "", LocalDate.parse("2017-10-24"), 0.1f));
        }
        if (qfID == null || qfID.equals("Pop")) {
            metrics.add(new DTOMetric("CR", "Customer Ranking", "Feedback Tool", "","", LocalDate.parse("2017-10-24"), 0.8f));
            metrics.add(new DTOMetric("PF", "Positive Feedback", "Feedback Tool", "","", LocalDate.parse("2017-10-24"), 0.5f));
        }
        // The following metrics are used in 2 factors: issues velocity and definition readiness. First we compute the shared metrics, and then for each factor we include the metrids they don't share
        if (qfID == null || qfID.equals("IV") || qfID.equals("DR")) {
            metrics.add(new DTOMetric("NI", "%New Issues", "","Jira", "", LocalDate.parse("2017-10-24"), 0.1f));
            metrics.add(new DTOMetric("MI", "%Modified Issues", "","Jira", "", LocalDate.parse("2017-10-24"), 0.9f));
        }
        if (qfID == null || qfID.equals("DR")) {
            metrics.add(new DTOMetric("CDI", "Complete Defined Issues","", "Jira", "", LocalDate.parse("2017-10-24"), 0.5f));
        }
        if (qfID == null || qfID.equals("IV")) {
            metrics.add(new DTOMetric("UI", "%Unsolved issues", "Jira","", "", LocalDate.parse("2017-10-24"), 0.5f));
            metrics.add(new DTOMetric("RI", "%Removed issues", "Jira","", "", LocalDate.parse("2017-10-24"), 0.0f));
        }

        return metrics;
    }


    public List<DTOMetric> getHistoricalMetrics(String qfID) {
        List<DTOMetric> metrics = new ArrayList<DTOMetric>();
        /*

        if (qfID == null || qfID.equals("CQ")) {
            metrics.add(new DTOMetric("DF", "% files without duplications", "SonarQube", "2017-10-22", 0.05f));
            metrics.add(new DTOMetric("DF", "% files without duplications", "SonarQube", "2017-10-23", 0.75f));
            metrics.add(new DTOMetric("DF", "% files without duplications", "SonarQube", "2017-10-24", 0.87f));
            metrics.add(new DTOMetric("CC", "% non-complex files", "SonarQube", "2017-10-22", 0.60f));
            metrics.add(new DTOMetric("CC", "% non-complex files", "SonarQube", "2017-10-23", 0.70f));
            metrics.add(new DTOMetric("CC", "% non-complex files", "SonarQube", "2017-10-24", 0.77f));
            metrics.add(new DTOMetric("CmC", "% commented files", "SonarQube", "2017-10-22", 0.37f));
            metrics.add(new DTOMetric("CmC", "% commented files", "SonarQube", "2017-10-23", 0.37f));
            metrics.add(new DTOMetric("CmC", "% commented files", "SonarQube", "2017-10-24", 0.37f));

        }
        if (qfID == null || qfID.equals("BC")) {
            metrics.add(new DTOMetric("PQR", "%Passed Quality Rules", "SonarQube", "2017-10-22", 0.87f));
            metrics.add(new DTOMetric("PQR", "%Passed Quality Rules", "SonarQube", "2017-10-23", 0.80f));
            metrics.add(new DTOMetric("PQR", "%Passed Quality Rules", "SonarQube", "2017-10-24", 0.75f));
        }
        if (qfID == null || qfID.equals("TS")) {
            metrics.add(new DTOMetric("TC", "Test Coverage","SonarQube", "2017-10-22", 0.0f));
            metrics.add(new DTOMetric("TC", "Test Coverage","SonarQube", "2017-10-23", 0.15f));
            metrics.add(new DTOMetric("TC", "Test Coverage","SonarQube", "2017-10-24", 0.25f));
            metrics.add(new DTOMetric("TD", "%Test Done", "Jenkings","2017-10-22", 0.60f));
            metrics.add(new DTOMetric("TD", "%Test Done", "Jenkings","2017-10-23", 0.80f));
            metrics.add(new DTOMetric("TD", "%Test Done", "Jenkings","2017-10-24", 0.9f));
            metrics.add(new DTOMetric("TP", "%Test Passed", "Jenkings", "2017-10-22", 0.7f));
            metrics.add(new DTOMetric("TP", "%Test Passed", "Jenkings", "2017-10-23", 0.75f));
            metrics.add(new DTOMetric("TP", "%Test Passed", "Jenkings", "2017-10-24", 0.8f));
        }
        if (qfID == null || qfID.equals("TP")) {
            metrics.add(new DTOMetric("UTD", "Unit Test Duration", "SonarQube", "2017-10-22", 0.1f));
            metrics.add(new DTOMetric("UTD", "Unit Test Duration", "SonarQube", "2017-10-23", 0.25f));
            metrics.add(new DTOMetric("UTD", "Unit Test Duration", "SonarQube", "2017-10-24", 0.5f));
            metrics.add(new DTOMetric("UTD", "Unit Test Run", "SonarQube", "2017-10-22", 0.6f));
            metrics.add(new DTOMetric("UTD", "Unit Test Run", "SonarQube", "2017-10-23", 0.75f));
            metrics.add(new DTOMetric("UTD", "Unit Test Run", "SonarQube", "2017-10-24", 0.9f));
        }
/*        if (qfID == null || qfID.equals("DI")) {
            metrics.add(new DTOMetric("DT", "Delayed Tasks", "Jira", "2017-10-22", 0.25f));
            metrics.add(new DTOMetric("DT", "Delayed Tasks", "Jira", "2017-10-23", 0.15f));
            metrics.add(new DTOMetric("DT", "Delayed Tasks", "Jira", "2017-10-24", 0.12f));
            metrics.add(new DTOMetric("BT", "Blocking Tasks", "Jira", "2017-10-22", 0.10f));
            metrics.add(new DTOMetric("BT", "Blocking Tasks", "Jira", "2017-10-23", 0.15f));
            metrics.add(new DTOMetric("BT", "Blocking Tasks", "Jira", "2017-10-24", 0.25f));
            metrics.add(new DTOMetric("HNS", "High Priorty Waiting", "Jira", "2017-10-22", 0.1f));
            metrics.add(new DTOMetric("HNS", "High Priorty Waiting", "Jira", "2017-10-23", 0.0f));
            metrics.add(new DTOMetric("HNS", "High Priorty Waiting", "Jira", "2017-10-24", 0.0f));
        }
        */ /*
        if (qfID == null || qfID.equals("U")) {
            metrics.add(new DTOMetric("HUF", "%Heavily used Features", "Jira", "2017-10-22", 0.8f));
            metrics.add(new DTOMetric("HUF", "%Heavily used Features", "Jira", "2017-10-23", 0.8f));
            metrics.add(new DTOMetric("HUF", "%Heavily used Features", "Jira", "2017-10-24", 0.9f));
            metrics.add(new DTOMetric("MUT", "Median usage Time", "Usage Log", "2017-10-22", 0.1f));
            metrics.add(new DTOMetric("MUT", "Median usage Time", "Usage Log", "2017-10-23", 0.15f));
            metrics.add(new DTOMetric("MUT", "Median usage Time", "Usage Log", "2017-10-24", 0.25f));
            metrics.add(new DTOMetric("MF", "Median usage Frecuency", "Usage Log", "2017-10-22", 0.80f));
            metrics.add(new DTOMetric("MF", "Median usage Frecuency", "Usage Log", "2017-10-23", 0.70f));
            metrics.add(new DTOMetric("MF", "Median usage Frecuency", "Usage Log", "2017-10-24", 0.63f));
        }
        if (qfID == null || qfID.equals("S")) {
            metrics.add(new DTOMetric("NC", "#Crashes", "exceptions Log", "2017-10-22", 0.2f));
            metrics.add(new DTOMetric("NC", "#Crashes", "exceptions Log", "2017-10-23", 0.2f));
            metrics.add(new DTOMetric("NC", "#Crashes", "exceptions Log", "2017-10-24", 0.2f));
            metrics.add(new DTOMetric("MT", "Mean time between failues", "exceptions Log", "2017-10-22", 0.75f));
            metrics.add(new DTOMetric("MT", "Mean time between failues", "exceptions Log", "2017-10-23", 0.75f));
            metrics.add(new DTOMetric("MT", "Mean time between failues", "exceptions Log", "2017-10-24", 0.75f));
            metrics.add(new DTOMetric("CB", "#Customer Bugs", "Jira", "2017-10-22", 0.1f));
            metrics.add(new DTOMetric("CB", "#Customer Bugs", "Jira", "2017-10-23", 0.1f));
            metrics.add(new DTOMetric("CB", "#Customer Bugs", "Jira", "2017-10-24", 0.1f));
        }
        if (qfID == null || qfID.equals("Pop")) {
            metrics.add(new DTOMetric("CR", "Customer Ranking", "Feedback Tool", "2017-10-22", 0.85f));
            metrics.add(new DTOMetric("CR", "Customer Ranking", "Feedback Tool", "2017-10-23", 0.75f));
            metrics.add(new DTOMetric("CR", "Customer Ranking", "Feedback Tool", "2017-10-24", 0.8f));
            metrics.add(new DTOMetric("PF", "Possitive Feedback", "Feedback Tool", "2017-10-24", 0.35f));
            metrics.add(new DTOMetric("PF", "Possitive Feedback", "Feedback Tool", "2017-10-24", 0.40f));
            metrics.add(new DTOMetric("PF", "Possitive Feedback", "Feedback Tool", "2017-10-24", 0.5f));
        }
        // The following metrics are used in 2 factors: issues velocity and definition readiness. First we compute the shared metrics, and then for each factor we include the metrids they don't share
        if (qfID == null || qfID.equals("IV") || qfID.equals("DR")) {
            metrics.add(new DTOMetric("NI", "%New Issues", "Jira", "2017-10-22", 0.1f));
            metrics.add(new DTOMetric("NI", "%New Issues", "Jira", "2017-10-23", 0.1f));
            metrics.add(new DTOMetric("NI", "%New Issues", "Jira", "2017-10-24", 0.1f));
            metrics.add(new DTOMetric("MI", "%Modified Issues", "Jira", "2017-10-22", 0.9f));
            metrics.add(new DTOMetric("MI", "%Modified Issues", "Jira", "2017-10-23", 0.9f));
            metrics.add(new DTOMetric("MI", "%Modified Issues", "Jira", "2017-10-24", 0.9f));
        }
        if (qfID == null || qfID.equals("DR")) {
            metrics.add(new DTOMetric("CDI", "Complete Defined Issues", "Jira", "2017-10-22", 0.1f));
            metrics.add(new DTOMetric("CDI", "Complete Defined Issues", "Jira", "2017-10-23", 0.25f));
            metrics.add(new DTOMetric("CDI", "Complete Defined Issues", "Jira", "2017-10-24", 0.5f));
        }
        if (qfID == null || qfID.equals("IV")) {
            metrics.add(new DTOMetric("UI", "%Unsolved issues", "Jira", "2017-10-22", 0.25f));
            metrics.add(new DTOMetric("UI", "%Unsolved issues", "Jira", "2017-10-23", 0.35f));
            metrics.add(new DTOMetric("UI", "%Unsolved issues", "Jira", "2017-10-24", 0.5f));
            metrics.add(new DTOMetric("RI", "%Removed issues", "Jira", "2017-10-22", 0.0f));
            metrics.add(new DTOMetric("RI", "%Removed issues", "Jira", "2017-10-23", 0.0f));
            metrics.add(new DTOMetric("RI", "%Removed issues", "Jira", "2017-10-24", 0.0f));
        }
        */
        return metrics;
    }
}
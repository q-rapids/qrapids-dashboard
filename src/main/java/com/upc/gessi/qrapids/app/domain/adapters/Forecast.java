package com.upc.gessi.qrapids.app.domain.adapters;

import com.google.gson.Gson;
import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
import com.upc.gessi.qrapids.app.domain.controllers.QualityFactorsController;
import com.upc.gessi.qrapids.app.domain.controllers.StrategicIndicatorsController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.upc.gessi.qrapids.app.config.QMAConnection;
import com.upc.gessi.qrapids.app.presentation.rest.dto.*;
import com.upc.gessi.qrapids.app.domain.repositories.StrategicIndicator.StrategicIndicatorRepository;
import com.upc.gessi.qrapids.app.domain.models.Strategic_Indicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.util.*;

@Component
public class Forecast {

    private static final String UTF_8 = "utf-8";
    private static final String FREQUENCY_QUERY = "&frequency=";
    private static final String HORIZON_QUERY = "&horizon=";
    private static final String TECHNIQUE_QUERY = "&technique=";
    private static final String METRIC_QUERY = "&metric=";
    private static final String HOST_QUERY = "&host=";
    private static final String PORT_QUERY = "&port=";
    private static final String PATH_QUERY = "&path=";
    private static final String USER_QUERY = "&user=";
    private static final String PWD_QUERY = "&pwd=";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    public static final String GET = "GET";
    private static final String ERROR = "error";
    private static final String LOWER_80 = "lower80";
    private static final String UPPER_80 = "upper80";
    private static final String LOWER_95 = "lower95";
    private static final String UPPER_95 = "upper95";
    private static final String MEAN = "mean";
    private static final String ID = "id";
    private static final String FORECAST_SOURCE = "Forecast";

    @Autowired
    private QMAConnection connection;

    @Value("${forecast.url}")
    private String url;

    // @Value("${forecast.path}")
    @Value("${qma.path}")
    private String path;

//    @Value("${forecast.prefix}")
    @Value("${qma.prefix}")
    private String prefix;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private AssesSI assesSI;

    public List<String> getForecastTechniques () {
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/api/ForecastTechniques")
                .queryParam("host", connection.getIp())
                .queryParam("port", String.valueOf(connection.getPort()))
                .queryParam("path", path)
                .queryParam("user", connection.getUsername())
                .queryParam("pwd", connection.getPassword());


        ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().encode().toUri(), String.class);

        HttpStatus statusCode = responseEntity.getStatusCode();
        List<String> techniques = new ArrayList<>();
        if (statusCode == HttpStatus.OK) {
            Gson gson = new Gson();
            String[] techniquesArray = gson.fromJson(responseEntity.getBody(), String[].class);
            techniques = Arrays.asList(techniquesArray);
        }
        return techniques;
    }

    public void trainMetricForecast(List<DTOMetric> metrics, String freq, String prj, String technique) {
        List<String> elements = new ArrayList<>();
        for (DTOMetric metric : metrics) {
            elements.add(metric.getId());
        }
        trainForecastRequest(elements, Constants.INDEX_METRICS, freq, prj, technique);
    }

    public void trainFactorForecast(List<DTOQualityFactor> factors, String freq, String prj, String technique) {
        List<String> elements = new ArrayList<>();
        for (DTOQualityFactor factor : factors) {
            elements.add(factor.getId());
        }
        trainForecastRequest(elements, Constants.INDEX_FACTORS, freq, prj, technique);
    }

    private void trainForecastRequest(List<String> elements, String index, String freq, String prj, String technique) {
        if (prefix == null) prefix = "";
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/api/Train")
                .queryParam("host", connection.getIp())
                .queryParam("port", String.valueOf(connection.getPort()))
                .queryParam("path", path)
                .queryParam("user", connection.getUsername())
                .queryParam("pwd", connection.getPassword())
                .queryParam("index", prefix + index + "." + prj)
                .queryParam("elements", (Object[]) elements.toArray(new String[0]))
                .queryParam("frequency", freq);

        if (technique != null) {
            builder.queryParam("technique", technique);
        }

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().encode().toUri(), String.class);

        HttpStatus statusCode = responseEntity.getStatusCode();
    }

    public List<DTOMetric> ForecastMetric(List<DTOMetric> metric, String technique, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/Metrics/Forecast?index_metrics=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_METRICS + "." + prj, UTF_8)).append(FREQUENCY_QUERY).append(URLEncoder.encode(freq, UTF_8));
        urlString.append(HORIZON_QUERY).append(URLEncoder.encode(horizon, UTF_8));
        urlString.append(TECHNIQUE_QUERY).append(URLEncoder.encode(technique, UTF_8));
        for(DTOMetric m : metric) {
            urlString.append(METRIC_QUERY).append(URLEncoder.encode(m.getId(), UTF_8));
        }
        urlString.append(HOST_QUERY).append(URLEncoder.encode(connection.getIp(), UTF_8));
        urlString.append(PORT_QUERY).append(URLEncoder.encode(String.valueOf(connection.getPort()), UTF_8));
        urlString.append(PATH_QUERY).append(URLEncoder.encode(path, UTF_8));
        urlString.append(USER_QUERY).append(URLEncoder.encode(connection.getUsername(), UTF_8));
        urlString.append(PWD_QUERY).append(URLEncoder.encode(connection.getPassword(), UTF_8));
        URL url = new URL(urlString.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(GET);

        con.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);

        int status = con.getResponseCode();
        if (status == 200) {
            return getDtoMetrics(metric, con);
        }
        return null;
    }

    private List<DTOMetric> getDtoMetrics(List<DTOMetric> metric, HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        List<DTOMetric> result = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray data = parser.parse(content.toString()).getAsJsonArray();
        for (int i = 0; i < data.size(); ++i) {
            JsonObject object = data.get(i).getAsJsonObject();

            //check if error occurred
            if (!object.get(ERROR).isJsonNull()) {
                getMetricWithError(metric, result, object);
            }
            else {
                getMetrics(metric, result, object);
            }
        }
        return result;
    }

    private void getMetrics(List<DTOMetric> metric, List<DTOMetric> result, JsonObject object) {
        //check if json values are null
        JsonArray lower80;
        if (!object.get(LOWER_80).isJsonNull()) lower80 = object.getAsJsonArray(LOWER_80);
        else lower80 = new JsonArray();

        JsonArray upper80;
        if (!object.get(UPPER_80).isJsonNull()) upper80 = object.getAsJsonArray(UPPER_80);
        else upper80 = new JsonArray();

        JsonArray lower95;
        if (!object.get(LOWER_95).isJsonNull()) lower95 = object.getAsJsonArray(LOWER_95);
        else lower95 = new JsonArray();

        JsonArray upper95;
        if (!object.get(UPPER_95).isJsonNull()) upper95 = object.getAsJsonArray(UPPER_95);
        else upper95 = new JsonArray();

        JsonArray mean;
        if (!object.get(MEAN).isJsonNull()) mean = object.getAsJsonArray(MEAN);
        else mean = new JsonArray();

        String id = object.get(ID).getAsString();

        for (DTOMetric m : metric) {
            buildMetric(result, lower80, upper80, lower95, upper95, mean, id, m);
        }
    }

    private void buildMetric(List<DTOMetric> result, JsonArray lower80, JsonArray upper80, JsonArray lower95, JsonArray upper95, JsonArray mean, String id, DTOMetric m) {
        if (m.getId().equals(id) && lower80.size() == upper80.size() && lower95.size() == upper95.size() && lower80.size() == lower95.size() && lower80.size() == mean.size()) {
            if (lower80.size() > 0) {
                for (int j = 0; j < lower80.size(); ++j) {
                    result.add(new DTOMetric(m.getId(), m.getName(),
                            m.getDescription(),
                            m.getDatasource(),
                            m.getRationale(),
                            LocalDate.now().plusDays((long) j), mean.get(j).getAsFloat(), Pair.of(upper80.get(j).getAsFloat(), lower80.get(j).getAsFloat()), Pair.of(upper95.get(j).getAsFloat(), lower95.get(j).getAsFloat())));
                }
            } else {
                result.add(new DTOMetric(m.getId(), m.getName(),
                        m.getDescription(),
                        m.getDatasource(),
                        m.getRationale(),
                        LocalDate.now(), null, null, null));
            }
        }
    }

    private void getMetricWithError(List<DTOMetric> metric, List<DTOMetric> result, JsonObject object) {
        String error = object.get(ERROR).getAsString();
        String id = object.get(ID).getAsString();
        for (DTOMetric m : metric) {
            if (m.getId().equals(id)) {
                result.add(new DTOMetric(id, m.getName(), error));
            }
        }
    }

    public List<DTOQualityFactor> ForecastFactor(List<DTOQualityFactor> factor, String technique, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/Metrics/Forecast?index_metrics=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_METRICS + "." + prj, UTF_8)).append(FREQUENCY_QUERY).append(URLEncoder.encode(freq, UTF_8));
        urlString.append(HORIZON_QUERY).append(URLEncoder.encode(horizon, UTF_8));
        urlString.append(TECHNIQUE_QUERY).append(URLEncoder.encode(technique, UTF_8));
        Map<String, ArrayList<Integer>> metrics = new HashMap<>();
        Map<String, String> metricsNames = new HashMap<>();

        buildMetricsForFactors(factor, metrics, metricsNames);

        for(Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
            urlString.append(METRIC_QUERY).append(URLEncoder.encode(m.getKey(), UTF_8));
        }
        urlString.append(HOST_QUERY).append(URLEncoder.encode(connection.getIp(), UTF_8));
        urlString.append(PORT_QUERY).append(URLEncoder.encode(String.valueOf(connection.getPort()), UTF_8));
        urlString.append(PATH_QUERY).append(URLEncoder.encode(path, UTF_8));
        urlString.append(USER_QUERY).append(URLEncoder.encode(connection.getUsername(), UTF_8));
        urlString.append(PWD_QUERY).append(URLEncoder.encode(connection.getPassword(), UTF_8));
        URL url = new URL(urlString.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(GET);

        con.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);

        int status = con.getResponseCode();
        if (status == 200) {
            return getDtoQualityFactors(factor, metrics, metricsNames, con);
        }
        return null;
    }

    private List<DTOQualityFactor> getDtoQualityFactors(List<DTOQualityFactor> factor, Map<String, ArrayList<Integer>> metrics, Map<String, String> metricsNames, HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        List<List<DTOMetric>> metricsMatrix = new ArrayList<>();
        for (int x = 0; x < factor.size(); ++x) {
            metricsMatrix.add(new ArrayList<>());
        }

        JsonParser parser = new JsonParser();
        JsonArray data = parser.parse(content.toString()).getAsJsonArray();
        for (int i = 0; i < data.size(); ++i) {
            JsonObject object = data.get(i).getAsJsonObject();

            //check if error occurred
            if (!object.get(ERROR).isJsonNull()) {
                getMetricForFactorWithError(metrics, metricsNames, metricsMatrix, object);
            }
            else {
                getMetricsForFactors(metrics, metricsNames, metricsMatrix, object);
            }
        }

        for (int i = 0; i < factor.size(); ++i) {
            factor.get(i).setMetrics(metricsMatrix.get(i));
        }
        return factor;
    }

    private void getMetricsForFactors(Map<String, ArrayList<Integer>> metrics, Map<String, String> metricsNames, List<List<DTOMetric>> metricsMatrix, JsonObject object) {
        //check if json values are null
        JsonArray lower80;
        if (!object.get(LOWER_80).isJsonNull()) lower80 = object.getAsJsonArray(LOWER_80);
        else lower80 = new JsonArray();

        JsonArray upper80;
        if (!object.get(UPPER_80).isJsonNull()) upper80 = object.getAsJsonArray(UPPER_80);
        else upper80 = new JsonArray();

        JsonArray lower95;
        if (!object.get(LOWER_95).isJsonNull()) lower95 = object.getAsJsonArray(LOWER_95);
        else lower95 = new JsonArray();

        JsonArray upper95;
        if (!object.get(UPPER_95).isJsonNull()) upper95 = object.getAsJsonArray(UPPER_95);
        else upper95 = new JsonArray();

        JsonArray mean;
        if (!object.get(MEAN).isJsonNull()) mean = object.getAsJsonArray(MEAN);
        else mean = new JsonArray();

        String id = object.get(ID).getAsString();

        for (Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
            buildMetricForFactor(metricsNames, metricsMatrix, lower80, upper80, lower95, upper95, mean, id, m);
        }
    }

    private void buildMetricForFactor(Map<String, String> metricsNames, List<List<DTOMetric>> metricsMatrix, JsonArray lower80, JsonArray upper80, JsonArray lower95, JsonArray upper95, JsonArray mean, String id, Map.Entry<String, ArrayList<Integer>> m) {
        if (m.getKey().equals(id) && lower80.size() == upper80.size() && lower95.size() == upper95.size() && lower80.size() == lower95.size() && lower80.size() == mean.size()) {
            if (lower80.size() > 0) {
                for (int j = 0; j < lower80.size(); ++j) {
                    for (Integer index : m.getValue())
                        metricsMatrix.get(index).add(new DTOMetric(m.getKey(),
                                metricsNames.get(m.getKey()),
                                "",
                                FORECAST_SOURCE,
                                FORECAST_SOURCE,
                                LocalDate.now().plusDays((long) j), mean.get(j).getAsFloat(), Pair.of(upper80.get(j).getAsFloat(), lower80.get(j).getAsFloat()), Pair.of(upper95.get(j).getAsFloat(), lower95.get(j).getAsFloat())));
                }
            } else {
                for (Integer index : m.getValue())
                    metricsMatrix.get(index).add(new DTOMetric(m.getKey(),
                            metricsNames.get(m.getKey()),
                            "",
                            FORECAST_SOURCE,
                            FORECAST_SOURCE,
                            LocalDate.now(), null, null, null));
            }
        }
    }

    private void getMetricForFactorWithError(Map<String, ArrayList<Integer>> metrics, Map<String, String> metricsNames, List<List<DTOMetric>> metricsMatrix, JsonObject object) {
        String error = object.get(ERROR).getAsString();
        String id = object.get(ID).getAsString();
        for (Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
            if (m.getKey().equals(id)) {
                for (Integer index : m.getValue())
                    metricsMatrix.get(index).add(new DTOMetric(id, metricsNames.get(m.getKey()), error));
            }
        }
    }

    private void buildMetricsForFactors(List<DTOQualityFactor> factor, Map<String, ArrayList<Integer>> metrics, Map<String, String> metricsNames) {
        for (int i = 0; i < factor.size(); ++i) {
            for (int j = 0; j < factor.get(i).getMetrics().size(); ++j) {
                if (metrics.containsKey(factor.get(i).getMetrics().get(j).getId())) {
                    metrics.get(factor.get(i).getMetrics().get(j).getId()).add(i);
                } else {
                    ArrayList<Integer> index = new ArrayList<>();
                    index.add(i);
                    metrics.put(factor.get(i).getMetrics().get(j).getId(), index);
                    metricsNames.put(factor.get(i).getMetrics().get(j).getId(), factor.get(i).getMetrics().get(j).getName());
                }
            }
        }
    }

    public List<DTODetailedStrategicIndicator> ForecastDSI(List<DTODetailedStrategicIndicator> dsi, String technique, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/QualityFactors/Forecast?index_factors=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_FACTORS + "." + prj, UTF_8)).append(FREQUENCY_QUERY).append(URLEncoder.encode(freq, UTF_8));
        urlString.append(HORIZON_QUERY).append(URLEncoder.encode(horizon, UTF_8));
        urlString.append(TECHNIQUE_QUERY).append(URLEncoder.encode(technique, UTF_8));
        Map<String, ArrayList<Integer>> factors = new HashMap<>();
        Map<String, String> factorsNames = new HashMap<>();

        buildFactorsForStrategicIndicator(dsi, factors, factorsNames);

        for(Map.Entry<String, ArrayList<Integer>> m : factors.entrySet()) {
            urlString.append("&factor=").append(URLEncoder.encode(m.getKey(), UTF_8));
        }
        urlString.append(HOST_QUERY).append(URLEncoder.encode(connection.getIp(), UTF_8));
        urlString.append(PORT_QUERY).append(URLEncoder.encode(String.valueOf(connection.getPort()), UTF_8));
        urlString.append(PATH_QUERY).append(URLEncoder.encode(path, UTF_8));
        urlString.append(USER_QUERY).append(URLEncoder.encode(connection.getUsername(), UTF_8));
        urlString.append(PWD_QUERY).append(URLEncoder.encode(connection.getPassword(), UTF_8));
        URL url = new URL(urlString.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(GET);

        con.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);

        int status = con.getResponseCode();
        if (status == 200) {
            return getDtoDetailedStrategicIndicators(dsi, factors, factorsNames, con);
        }
        return null;
    }

    private List<DTODetailedStrategicIndicator> getDtoDetailedStrategicIndicators(List<DTODetailedStrategicIndicator> dsi, Map<String, ArrayList<Integer>> factors, Map<String, String> factorsNames, HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        List<List<DTOFactor>> factorsMatrix = new ArrayList<>();
        for (int x = 0; x < dsi.size(); ++x) {
            factorsMatrix.add(new ArrayList<>());
        }

        JsonParser parser = new JsonParser();
        JsonArray data = parser.parse(content.toString()).getAsJsonArray();
        for (int i = 0; i < data.size(); ++i) {
            JsonObject object = data.get(i).getAsJsonObject();

            if (!object.get(ERROR).isJsonNull()) {
                getFactorWithError(factors, factorsNames, factorsMatrix, object);
            }
            else {
                getFactors(factors, factorsNames, factorsMatrix, object);
            }
        }

        for (int i = 0; i < dsi.size(); ++i) {
            dsi.get(i).setFactors(factorsMatrix.get(i));
        }
        return dsi;
    }

    private void getFactors(Map<String, ArrayList<Integer>> factors, Map<String, String> factorsNames, List<List<DTOFactor>> factorsMatrix, JsonObject object) {
        //check if json values are null
        JsonArray lower80;
        if (!object.get(LOWER_80).isJsonNull()) lower80 = object.getAsJsonArray(LOWER_80);
        else lower80 = new JsonArray();

        JsonArray upper80;
        if (!object.get(UPPER_80).isJsonNull()) upper80 = object.getAsJsonArray(UPPER_80);
        else upper80 = new JsonArray();

        JsonArray lower95;
        if (!object.get(LOWER_95).isJsonNull()) lower95 = object.getAsJsonArray(LOWER_95);
        else lower95 = new JsonArray();

        JsonArray upper95;
        if (!object.get(UPPER_95).isJsonNull()) upper95 = object.getAsJsonArray(UPPER_95);
        else upper95 = new JsonArray();

        JsonArray mean;
        if (!object.get(MEAN).isJsonNull()) mean = object.getAsJsonArray(MEAN);
        else mean = new JsonArray();

        String id = object.get(ID).getAsString();

        for (Map.Entry<String, ArrayList<Integer>> m : factors.entrySet()) {
            buildFactor(factorsNames, factorsMatrix, lower80, upper80, lower95, upper95, mean, id, m);
        }
    }

    private void buildFactor(Map<String, String> factorsNames, List<List<DTOFactor>> factorsMatrix, JsonArray lower80, JsonArray upper80, JsonArray lower95, JsonArray upper95, JsonArray mean, String id, Map.Entry<String, ArrayList<Integer>> m) {
        if (m.getKey().equals(id) && lower80.size() == upper80.size() && lower95.size() == upper95.size() && lower80.size() == lower95.size() && lower80.size() == mean.size()) {
            if (lower80.size() > 0) {
                for (int j = 0; j < lower80.size(); ++j) {
                    for (Integer index : m.getValue())
                        factorsMatrix.get(index).add(new DTOFactor(m.getKey(), factorsNames.get(m.getKey()), "",
                                mean.get(j).getAsFloat(), LocalDate.now().plusDays((long) j), FORECAST_SOURCE, FORECAST_SOURCE, null));
                }
            } else {
                for (Integer index : m.getValue())
                    factorsMatrix.get(index).add(new DTOFactor(m.getKey(), factorsNames.get(m.getKey()), "",
                            null, LocalDate.now(), FORECAST_SOURCE, FORECAST_SOURCE, null));
            }
        }
    }

    private void getFactorWithError(Map<String, ArrayList<Integer>> factors, Map<String, String> factorsNames, List<List<DTOFactor>> factorsMatrix, JsonObject object) {
        String error = object.get(ERROR).getAsString();
        String id = object.get(ID).getAsString();
        for (Map.Entry<String, ArrayList<Integer>> f : factors.entrySet()) {
            if (f.getKey().equals(id)) {
                for (Integer index : f.getValue())
                    factorsMatrix.get(index).add(new DTOFactor(id, factorsNames.get(f.getKey()), error));
            }
        }
    }

    private void buildFactorsForStrategicIndicator(List<DTODetailedStrategicIndicator> dsi, Map<String, ArrayList<Integer>> factors, Map<String, String> factorsNames) {
        for (int i = 0; i < dsi.size(); ++i) {
            for (int j = 0; j < dsi.get(i).getFactors().size(); ++j) {
                if (factors.containsKey(dsi.get(i).getFactors().get(j).getId())) {
                    factors.get(dsi.get(i).getFactors().get(j).getId()).add(i);
                } else {
                    ArrayList<Integer> index = new ArrayList<>();
                    index.add(i);
                    factors.put(dsi.get(i).getFactors().get(j).getId(), index);
                    factorsNames.put(dsi.get(i).getFactors().get(j).getId(), dsi.get(i).getFactors().get(j).getName());
                }
            }
        }
    }

    public List<DTOStrategicIndicatorEvaluation> ForecastSI(String technique, String freq, String horizon, String prj) throws IOException {
        List<DTODetailedStrategicIndicator> dsis = ForecastDSI(qmadsi.CurrentEvaluation(null, prj), technique, freq, horizon, prj);
        List<DTOStrategicIndicatorEvaluation> result = new ArrayList<>();
        String categoriesDescription = strategicIndicatorsController.getCategories().toString();
        for (DTODetailedStrategicIndicator dsi : dsis) {
            Map<LocalDate, List<DTOFactor>> listSIFactors = new HashMap<>();
            Map<LocalDate,Map<String,String>> mapSIFactors = new HashMap<>();
            boolean factorHasForecastingError = factorHasForecastingError(dsi, listSIFactors, mapSIFactors);
            Strategic_Indicator si = null;
            for (Strategic_Indicator s : siRep.findAll()) {
                if (s.getName().replaceAll("\\s+","").toLowerCase().equals(dsi.getId()))
                    si = s;
            }
            if (factorHasForecastingError) {
                result.add(new DTOStrategicIndicatorEvaluation(dsi.getId(), dsi.getName(), "One or more factors have forecasting errors"));
            }
            else if (si != null && si.getNetwork() != null && si.getNetwork().length > 10) {
                getAndBuildDTOStrategicIndicatorEvaluationWithBayesianNetwork(result, categoriesDescription, dsi, mapSIFactors, si);
            } else if (si != null){
                getAndBuildDTOStrategicIndicatorEvaluation(result, categoriesDescription, listSIFactors, si);
            }
        }
        return result;
    }

    private void getAndBuildDTOStrategicIndicatorEvaluation(List<DTOStrategicIndicatorEvaluation> result, String categoriesDescription, Map<LocalDate, List<DTOFactor>> listSIFactors, Strategic_Indicator si) {
        for(Map.Entry<LocalDate,List<DTOFactor>> l : listSIFactors.entrySet()) {
            float value = strategicIndicatorsController.computeStrategicIndicatorValue(l.getValue());
            result.add(new DTOStrategicIndicatorEvaluation(si.getName().replaceAll("\\s+", "").toLowerCase(),
                    si.getName(),
                    si.getDescription(),
                    Pair.of(value, strategicIndicatorsController.getLabel(value)),
                    strategicIndicatorsController.getCategories(),
                    l.getKey(), "Dashboard Assessment",
                    si.getId(),
                    categoriesDescription,
                    si.getNetwork() != null));
        }
    }

    private void getAndBuildDTOStrategicIndicatorEvaluationWithBayesianNetwork(List<DTOStrategicIndicatorEvaluation> result, String categoriesDescription, DTODetailedStrategicIndicator dsi, Map<LocalDate, Map<String, String>> mapSIFactors, Strategic_Indicator si) throws IOException {
        for(Map.Entry<LocalDate,Map<String,String>> m : mapSIFactors.entrySet()) {
            File tempFile = File.createTempFile("network", ".dne", null);
            try(FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(si.getNetwork());
            }
            List<DTOSIAssessment> assessment = assesSI.assesSI(si.getName().replaceAll("\\s+", "").toLowerCase(), m.getValue(), tempFile);
            float value = strategicIndicatorsController.getValueAndLabelFromCategories(assessment).getFirst();
            result.add(new DTOStrategicIndicatorEvaluation(dsi.getId(),
                    si.getName(),
                    si.getDescription(),
                    Pair.of(value, strategicIndicatorsController.getLabel(value)),
                    assessment, m.getKey(),
                    "Dashboard Assessment",
                    si.getId(),
                    categoriesDescription,
                    si.getNetwork() != null));
        }
    }

    private boolean factorHasForecastingError(DTODetailedStrategicIndicator dsi, Map<LocalDate, List<DTOFactor>> listSIFactors, Map<LocalDate, Map<String, String>> mapSIFactors) {
        boolean factorHasForecastingError = false;
        for (DTOFactor factor : dsi.getFactors()) {
            if (!factorHasForecastingError) factorHasForecastingError = (factor.getForecastingError() != null);
            if (listSIFactors.containsKey(factor.getDate())) {
                listSIFactors.get(factor.getDate()).add(factor);
                mapSIFactors.get(factor.getDate()).put(factor.getId(), qualityFactorsController.getFactorLabelFromValue(factor.getValue()));
            } else {
                listSIFactors.put(factor.getDate(), new ArrayList<>());
                listSIFactors.get(factor.getDate()).add(factor);
                mapSIFactors.put(factor.getDate(), new HashMap<>());
                mapSIFactors.get(factor.getDate()).put(factor.getId(), qualityFactorsController.getFactorLabelFromValue(factor.getValue()));
            }
        }
        return factorHasForecastingError;
    }



}

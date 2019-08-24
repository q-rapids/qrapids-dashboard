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
import com.upc.gessi.qrapids.app.dto.*;
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

    @Autowired
    private QMAConnection connection;

    @Value("${forecast.url}")
    private String url;

    // @Value("${forecast.path}")
    @Value("${qma.path}")
    private String path;

//    @Value("${forecast.prefix}")
    @Value("${qma.prefix}")
    public static String prefix;

    @Autowired
    private QMADetailedStrategicIndicators qmadsi;

    @Autowired
    private com.upc.gessi.qrapids.app.domain.services.Util util;

    @Autowired
    private QualityFactorsController qualityFactorsController;

    @Autowired
    private StrategicIndicatorsController strategicIndicatorsController;

    @Autowired
    private StrategicIndicatorRepository siRep;

    @Autowired
    private AssesSI AssesSI;

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
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_METRICS + "." + prj, "utf-8")).append("&frequency=").append(URLEncoder.encode(freq, "utf-8"));
        urlString.append("&horizon=").append(URLEncoder.encode(horizon, "utf-8"));
        urlString.append("&technique=").append(URLEncoder.encode(technique, "utf-8"));
        for(DTOMetric m : metric) {
            urlString.append("&metric=").append(URLEncoder.encode(m.getId(), "utf-8"));
        }
        urlString.append("&host=").append(URLEncoder.encode(connection.getIp(), "utf-8"));
        urlString.append("&port=").append(URLEncoder.encode(String.valueOf(connection.getPort()), "utf-8"));
        urlString.append("&path=").append(URLEncoder.encode(path, "utf-8"));
        urlString.append("&user=").append(URLEncoder.encode(connection.getUsername(), "utf-8"));
        urlString.append("&pwd=").append(URLEncoder.encode(connection.getPassword(), "utf-8"));
        URL url = new URL(urlString.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Content-Type", "application/json");

        int status = con.getResponseCode();
        if (status == 200) {
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
                if (!object.get("error").isJsonNull()) {
                    String error = object.get("error").getAsString();
                    String id = object.get("id").getAsString();
                    for (DTOMetric m : metric) {
                        if (m.getId().equals(id)) {
                            result.add(new DTOMetric(id, m.getName(), error));
                        }
                    }
                }
                else {
                    //check if json values are null
                    JsonArray lower80;
                    if (!object.get("lower80").isJsonNull()) lower80 = object.getAsJsonArray("lower80");
                    else lower80 = new JsonArray();

                    JsonArray upper80;
                    if (!object.get("upper80").isJsonNull()) upper80 = object.getAsJsonArray("upper80");
                    else upper80 = new JsonArray();

                    JsonArray lower95;
                    if (!object.get("lower95").isJsonNull()) lower95 = object.getAsJsonArray("lower95");
                    else lower95 = new JsonArray();

                    JsonArray upper95;
                    if (!object.get("upper95").isJsonNull()) upper95 = object.getAsJsonArray("upper95");
                    else upper95 = new JsonArray();

                    JsonArray mean;
                    if (!object.get("mean").isJsonNull()) mean = object.getAsJsonArray("mean");
                    else mean = new JsonArray();

                    String id = object.get("id").getAsString();

                    for (DTOMetric m : metric) {
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
                }
            }
            return result;
        }
        return null;
    }

    public List<DTOQualityFactor> ForecastFactor(List<DTOQualityFactor> factor, String technique, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/Metrics/Forecast?index_metrics=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_METRICS + "." + prj, "utf-8")).append("&frequency=").append(URLEncoder.encode(freq, "utf-8"));
        urlString.append("&horizon=").append(URLEncoder.encode(horizon, "utf-8"));
        urlString.append("&technique=").append(URLEncoder.encode(technique, "utf-8"));
        Map<String, ArrayList<Integer>> metrics = new HashMap<>();
        Map<String, String> metricsNames = new HashMap<>();

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
        for(Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
            urlString.append("&metric=").append(URLEncoder.encode(m.getKey(), "utf-8"));
        }
        urlString.append("&host=").append(URLEncoder.encode(connection.getIp(), "utf-8"));
        urlString.append("&port=").append(URLEncoder.encode(String.valueOf(connection.getPort()), "utf-8"));
        urlString.append("&path=").append(URLEncoder.encode(path, "utf-8"));
        urlString.append("&user=").append(URLEncoder.encode(connection.getUsername(), "utf-8"));
        urlString.append("&pwd=").append(URLEncoder.encode(connection.getPassword(), "utf-8"));
        URL url = new URL(urlString.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Content-Type", "application/json");

        int status = con.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            List<List<DTOMetric>> metricsMatrix = new ArrayList<List<DTOMetric>>() {{
                for (int x = 0; x < factor.size(); ++x) {
                    add(new ArrayList<>());
                }
            }};

            JsonParser parser = new JsonParser();
            JsonArray data = parser.parse(content.toString()).getAsJsonArray();
            for (int i = 0; i < data.size(); ++i) {
                JsonObject object = data.get(i).getAsJsonObject();

                //check if error occurred
                if (!object.get("error").isJsonNull()) {
                    String error = object.get("error").getAsString();
                    String id = object.get("id").getAsString();
                    for (Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
                        if (m.getKey().equals(id)) {
                            for (Integer index : m.getValue())
                                metricsMatrix.get(index).add(new DTOMetric(id, metricsNames.get(m.getKey()), error));
                        }
                    }
                }
                else {
                    //check if json values are null
                    JsonArray lower80;
                    if (!object.get("lower80").isJsonNull()) lower80 = object.getAsJsonArray("lower80");
                    else lower80 = new JsonArray();

                    JsonArray upper80;
                    if (!object.get("upper80").isJsonNull()) upper80 = object.getAsJsonArray("upper80");
                    else upper80 = new JsonArray();

                    JsonArray lower95;
                    if (!object.get("lower95").isJsonNull()) lower95 = object.getAsJsonArray("lower95");
                    else lower95 = new JsonArray();

                    JsonArray upper95;
                    if (!object.get("upper95").isJsonNull()) upper95 = object.getAsJsonArray("upper95");
                    else upper95 = new JsonArray();

                    JsonArray mean;
                    if (!object.get("mean").isJsonNull()) mean = object.getAsJsonArray("mean");
                    else mean = new JsonArray();

                    String id = object.get("id").getAsString();

                    for (Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
                        if (m.getKey().equals(id) && lower80.size() == upper80.size() && lower95.size() == upper95.size() && lower80.size() == lower95.size() && lower80.size() == mean.size()) {
                            if (lower80.size() > 0) {
                                for (int j = 0; j < lower80.size(); ++j) {
                                    for (Integer index : m.getValue())
                                        metricsMatrix.get(index).add(new DTOMetric(m.getKey(),
                                                metricsNames.get(m.getKey()),
                                                "",
                                                "Forecast",
                                                "Forecast",
                                                LocalDate.now().plusDays((long) j), mean.get(j).getAsFloat(), Pair.of(upper80.get(j).getAsFloat(), lower80.get(j).getAsFloat()), Pair.of(upper95.get(j).getAsFloat(), lower95.get(j).getAsFloat())));
                                }
                            } else {
                                for (Integer index : m.getValue())
                                    metricsMatrix.get(index).add(new DTOMetric(m.getKey(),
                                            metricsNames.get(m.getKey()),
                                            "",
                                            "Forecast",
                                            "Forecast",
                                            LocalDate.now(), null, null, null));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < factor.size(); ++i) {
                factor.get(i).setMetrics(metricsMatrix.get(i));
            }
            return factor;
        }
        return null;
    }

    public List<DTODetailedStrategicIndicator> ForecastDSI(List<DTODetailedStrategicIndicator> dsi, String technique, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/QualityFactors/Forecast?index_factors=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_FACTORS + "." + prj, "utf-8")).append("&frequency=").append(URLEncoder.encode(freq, "utf-8"));
        urlString.append("&horizon=").append(URLEncoder.encode(horizon, "utf-8"));
        urlString.append("&technique=").append(URLEncoder.encode(technique, "utf-8"));
        Map<String, ArrayList<Integer>> factors = new HashMap<>();
        Map<String, String> factorsNames = new HashMap<>();

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
        for(Map.Entry<String, ArrayList<Integer>> m : factors.entrySet()) {
            urlString.append("&factor=").append(URLEncoder.encode(m.getKey(), "utf-8"));
        }
        urlString.append("&host=").append(URLEncoder.encode(connection.getIp(), "utf-8"));
        urlString.append("&port=").append(URLEncoder.encode(String.valueOf(connection.getPort()), "utf-8"));
        urlString.append("&path=").append(URLEncoder.encode(path, "utf-8"));
        urlString.append("&user=").append(URLEncoder.encode(connection.getUsername(), "utf-8"));
        urlString.append("&pwd=").append(URLEncoder.encode(connection.getPassword(), "utf-8"));
        URL url = new URL(urlString.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty("Content-Type", "application/json");

        int status = con.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            List<List<DTOFactor>> factorsMatrix = new ArrayList<List<DTOFactor>>() {{
                for (int x = 0; x < dsi.size(); ++x) {
                    add(new ArrayList<>());
                }
            }};

            JsonParser parser = new JsonParser();
            JsonArray data = parser.parse(content.toString()).getAsJsonArray();
            for (int i = 0; i < data.size(); ++i) {
                JsonObject object = data.get(i).getAsJsonObject();

                if (!object.get("error").isJsonNull()) {
                    String error = object.get("error").getAsString();
                    String id = object.get("id").getAsString();
                    for (Map.Entry<String, ArrayList<Integer>> f : factors.entrySet()) {
                        if (f.getKey().equals(id)) {
                            for (Integer index : f.getValue())
                                factorsMatrix.get(index).add(new DTOFactor(id, factorsNames.get(f.getKey()), error));
                        }
                    }
                }
                else {
                    //check if json values are null
                    JsonArray lower80;
                    if (!object.get("lower80").isJsonNull()) lower80 = object.getAsJsonArray("lower80");
                    else lower80 = new JsonArray();

                    JsonArray upper80;
                    if (!object.get("upper80").isJsonNull()) upper80 = object.getAsJsonArray("upper80");
                    else upper80 = new JsonArray();

                    JsonArray lower95;
                    if (!object.get("lower95").isJsonNull()) lower95 = object.getAsJsonArray("lower95");
                    else lower95 = new JsonArray();

                    JsonArray upper95;
                    if (!object.get("upper95").isJsonNull()) upper95 = object.getAsJsonArray("upper95");
                    else upper95 = new JsonArray();

                    JsonArray mean;
                    if (!object.get("mean").isJsonNull()) mean = object.getAsJsonArray("mean");
                    else mean = new JsonArray();

                    String id = object.get("id").getAsString();

                    for (Map.Entry<String, ArrayList<Integer>> m : factors.entrySet()) {
                        if (m.getKey().equals(id) && lower80.size() == upper80.size() && lower95.size() == upper95.size() && lower80.size() == lower95.size() && lower80.size() == mean.size()) {
                            if (lower80.size() > 0) {
                                for (int j = 0; j < lower80.size(); ++j) {
                                    for (Integer index : m.getValue())
                                        factorsMatrix.get(index).add(new DTOFactor(m.getKey(), factorsNames.get(m.getKey()), "",
                                                mean.get(j).getAsFloat(), LocalDate.now().plusDays((long) j), "Forecast", "Forecast", null));
                                }
                            } else {
                                for (Integer index : m.getValue())
                                    factorsMatrix.get(index).add(new DTOFactor(m.getKey(), factorsNames.get(m.getKey()), "",
                                            null, LocalDate.now(), "Forecast", "Forecast", null));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < dsi.size(); ++i) {
                dsi.get(i).setFactors(factorsMatrix.get(i));
            }
            return dsi;
        }
        return null;
    }

    public List<DTOStrategicIndicatorEvaluation> ForecastSI(String technique, String freq, String horizon, String prj) throws IOException {
        List<DTODetailedStrategicIndicator> dsis = ForecastDSI(qmadsi.CurrentEvaluation(null, prj), technique, freq, horizon, prj);
        List<DTOStrategicIndicatorEvaluation> result = new ArrayList<>();
        String categories_description = util.getCategories().toString();
        for (DTODetailedStrategicIndicator dsi : dsis) {
            Map<LocalDate, List<DTOFactor>> listSIFactors = new HashMap<>();
            Map<LocalDate,Map<String,String>> mapSIFactors = new HashMap<>();
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
            Strategic_Indicator si = null;
            for (Strategic_Indicator s : siRep.findAll()) {
                if (s.getName().replaceAll("\\s+","").toLowerCase().equals(dsi.getId()))
                    si = s;
            }
            if (factorHasForecastingError) {
                result.add(new DTOStrategicIndicatorEvaluation(dsi.getId(), dsi.getName(), "One or more factors have forecasting errors"));
            }
            else if (si != null && si.getNetwork() != null && si.getNetwork().length > 10) {
                for(Map.Entry<LocalDate,Map<String,String>> m : mapSIFactors.entrySet()) {
                    File tempFile = File.createTempFile("network", ".dne", null);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(si.getNetwork());
                    List<DTOSIAssessment> assessment = AssesSI.AssesSI(si.getName().replaceAll("\\s+", "").toLowerCase(), m.getValue(), tempFile);
                    float value = strategicIndicatorsController.getValueAndLabelFromCategories(assessment).getFirst();
                    result.add(new DTOStrategicIndicatorEvaluation(dsi.getId(),
                            si.getName(),
                            si.getDescription(),
                            Pair.of(value, util.getLabel(value)),
                            assessment, m.getKey(),
                            "Dashboard Assessment",
                            si.getId(),
                            categories_description,
                            si.getNetwork() != null));
                }
            } else if (si != null){
                for(Map.Entry<LocalDate,List<DTOFactor>> l : listSIFactors.entrySet()) {
                    float value = com.upc.gessi.qrapids.app.domain.services.Util.assesSI(l.getValue());
                    result.add(new DTOStrategicIndicatorEvaluation(si.getName().replaceAll("\\s+", "").toLowerCase(),
                            si.getName(),
                            si.getDescription(),
                            Pair.of(value, util.getLabel(value)),
                            util.getCategories(),
                            l.getKey(), "Dashboard Assessment",
                            si.getId(),
                            categories_description,
                            si.getNetwork() != null));
                }
            }
        }
        return result;
    }

}

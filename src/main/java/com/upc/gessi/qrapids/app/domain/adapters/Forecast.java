package com.upc.gessi.qrapids.app.domain.adapters;

import com.upc.gessi.qrapids.app.domain.adapters.QMA.QMADetailedStrategicIndicators;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private StrategicIndicatorRepository siRep;

    @Autowired
    private AssesSI AssesSI;

    public List<DTOMetric> ForecastMetric(List<DTOMetric> metric, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/Metrics/Forecast?index_metrics=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_METRICS + "." + prj, "utf-8")).append("&frequency=").append(URLEncoder.encode(freq, "utf-8"));
        urlString.append("&horizon=").append(URLEncoder.encode(horizon, "utf-8"));
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
                        }
                        else {
                            result.add(new DTOMetric(m.getId(), m.getName(),
                                    m.getDescription(),
                                    m.getDatasource(),
                                    m.getRationale(),
                                    LocalDate.now(), null, null, null));
                        }
                    }
                }
            }
            return result;
        }
        return null;
    }

    public List<DTOQualityFactor> ForecastFactor(List<DTOQualityFactor> factor, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/Metrics/Forecast?index_metrics=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_METRICS + "." + prj, "utf-8")).append("&frequency=").append(URLEncoder.encode(freq, "utf-8"));
        urlString.append("&horizon=").append(URLEncoder.encode(horizon, "utf-8"));
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

                for(Map.Entry<String, ArrayList<Integer>> m : metrics.entrySet()) {
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
                        }
                        else {
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

            for (int i = 0; i < factor.size(); ++i) {
                factor.get(i).setMetrics(metricsMatrix.get(i));
            }
            return factor;
        }
        return null;
    }

    public List<DTODetailedStrategicIndicator> ForecastDSI(List<DTODetailedStrategicIndicator> dsi, String freq, String horizon, String prj) throws IOException {
        StringBuffer urlString = new StringBuffer(url + "/api/QualityFactors/Forecast?index_factors=");
        if (prefix == null) prefix = "";
        urlString.append(URLEncoder.encode(prefix + Constants.INDEX_FACTORS + "." + prj, "utf-8")).append("&frequency=").append(URLEncoder.encode(freq, "utf-8"));
        urlString.append("&horizon=").append(URLEncoder.encode(horizon, "utf-8"));
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

                for(Map.Entry<String, ArrayList<Integer>> m : factors.entrySet()) {
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

            for (int i = 0; i < dsi.size(); ++i) {
                dsi.get(i).setFactors(factorsMatrix.get(i));
            }
            return dsi;
        }
        return null;
    }

    public List<DTOStrategicIndicatorEvaluation> ForecastSI(String freq, String horizon, String prj) throws IOException {
        List<DTODetailedStrategicIndicator> dsis = ForecastDSI(qmadsi.CurrentEvaluation(null, prj), freq, horizon, prj);
        List<DTOStrategicIndicatorEvaluation> result = new ArrayList<>();
        String categories_description = util.getCategories().toString();
        for (DTODetailedStrategicIndicator dsi : dsis) {
            Map<LocalDate, List<DTOFactor>> listSIFactors = new HashMap<>();
            Map<LocalDate,Map<String,String>> mapSIFactors = new HashMap<>();
            for (DTOFactor factor : dsi.getFactors()) {
                if (listSIFactors.containsKey(factor.getDate())) {
                    listSIFactors.get(factor.getDate()).add(factor);
                    mapSIFactors.get(factor.getDate()).put(factor.getId(), util.getQFLabelFromValue(factor.getValue()));
                } else {
                    listSIFactors.put(factor.getDate(), new ArrayList<>());
                    listSIFactors.get(factor.getDate()).add(factor);
                    mapSIFactors.put(factor.getDate(), new HashMap<>());
                    mapSIFactors.get(factor.getDate()).put(factor.getId(), util.getQFLabelFromValue(factor.getValue()));
                }
            }
            Strategic_Indicator si = null;
            for (Strategic_Indicator s : siRep.findAll()) {
                if (s.getName().replaceAll("\\s+","").toLowerCase().equals(dsi.getId()))
                    si = s;
            }
            if (si != null && si.getNetwork() != null && si.getNetwork().length > 10) {
                for(Map.Entry<LocalDate,Map<String,String>> m : mapSIFactors.entrySet()) {
                    File tempFile = File.createTempFile("network", ".dne", null);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(si.getNetwork());
                    List<DTOSIAssesment> assessment = AssesSI.AssesSI(si.getName().replaceAll("\\s+", "").toLowerCase(), m.getValue(), tempFile);
                    float value = util.getValueFromCategories(assessment);
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

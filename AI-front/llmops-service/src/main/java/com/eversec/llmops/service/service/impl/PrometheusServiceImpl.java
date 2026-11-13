package com.eversec.llmops.service.service.impl;



import com.eversec.llmops.service.service.PrometheusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class PrometheusServiceImpl implements PrometheusService {

    @Value("${prometheus.host}")
    private String prometheusHost;

    String podMetricsTemplate = "container_memory_working_set_bytes{job=\"kubelet\",pod=\"%s\"}";
    @Override
    public Map<String, Object> query(String namespaceName, String serviceName) {
        Map<String, Object> result = new HashMap<>();
        String prometheusQuery = String.format(podMetricsTemplate, serviceName);
        
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URIBuilder builder = new URIBuilder(prometheusHost + "/api/v1/query");
            builder.setParameter("query", prometheusQuery);
            
            HttpGet httpGet = new HttpGet(builder.build());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            result.put("status", jsonNode.path("status").asText());
            result.put("data", jsonNode.path("data"));

            if (jsonNode.has("error")) {
                result.put("error", jsonNode.path("error").asText());
            }
            
            response.close();
        } catch (Exception e) {
            result.put("error", e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }




    public Map<String, Object> queryRange(String promql, String start, String end, String step) {
        Map<String, Object> result = new HashMap<>();

        long now = Instant.now().getEpochSecond();
        long defaultEnd = now;
        long defaultStart = defaultEnd - 3600;
        String queryStart = (start != null && !start.isEmpty()) ? start : String.valueOf(defaultStart);
        String queryEnd   = (end   != null && !end.isEmpty())   ? end   : String.valueOf(defaultEnd);
        String queryStep  = (step  != null && !step.isEmpty())  ? step  : "15";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URIBuilder builder = new URIBuilder(prometheusHost + "/api/v1/query_range");
            builder.setParameter("query", promql);
            builder.setParameter("start", queryStart);
            builder.setParameter("end", queryEnd);
            builder.setParameter("step", queryStep);
            HttpGet httpGet = new HttpGet(builder.build());
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);
            result.put("status", jsonNode.path("status").asText());
            result.put("data", jsonNode.path("data"));
            if (jsonNode.has("error")) {
                result.put("error", jsonNode.path("error").asText());
            }
            response.close();
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
package com.eversec.llmops.service.service;

import java.util.Map;

public interface PrometheusService {

    Map<String, Object> query(String nameSpace, String serviceName);

    Map<String, Object> queryRange(String promql, String start, String end, String step);
}

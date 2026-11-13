package com.eversec.llmops.service.util.k8s;

import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangfei
 */
@Slf4j
public final class ExceptionUtil {
	private ExceptionUtil() {
	}

	public static boolean notFound(Exception e) {
		if (e.getMessage() == null) {
			return false;
		}

		boolean ret = e.getMessage().equalsIgnoreCase("Not Found");
		if (!ret) {
			if (e instanceof ApiException) {
				if (((ApiException) e).getCode() == 404) {
					return true;
				}
				log.warn("ApiException: {} - {}, {}, {}", ((ApiException) e).getCode(), ((ApiException) e).getResponseBody(), e.getMessage(),
						e.getLocalizedMessage(), e);
			} else {
				log.warn("NoApiException: {} - {}", e.getMessage(), e.getLocalizedMessage(), e);
			}
		}
		return ret;
	}
}

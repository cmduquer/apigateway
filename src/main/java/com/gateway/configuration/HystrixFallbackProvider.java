package com.gateway.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.exception.HystrixTimeoutException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HystrixFallbackProvider implements FallbackProvider{

	@Override
	public String getRoute() {
		return "multiplications";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		log.warn(String.format("route:%s,exceptionType:%s,stackTrace:%s", route, cause.getClass().getName(), Arrays.toString(cause.getStackTrace())));
        
		if (cause instanceof HystrixTimeoutException) {
            return response(HttpStatus.GATEWAY_TIMEOUT);
        } else {
            return response(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	private ClientHttpResponse response(final HttpStatus status) {
		return new ClientHttpResponse() {

			@Override
			public InputStream getBody() throws IOException {
				return new ByteArrayInputStream("{\"factorA\":\"Sorry, Service is Down!\",\"factorB\":\"?\",\"id\":null}".getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccessControlAllowCredentials(true);
                headers.setAccessControlAllowOrigin("*");
                return headers;
			}

			@Override
			public HttpStatus getStatusCode() throws IOException {
				return status;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return status.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return status.getReasonPhrase();
			}

			@Override
			public void close() {
			}
			
		};
	}
	

}

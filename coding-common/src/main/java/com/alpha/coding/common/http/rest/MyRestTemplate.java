package com.alpha.coding.common.http.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alpha.coding.bo.constant.Keys;

/**
 * MyRestTemplate.java
 *
 * @author nick
 * @version 1.0
 * Date: 2018-04-24
 */
public class MyRestTemplate extends RestTemplate {

    public MyRestTemplate() {
        super();
    }

    public MyRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
    }

    public MyRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    public <T> T getForObjectGeneric(String url, Type responseType, Class<T> responseClass, Object... uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseClass, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T getForObjectGeneric(String url, Type responseType, Class<T> responseClass, Map<String, ?> uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T getForObjectGeneric(URI url, Type responseType, Class<T> responseClass) throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    public <T> T postForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                      Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseClass);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T postForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                      Map<String, ?> uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseClass);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T postForObjectGeneric(URI url, Object request, Type responseType, Class<T> responseClass)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseClass);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.POST, requestCallback, responseExtractor);
    }

    public <T> T patchForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                       Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T patchForObjectGeneric(String url, Object request, Class<T> responseType,
                                       Object... uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T patchForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                       Map<String, ?> uriVariables) throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T patchForObjectGeneric(URI url, Object request, Type responseType, Class<T> responseClass)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityCallback(request, responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor);
    }

    protected <T> RequestCallback acceptHeaderRequestCallback(Type responseType) {
        return new AcceptHeaderRequestCallback(responseType);
    }

    /**
     * Request callback implementation that prepares the request's accept headers.
     */
    private class AcceptHeaderRequestCallback implements RequestCallback {

        private final Type responseType;

        private AcceptHeaderRequestCallback(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public void doWithRequest(ClientHttpRequest request) throws IOException {
            try {
                if (MDC.getMDCAdapter() != null && MDC.get(Keys.TRACE_ID) != null) {
                    request.getHeaders().add(Keys.HEADER_TRACE, MDC.get(Keys.TRACE_ID));
                }
            } catch (Exception e) {
                // nothing
            }
            if (this.responseType != null) {
                Class<?> responseClass = null;
                if (this.responseType instanceof Class) {
                    responseClass = (Class<?>) this.responseType;
                }
                List<MediaType> allSupportedMediaTypes = new ArrayList<>();
                for (HttpMessageConverter<?> converter : getMessageConverters()) {
                    if (responseClass != null) {
                        if (converter.canRead(responseClass, null)) {
                            allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
                        }
                    } else if (converter instanceof GenericHttpMessageConverter) {
                        GenericHttpMessageConverter<?> genericConverter = (GenericHttpMessageConverter<?>) converter;
                        if (genericConverter.canRead(this.responseType, null, null)) {
                            allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
                        }
                    }
                }
                if (!allSupportedMediaTypes.isEmpty()) {
                    MediaType.sortBySpecificity(allSupportedMediaTypes);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting request Accept header to " + allSupportedMediaTypes);
                    }
                    request.getHeaders().setAccept(allSupportedMediaTypes);
                }
            }
        }

        private List<MediaType> getSupportedMediaTypes(HttpMessageConverter<?> messageConverter) {
            List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
            List<MediaType> result = new ArrayList<>(supportedMediaTypes.size());
            for (MediaType supportedMediaType : supportedMediaTypes) {
                if (supportedMediaType.getCharset() != null) {
                    supportedMediaType =
                            new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
                }
                result.add(supportedMediaType);
            }
            return result;
        }
    }

}

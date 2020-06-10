package com.alpha.coding.common.http.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
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

    public <T> T getForObjectGeneric(String url, Type responseType, Class<T> responseClass,
                                     Map<String, String[]> headerMap, Object... uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseClass, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T getForObjectGeneric(String url, Type responseType, Class<T> responseClass,
                                     Map<String, String[]> headerMap, Map<String, ?> uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T getForObjectGeneric(URI url, Type responseType, Class<T> responseClass,
                                     Map<String, String[]> headerMap) throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.GET, requestCallback, responseExtractor);
    }

    public <T> T postForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                      Map<String, String[]> headerMap, Object... uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseClass, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T postForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                      Map<String, String[]> headerMap, Map<String, ?> uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseClass, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T postForObjectGeneric(URI url, Object request, Type responseType, Class<T> responseClass,
                                      Map<String, String[]> headerMap)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseClass, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.POST, requestCallback, responseExtractor);
    }

    public <T> T patchForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                       Map<String, String[]> headerMap, Object... uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T patchForObjectGeneric(String url, Object request, Class<T> responseType,
                                       Map<String, String[]> headerMap, Object... uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T patchForObjectGeneric(String url, Object request, Type responseType, Class<T> responseClass,
                                       Map<String, String[]> headerMap, Map<String, ?> uriVariables)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, uriVariables);
    }

    public <T> T patchForObjectGeneric(URI url, Object request, Type responseType, Class<T> responseClass,
                                       Map<String, String[]> headerMap)
            throws RestClientException {
        RequestCallback requestCallback = httpEntityRequestCallback(request, responseType, headerMap);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<>(responseType, getMessageConverters(), logger);
        responseExtractor.setResponseType(responseType);
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor);
    }

    protected <T> RequestCallback acceptHeaderRequestCallback(Type responseType, Map<String, String[]> headerMap) {
        return new MyAcceptHeaderRequestCallback(responseType, headerMap);
    }

    protected <T> RequestCallback httpEntityRequestCallback(Type responseType, Map<String, String[]> headerMap) {
        return new MyHttpEntityRequestCallback(responseType, headerMap);
    }

    protected <T> RequestCallback httpEntityRequestCallback(Object requestBody, Type responseType,
                                                            Map<String, String[]> headerMap) {
        return new MyHttpEntityRequestCallback(requestBody, responseType, headerMap);
    }

    /**
     * Request callback implementation that prepares the request's accept headers.
     */
    private class MyAcceptHeaderRequestCallback implements RequestCallback {

        private final Type responseType;
        private Map<String, String[]> headerMap;

        private MyAcceptHeaderRequestCallback(Type responseType) {
            this.responseType = responseType;
        }

        private MyAcceptHeaderRequestCallback(Type responseType, Map<String, String[]> headerMap) {
            this.responseType = responseType;
            this.headerMap = headerMap;
        }

        @Override
        public void doWithRequest(ClientHttpRequest request) throws IOException {
            final HttpHeaders headers = request.getHeaders();
            try {
                if (MDC.getMDCAdapter() != null && MDC.get(Keys.TRACE_ID) != null) {
                    headers.add(Keys.HEADER_TRACE, MDC.get(Keys.TRACE_ID));
                }
            } catch (Exception e) {
                // nothing
            }
            if (this.headerMap != null) {
                this.headerMap.entrySet().stream()
                        .filter(p -> p.getValue() != null)
                        .forEach(p -> Arrays.stream(p.getValue()).forEach(v -> headers.add(p.getKey(), v)));
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
                    headers.setAccept(allSupportedMediaTypes);
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

    private class MyHttpEntityRequestCallback extends MyAcceptHeaderRequestCallback {
        private final HttpEntity<?> requestEntity;

        private MyHttpEntityRequestCallback(Object requestBody) {
            this(requestBody, (Type) null);
        }

        private MyHttpEntityRequestCallback(Object requestBody, Map<String, String[]> headerMap) {
            this(requestBody, (Type) null, headerMap);
        }

        private MyHttpEntityRequestCallback(Object requestBody, Type responseType) {
            this(requestBody, responseType, (Map<String, String[]>) null);
        }

        private MyHttpEntityRequestCallback(Object requestBody, Type responseType, Map<String, String[]> headerMap) {
            super(responseType, headerMap);
            if (requestBody instanceof HttpEntity) {
                this.requestEntity = (HttpEntity) requestBody;
            } else if (requestBody != null) {
                this.requestEntity = new HttpEntity(requestBody);
            } else {
                this.requestEntity = HttpEntity.EMPTY;
            }
        }

        public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
            super.doWithRequest(httpRequest);
            if (!this.requestEntity.hasBody()) {
                HttpHeaders httpHeaders = httpRequest.getHeaders();
                HttpHeaders requestHeaders = this.requestEntity.getHeaders();
                if (!requestHeaders.isEmpty()) {
                    Iterator var15 = requestHeaders.entrySet().iterator();

                    while (var15.hasNext()) {
                        Map.Entry<String, List<String>> entryx = (Map.Entry) var15.next();
                        httpHeaders.put((String) entryx.getKey(), new LinkedList((Collection) entryx.getValue()));
                    }
                }

                if (httpHeaders.getContentLength() < 0L) {
                    httpHeaders.setContentLength(0L);
                }

            } else {
                Object requestBody = this.requestEntity.getBody();
                Class<?> requestBodyClass = requestBody.getClass();
                Type requestBodyType =
                        this.requestEntity instanceof RequestEntity ? ((RequestEntity) this.requestEntity).getType()
                                : requestBodyClass;
                HttpHeaders httpHeadersx = httpRequest.getHeaders();
                HttpHeaders requestHeadersx = this.requestEntity.getHeaders();
                MediaType requestContentType = requestHeadersx.getContentType();
                Iterator var8 = MyRestTemplate.this.getMessageConverters().iterator();

                while (var8.hasNext()) {
                    HttpMessageConverter messageConverter = (HttpMessageConverter) var8.next();
                    if (messageConverter instanceof GenericHttpMessageConverter) {
                        GenericHttpMessageConverter<Object> genericMessageConverter =
                                (GenericHttpMessageConverter) messageConverter;
                        if (genericMessageConverter
                                .canWrite((Type) requestBodyType, requestBodyClass, requestContentType)) {
                            if (!requestHeadersx.isEmpty()) {
                                Iterator var11 = requestHeadersx.entrySet().iterator();

                                while (var11.hasNext()) {
                                    Map.Entry<String, List<String>> entry = (Map.Entry) var11.next();
                                    httpHeadersx.put((String) entry.getKey(),
                                            new LinkedList((Collection) entry.getValue()));
                                }
                            }

                            if (MyRestTemplate.this.logger.isDebugEnabled()) {
                                if (requestContentType != null) {
                                    MyRestTemplate.this.logger
                                            .debug("Writing [" + requestBody + "] as \"" + requestContentType
                                                    + "\" using [" + messageConverter + "]");
                                } else {
                                    MyRestTemplate.this.logger
                                            .debug("Writing [" + requestBody + "] using [" + messageConverter + "]");
                                }
                            }

                            genericMessageConverter
                                    .write(requestBody, (Type) requestBodyType, requestContentType, httpRequest);
                            return;
                        }
                    } else if (messageConverter.canWrite(requestBodyClass, requestContentType)) {
                        if (!requestHeadersx.isEmpty()) {
                            Iterator var18 = requestHeadersx.entrySet().iterator();

                            while (var18.hasNext()) {
                                Map.Entry<String, List<String>> entryxx = (Map.Entry) var18.next();
                                httpHeadersx.put((String) entryxx.getKey(),
                                        new LinkedList((Collection) entryxx.getValue()));
                            }
                        }

                        if (MyRestTemplate.this.logger.isDebugEnabled()) {
                            if (requestContentType != null) {
                                MyRestTemplate.this.logger
                                        .debug("Writing [" + requestBody + "] as \"" + requestContentType + "\" using ["
                                                + messageConverter + "]");
                            } else {
                                MyRestTemplate.this.logger
                                        .debug("Writing [" + requestBody + "] using [" + messageConverter + "]");
                            }
                        }

                        messageConverter.write(requestBody, requestContentType, httpRequest);
                        return;
                    }
                }

                String message = "Could not write request: no suitable HttpMessageConverter found for request type ["
                        + requestBodyClass.getName() + "]";
                if (requestContentType != null) {
                    message = message + " and content type [" + requestContentType + "]";
                }

                throw new RestClientException(message);
            }
        }
    }

}

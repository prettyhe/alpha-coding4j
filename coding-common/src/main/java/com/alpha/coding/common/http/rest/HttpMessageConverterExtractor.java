/*
 * Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alpha.coding.common.http.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.Data;

/**
 * Response extractor that uses the given {@linkplain HttpMessageConverter entity
 * converters} to convert the response into a type {@code T}.
 *
 * @author Arjen Poutsma
 * @see RestTemplate
 * @since 3.0
 */
@Data
public class HttpMessageConverterExtractor<T> implements ResponseExtractor<T> {

	private Type responseType;

	private Class<T> responseClass;

	private final List<HttpMessageConverter<?>> messageConverters;

	private final Log logger;

	/**
	 * Creates a new instance of the {@code HttpMessageConverterExtractor} with the given
	 * response type and message converters. The given converters must support the response
	 * type.
	 */
	public HttpMessageConverterExtractor(Class<T> responseType, List<HttpMessageConverter<?>> messageConverters) {
		this((Type) responseType, messageConverters);
	}

	/**
	 * Creates a new instance of the {@code HttpMessageConverterExtractor} with the given
	 * response type and message converters. The given converters must support the response
	 * type.
	 */
	public HttpMessageConverterExtractor(Type responseType, List<HttpMessageConverter<?>> messageConverters) {
		this(responseType, messageConverters, LogFactory.getLog(
                HttpMessageConverterExtractor.class));
	}

	@SuppressWarnings("unchecked")
    HttpMessageConverterExtractor(Type responseType, List<HttpMessageConverter<?>> messageConverters, Log logger) {
		Assert.notNull(responseType, "'responseType' must not be null");
		Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
		this.responseType = responseType;
		this.responseClass = (responseType instanceof Class) ? (Class<T>) responseType : null;
		this.messageConverters = messageConverters;
		this.logger = logger;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T extractData(ClientHttpResponse response) throws IOException {
		if (!hasMessageBody(response)) {
			return null;
		}
		MediaType contentType = getContentType(response);

		for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
			if (messageConverter instanceof GenericHttpMessageConverter) {
				GenericHttpMessageConverter<?> genericMessageConverter = (GenericHttpMessageConverter<?>) messageConverter;
				if (genericMessageConverter.canRead(this.responseType, null, contentType)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Reading [" + this.responseType + "] as \"" +
								contentType + "\" using [" + messageConverter + "]");
					}
					return (T) genericMessageConverter.read(this.responseType, null, response);
				}
			}
			if (this.responseClass != null) {
				if (messageConverter.canRead(this.responseClass, contentType)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Reading [" + this.responseClass.getName() + "] as \"" +
								contentType + "\" using [" + messageConverter + "]");
					}
					return (T) messageConverter.read((Class) this.responseClass, response);
				}
			}
		}
		throw new RestClientException(
				"Could not extract response: no suitable HttpMessageConverter found for response type [" +
						this.responseType + "] and content type [" + contentType + "]");
	}

	private MediaType getContentType(ClientHttpResponse response) {
		MediaType contentType = response.getHeaders().getContentType();
		if (contentType == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("No Content-Type header found, defaulting to application/octet-stream");
			}
			contentType = MediaType.APPLICATION_OCTET_STREAM;
		}
		return contentType;
	}

	/**
	 * Indicates whether the given response has a message body.
	 * <p>Default implementation returns {@code false} for:
	 * <ul>
	 *     <li>a response status of {@code 204} or {@code 304}</li>
	 *     <li>a {@code Content-Length} of {@code 0}</li>
	 *     <li>no indication of content (no {@code Content-Length} nor {@code Transfer-encoding: chunked}) and
	 *     a ({@code Connection: closed}) header. See rfc7230 section 3.4</li>
	 * </ul>
	 *
	 * @param response the response to check for a message body
	 * @return {@code true} if the response has a body, {@code false} otherwise
	 * @throws IOException in case of I/O errors
	 */
	protected boolean hasMessageBody(ClientHttpResponse response) throws IOException {
		HttpStatus responseStatus = response.getStatusCode();
		if (responseStatus == HttpStatus.NO_CONTENT ||
				responseStatus == HttpStatus.NOT_MODIFIED) {
			return false;
		}
		HttpHeaders headers = response.getHeaders();
		long contentLength = headers.getContentLength();
		if(contentLength == 0) {
			return false;
		}
		boolean chunked = headers.containsKey(HttpHeaders.TRANSFER_ENCODING)
				&& headers.get(HttpHeaders.TRANSFER_ENCODING).contains("chunked");
		boolean closed = headers.containsKey(HttpHeaders.CONNECTION)
				&& headers.getConnection().contains("close");
		if(!chunked && contentLength == -1 && closed) {
			return false;
		}
		return true;
	}

}

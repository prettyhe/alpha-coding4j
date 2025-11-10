package com.alpha.coding.common.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * HttpRequestTypeEnum
 *
 * @version 1.0
 * @date 2025年10月22日
 */
public enum HttpRequestTypeEnum {

    POST {
        @Override
        public HttpRequestBase makeHttpRequest(String url) {
            return new HttpPost(url);
        }

        @Override
        public Class<? extends HttpRequestBase> requestType() {
            return HttpPost.class;
        }
    },
    GET {
        @Override
        public HttpRequestBase makeHttpRequest(String url) {
            return new HttpGet(url);
        }

        @Override
        public Class<? extends HttpRequestBase> requestType() {
            return HttpGet.class;
        }
    },
    DELETE {
        @Override
        public HttpRequestBase makeHttpRequest(String url) {
            return new HttpDelete(url);
        }

        @Override
        public Class<? extends HttpRequestBase> requestType() {
            return HttpDelete.class;
        }
    },
    PUT {
        @Override
        public HttpRequestBase makeHttpRequest(String url) {
            return new HttpPut(url);
        }

        @Override
        public Class<? extends HttpRequestBase> requestType() {
            return HttpPut.class;
        }
    },
    ;

    /**
     * 构造请求
     */
    public abstract HttpRequestBase makeHttpRequest(String url);

    /**
     * 请求的类型
     */
    public abstract Class<? extends HttpRequestBase> requestType();

}

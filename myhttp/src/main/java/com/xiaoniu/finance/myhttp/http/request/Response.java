package com.xiaoniu.finance.myhttp.http.request;

import com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode;
import java.util.Map;

/**
 * 消息响应对象
 * 返回码正常的http的返回码，
 * 如果时异常的情况，请参考@see{com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode}
 *
 * @author zhonghu
 */

public class Response {

    /**
     * http响应码
     */
    private final int httpRetCode;
    /**
     * http响应码对应的消息
     */
    private final String httpRetMsg;
    /**
     * http响应头数据
     * TODO 后期加上
     */
    private final Map<String, String> httpRetHeader;
    /**
     * http响应体数据
     */
    private final String httpRetData;

    private Response(Builder builder) {
        this.httpRetCode = builder.httpRetCode;
        this.httpRetMsg = builder.httpRetMsg;
        this.httpRetHeader = builder.httpRetHeader;
        this.httpRetData = builder.httpRetData;
    }

    /**
     * 创建一个空对象
     */
    public static Response emptyResponse() {
        return new Builder().build();
    }

    /**
     * 创建一个对象,只有code
     */
    public static Response onlyCodeResponse(int httpRetCode) {
        return new Builder().code(httpRetCode).build();
    }

    /**
     * 网络访问成功
     */
    public boolean isSuccess() {
        return httpRetCode == HttpRespCode.STATE_SUC;
    }

    /**
     * 返回响应码
     */
    public int getHttpRetCode() {
        return httpRetCode;
    }

    /**
     * 返回响应码的描述
     */
    public String getHttpRetMsg() {
        return httpRetMsg;
    }

    /**
     * 返回响应头部
     */
    public Map<String, String> getHttpRetHeader() {
        return httpRetHeader;
    }

    /**
     * 返回字符串的响应体
     */
    public String getHttpRetData() {
        return httpRetData;
    }

    public static class Builder {

        private int httpRetCode;
        private String httpRetMsg;
        private Map<String, String> httpRetHeader;
        private String httpRetData;

        public Builder() {
            httpRetCode = HttpRespCode.STATE_EXCEPTION;
        }

        public Builder(Response response) {
            this.httpRetCode = response.httpRetCode;
            this.httpRetMsg = response.httpRetMsg;
            this.httpRetHeader = response.httpRetHeader;
            this.httpRetData = response.httpRetData;
        }

        public Builder code(int code) {
            this.httpRetCode = code;
            return this;
        }

        public Builder msg(String msg) {
            this.httpRetMsg = msg;
            return this;
        }

        public Builder data(String data) {
            this.httpRetData = data;
            return this;
        }

        public Builder header(Map<String, String> header) {
            this.httpRetHeader = header;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}



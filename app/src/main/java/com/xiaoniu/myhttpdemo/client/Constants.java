package com.xiaoniu.myhttpdemo.client;

/**
 * Created by zhonghu on 2019/1/30.
 */

public final class Constants {

    /**
     * 测试及准生产环境的Https证书
     **/
    private static final String CA_DEV_TEST_215 = "";
    /**
     * 生产环境的Https证书
     **/
    private static final String CA_DEV_PRODUCTION = "";
    private static final String CA_DEV_PRODUCTION_NEW = "";
    /**
     * 生产环境信任的证书
     */
    public static final String[] TRUST_CA_STR = new String[]{
            CA_DEV_PRODUCTION,
            CA_DEV_PRODUCTION_NEW
    };
    /**
     * 测试环境信任的证书
     */
    public static final String[] TRUST_CA_TEST_STR = new String[]{
            CA_DEV_TEST_215,
            CA_DEV_PRODUCTION,
            CA_DEV_PRODUCTION_NEW
    };

    public static final String BASE_URL = "https://www.xxxx.com:443";

    public static class ResultCode {

        /**
         * 业务正常完成
         */
        public static final String SUCCESS = "1000000";
        /**
         * 自登录Token失效
         */
        public static final String AUTO_TOKEN_ERRO = "1000011";
        /**
         * 未登录
         */
        public static final String RESPONSE_CODE_LOGIN_TIMEOUT = "1000022";
    }


    public final static class HttpHeader {

        public static final String OS_TYPE = "ANDROID";
        public static final String USER_AGENT = "App-Android-1.0";
        public static final String HEADER_COOKIE = "Cookie";
        public static final String HEADER_SESSION = "Session";
        public static final String ANDROID_USER_AGENT = "App-Android-";
        /**
         * 应用类型
         */
        public static final String HTTP_HEADER_APP_TYPE = "MYHTTP";
    }

}

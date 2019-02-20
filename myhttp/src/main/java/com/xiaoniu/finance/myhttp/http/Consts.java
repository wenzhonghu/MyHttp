package com.xiaoniu.finance.myhttp.http;

/**
 * Created by zhonghu on 2019/1/21.
 */

public class Consts {
    private Consts(){}

    /** http-method */
    public static final int HTTP_TYPE_GET = 0;
    public static final int HTTP_TYPE_POST = 1;
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";


    /**request数据类型*/
    public static final int DATE_NULL = -1;
    public static final int DATE_STRING = 0;
    public static final int DATA_MAP = 1;
    public static final int DATA_BYTES = 2;
    public static final int DATA_OBJET = 3;

    /**执行优先级*/
    public static final int PRIOPITY_MAX = 1;
    public static final int PRIOPITY_HIGHER = 3;
    public static final int PRIOPITY_NORMAL = 5;
    public static final int PRIOPITY_LOWER = 7;
    public static final int PRIOPITY_MIN = 10;

    /**http*/
    public static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;
    public static final String CHARSET_UTF = "utf-8";
    public static final String PARAM_SEQ = "?";
    public static final String PARAM_SPLIT = "&";
    public static final String PARAM_EQ = "=";
    public static final String PARAM_FD = "/";
    public static final String PARAM_D = ",";

    public static final String GZIP_ENCODING = "gzip";
    public static final String CONTENT_TYPE = "application/json; charset=utf-8";



    public final static class HttpHeader {
        public static final String KEY_COOKIE = "Cookie";
        public static final String KEY_SESSION = "Session";
        public static final String KEY_USER_AGENT = "User-Agent";
        public static final String OS_TYPE = "ANDROID";
        public static final String USER_AGENT = "App-Android_";
    }

    /**
     * 缓存常量
     */
    public static class CacheConst{

        public static final String COLUMN_KEY = "c_key";
        public static final String COLUMN_VALUE = "c_value";
        public static final String COLUMN_TYPE = "c_type";
        public static final String COLUMN_DATE = "c_date";
        public static final String COLUMN_ID = "_id";
        public static final String TABLE_NAME = "t_cache";
    }


    /**
     * 返回码
     */
    public static class HttpRespCode {
        /**
         * State包括以下错误码，也包括HTTP的返回错误码
         */

        /**
         * 调用异常
         */
        public static final int STATE_EXCEPTION = 1;

        /**
         * 请求超时
         */
        public static final int STATE_TIME_OUT = 2;

        /**
         * 请求无网络
         */
        public static final int STATE_NO_NETWORK = 3;

        /**
         * SSL握手失败
         * 请求连接被监控，不安全
         */
        public static final int STATE_SSL_HANDSHARE = 4;

        /**
         * 请求成功
         */
        public static final int STATE_SUC = 200;
    }


    /**
     * 常量命令码
     */
    public static class CmdCode{
        public static final int PUSHMSG_CMDID = 10001;

        public static final int FLOW_CMDID = 10002;

        public static final int NET_CMDID = 10003;

        public static final int TASK_CMDID = 10004;

        public static final int HTTP_CMDID = 10005;
    }

}

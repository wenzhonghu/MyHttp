package com.xiaoniu.finance.myhttp.statistic.profile;

import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.util.StringUtil;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * http业务统计信息类
 */
public class BizProfile implements Cloneable, Comparable<BizProfile>, Serializable {

    public BizProfile() {
    }

    /**
     * 启动时间
     */
    public long startTaskTime;

    /**
     * http请求时间
     */
    public long requestTime;


    /**
     * http地址
     */
    public String url;

    /**
     * 请求类型
     */
    public int httpType;

    /**
     * 优先级
     */
    public int priority;

    /**
     * 任务组的ID
     */
    public String taskGroupID;

    /**
     * 是否取消
     */
    public boolean runBackground;

    /**
     * 是否启动缓存
     */
    public int isCacheData;

    /**
     * 缓存的key
     */
    public String cacheType;

    /**
     * 是否启动禁用过滤器功能
     */
    public boolean isForbidFilter;

    /**
     * 响应码
     */
    public int responseCode;

    /**
     * 响应码对应的描述
     */
    public String responseMsg;

    /**
     * 请求消息体数据
     */
    public String requestData;

    /**
     * 请求头部
     */
    public String requestHeader;

    /**
     * 响应消息体数据
     */
    public String responseData;

    public String responseHeader;


    public static byte[] parseResult(BizProfile profile) {
        String data = profile.toString();
        return data.getBytes(Charset.forName("UTF-8"));
    }


    public static BizProfile parseToResult(byte[] data) {
        String str = new String(data, Charset.forName("UTF-8"));
        if (StringUtil.isTextEmpty(str)) {
            return null;
        }
        String[] sps = str.split(Consts.PARAM_D);
        if (sps == null || sps.length <= 0) {
            return null;
        }

        BizProfile profile = new BizProfile();
        for (String sp : sps) {
            if (StringUtil.isNotTextEmpty(sp)) {
                String[] ss = sp.split(Consts.PARAM_EQ);
                if (ss != null && ss.length == 2) {
                    if (StringUtil.isValueEmpty(ss[1])) {
                        continue;
                    }
                    if ("startTaskTime".equals(ss[0])) {
                        profile.startTaskTime = Long.parseLong(ss[1]);
                        continue;
                    }
                    if ("requestTime".equals(ss[0])) {
                        profile.requestTime = Long.parseLong(ss[1]);
                        continue;
                    }
                    if ("url".equals(ss[0])) {
                        profile.url = ss[1];
                        continue;
                    }
                    if ("httpType".equals(ss[0])) {
                        profile.httpType = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("priority".equals(ss[0])) {
                        profile.priority = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("taskGroupID".equals(ss[0])) {
                        profile.taskGroupID = ss[1];
                        continue;
                    }
                    if ("runBackground".equals(ss[0])) {
                        profile.runBackground = Boolean.parseBoolean(ss[1]);
                        continue;
                    }
                    if ("isCacheData".equals(ss[0])) {
                        profile.isCacheData = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("isForbidFilter".equals(ss[0])) {
                        profile.isForbidFilter = Boolean.parseBoolean(ss[1]);
                        continue;
                    }
                    if ("cacheType".equals(ss[0])) {
                        profile.cacheType = ss[1];
                        continue;
                    }
                    if ("responseCode".equals(ss[0])) {
                        profile.responseCode = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("responseMsg".equals(ss[0])) {
                        profile.responseMsg = ss[1];
                        continue;
                    }
                    if ("requestData".equals(ss[0])) {
                        profile.requestData = ss[1];
                        continue;
                    }
                    if ("requestHeader".equals(ss[0])) {
                        profile.requestHeader = ss[1];
                        continue;
                    }
                    if ("responseData".equals(ss[0])) {
                        profile.responseData = ss[1];
                        continue;
                    }
                    if ("responseHeader".equals(ss[0])) {
                        profile.responseHeader = ss[1];
                        continue;
                    }
                }
            }
        }

        return profile;
    }

    @Override
    public int compareTo(BizProfile o) {
        return this.url.compareTo(o.url);
    }


    @Override
    public String toString() {
        return "startTaskTime=" + startTaskTime +
                ",requestTime=" + requestTime +
                ",url=" + url +
                ",httpType=" + httpType +
                ",priority=" + priority +
                ",taskGroupID=" + taskGroupID +
                ",runBackground=" + runBackground +
                ",isCacheData=" + isCacheData +
                ",cacheType=" + cacheType +
                ",isForbidFilter=" + isForbidFilter +
                ",responseCode=" + responseCode +
                ",responseMsg=" + responseMsg +
                ",requestData=" + requestData +
                ",requestHeader=" + requestHeader +
                ",responseData=" + responseData +
                ",responseHeader=" + responseHeader;
    }
}

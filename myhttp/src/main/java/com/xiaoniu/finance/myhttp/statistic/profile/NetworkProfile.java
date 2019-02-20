package com.xiaoniu.finance.myhttp.statistic.profile;

import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.util.StringUtil;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * 网络统计信息类
 */

public class NetworkProfile implements Cloneable, Comparable<NetworkProfile>, Serializable {

    public NetworkProfile() {
    }

    /**
     * 网络切换时间
     */
    public long changeTime;

    /**
     * 接入点名
     */
    public String lastApnName;

    /**
     * 网络类型名称
     */
    public String lastNetTypeName;

    /**
     * 网络类型是否有效
     */
    public boolean lastNetTypeAvailable;


    /**
     * 网络类型是否有效
     */
    public String lastProvideServiceName;


    /**
     * 信号级别
     */
    public int lastSignalLevel;

    /**
     * 接入点名
     */
    public String newApnName;

    /**
     * 网络类型名称
     */
    public String newNetTypeName;

    /**
     * 网络类型是否有效
     */
    public boolean newNetTypeAvailable;


    /**
     * 网络类型是否有效
     */
    public String newProvideServiceName;


    /**
     * 信号级别
     */
    public int newSignalLevel;


    public static byte[] parseResult(NetworkProfile profile) {
        String data = profile.toString();
        return data.getBytes(Charset.forName("UTF-8"));
    }


    public static NetworkProfile parseToResult(byte[] data) {
        String str = new String(data, Charset.forName("UTF-8"));
        if (StringUtil.isTextEmpty(str)) {
            return null;
        }
        String[] sps = str.split(Consts.PARAM_D);
        if (sps == null || sps.length <= 0) {
            return null;
        }

        NetworkProfile profile = new NetworkProfile();
        for (String sp : sps) {
            if (StringUtil.isNotTextEmpty(sp)) {
                String[] ss = sp.split(Consts.PARAM_EQ);
                if (ss != null && ss.length == 2) {
                    if (StringUtil.isValueEmpty(ss[1])) {
                        continue;
                    }
                    if ("changeTime".equals(ss[0])) {
                        profile.changeTime = Long.parseLong(ss[1]);
                        continue;
                    }
                    if ("lastApnName".equals(ss[0])) {
                        profile.lastApnName = ss[1];
                        continue;
                    }
                    if ("lastNetTypeName".equals(ss[0])) {
                        profile.lastNetTypeName = ss[1];
                        continue;
                    }
                    if ("lastNetTypeAvailable".equals(ss[0])) {
                        profile.lastNetTypeAvailable = Boolean.parseBoolean(ss[1]);
                        continue;
                    }
                    if ("lastProvideServiceName".equals(ss[0])) {
                        profile.lastProvideServiceName = ss[1];
                        continue;
                    }
                    if ("lastSignalLevel".equals(ss[0])) {
                        profile.lastSignalLevel = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("newApnName".equals(ss[0])) {
                        profile.newApnName = ss[1];
                        continue;
                    }
                    if ("newNetTypeName".equals(ss[0])) {
                        profile.newNetTypeName = ss[1];
                        continue;
                    }
                    if ("newNetTypeAvailable".equals(ss[0])) {
                        profile.newNetTypeAvailable = Boolean.parseBoolean(ss[1]);
                        continue;
                    }
                    if ("newProvideServiceName".equals(ss[0])) {
                        profile.newProvideServiceName = ss[1];
                        continue;
                    }
                    if ("newSignalLevel".equals(ss[0])) {
                        profile.newSignalLevel = Integer.parseInt(ss[1]);
                        continue;
                    }
                }
            }
        }

        return profile;
    }

    @Override
    public int compareTo(NetworkProfile o) {
        return Long.valueOf(this.changeTime).compareTo(o.changeTime);
    }

    @Override
    public String toString() {
        return "changeTime=" + changeTime +
                ",lastApnName=" + lastApnName +
                ",lastNetTypeName=" + lastNetTypeName +
                ",lastNetTypeAvailable=" + lastNetTypeAvailable +
                ",lastProvideServiceName=" + lastProvideServiceName +
                ",lastSignalLevel=" + lastSignalLevel +
                ",newApnName=" + newApnName +
                ",newNetTypeName=" + newNetTypeName +
                ",newNetTypeAvailable=" + newNetTypeAvailable +
                ",newProvideServiceName=" + newProvideServiceName +
                ",newSignalLevel=" + newSignalLevel;
    }
}

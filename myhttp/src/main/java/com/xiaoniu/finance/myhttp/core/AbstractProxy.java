package com.xiaoniu.finance.myhttp.core;


import com.xiaoniu.finance.myhttp.core.Device.Network.Proxy;

/**
 * Created by zhonghu on 2019/1/23.
 */

public abstract class AbstractProxy {

    public static AbstractProxy Default = new Proxy();

    public static final char PROTOCOL_PORT_SPLITTER = ':';

    public abstract String getHost();

    public abstract int getPort();

    @Override
    public String toString() {
        return getHost() + PROTOCOL_PORT_SPLITTER + getPort();
    }
}

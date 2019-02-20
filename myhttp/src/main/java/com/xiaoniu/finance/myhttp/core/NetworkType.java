
package com.xiaoniu.finance.myhttp.core;

/**
 * 网络类型枚举，同时包含了常用网络类型 <br>
 * <br>
 * <i>使用枚举的意图是方便在程序的代码中使用switch来处理不同网络类型的策略逻辑</i><br>
 * <br>
 *
 * @author chenrunan
 */
public enum NetworkType {
    /**
     * 无网络，网络不可用
     */
    NONE("None", false),

    /**
     * Wifi网络
     */
    WIFI("Wifi", true),

    /**
     * 2G网络 / 低速移动网络
     */
    MOBILE_2G("2G", true),

    /**
     * 3G网络 / 高速移动网络
     */
    MOBILE_3G("3G", true),

    /**
     * 4G网络 / 超高速移动网络
     */
    MOBILE_4G("4G", true),

    /**
     * 有线网路
     */
    ETHERNET("Ethernet", true),

    /**
     * 其他网络，含蓝牙、WIFI P2P等
     */
    OTHERS("Other", true);

    private String name;
    private boolean available;

    NetworkType(String friendlyName, boolean available) {
        setName(friendlyName);
        setAvailable(available);
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

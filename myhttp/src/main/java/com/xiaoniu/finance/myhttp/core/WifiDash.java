
package com.xiaoniu.finance.myhttp.core;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xiaoniu.finance.myhttp.Global;

public class WifiDash {

    public static final String ZERO_BSSID = "00:00:00:00:00:00";
    private static final String FF_BSSID = "FF:FF:FF:FF:FF:FF";

    volatile static String currBSSID = null;

    volatile static String currMAC = null;

    /**
     * 获得当前接入点的BSSID<br>
     * <br>
     * <i>BSSID可以作为WIFI接入点的唯一标识</i>
     *
     * @return 形如MAC地址的字符串，{@code XX:XX:XX:XX:XX:XX}
     * @see WifiInfo
     */
    public static String getBSSID() {
        if (currBSSID == null) {
            synchronized (WifiDash.class) {
                if (currBSSID == null) {
                    updateBSSID();
                }
            }
        }

        if (StrUtils.NOT_AVALIBLE.equals(currBSSID) || ZERO_BSSID.equals(currBSSID)
                || FF_BSSID.equalsIgnoreCase(currBSSID)) {
            return null;
        } else {
            return currBSSID;
        }
    }

    public static String updateBSSID() {
        synchronized (WifiDash.class) {
            Object wifiInfo = queryWifiInfo(StrUtils.NOT_AVALIBLE);

            String BSSID = null;

            if (wifiInfo != StrUtils.NOT_AVALIBLE) {
                BSSID = ((WifiInfo) wifiInfo).getBSSID();

                if (BSSID == null) {
                    BSSID = StrUtils.NOT_AVALIBLE;
                }
            }

            currBSSID = BSSID;

            return BSSID;
        }
    }

    public static String getMacAddress() {
        if (currMAC == null) {
            synchronized (WifiDash.class) {
                if (currMAC == null) {
                    updateWIFIMacAddress();
                }
            }
        }

        if (StrUtils.NOT_AVALIBLE.equals(currMAC) || ZERO_BSSID.equals(currMAC)
                || FF_BSSID.equalsIgnoreCase(currMAC)) {
            return null;
        } else {
            return currMAC;
        }
    }

    public static String updateWIFIMacAddress() {
        synchronized (WifiDash.class) {
            Object wifiInfo = queryWifiInfo(StrUtils.NOT_AVALIBLE);

            String MAC = null;

            if (wifiInfo != StrUtils.NOT_AVALIBLE) {
                MAC = ((WifiInfo) wifiInfo).getMacAddress();

                if (MAC == null) {
                    MAC = StrUtils.NOT_AVALIBLE;
                }
            }

            currMAC = MAC;

            return currMAC;
        }
    }

    public static int getSignalLevel() {
        Object wifiInfo = queryWifiInfo(StrUtils.NOT_AVALIBLE);

        if (wifiInfo == StrUtils.NOT_AVALIBLE) {
            return -1;
        }

        return WifiManager.calculateSignalLevel(((WifiInfo) wifiInfo).getRssi(), 5);
    }

    private static Object queryWifiInfo(Object defValue) {
        WifiManager wifiManager = (WifiManager) Global.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            return defValue;
        }

        WifiInfo wifiInfo = null;

        try {
            wifiInfo = wifiManager.getConnectionInfo();
        } catch (Exception e) {
            wifiInfo = null;
        }

        if (wifiInfo == null) {
            return defValue;
        }

        return wifiInfo;
    }

    public static String getWifiInfo() {
        WifiManager wifiManager = (WifiManager) Global.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            return "[-]";
        }

        WifiInfo wifiInfo = null;

        try {
            wifiInfo = wifiManager.getConnectionInfo();
        } catch (Exception e) {
            wifiInfo = null;
        }

        if (wifiInfo == null) {
            return "[-]";
        }

        String ssid = wifiInfo.getSSID();

        String signal = String.valueOf(WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5));

        String speed = String.valueOf(wifiInfo.getLinkSpeed()) + " " + WifiInfo.LINK_SPEED_UNITS;

        String bssid = wifiInfo.getBSSID();

        StringBuffer buffer = new StringBuffer();

        buffer.append('[').append(signal).append(", ").append(ssid).append(", ").append(speed).append(", ").append(bssid).append(']');

        return buffer.toString();
    }
}

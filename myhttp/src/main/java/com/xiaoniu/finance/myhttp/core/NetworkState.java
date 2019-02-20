
package com.xiaoniu.finance.myhttp.core;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 网络状态
 */
public class NetworkState {

    private static final NetworkState NONE = new NetworkState(false, null, AccessPoint.NONE, NetworkType.NONE);

    /**
     * 从{@link NetworkInfo}构造网络状态信息
     *
     * @param info NetworkInfo对象
     * @return 网络状态
     */
    public static NetworkState fromNetworkInfo(NetworkInfo info) {
        // 得不到信息，返回NONE对象
        if (info == null) {
            return NetworkState.NONE;
        }

        NetworkState state = new NetworkState();

        state.setConnected(info.isConnected());
        state.setApnName(info.getExtraInfo());
        state.setAccessPoint(AccessPoint.forName(state.getApnName()));

        switch (info.getType()) {
            // WIFI网络
            case ConnectivityManager.TYPE_WIFI: {
                state.setType(NetworkType.WIFI);
                break;
            }
            //移动网络
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                // 根据速度判定是否2G网络以及3G以上网络
                state.setType(is4GMobileType(info.getSubtype()) ? NetworkType.MOBILE_4G :
                        is3GMobileType(info.getSubtype()) ? NetworkType.MOBILE_3G : NetworkType.MOBILE_2G);
                break;
            // 其他网络
            default:
                state.setType(NetworkType.OTHERS);
                break;
        }

        // 保存额外的信息
        state.setMoreInfo(info);

        return state;
    }

    private boolean connected = false;
    private String apnName = null;
    private NetworkType type = NetworkType.NONE;
    private AccessPoint accessPoint = AccessPoint.NONE;

    private NetworkInfo moreInfo;

    private NetworkState(boolean conn, String apn, AccessPoint ap, NetworkType tp) {
        setConnected(conn);
        setApnName(apn);
        setAccessPoint(ap);
        setType(tp);
    }

    private NetworkState() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof NetworkState) {
            return (((NetworkState) o).isConnected() == this.isConnected())
                    && (((NetworkState) o).getType().equals(this.getType()))
                    && (((NetworkState) o).getApnName().equals(this.getApnName()));
        } else {
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isAvailable() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getApnName() {
        if (apnName == null) {
            return StrUtils.EMPTY;
        } else {
            return apnName;
        }
    }

    public void setApnName(String apnName) {
        this.apnName = apnName;
    }

    public NetworkType getType() {
        return type;
    }

    public void setType(NetworkType type) {
        this.type = type;
    }

    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
    }

    /**
     * wifi网络
     * @param type
     * @return
     */
    public static boolean isWifiType(int type) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        }

        return false;
    }
    /**
     * 2G网络
     * @param type
     * @return
     */
    public static boolean is2GMobileType(int type) {
        if (type == TelephonyManager.NETWORK_TYPE_EDGE || type == TelephonyManager.NETWORK_TYPE_GPRS || type == TelephonyManager.NETWORK_TYPE_CDMA) {
            return true;
        }

        return false;
    }

    /**
     * 判断子类型是否是快速网络，姑且认为快速网络即3G网络
     *
     * @param type 网络子类型
     * @return 3G网络，返回true; 2G网络或其他未知网络，返回false
     */
    private static boolean is3GMobileType(int type) {
        if (type >= TelephonyManager.NETWORK_TYPE_EVDO_0 && type < TelephonyManager.NETWORK_TYPE_LTE) {
            return true;
        }

        return false;
    }

    /**
     * 判断子类型是否是快速网络，姑且认为快速网络即3G网络
     *
     * @param type 网络子类型
     * @return 3G网络，返回true; 2G网络或其他未知网络，返回false
     */
    private static boolean is4GMobileType(int type) {
        if (type >= TelephonyManager.NETWORK_TYPE_LTE) {
            return true;
        }

        return false;
    }

    public NetworkInfo getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(NetworkInfo moreInfo) {
        this.moreInfo = moreInfo;
    }

    @Override
    public String toString() {
        return "NetworkState [connected=" + connected + ", apnName=" + apnName + ", type=" + type + ", accessPoint="
                + accessPoint + "]";
    }
}

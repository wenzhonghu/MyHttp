
package com.xiaoniu.finance.myhttp.core;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.xiaoniu.finance.myhttp.Global;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 设备信息获取
 */
public class Device extends DeviceDash {

    public final static String TAG = "Device";
    public final static String UUID_RUN_TIME = "uuid_run_time";
    public final static String RESUME_TIME = "resume_time";
    public final static String PAUSE_TIME = "pause_time";

    /**
     * 存储器信息
     */
    public static class Storage extends StorageDash {

    }

    /**
     * 网络信息
     */
    public static class Network extends NetworkDash {

        /**
         * 系统代理信息<br>
         * <br>
         * 要获取系统默认代理，使用{@link Proxy#Default}
         */
        public static class Proxy extends AbstractProxy {

            @Override
            public int getPort() {
                return android.net.Proxy.getDefaultPort();
            }

            @Override
            public String getHost() {
                return android.net.Proxy.getDefaultHost();
            }
        }

        /**
         * WIFI网卡信息
         */
        public static class Wifi extends WifiDash {

        }

        /**
         * 没尝试过：<p/>
         * 1/尝试直连，成功了就认为永久直连<br/>
         * 2/失败了就再试一次代理，成功了就永久代理<br/>
         * 3/失败了下次继续尝试
         */
        public enum ProxyMode {
            /**
             * 没尝试过
             */
            NeverTry,
            /**
             * 直连
             */
            Direct,
            /**
             * 通过代理
             */
            ViaProxy
        }
    }


    /**
     * uuid
     */
    private static String sUUID = null;
    /**
     * 设备id
     */
    private static String deviceId;
    /**
     * mac地址
     */
    private static String mac;
    /**
     * 设备型号
     */
    private static String model;
    /**
     * 操作系统版本
     */
    private static String osVersion;
    /**
     * 屏幕分辨率
     */
    private static String screen;
    /**
     * 国家
     */
    private static String country;
    /**
     * 语言
     */
    private static String language;
    /**
     * 时区
     */
    private static String timezone;
    /**
     * 联网方式
     */
    private static String network;
    /**
     * 设备名称
     */
    private static String deviceName;
    /**
     * 运营商
     */
    private static String operator;
    /**
     * 设备启动时间（ios）
     */
    private static String bootTime;
    /**
     * CUP型号（android）
     */
    private static String cpuAbi;
    /**
     * 硬盘空间大小
     */
    private static String diskSpace;
    /**
     * 厂商
     */
    private static String manufacturer;

    /**
     * imsi
     */
    private static String imsi;


    /**
     * 获取双卡双待IMEI
     */
    private static String getDeviceIdBySlot(Context context, int slotID) {
        if (context == null) {
            return null;
        }
        if (slotID < 0 || slotID > 1) {
            return null;
        }

        String imei = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> mLoadClass = Class.forName("android.telephony.TelephonyManager");

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = mLoadClass.getMethod("getSimOperatorGemini", parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephonyManager, obParameter);

            if (ob_phone != null) {
                imei = ob_phone.toString();
            }
        } catch (Exception e) {

        }
        return imei;
    }

    /**
     * 获取手机的IMSI
     * 支持双卡手机
     */
    public static String getIMSI() {
        if (imsi != null) {
            return imsi;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) Global.getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI1 = telephonyManager.getSimOperator();
            // 处理双卡手机
            if (TextUtils.isEmpty(IMSI1)) {
                IMSI1 = getDeviceIdBySlot(Global.getContext(), 0);
            }
            if (TextUtils.isEmpty(IMSI1)) {
                IMSI1 = getDeviceIdBySlot(Global.getContext(), 1);
            }
            imsi = IMSI1;

            return IMSI1;
        } catch (Exception e) {
            return null;
        }
    }


    public final static String getUUID() {
        SharedPreferences pauseTime = Global.getContext().getSharedPreferences(UUID_RUN_TIME, Context.MODE_PRIVATE);
        long resume = pauseTime.getLong(RESUME_TIME, 0);
        long pause = pauseTime.getLong(PAUSE_TIME, 0);

        if ((pause > 0 && (resume - pause) > 30000) || sUUID == null) {
            sUUID = UUID.randomUUID().toString();
            Context ctx = Global.getContext();
            if (ctx != null) {
                SharedPreferences.Editor editor = pauseTime.edit();
                editor.putLong(RESUME_TIME, 0);
                editor.putLong(PAUSE_TIME, 0);
                editor.commit();
            }

            return sUUID;
        } else {
            return sUUID;
        }
    }


    public static String getDeviceID() {
        if (deviceId != null) {
            return deviceId;
        }

        TelephonyManager mTelephonyMgr = (TelephonyManager) Global.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceId = mTelephonyMgr.getImei();
            } else {
                deviceId = mTelephonyMgr.getDeviceId();
            }
        } catch (Exception e) {
            deviceId = "N/A";
            Log.e(TAG, "get device id fail", e);
        }

        return deviceId;
    }

    public static String getHardwareAddress() {
        if (mac != null) {
            return mac;
        }

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : interfaces) {
                if ("wlan0".equals(iface.getDisplayName())) {
                    List<InetAddress> addresses = Collections.list(iface.getInetAddresses());
                    for (InetAddress address : addresses) {
                        if (address instanceof Inet4Address) {
                            return mac = address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "getHardwareAddress fail", e);
        }

        return null;
    }

    public static String getScreenDisplay() {
        if (screen != null) {
            return screen;
        }
        WindowManager manager = (WindowManager) Global.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);

        return screen = displayMetrics.widthPixels + "*" + displayMetrics.heightPixels;
    }

    public static String getNetwork() {
        NetworkState networkState = Device.Network.getCurrState();
        return networkState.getType().getName();
    }

    public static String getOperator() {
        if (operator != null) {
            return operator;
        } else {
            return operator = Device.Network.getIMSIProvider().getName();
        }
    }

    /**
     * 获取手机CPU信息
     */
    public static String getCpuInfo() {
        if (cpuAbi != null) {
            return cpuAbi;
        }

        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""};
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
            Log.e(TAG, "getCpuInfo fail", e);
        }

        cpuAbi = cpuInfo[0];
        return cpuAbi;
    }

    public static String getModel() {
        if (model != null) {
            return model;
        } else {
            return model = android.os.Build.MODEL;
        }
    }

    public static String getManufacturer() {
        if (manufacturer != null) {
            return manufacturer;
        } else {
            return manufacturer = android.os.Build.MANUFACTURER;
        }
    }

    public static String getOsVersion() {
        if (osVersion != null) {
            return osVersion;
        } else {
            return osVersion = android.os.Build.VERSION.RELEASE;
        }
    }

}

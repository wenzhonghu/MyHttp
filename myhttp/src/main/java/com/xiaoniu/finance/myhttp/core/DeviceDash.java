
package com.xiaoniu.finance.myhttp.core;


import android.os.SystemClock;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.http.Consts.CmdCode;
import com.xiaoniu.finance.myhttp.statistic.profile.NetworkProfile;
import com.xiaoniu.finance.myhttp.statistic.PushMessage;
import com.xiaoniu.finance.myhttp.statistic.StatisticHandler;

public class DeviceDash implements NetworkStateListener {

    private static final DeviceDash instance = new DeviceDash();

    public static DeviceDash getInstance() {
        return instance;
    }

    private String mDeviceInfo = null;

    public DeviceDash() {
        Device.Network.addListener(this);
    }

    private String getStorageInfo() {
        StorageInfo innerInfo = StorageDash.getInnerInfo();
        StorageInfo extInfo = StorageDash.getExternalInfo();

        String resu = String.format("{IN : %s |EXT: %s}", (innerInfo == null) ? "N/A" : innerInfo.toString(),
                (extInfo == null) ? "N/A" : extInfo.toString());

        return resu;
    }

    @Override
    public void onNetworkStateChanged(NetworkState lastState, NetworkState newState) {
        if(Global.enableStatistic()){
            try {
                statistic(lastState, newState);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private void statistic(NetworkState lastState, NetworkState newState){
        NetworkProfile n = new NetworkProfile();
        n.changeTime = SystemClock.currentThreadTimeMillis();
        if(lastState != null){
            n.lastApnName = lastState.getApnName();
            n.lastNetTypeName = lastState.getType().getName();
            n.lastNetTypeAvailable = lastState.getType().isAvailable();
            n.lastProvideServiceName = lastState.getAccessPoint().getName();
            int level = -1;
            if(lastState.getMoreInfo()!=null){
                level = NetworkState.isWifiType(lastState.getMoreInfo().getType())?WifiDash.getSignalLevel():NetworkDash.getCellLevel();
            }
            n.lastSignalLevel = level;
        }

        n.newApnName = newState.getApnName();
        n.newNetTypeName = newState.getType().getName();
        n.newNetTypeAvailable = newState.getType().isAvailable();
        n.newProvideServiceName = newState.getAccessPoint().getName();
        int newLevel = -1;
        if(newState.getMoreInfo()!=null){
            newLevel = NetworkState.isWifiType(newState.getMoreInfo().getType())?WifiDash.getSignalLevel():NetworkDash.getCellLevel();
        }
        n.newSignalLevel = newLevel;

        PushMessage m = new PushMessage(CmdCode.NET_CMDID, NetworkProfile.parseResult(n));
        StatisticHandler.getInstance().handleRecvMessage(m);

    }
}

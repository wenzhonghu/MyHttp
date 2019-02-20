package com.xiaoniu.finance.myhttp.client;

/**
 * 获取客户端的常见参数设置
 * Created by zhonghu on 2019/1/25.
 */

public class ClientVariableManager {


    private final static ClientVariableManager sInstance = new ClientVariableManager();

    private ClientVariableManager() {
    }

    public static ClientVariableManager getInstance() {
        return sInstance;
    }


    private boolean mValidateCA;
    private String[] mAppTrustCAStr;

    private int mConnectionTimeout;
    private int mReadConnectionTimeout;

    /**
     * 连接超时,单位是ms
     */
    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    /**
     * 读超时,单位是ms
     */
    public int getReadConnectionTimeout() {
        return mReadConnectionTimeout;
    }


    /**
     * 获取是否检验CA证书
     */
    public boolean getAppValidateHttpsCA() {
        return mValidateCA;
    }

    /**
     * 获取CA证书字符串
     */
    public String[] getAppTrustCAStr() {
        return mAppTrustCAStr;
    }


    /**
     * 设置client的超时时间，单位为秒
     */
    public synchronized void setTimeout(int connectionTimeout, int readConnectionTimeout) {
        this.mConnectionTimeout = connectionTimeout;
        this.mReadConnectionTimeout = readConnectionTimeout;
    }

    /**
     * 添加证书检验参数
     *
     * @param validateCA true则表示需要，false则表示跳过
     * @param appTrustCAStr 对应上面validateCA添加证书字符串
     */
    public synchronized void resetTrustCA(boolean validateCA, String[] appTrustCAStr) {
        this.mValidateCA = validateCA;
        this.mAppTrustCAStr = appTrustCAStr;
    }


}

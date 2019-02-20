package com.xiaoniu.finance.myhttp.core;

import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * DNS解析器
 */

public interface DnsResolverController {

    DnsResolverController DEFALUT = new DnsResolverController() {
        private String TAG = "DnsResolverController";
        @Override
        public String dnsResolver(String domain) {
            try {
                InetAddress inetAddress = InetAddress.getByName(domain);
                return inetAddress.getHostAddress();

            } catch (UnknownHostException e) {
                Log.e(TAG, "Inet Address Analyze fail exception : ", e);
            } catch (Exception e) {
                Log.e(TAG, "Inet Address Analyze fail exception : ", e);
            } catch (Error e) {
                Log.e(TAG, "Inet Address Analyze fail exception : ", e);
            }
            return null;
        }
    };

    /**
     *
     * @param domain
     * @return
     */
    String dnsResolver(String domain);
}

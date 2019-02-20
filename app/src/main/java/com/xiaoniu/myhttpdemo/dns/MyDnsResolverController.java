package com.xiaoniu.myhttpdemo.dns;

import com.xiaoniu.finance.myhttp.core.DnsResolverController;

/**
 * 如果有明确的ip，可以直接写死
 * Created by zhonghu on 2019/2/16.
 */

public class MyDnsResolverController implements DnsResolverController {

    @Override
    public String dnsResolver(String domain) {
        return "114.23.4.56";
    }
}

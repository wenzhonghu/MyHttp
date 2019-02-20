package com.xiaoniu.myhttpdemo.dns;

import com.xiaoniu.finance.myhttp.core.DnsResolverController;
import com.xiaoniu.myhttpdemo.dns.httpdns.DnspodFree;
import com.xiaoniu.myhttpdemo.dns.httpdns.Domain;
import com.xiaoniu.myhttpdemo.dns.httpdns.Record;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持腾讯的httpdns+等第三方dns解析库
 * Created by zhonghu on 2019/2/16.
 */

public class HttpDnsResolverController implements DnsResolverController {


    private ConcurrentHashMap<String, String> mDomainMap = new ConcurrentHashMap<String, String>();

    @Override
    public String dnsResolver(String domain) {
        if (mDomainMap.contains(domain)) {
            return mDomainMap.get(domain);
        }

        try {
            Record[] rs = new DnspodFree().resolve(new Domain(domain));
            /**
             * 查询最优的ip地址
             * 其中规则，自己延伸扩展，目前简单获取第一个
             */
            for (Record record : rs) {
                if (!record.isExpired()) {
                    mDomainMap.put(domain, record.value);
                    return record.value;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return domain;
    }
}

package com.xiaoniu.finance.myhttp.client.trust;

import javax.net.ssl.SSLException;
import org.apache.http.conn.ssl.AbstractVerifier;

/**
 * Created by zhonghu on 2019/1/25.
 */
class AllowAllHostnameVerifier extends AbstractVerifier {

    /**
     * Allow everything - so never blowup.
     * 目前允许所有通过
     * Checks to see if the supplied hostname matches any of the supplied CNs
     * or "DNS" Subject-Alts.  Most implementations only look at the first CN,
     * and ignore any additional CNs.  Most implementations do look at all of
     * the "DNS" Subject-Alts. The CNs or Subject-Alts may contain wildcards
     * according to RFC 2818.
     *
     * @param cns         CN fields, in order, as extracted from the X.509
     *                    certificate.
     * @param subjectAlts Subject-Alt fields of type 2 ("DNS"), as extracted
     *                    from the X.509 certificate.
     * @param host        The hostname to verify.
     * @throws SSLException If verification failed.
     */
    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        // Allow everything - so never blowup.
    }
}

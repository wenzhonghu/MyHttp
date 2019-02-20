package com.xiaoniu.finance.myhttp.client.trust;

import android.util.Log;
import com.xiaoniu.finance.myhttp.client.ClientVariableManager;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * X509证书检验
 * 自定义信任处理器（TrustManager）来替代系统默认的信任处理器，这样我们才能正常的使用自定义的正说或者非android认可的证书颁发机构颁发的证书。
 */
class VerifierTrustManager implements X509TrustManager {

    private final static String TAG = VerifierTrustManager.class.getSimpleName();

    /**
     * 检验服务端证书
     * @param chain
     * @param authType
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        Log.v(TAG, "checkServerTrusted 开始认证证书");
        if (!ClientVariableManager.getInstance().getAppValidateHttpsCA()) {
            Log.v(TAG, "checkServerTrusted 证书不认证");
            return;
        }
        if (chain == null || chain.length == 0) {
            throw new CertificateException();
        }
        //打印证书信息
        for (X509Certificate certificate : chain) {
            String encoded = new BigInteger(1 /* positive */, certificate.getEncoded()).toString(16);
            Log.v(TAG, "publicKey: " + encoded);
            Log.v(TAG, "checkServerTrusted certificate" + certificate);
        }
        X509Certificate serverCertificate = chain[0];
        for (String caStr : ClientVariableManager.getInstance().getAppTrustCAStr()) {
            String encoded = new BigInteger(1, serverCertificate.getEncoded()).toString(16);
            if (caStr.equalsIgnoreCase(encoded)) {
                Log.v(TAG, "checkServerTrusted 证书认证通过");
                return;
            }
        }
        throw new CertificateException("SSL握手失败，该证书不可信");

        /**
         * 以下认证方式通过文件进行
         * 考虑到IO读取耗时且asset文件更容易被替换重新打包，弃用文件方式改用内存对比方式
         */
//			if (trustCertificates == null) {
////				Log.v(TAG, "TrustManager 加载证书");
//				trustCertificates = new X509Certificate[AppSetting.APP_TRUST_CA_FILE.length];
//				for(int i = 0;i < AppSetting.APP_TRUST_CA_FILE.length;i++) {
//					try {
//						InputStream ins = context.getAssets().open(AppSetting.APP_TRUST_CA_FILE[i]);
//						CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
//						X509Certificate cer = (X509Certificate)cerFactory.generateCertificate(ins);
//						trustCertificates[i] = cer;
//
//						String encoded = new BigInteger(1 /* positive */, cer.getEncoded()).toString(16);
//						Log.v(TAG, "publicKey: " + encoded);
//						Log.v(TAG, "trustCertificates: " + cer);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//
//			X509Certificate serverCertificate = chain[0];
//			for(X509Certificate trustCertificate : trustCertificates) {
//				boolean isOk = serverCertificate.equals(trustCertificate);
//				if (isOk) {
//					Log.v(TAG, "checkServerTrusted 证书认证通过");
//					return;
//				} else {
//					Log.v(TAG, "checkServerTrusted serverCertificate：" + serverCertificate.getSerialNumber()
//							+ " - trustCertificate:" + trustCertificate.getSerialNumber());
//				}
//			}
//			throw new CertificateException("认证失败，该证书不可信");
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        Log.v(TAG, "checkClientTrusted");
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author James
 */
public class CertificateManager {

    private static boolean disable = true;

    private static void doTrustToCertificates() throws Exception {
        if (disable) {
            return;
        }
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    return;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    return;
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                System.out.println("Debug:" + urlHostName + " " + session.getPeerHost());
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    private static boolean hasExpiredCertificate(URL url) throws MalformedURLException, Exception {
        if (disable) {
            return false;
        }
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.connect();
        Certificate[] certs = conn.getServerCertificates();
        for (Certificate cert : certs) {
            if (cert.toString().contains("DNSName: " + url.getHost())) {
                if (cert instanceof X509Certificate) {
                    try {
                        ((X509Certificate) cert).checkValidity();
                        System.out.println("Certificate is active for current date");
                        return false;
                    } catch (CertificateExpiredException cee) {
                        System.out.println("Certificate is expired");
                        return true;
                    }
                }
            }

        }
        System.out.println("Certificate not found for: " + url.getHost());
        return true;
    }

    public static void connectToUrl(URL url) throws MalformedURLException, Exception {
        if (disable) {
            return;
        }
        doTrustToCertificates();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        System.out.println("ResponseCoede =" + conn.getResponseCode());

        URLConnection connection = url.openConnection();
        try {
            connection.connect();
            System.out.println("Headers of " + url + " => "
                    + connection.getHeaderFields());
        } catch (SSLHandshakeException e) {
            System.out.println("Untrusted: " + url);
        }
        hasExpiredCertificate(url);
    }
}

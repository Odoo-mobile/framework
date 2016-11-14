/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 21/4/15 5:18 PM
 */
package com.odoo.core.rpc.http;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OdooSafeClient {
    private static DefaultHttpClient httpClient;

    public static DefaultHttpClient getSafeClient(boolean forceConnect) {
        createThreadSafeClient(forceConnect);
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "odoo-mobile-android");
        return httpClient;
    }

    private static void createThreadSafeClient(boolean forceSecure) {
        httpClient = new DefaultHttpClient();
        ClientConnectionManager mgr = httpClient.getConnectionManager();
        HttpParams params = httpClient.getParams();
        SchemeRegistry schemeRegistry = mgr.getSchemeRegistry();

        if (forceSecure) {
            schemeRegistry.register(new Scheme("https",
                    getSecureConnectionSetting(), 443));
        } else {
            HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            SSLSocketFactory socketFactory = SSLSocketFactory
                    .getSocketFactory();
            socketFactory
                    .setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            schemeRegistry.register(new Scheme("https", socketFactory, 443));
        }

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                schemeRegistry), params);
    }

    private static SSLSocketFactory getSecureConnectionSetting() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                           String authType) throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                           String authType) throws java.security.cert.CertificateException {

            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

        }};
        SSLSocketFactory ssf = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, null);
            ssf = new OSSLSocketFactory(sc);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception ea) {
            ea.printStackTrace();
        }
        return ssf;
    }
}

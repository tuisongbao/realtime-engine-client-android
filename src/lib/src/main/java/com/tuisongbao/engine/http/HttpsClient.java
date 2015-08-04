package com.tuisongbao.engine.http;

import com.tuisongbao.engine.log.LogUtil;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsClient {
    private static final String TAG = "TSB" + HttpsClient.class.getSimpleName();

    public static DefaultHttpClient getDefaultHttpClient() {
        try {
            return new HttpsClient().getHttpsClient();
        } catch (Exception e) {
            LogUtil.error(TAG, "Get httpsClient", e);
        }
        return new HttpsClient().getHttpClient();
    }

    public DefaultHttpClient getHttpsClient() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {

        BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);

        SchemeRegistry schreg = new SchemeRegistry();
        schreg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        SSLSocketFactory factory = new CustomSSLSocketFactory(trustStore);
        schreg.register(new Scheme("https", factory, 443));

        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schreg);
        return new DefaultHttpClient(manager, params);
    }

    public DefaultHttpClient getHttpClient() {
        BasicHttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        return new DefaultHttpClient(params);
    }

    private class CustomSSLSocketFactory extends SSLSocketFactory {
        SSLContext mSslContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore trustStore) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
            super(trustStore);

            TrustManager tm = new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }
            };

            mSslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                boolean autoClose) throws IOException {
            return mSslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return mSslContext.getSocketFactory().createSocket();
        }
    }
}

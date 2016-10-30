package com.smoothsync.smoothsetup.utils;

import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


/**
 * An {@link HttpUrlConnectionFactory} decorator that sets a custom truststore for SSL connections.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class Trusted implements HttpUrlConnectionFactory
{
    private final HttpUrlConnectionFactory mDelegate;
    private final KeyStore mTrustStore;


    public Trusted(HttpUrlConnectionFactory delegate, KeyStore trustStore)
    {
        mDelegate = delegate;
        mTrustStore = trustStore;
    }


    @Override
    public HttpURLConnection httpUrlConnection(URI uri) throws IllegalArgumentException, IOException
    {
        HttpURLConnection urlConnection = mDelegate.httpUrlConnection(uri);
        if (!(urlConnection instanceof HttpsURLConnection))
        {
            return urlConnection;
        }
        try
        {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(mTrustStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
            return urlConnection;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Can't create TLS SSLContext", e);
        }
        catch (KeyStoreException e)
        {
            throw new RuntimeException("Can't initialize TrustManagerFactory", e);
        }
        catch (KeyManagementException e)
        {
            throw new RuntimeException("Can't initialize SSLContext", e);
        }
    }
}

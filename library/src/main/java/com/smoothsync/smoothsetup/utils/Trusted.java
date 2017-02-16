/*
 * Copyright (c) 2017 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @author Marten Gajda
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

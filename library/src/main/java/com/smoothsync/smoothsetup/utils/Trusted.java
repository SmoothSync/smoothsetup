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

import org.dmfs.jems2.Generator;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;


public final class Trusted implements Generator<OkHttpClient.Builder>
{
    private final Generator<OkHttpClient.Builder> mDelegate;
    private final KeyStore mTrustStore;


    public Trusted(Generator<OkHttpClient.Builder> delegate, KeyStore trustStore)
    {
        mDelegate = delegate;
        mTrustStore = trustStore;
    }


    @Override
    public OkHttpClient.Builder next()
    {
        try
        {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(mTrustStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return mDelegate.next().sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
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

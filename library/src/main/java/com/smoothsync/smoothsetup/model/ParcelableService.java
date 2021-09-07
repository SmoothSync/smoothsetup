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

package com.smoothsync.smoothsetup.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.smoothsync.api.model.Service;

import org.dmfs.jems2.Optional;
import org.dmfs.jems2.optional.Present;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.dmfs.jems2.optional.Absent.absent;


/**
 * Decorator for {@link Service}s that can be parcelled.
 *
 * @author Marten Gajda
 */
public final class ParcelableService implements Service, Parcelable
{
    public final static Creator<ParcelableService> CREATOR = new Creator<ParcelableService>()
    {
        @Override
        public ParcelableService createFromParcel(Parcel source)
        {
            return new ParcelableService(
                new UnparcelledService(source.readString(), source.readString(), (URI) source.readSerializable(), source.createByteArray()));
        }


        @Override
        public ParcelableService[] newArray(int size)
        {
            return new ParcelableService[size];
        }
    };
    private final Service mDecorated;


    public ParcelableService(Service decorated)
    {
        mDecorated = decorated;
    }


    @Override
    public String name()
    {
        return mDecorated.name();
    }


    @Override
    public String serviceType()
    {
        return mDecorated.serviceType();
    }


    @Override
    public URI uri()
    {
        return mDecorated.uri();
    }


    @Override
    public Optional<KeyStore> keyStore()
    {
        return mDecorated.keyStore();
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name());
        dest.writeString(serviceType());
        dest.writeSerializable(uri());

        Optional<KeyStore> keyStore = keyStore();
        if (keyStore.isPresent())
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try
            {
                keyStore.value().store(outputStream, null);
            }
            catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e)
            {
                throw new RuntimeException("Can't serialize KeyStore", e);
            }
            dest.writeByteArray(outputStream.toByteArray());
        }
        else
        {
            dest.writeByteArray(new byte[0]);
        }
    }


    private final static class UnparcelledService implements Service
    {

        private final String mName;
        private final String mServiceType;
        private final URI mUri;
        private final byte[] mKeyStoreBytes;


        public UnparcelledService(String name, String serviceType, URI uri, byte[] keyStoreBytes)
        {
            mName = name;
            mServiceType = serviceType;
            mUri = uri;
            mKeyStoreBytes = keyStoreBytes;
        }


        @Override
        public String name()
        {
            return mName;
        }


        @Override
        public String serviceType()
        {
            return mServiceType;
        }


        @Override
        public URI uri()
        {
            return mUri;
        }


        @Override
        public Optional<KeyStore> keyStore()
        {
            if (mKeyStoreBytes == null || mKeyStoreBytes.length == 0)
            {
                return absent();
            }
            try
            {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(new ByteArrayInputStream(mKeyStoreBytes), null);
                return new Present<>(keyStore);
            }
            catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e)
            {
                throw new RuntimeException("Can't create KeyStore", e);
            }
        }
    }

}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.types.Link;
import org.dmfs.rfc5545.DateTime;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * {@link Provider} decorator that makes a Provider Parcelable.
 *
 * @author Marten Gajda
 */
public final class ParcelableProvider implements Provider, Parcelable
{
    public final static Creator<Provider> CREATOR = new Creator<Provider>()
    {
        @Override
        public Provider createFromParcel(Parcel source)
        {
            ClassLoader classLoader = getClass().getClassLoader();
            String id = source.readString();
            String name = source.readString();
            String[] domains = source.createStringArray();
            List<Link> links = new ArrayList<>();
            Link link = source.readParcelable(classLoader);
            while (link != null)
            {
                links.add(link);
                link = source.readParcelable(classLoader);
            }
            List<Service> services = new ArrayList<>();
            Service service = source.readParcelable(classLoader);
            while (service != null)
            {
                services.add(service);
                service = source.readParcelable(classLoader);
            }
            DateTime lastModified = new DateTime(source.readLong());

            return new ParcelableProvider(new UnparcelledProvider(id, name, domains, links, services, lastModified));
        }


        @Override
        public Provider[] newArray(int size)
        {
            return new Provider[size];
        }
    };
    private final Provider mDecorated;


    public ParcelableProvider(Provider provider)
    {
        mDecorated = provider;
    }


    @Override
    public String id() throws ProtocolException
    {
        return mDecorated.id();
    }


    @Override
    public String name() throws ProtocolException
    {
        return mDecorated.name();
    }


    @Override
    public String[] domains() throws ProtocolException
    {
        return mDecorated.domains();
    }


    @Override
    public Iterator<Link> links() throws ProtocolException
    {
        return mDecorated.links();
    }


    @Override
    public Iterator<Service> services() throws ProtocolException
    {
        return mDecorated.services();
    }


    public DateTime lastModified() throws ProtocolException
    {
        return mDecorated.lastModified();
    };


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        try
        {
            dest.writeString(mDecorated.id());
            dest.writeString(mDecorated.name());
            dest.writeStringArray(mDecorated.domains());
            Iterator<Link> linkIterator = mDecorated.links();
            while (linkIterator.hasNext())
            {
                Link next = linkIterator.next();
                dest.writeParcelable(next instanceof Parcelable ? (Parcelable) next : new ParcelableLink(next), 0);
            }
            dest.writeParcelable(null, 0);

            Iterator<Service> iterator = mDecorated.services();
            while (iterator.hasNext())
            {
                Service service = iterator.next();
                dest.writeParcelable(service instanceof Parcelable ? (Parcelable) service : new ParcelableService(service), 0);
            }
            dest.writeParcelable(null, 0);
            dest.writeLong(mDecorated.lastModified().getTimestamp());
        }
        catch (ProtocolException e)
        {

        }
    }


    private final static class UnparcelledProvider implements Provider
    {

        private final String mId;
        private final String mName;
        private final String[] mDomains;
        private final List<Link> mLinks;
        private final List<Service> mServices;
        private final DateTime mLastModified;


        public UnparcelledProvider(String id, String name, String[] domains, List<Link> links, List<Service> services, DateTime lastModified)
        {
            mId = id;
            mName = name;
            mDomains = domains;
            mLinks = links;
            mServices = services;
            mLastModified = lastModified;
        }


        @Override
        public String id() throws ProtocolException
        {
            return mId;
        }


        @Override
        public String name() throws ProtocolException
        {
            return mName;
        }


        @Override
        public String[] domains() throws ProtocolException
        {
            return mDomains.clone();
        }


        @Override
        public Iterator<Link> links() throws ProtocolException
        {
            return Collections.unmodifiableList(mLinks).iterator();
        }


        @Override
        public Iterator<Service> services() throws ProtocolException
        {
            return Collections.unmodifiableList(mServices).iterator();
        }


        @Override
        public DateTime lastModified() throws ProtocolException
        {
            return mLastModified;
        }
    }

}

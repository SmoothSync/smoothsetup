/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.types.Link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * {@link Provider} decorator that makes a Provider Parcelable.
 */
public final class ParcelableProvider implements Provider, Parcelable
{

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
			Iterator<Service> iterator = mDecorated.services();
			while (iterator.hasNext())
			{
				Service service = iterator.next();
				if (service instanceof Parcelable)
				{
					dest.writeParcelable((Parcelable) service, 0);
				}
				else
				{
					dest.writeParcelable(new ParcelableService(service), 0);
				}
			}
			dest.writeParcelable(null, 0);
		}
		catch (ProtocolException e)
		{

		}
	}

	public final static Creator<Provider> CREATOR = new Creator<Provider>()
	{
		@Override
		public Provider createFromParcel(Parcel source)
		{
			String id = source.readString();
			String name = source.readString();
			String[] domains = source.createStringArray();
			List<Service> services = new ArrayList<Service>();
			ClassLoader classLoader = getClass().getClassLoader();
			Service service = source.readParcelable(classLoader);
			while (service != null)
			{
				services.add(service);
				service = source.readParcelable(classLoader);
			}

			return new UnparcelledProvider(id, name, domains, services);
		}


		@Override
		public Provider[] newArray(int size)
		{
			return new Provider[size];
		}
	};

	private final static class UnparcelledProvider implements Provider, Parcelable
	{

		private final String mId;
		private final String mName;
		private final String[] mDomains;
		private final List<Service> mServices;


		public UnparcelledProvider(String id, String name, String[] domains, List<Service> services)
		{
			mId = id;
			mName = name;
			mDomains = domains;
			mServices = services;
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
			return Collections.<Link> emptyList().iterator();
		}


		@Override
		public Iterator<Service> services() throws ProtocolException
		{
			return Collections.unmodifiableList(mServices).iterator();
		}


		@Override
		public int describeContents()
		{
			return 0;
		}


		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			dest.writeString(mId);
			dest.writeString(mName);
			dest.writeStringArray(mDomains);
			Iterator<Service> iterator = mServices.iterator();
			while (iterator.hasNext())
			{
				Service service = iterator.next();
				if (service instanceof Parcelable)
				{
					dest.writeParcelable((Parcelable) service, 0);
				}
				else
				{
					dest.writeParcelable(new ParcelableService(service), 0);
				}
			}
			dest.writeParcelable(null, 0);
		}

		public final static Creator<Provider> CREATOR = ParcelableProvider.CREATOR;
	}

}

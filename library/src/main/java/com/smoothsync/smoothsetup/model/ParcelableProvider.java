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

import org.dmfs.httpclient.exceptions.ProtocolException;
import org.dmfs.httpclient.types.Link;

import java.util.Iterator;


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
		return null;
	}


	@Override
	public Iterator<Service> services() throws ProtocolException
	{
		return null;
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		// dest.writeString(id());
		// dest.writeString(name());
		// dest.writeStringArray(domains());
		// dest.writeParcelableArray();
	}

	public final static Creator<Provider> CREATOR = new Creator<Provider>()
	{
		@Override
		public Provider createFromParcel(Parcel source)
		{
			return null;
		}


		@Override
		public Provider[] newArray(int size)
		{
			return new Provider[size];
		}
	};
}

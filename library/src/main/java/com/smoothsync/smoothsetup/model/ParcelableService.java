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

import com.smoothsync.api.model.Service;

import java.net.URI;


/**
 * Decorator for {@link Service}s that can be parcelled.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ParcelableService implements Service, Parcelable
{
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
	}

	public final static Creator<Service> CREATOR = new Creator<Service>()
	{
		@Override
		public Service createFromParcel(Parcel source)
		{
			return new UnparcelledService(source.readString(), source.readString(), (URI) source.readSerializable());
		}


		@Override
		public Service[] newArray(int size)
		{
			return new Service[size];
		}
	};

	private final static class UnparcelledService implements Service, Parcelable
	{

		private final String mName;
		private final String mServiceType;
		private final URI mUri;


		public UnparcelledService(String name, String serviceType, URI uri)
		{
			this.mName = name;
			this.mServiceType = serviceType;
			this.mUri = uri;
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
		}

		public final static Creator<Service> CREATOR = ParcelableService.CREATOR;
	}

}

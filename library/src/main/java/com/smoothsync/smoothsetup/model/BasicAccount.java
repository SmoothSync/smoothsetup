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


/**
 * A basic implementation of an {@link Account}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class BasicAccount implements Account
{
	private final String mAccountId;
	private final Provider mProvider;


	public BasicAccount(String accountId, Provider provider)
	{
		mAccountId = accountId;
		mProvider = provider;
	}


	@Override
	public String accountId()
	{
		return mAccountId;
	}


	@Override
	public Provider provider()
	{
		return mProvider;
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(mAccountId);
		if (mProvider instanceof Parcelable)
		{
			dest.writeParcelable((Parcelable) mProvider, flags);
		}
		else
		{
			dest.writeParcelable(new ParcelableProvider(mProvider), flags);
		}
	}

	public final static Creator<BasicAccount> CREATOR = new Creator<BasicAccount>()
	{

		@Override
		public BasicAccount createFromParcel(Parcel source)
		{
			return new BasicAccount(source.readString(), (Provider) source.readParcelable(getClass().getClassLoader()));
		}


		@Override
		public BasicAccount[] newArray(int size)
		{
			return new BasicAccount[0];
		}
	};
}

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

package com.smoothsync.smoothsetup.wizardsteps;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.Adapter;
import android.widget.Filterable;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.ProviderAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.ParcelableProvider;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.ProviderSmoothSetupAdapter;

import org.dmfs.httpessentials.exceptions.ProtocolException;


/**
 * A WizardStep to set up a specific provider.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ProviderLoginWizardStep implements WizardStep
{
	private final Provider mProvider;
	private final String mAccount;


	public ProviderLoginWizardStep(Provider provider, String account)
	{
		mProvider = provider;
		mAccount = account;
	}


	@Override
	public String title(Context context)
	{
		try
		{
			return mProvider.name();
		}
		catch (ProtocolException e)
		{
			throw new RuntimeException("Can't load provider title", e);
		}
	}


	@Override
	public boolean skipOnBack()
	{
		return false;
	}


	@Override
	public Fragment fragment(Context context)
	{
		return LoginFragment.newInstance(this, new ProviderLoginFormAdapterFactory(mProvider), mAccount);
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeParcelable(new ParcelableProvider(mProvider), flags);
		dest.writeString(mAccount);
	}

	public final static Creator CREATOR = new Creator()
	{
		@Override
		public Object createFromParcel(Parcel source)
		{
			return new ProviderLoginWizardStep((Provider) source.readParcelable(getClass().getClassLoader()), source.readString());
		}


		@Override
		public Object[] newArray(int size)
		{
			return new ProviderLoginWizardStep[size];
		}
	};

	private final static class ProviderLoginFormAdapterFactory implements LoginFragment.LoginFormAdapterFactory
	{
		private final Provider mProvider;


		private ProviderLoginFormAdapterFactory(Provider provider)
		{
			mProvider = provider;
		}


		@Override
		public <T extends RecyclerView.Adapter<BasicButtonViewHolder>, SetupButtonAdapter> T setupButtonAdapter(Context context,
			com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter.OnProviderSelectListener providerSelectListener, SmoothSyncApi api)
		{
			return (T) new ProviderSmoothSetupAdapter(mProvider, providerSelectListener);
		}


		@Override
		public <T extends Adapter & Filterable> T autoCompleteAdapter(Context context, SmoothSyncApi api)
		{
			return (T) new ProviderAutoCompleteAdapter(mProvider);
		}


		@Override
		public String promptText(Context context)
		{
			try
			{
				return context.getString(R.string.smoothsetup_login_prompt_branded, mProvider.name());
			}
			catch (ProtocolException e)
			{
				throw new RuntimeException("Can't retrieve provider name", e);
			}
		}


		@Override
		public int describeContents()
		{
			return 0;
		}


		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			dest.writeParcelable(new ParcelableProvider(mProvider), flags);
		}

		public final static Creator<LoginFragment.LoginFormAdapterFactory> CREATOR = new Creator<LoginFragment.LoginFormAdapterFactory>()
		{
			@Override
			public LoginFragment.LoginFormAdapterFactory createFromParcel(Parcel source)
			{
				return new ProviderLoginFormAdapterFactory((Provider) source.readParcelable(getClass().getClassLoader()));
			}


			@Override
			public LoginFragment.LoginFormAdapterFactory[] newArray(int size)
			{
				return new ProviderLoginFormAdapterFactory[size];
			}
		};
	}
}

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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.model.ParcelableProvider;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;
import com.smoothsync.smoothsetup.wizardtransitions.ForwardWizardTransition;

import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.util.Arrays;
import java.util.Comparator;


/**
 * A WizardStep that presents the user with a list of providers to choose from.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ChooseProviderStep implements WizardStep
{
	private final ParcelableProvider[] mProviders;
	private final String mAccount;


	public ChooseProviderStep(Provider[] providers, String account)
	{
		mProviders = new ParcelableProvider[providers.length];
		for (int i = 0, count = providers.length; i < count; ++i)
		{
			Provider p = providers[i];
			if (!(p instanceof Parcelable))
			{
				mProviders[i] = new ParcelableProvider(p);
			}
			else
			{
				mProviders[i] = (ParcelableProvider) p;
			}
		}
		Arrays.sort(mProviders, new Comparator<ParcelableProvider>()
		{
			@Override
			public int compare(ParcelableProvider lhs, ParcelableProvider rhs)
			{
				try
				{
					return lhs.name().compareToIgnoreCase(rhs.name());
				}
				catch (ProtocolException e)
				{
					throw new RuntimeException("can't read provider name ", e);
				}
			}
		});
		mAccount = account;
	}


	@Override
	public String title(Context context)
	{
		return context.getString(R.string.smoothsetup_wizard_title_choose_provider);
	}


	@Override
	public boolean skipOnBack()
	{
		return false;
	}


	@Override
	public Fragment fragment(Context context)
	{
		Fragment result = new ProviderListFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelableArray(ProviderListFragment.ARG_PROVIDERS, mProviders);
		arguments.putParcelable(ARG_WIZARD_STEP, this);
		arguments.putString(ProviderListFragment.ARG_ACCOUNT, mAccount);
		result.setArguments(arguments);
		result.setRetainInstance(true);
		return result;
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeParcelableArray(mProviders, flags);
		dest.writeString(mAccount);
	}

	public final static Creator<ChooseProviderStep> CREATOR = new Creator<ChooseProviderStep>()
	{
		@Override
		public ChooseProviderStep createFromParcel(Parcel source)
		{
			return new ChooseProviderStep((Provider[]) source.readParcelableArray(getClass().getClassLoader()), source.readString());
		}


		@Override
		public ChooseProviderStep[] newArray(int size)
		{
			return new ChooseProviderStep[size];
		}
	};

	/**
	 * A Fragment that shows a list of providers.
	 *
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	public static final class ProviderListFragment extends Fragment implements SetupButtonAdapter.OnProviderSelectListener
	{

		public final static String ARG_PROVIDERS = "providers";
		public final static String ARG_ACCOUNT = "account";

		private RecyclerView mView;


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			mView = (RecyclerView) inflater.inflate(R.layout.smoothsetup_wizard_fragment_provider_list, container, false);
			mView.setLayoutManager(new LinearLayoutManager(getContext()));
			mView.setAdapter(new ProvidersRecyclerViewAdapter(Arrays.<Provider> asList((Provider[]) getArguments().getParcelableArray(ARG_PROVIDERS)), this));
			return mView;
		}


		@Override
		public void onProviderSelected(Provider provider)
		{
			String account = getArguments().getString(ARG_ACCOUNT);
			if (TextUtils.isEmpty(account))
			{
				new ForwardWizardTransition((new ProviderLoginWizardStep(provider, ""))).execute(getContext());
			}
			else
			{
				new ForwardWizardTransition(new PasswordWizardStep(new BasicAccount(account, provider))).execute(getContext());
			}
		}
	}
}

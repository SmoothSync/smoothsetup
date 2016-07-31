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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.ProviderLoadTask;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.services.FutureLocalServiceConnection;
import com.smoothsync.smoothsetup.services.FutureServiceConnection;
import com.smoothsync.smoothsetup.services.SmoothSyncApiProxy;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;
import com.smoothsync.smoothsetup.wizardtransitions.AutomaticWizardTransition;


/**
 * A {@link WizardStep} that loads a provider by its id, before moving on to a setup step.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ProviderLoadWizardStep implements WizardStep
{
	private final static String ARG_PROVIDER_ID = "provider-id";
	private final static String ARG_ACCOUNT = "account";

	private final String mProviderId;
	private final String mAccount;


	/**
	 * Create a ProviderLoadWizardStep that loads the provider with the given id.
	 * 
	 * @param providerId
	 *            The provider id.
	 * @param account
	 *            An optional account to set up.
	 */
	public ProviderLoadWizardStep(String providerId, String account)
	{
		mProviderId = providerId;
		mAccount = account;
	}


	@Override
	public String title(Context context)
	{
		return context.getString(R.string.smoothsetup_loading);
	}


	@Override
	public boolean skipOnBack()
	{
		return true;
	}


	@Override
	public Fragment fragment(Context context)
	{
		Fragment result = new LoadFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PROVIDER_ID, mProviderId);
		args.putString(ARG_ACCOUNT, mAccount);
		args.putParcelable(ARG_WIZARD_STEP, this);
		result.setArguments(args);
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
		dest.writeString(mProviderId);
		dest.writeString(mAccount);
	}

	public final static Creator CREATOR = new Creator()
	{
		@Override
		public ProviderLoadWizardStep createFromParcel(Parcel source)
		{
			return new ProviderLoadWizardStep(source.readString(), source.readString());
		}


		@Override
		public ProviderLoadWizardStep[] newArray(int size)
		{
			return new ProviderLoadWizardStep[size];
		}
	};

	public static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Provider>
	{
		private final static int DELAY_WAIT_MESSAGE = 2500;

		private View mWaitMessage;
		private Handler mHandler = new Handler();
		private FutureServiceConnection<SmoothSyncApi> mApiService;


		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			View result = inflater.inflate(R.layout.smoothsetup_wizard_fragment_loading, container, false);
			mWaitMessage = result.findViewById(android.R.id.message);
			mHandler.postDelayed(mShowWaitMessage, DELAY_WAIT_MESSAGE);
			return result;
		}


		@Override
		public void onCreate(@Nullable Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			Context context = getContext();
			mApiService = new FutureLocalServiceConnection<SmoothSyncApi>(context,
				new Intent("com.smoothsync.action.BIND_API").setPackage(context.getPackageName()));
			new ProviderLoadTask(new SmoothSyncApiProxy(mApiService), this).execute(getArguments().getString(ARG_PROVIDER_ID));
		}


		@Override
		public void onDetach()
		{
			mHandler.removeCallbacks(mShowWaitMessage);
			super.onDetach();
		}


		@Override
		public void onDestroy()
		{
			mApiService.disconnect();
			super.onDestroy();
		}


		@Override
		public void onResult(final AsyncTaskResult<Provider> result)
		{
			if (isAdded())
			{
				try
				{
					new AutomaticWizardTransition(new ProviderLoginWizardStep(result.value(), getArguments().getString(ARG_ACCOUNT))).execute(getContext());
				}
				catch (Exception e)
				{
					new AutomaticWizardTransition(new ErrorResetWizardStep((WizardStep) getArguments().getParcelable(ARG_WIZARD_STEP))).execute(getContext());
				}
			}
		}

		private final Runnable mShowWaitMessage = new Runnable()
		{
			@Override
			public void run()
			{
				mWaitMessage.animate().alpha(1f).start();
			}
		};
	}
}

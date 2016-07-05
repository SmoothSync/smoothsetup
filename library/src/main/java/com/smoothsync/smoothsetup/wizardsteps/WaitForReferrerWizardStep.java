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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.SmoothSetupDispatchActivity;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardtransitions.AutomaticWizardTransition;

import java.util.concurrent.TimeUnit;


/**
 * A {@link WizardStep} that waits for a moment for any INSTALL_REFERRER broadcast coming in.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class WaitForReferrerWizardStep implements WizardStep
{

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
		Fragment result = new WaitForReferrerFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(ARG_WIZARD_STEP, this);
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

	}

	public final static Creator CREATOR = new Creator()
	{
		@Override
		public WaitForReferrerWizardStep createFromParcel(Parcel source)
		{
			return new WaitForReferrerWizardStep();
		}


		@Override
		public WaitForReferrerWizardStep[] newArray(int size)
		{
			return new WaitForReferrerWizardStep[size];
		}
	};

	public static class WaitForReferrerFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
	{
		private final Handler mHandler = new Handler();
		private SharedPreferences mPreferences;


		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.smoothsetup_wizard_fragment_loading, container, false);
		}


		@Override
		public void onResume()
		{
			super.onResume();
			mPreferences = getActivity().getSharedPreferences("com.smoothsync.smoothsetup.prefs", 0);
			mPreferences.registerOnSharedPreferenceChangeListener(this);
			if (mPreferences.contains(SmoothSetupDispatchActivity.PREF_REFERRER))
			{
				onSharedPreferenceChanged(mPreferences, SmoothSetupDispatchActivity.PREF_REFERRER);
			}
			mHandler.postDelayed(mMoveOn, TimeUnit.SECONDS.toMillis(1));
		}


		@Override
		public void onPause()
		{
			mHandler.removeCallbacks(mMoveOn);
			mPreferences.unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}


		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			if (!SmoothSetupDispatchActivity.PREF_REFERRER.equals(key))
			{
				// not the right key
				return;
			}

			String referrer = sharedPreferences.getString(SmoothSetupDispatchActivity.PREF_REFERRER, null);
			if (referrer == null || referrer.isEmpty())
			{
				// referrer is null
				return;
			}

			Uri uri = Uri.parse(referrer);
			if (uri.getQueryParameter(SmoothSetupDispatchActivity.PARAM_PROVIDER) == null)
			{
				// no provider present
				return;
			}

			mHandler.removeCallbacks(mMoveOn);
			new AutomaticWizardTransition(new ProviderLoadWizardStep(uri.getQueryParameter(SmoothSetupDispatchActivity.PARAM_PROVIDER),
				uri.getQueryParameter(SmoothSetupDispatchActivity.PARAM_ACCOUNT))).execute(getContext());
		}

		/**
		 * A Runnable that's executed when the waiting time is up and no broadcast was received.
		 */
		private final Runnable mMoveOn = new Runnable()
		{
			@Override
			public void run()
			{
				mPreferences.unregisterOnSharedPreferenceChangeListener(WaitForReferrerFragment.this);

				// make sure we won't wait for the broadcast again
				mPreferences.edit().putString("referrer", "").apply();

				// move on
				new AutomaticWizardTransition(new GenericProviderWizardStep()).execute(getContext());
			}
		};
	}
}

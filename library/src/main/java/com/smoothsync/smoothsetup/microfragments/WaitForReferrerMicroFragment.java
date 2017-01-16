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

package com.smoothsync.smoothsetup.microfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.SmoothSetupDispatchActivity;

import org.dmfs.android.microfragments.BasicMicroFragmentEnvironment;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;


/**
 * A {@link MicroFragment} that waits a moment for any INSTALL_REFERRER broadcast coming in.
 *
 * @author Marten Gajda
 */
public final class WaitForReferrerMicroFragment implements MicroFragment<Void>
{

    public final static Creator<WaitForReferrerMicroFragment> CREATOR = new Creator<WaitForReferrerMicroFragment>()
    {
        @Override
        public WaitForReferrerMicroFragment createFromParcel(Parcel source)
        {
            return new WaitForReferrerMicroFragment();
        }


        @Override
        public WaitForReferrerMicroFragment[] newArray(int size)
        {
            return new WaitForReferrerMicroFragment[size];
        }
    };


    public WaitForReferrerMicroFragment()
    {
        // nothing to do here
    }


    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_loading);
    }


    @Override
    public boolean skipOnBack()
    {
        return true;
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost host)
    {
        Fragment result = new WaitForReferrerFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(MicroFragment.ARG_ENVIRONMENT, new BasicMicroFragmentEnvironment<>(this, host));
        result.setArguments(arguments);
        return result;
    }


    @NonNull
    @Override
    public Void parameters()
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
        // nothing to do
    }


    public final static class WaitForReferrerFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        private final static int WAIT_TIME = 1000; // milliseconds

        private final Handler mHandler = new Handler();
        private SharedPreferences mPreferences;
        private MicroFragmentEnvironment<Void> mMicroFragmentEnvironment;
        private Timestamp mTimeStamp = new UiTimestamp();
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

                // move on without provider
                mMicroFragmentEnvironment.host().execute(getActivity(), new XFaded(new ForwardTransition(new GenericProviderMicroFragment(), mTimeStamp)));
            }
        };


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            return inflater.inflate(R.layout.smoothsetup_microfragment_loading, container, false);
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
            mHandler.postDelayed(mMoveOn, WAIT_TIME);
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
            if (!isResumed())
            {
                // not in the foreground
                return;
            }

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
            // got a referrer, move on and load the provider
            mMicroFragmentEnvironment.host()
                    .execute(getActivity(),
                            new XFaded(
                                    new ForwardTransition(
                                            new ProviderLoadMicroFragment(
                                                    uri.getQueryParameter(SmoothSetupDispatchActivity.PARAM_PROVIDER),
                                                    uri.getQueryParameter(SmoothSetupDispatchActivity.PARAM_ACCOUNT)), mTimeStamp)));
        }
    }
}

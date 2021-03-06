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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.LoginRequest;
import com.smoothsync.smoothsetup.utils.SimpleLoginRequest;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.optional.NullSafe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} that waits a moment for any INSTALL_REFERRER broadcast coming in.
 *
 * @author Marten Gajda
 */
public final class WaitForReferrerMicroFragment implements MicroFragment<WaitForReferrerMicroFragment.Params>
{

    public final static Creator<WaitForReferrerMicroFragment> CREATOR = new Creator<WaitForReferrerMicroFragment>()
    {
        @Override
        public WaitForReferrerMicroFragment createFromParcel(Parcel source)
        {
            return new WaitForReferrerMicroFragment(new Unboxed<MicroWizard<LoginRequest>>(source).value(), new Unboxed<MicroWizard<Void>>(source).value());
        }


        @Override
        public WaitForReferrerMicroFragment[] newArray(int size)
        {
            return new WaitForReferrerMicroFragment[size];
        }
    };
    private final MicroWizard<LoginRequest> mNext;
    private final MicroWizard<Void> mFallback;


    public WaitForReferrerMicroFragment(MicroWizard<LoginRequest> next, MicroWizard<Void> fallback)
    {
        mNext = next;
        mFallback = fallback;
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
        return new WaitForReferrerFragment();
    }


    @NonNull
    @Override
    public Params parameter()
    {
        return new Params()
        {
            @Override
            public MicroWizard<LoginRequest> next()
            {
                return mNext;
            }


            @Override
            public MicroWizard<Void> fallback()
            {
                return mFallback;
            }
        };
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mNext.boxed(), flags);
        dest.writeParcelable(mFallback.boxed(), flags);
    }


    public final static class WaitForReferrerFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        private final static int WAIT_TIME = 1500; // milliseconds

        private final Handler mHandler = new Handler();
        private SharedPreferences mPreferences;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
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
                mMicroFragmentEnvironment.host()
                        .execute(getActivity(), new XFaded(new ForwardTransition<>(
                                mMicroFragmentEnvironment.microFragment().parameter().fallback().microFragment(getActivity(), null),
                                mTimeStamp)));
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
            if (mPreferences.contains(SetupDispatchMicroFragment.DispatchFragment.PREF_REFERRER))
            {
                onSharedPreferenceChanged(mPreferences, SetupDispatchMicroFragment.DispatchFragment.PREF_REFERRER);
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

            if (!SetupDispatchMicroFragment.DispatchFragment.PREF_REFERRER.equals(key))
            {
                // not the right key
                return;
            }

            String referrer = sharedPreferences.getString(SetupDispatchMicroFragment.DispatchFragment.PREF_REFERRER, null);
            if (referrer == null || referrer.isEmpty())
            {
                // referrer is null
                return;
            }

            Uri uri = Uri.parse(referrer);
            if (uri.getQueryParameter(SetupDispatchMicroFragment.DispatchFragment.PARAM_PROVIDER) == null)
            {
                // no provider present
                return;
            }

            mHandler.removeCallbacks(mMoveOn);
            // got a referrer, move on and load the provider
            mMicroFragmentEnvironment.host()
                    .execute(getActivity(),
                            new XFaded(
                                    new ForwardTransition<>(
                                            mMicroFragmentEnvironment.microFragment().parameter().next().microFragment(getActivity(),
                                                    new SimpleLoginRequest(
                                                            uri.getQueryParameter(SetupDispatchMicroFragment.DispatchFragment.PARAM_PROVIDER),
                                                            new NullSafe<>(uri.getQueryParameter(SetupDispatchMicroFragment.DispatchFragment.PARAM_ACCOUNT))
                                                    )), mTimeStamp)));
        }
    }


    protected interface Params
    {
        MicroWizard<LoginRequest> next();

        MicroWizard<Void> fallback();
    }
}

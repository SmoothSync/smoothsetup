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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.ProviderLoadTask;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.FutureApiServiceConnection;
import com.smoothsync.smoothsetup.services.SmoothSyncApiProxy;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.LoginInfo;
import com.smoothsync.smoothsetup.utils.LoginRequest;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.optional.Optional;


/**
 * A {@link MicroFragment} that loads a provider by its id before moving on to a setup step.
 *
 * @author Marten Gajda
 */
public final class ProviderLoadMicroFragment implements MicroFragment<ProviderLoadMicroFragment.Params>
{
    public final static Creator<ProviderLoadMicroFragment> CREATOR = new Creator<ProviderLoadMicroFragment>()
    {
        @Override
        public ProviderLoadMicroFragment createFromParcel(Parcel source)
        {
            return new ProviderLoadMicroFragment(new Unboxed<LoginRequest>(source).value(), new Unboxed<MicroWizard<LoginInfo>>(source).value());
        }


        @Override
        public ProviderLoadMicroFragment[] newArray(int size)
        {
            return new ProviderLoadMicroFragment[size];
        }
    };
    private final LoginRequest mLoginRequest;
    private final MicroWizard<LoginInfo> mNext;


    /**
     * Create a ProviderLoadWizardStep that loads the provider with the given id.
     */
    public ProviderLoadMicroFragment(@NonNull LoginRequest loginRequest, MicroWizard<LoginInfo> next)
    {
        mLoginRequest = loginRequest;
        mNext = next;
    }


    @NonNull
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
        return new LoadFragment();
    }


    @NonNull
    @Override
    public Params parameter()
    {
        return new Params()
        {
            @Override
            public LoginRequest loginRequest()
            {
                return mLoginRequest;
            }


            @Override
            public MicroWizard<LoginInfo> next()
            {
                return mNext;
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
        dest.writeParcelable(mLoginRequest.boxed(), flags);
        dest.writeParcelable(mNext.boxed(), flags);
    }


    interface Params
    {
        LoginRequest loginRequest();

        MicroWizard<LoginInfo> next();
    }


    public final static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Provider>
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private final Runnable mShowWaitMessage = new Runnable()
        {
            @Override
            public void run()
            {
                getView().findViewById(android.R.id.message).animate().alpha(1f).start();
            }
        };
        private Handler mHandler = new Handler();
        private FutureServiceConnection<SmoothSyncApi> mApiService;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Timestamp mTimestamp = new UiTimestamp();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mApiService = new FutureApiServiceConnection(getActivity());
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_loading, container, false);
            mHandler.postDelayed(mShowWaitMessage, DELAY_WAIT_MESSAGE);
            return result;
        }


        @Override
        public void onResume()
        {
            super.onResume();
            new ProviderLoadTask(new SmoothSyncApiProxy(mApiService), this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    mMicroFragmentEnvironment.microFragment().parameter().loginRequest().providerId());
        }


        @Override
        public void onDestroyView()
        {
            mHandler.removeCallbacks(mShowWaitMessage);
            super.onDestroyView();
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
            if (isResumed())
            {
                try
                {
                    mMicroFragmentEnvironment.host()
                            .execute(getActivity(),
                                    new XFaded(new ForwardTransition<>(
                                            mMicroFragmentEnvironment.microFragment()
                                                    .parameter()
                                                    .next()
                                                    .microFragment(getActivity(), new SimpleProviderInfo(result.value(),
                                                            mMicroFragmentEnvironment.microFragment().parameter().loginRequest().username())),
                                            mTimestamp)));
                }
                catch (Exception e)
                {
                    mMicroFragmentEnvironment.host()
                            .execute(getActivity(),
                                    new XFaded(new ForwardTransition<>(new ErrorResetMicroFragment(mMicroFragmentEnvironment.microFragment()), mTimestamp)));
                }
            }
        }
    }


    public final static class SimpleProviderInfo implements LoginInfo
    {
        private final Provider mProvider;
        private final Optional<String> mUsername;


        public SimpleProviderInfo(Provider provider, Optional<String> musername)
        {
            mProvider = provider;
            mUsername = musername;
        }


        @Override
        public Provider provider()
        {
            return mProvider;
        }


        @Override
        public Optional<String> username()
        {
            return mUsername;
        }


        @Override
        public Box<LoginInfo> boxed()
        {
            return null;
        }
    }
}

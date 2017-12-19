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
import com.smoothsync.smoothsetup.ProvidersLoadTask;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.FutureApiServiceConnection;
import com.smoothsync.smoothsetup.services.SmoothSyncApiProxy;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
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
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;

import java.util.List;


/**
 * A {@link MicroFragment} that loads a provider by its id before moving on to a setup step.
 *
 * @author Marten Gajda
 */
public final class ProvidersLoadMicroFragment implements MicroFragment<ProvidersLoadMicroFragment.Params>
{
    public final static Creator<ProvidersLoadMicroFragment> CREATOR = new Creator<ProvidersLoadMicroFragment>()
    {
        @Override
        public ProvidersLoadMicroFragment createFromParcel(Parcel source)
        {
            return new ProvidersLoadMicroFragment(new NullSafe<>(source.readString()),
                    new Unboxed<MicroWizard<ChooseProviderMicroFragment.ProviderSelection>>(source).value());
        }


        @Override
        public ProvidersLoadMicroFragment[] newArray(int size)
        {
            return new ProvidersLoadMicroFragment[size];
        }
    };
    @Nullable
    private final Optional<String> mAccount;
    private final MicroWizard<ChooseProviderMicroFragment.ProviderSelection> mNext;


    protected interface Params
    {
        Optional<String> username();

        MicroWizard<ChooseProviderMicroFragment.ProviderSelection> next();
    }


    /**
     * Create a {@link ProvidersLoadMicroFragment} that loads the provider with the given id.
     *
     * @param account
     *         An optional account identifier to set up.
     * @param next
     */
    public ProvidersLoadMicroFragment(@Nullable Optional<String> account, MicroWizard<ChooseProviderMicroFragment.ProviderSelection> next)
    {
        mAccount = account;
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
            public Optional<String> username()
            {
                return mAccount;
            }


            @Override
            public MicroWizard<ChooseProviderMicroFragment.ProviderSelection> next()
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
        dest.writeString(mAccount.value(null));
        dest.writeParcelable(mNext.boxed(), flags);
    }


    public final static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<List<Provider>>
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
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mApiService = new FutureApiServiceConnection(getActivity());
        }


        @Override
        public void onResume()
        {
            super.onResume();
            new ProvidersLoadTask(new SmoothSyncApiProxy(mApiService), this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        public void onResult(final AsyncTaskResult<List<Provider>> result)
        {
            if (!isResumed())
            {
                return;
            }
            try
            {
                List<Provider> providers = result.value();
                mMicroFragmentEnvironment.host().execute(getActivity(),
                        new XFaded(
                                new ForwardTransition<>(
                                        mMicroFragmentEnvironment.microFragment().parameter().next().microFragment(
                                                getActivity(),
                                                new ChooseProviderMicroFragment.ProviderSelection()
                                                {
                                                    @NonNull
                                                    @Override
                                                    public Provider[] providers()
                                                    {
                                                        return providers.toArray(new Provider[providers.size()]);
                                                    }


                                                    @NonNull
                                                    @Override
                                                    public Optional<String> username()
                                                    {
                                                        return mMicroFragmentEnvironment.microFragment().parameter().username();
                                                    }
                                                }
                                        ))));
            }
            catch (Exception e)
            {
                mMicroFragmentEnvironment.host()
                        .execute(getActivity(),
                                new XFaded(
                                        new ForwardTransition<>(
                                                new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_load_provider)), mTimestamp)));
            }
        }
    }
}

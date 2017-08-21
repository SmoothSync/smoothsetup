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

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.AccountService;
import com.smoothsync.smoothsetup.services.FutureAidlServiceConnection;
import com.smoothsync.smoothsetup.services.FutureServiceConnection;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;


/**
 * A {@link MicroFragment} that loads an account.
 *
 * @author Marten Gajda
 */
public final class AccountLoadMicroFragment implements MicroFragment<Account>
{
    public final static Creator<AccountLoadMicroFragment> CREATOR = new Creator<AccountLoadMicroFragment>()
    {
        @Override
        public AccountLoadMicroFragment createFromParcel(Parcel source)
        {
            Account account = source.readParcelable(getClass().getClassLoader());
            return new AccountLoadMicroFragment(account);
        }


        @Override
        public AccountLoadMicroFragment[] newArray(int size)
        {
            return new AccountLoadMicroFragment[size];
        }
    };
    private final Account mAccount;


    /**
     * Create a AccountLoadMicroFragment that loads the given account.
     *
     * @param account
     *         The android account to load.
     */
    public AccountLoadMicroFragment(@NonNull Account account)
    {
        mAccount = account;
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
    public Account parameter()
    {
        return mAccount;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mAccount, flags);
    }


    public final static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<com.smoothsync.smoothsetup.model.Account>
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
        private FutureServiceConnection<AccountService> mAccountService;
        private MicroFragmentEnvironment<Account> mMicroFragmentEnvironment;
        private Timestamp mTimestamp = new UiTimestamp();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            // this must be a retained Fragment, we don't want to create accounts more than once
            mAccountService = new FutureAidlServiceConnection<AccountService>(getContext(),
                    new Intent("com.smoothsync.action.ACCOUNT_SERVICE").setPackage(getContext().getPackageName()),
                    new FutureAidlServiceConnection.StubProxy<AccountService>()
                    {
                        @Override
                        public AccountService asInterface(IBinder service)
                        {
                            return AccountService.Stub.asInterface(service);
                        }
                    });
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
            new ThrowingAsyncTask<Account, Void, com.smoothsync.smoothsetup.model.Account>(this)
            {
                @Override
                protected com.smoothsync.smoothsetup.model.Account doInBackgroundWithException(Account... params) throws Exception
                {
                    return mAccountService.service(5000).providerForAccount(params[0]);
                }
            }.execute(mMicroFragmentEnvironment.microFragment().parameter());
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
            mAccountService.disconnect();
            super.onDestroy();
        }


        @Override
        public void onResult(final AsyncTaskResult<com.smoothsync.smoothsetup.model.Account> result)
        {
            if (isResumed())
            {
                try
                {
                    mMicroFragmentEnvironment.host()
                            .execute(getActivity(),
                                    new XFaded(new ForwardTransition<>(
                                            new AuthErrorMicroFragment(result.value()), mTimestamp)));
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
}

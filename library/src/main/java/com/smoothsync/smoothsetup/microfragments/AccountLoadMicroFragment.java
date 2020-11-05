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
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.AccountService;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.bolts.service.elementary.FutureAidlServiceConnection;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} that loads an account.
 *
 * @author Marten Gajda
 */
public final class AccountLoadMicroFragment implements MicroFragment<AccountLoadMicroFragment.Params>
{
    public final static Creator<AccountLoadMicroFragment> CREATOR = new Creator<AccountLoadMicroFragment>()
    {
        @Override
        public AccountLoadMicroFragment createFromParcel(Parcel source)
        {
            Account account = source.readParcelable(getClass().getClassLoader());
            return new AccountLoadMicroFragment(account, new Unboxed<MicroWizard<com.smoothsync.smoothsetup.model.Account>>(source).value());
        }


        @Override
        public AccountLoadMicroFragment[] newArray(int size)
        {
            return new AccountLoadMicroFragment[size];
        }
    };
    private final Account mAccount;
    private final MicroWizard<com.smoothsync.smoothsetup.model.Account> mNext;


    /**
     * Create a AccountLoadMicroFragment that loads the given account.
     *
     * @param account
     *         The android account to load.
     * @param next
     */
    public AccountLoadMicroFragment(@NonNull Account account, MicroWizard<com.smoothsync.smoothsetup.model.Account> next)
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
            public Account account()
            {
                return mAccount;
            }


            @Override
            public MicroWizard<com.smoothsync.smoothsetup.model.Account> next()
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
        dest.writeParcelable(mAccount, flags);
        dest.writeParcelable(mNext.boxed(), flags);
    }


    public final static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<com.smoothsync.smoothsetup.model.Account>
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private FutureServiceConnection<AccountService> mAccountService;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Timestamp mTimestamp = new UiTimestamp();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mAccountService = new FutureAidlServiceConnection<>(
                    getContext(),
                    new Intent("com.smoothsync.action.ACCOUNT_SERVICE").setPackage(getContext().getPackageName()),
                    AccountService.Stub::asInterface);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_loading, container, false);
            result.findViewById(android.R.id.message).animate().setStartDelay(DELAY_WAIT_MESSAGE).alpha(1f).start();
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
            }.execute(mMicroFragmentEnvironment.microFragment().parameter().account());
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
                                            mMicroFragmentEnvironment.microFragment().parameter().next().microFragment(getActivity(), result.value()),
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


    protected interface Params
    {
        Account account();

        MicroWizard<com.smoothsync.smoothsetup.model.Account> next();
    }
}

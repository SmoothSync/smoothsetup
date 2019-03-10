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
import android.content.Intent;
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

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.services.AccountService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;
import com.smoothsync.smoothsetup.utils.usercredentials.Parcelable;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.bolts.service.elementary.FutureAidlServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardResetTransition;
import org.dmfs.android.microfragments.transitions.FragmentTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;


/**
 * A {@link MicroFragment} to create the account and finish the setup procedure.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class CreateAccountMicroFragment implements MicroFragment<CreateAccountMicroFragment.Params>
{
    public final static Creator<CreateAccountMicroFragment> CREATOR = new Creator<CreateAccountMicroFragment>()
    {
        @Override
        public CreateAccountMicroFragment createFromParcel(Parcel source)
        {
            return new CreateAccountMicroFragment(new Unboxed<AccountDetails>(source).value(), new Unboxed<MicroWizard<Account>>(source).value());
        }


        @Override
        public CreateAccountMicroFragment[] newArray(int size)
        {
            return new CreateAccountMicroFragment[size];
        }
    };


    public interface Params
    {
        AccountDetails accountDetails();

        MicroWizard<Account> next();
    }


    private final AccountDetails mAccountDetails;
    private final MicroWizard<Account> mNext;


    public CreateAccountMicroFragment(@NonNull AccountDetails accountDetails, MicroWizard<Account> next)
    {
        mAccountDetails = accountDetails;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_completing_setup);
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
            public AccountDetails accountDetails()
            {
                return mAccountDetails;
            }


            @Override
            public MicroWizard<Account> next()
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
        dest.writeParcelable(mAccountDetails.boxed(), 0);
        dest.writeParcelable(mNext.boxed(), 0);
    }


    public final static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Boolean>
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private final Timestamp mTimestamp = new UiTimestamp();
        private final Runnable mShowWaitMessage = () -> getView().findViewById(android.R.id.message).animate().alpha(1f).start();
        private Handler mHandler = new Handler();
        private FutureServiceConnection<AccountService> mAccountService;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private AccountDetails mAccountDetails;
        private FragmentTransition mFragmentTransition;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            // this must be a retained Fragment, we don't want to create accounts more than once
            mAccountService = new FutureAidlServiceConnection<>(getContext(),
                    new Intent("com.smoothsync.action.ACCOUNT_SERVICE").setPackage(getContext().getPackageName()),
                    AccountService.Stub::asInterface);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mAccountDetails = mMicroFragmentEnvironment.microFragment().parameter().accountDetails();
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
            if (mFragmentTransition != null)
            {
                // the operation completed in the background
                mMicroFragmentEnvironment.host().execute(getActivity(), mFragmentTransition);
            }
            else
            {
                // creating an account is expected to be an itempotent operation, hence it shouldn't be a problem if we create it again if the
                // fragment was paused and resumed
                new ThrowingAsyncTask<Void, Void, Boolean>(this)
                {
                    @Override
                    protected Boolean doInBackgroundWithException(Void... params) throws Exception
                    {
                        AccountService service = mAccountService.service(10000);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("account", mAccountDetails.account());
                        bundle.putParcelable("credentials", new Parcelable(mAccountDetails.credentials()));
                        service.createAccount(bundle);
                        return true;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
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
        public void onResult(AsyncTaskResult<Boolean> result)
        {
            FragmentTransition transition;
            Context context = getContext();
            try
            {
                // the account has been created, wipe any temporary branding
                context.getSharedPreferences("com.smoothsync.smoothsetup.prefs", 0).edit().putString("referrer", null).apply();

                // Go to the next step, but reset the back stack, so there is no way back.
                transition = new Swiped(
                        new ForwardResetTransition<>(
                                mMicroFragmentEnvironment.microFragment().parameter().next().microFragment(context, mAccountDetails.account()),
                                mTimestamp));
            }
            catch (Exception e)
            {
                transition = new XFaded(new ForwardResetTransition<>(new ErrorRetryMicroFragment("Unexpected Exception:\n\n" + e.getMessage()), mTimestamp));
            }
            if (isResumed())
            {
                mMicroFragmentEnvironment.host().execute(context, transition);
            }
            else
            {
                mFragmentTransition = transition;
            }
        }
    }
}

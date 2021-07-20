/*
 * Copyright (c) 2019 dmfs GmbH
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.ProviderValidationService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.bolts.service.elementary.FutureLocalServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.httpessentials.exceptions.UnauthorizedException;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} to verify a user's authorization.
 *
 * @author Marten Gajda
 */
public final class ValidateCustomMicroFragment implements MicroFragment<ValidateCustomMicroFragment.Params>
{
    public final static Creator<ValidateCustomMicroFragment> CREATOR = new Creator<ValidateCustomMicroFragment>()
    {
        @Override
        public ValidateCustomMicroFragment createFromParcel(Parcel source)
        {
            return new ValidateCustomMicroFragment(new Unboxed<AccountDetails>(source).value(), new Unboxed<MicroWizard<AccountDetails>>(source).value());
        }


        @Override
        public ValidateCustomMicroFragment[] newArray(int size)
        {
            return new ValidateCustomMicroFragment[size];
        }
    };
    private final AccountDetails mAccountDetails;
    private final MicroWizard<AccountDetails> mNext;


    public ValidateCustomMicroFragment(@NonNull AccountDetails accountDetails, MicroWizard<AccountDetails> next)
    {
        mAccountDetails = accountDetails;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_authenticating);
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
            public MicroWizard<AccountDetails> next()
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


    public static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<AccountDetails>
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Params mParams;
        private final Timestamp mTimestamp = new UiTimestamp();
        private Runnable backGroundVerification;


        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mParams = mMicroFragmentEnvironment.microFragment().parameter();
            final Context context = getContext();
            backGroundVerification = () ->
                    new ThrowingAsyncTask<Void, Void, AccountDetails>(this)
                    {
                        @Override
                        protected AccountDetails doInBackgroundWithException(Void[] params) throws Exception
                        {
                            FutureServiceConnection<ProviderValidationService> serviceConnection =
                                    new FutureLocalServiceConnection<>(
                                            context,
                                            new Intent(ProviderValidationService.ACTION)
                                                    .setPackage(context.getPackageName()));
                            return serviceConnection
                                    .service(1000)
                                    .providerForUrl(mParams.accountDetails().account().provider(), mParams.accountDetails().credentials());
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
            getView().postDelayed(backGroundVerification, 50);
        }


        @Override
        public void onPause()
        {
            getView().removeCallbacks(backGroundVerification);
            super.onPause();
        }


        @Override
        public void onResult(final AsyncTaskResult<AccountDetails> result)
        {
            Activity activity = getActivity();
            if (isResumed() && activity != null)
            {
                try
                {
                    forward(activity, mParams.next().microFragment(activity, result.value()));
                }
                catch (UnauthorizedException e)
                {
                    forward(activity, new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_authentication)));
                }
                catch (Exception e)
                {
                    forward(activity, new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_network)));
                }
            }
        }


        private void forward(Activity activity, MicroFragment<?> mf)
        {
            mMicroFragmentEnvironment.host()
                    .execute(activity,
                            mTimestamp.nanoSeconds() + TimeUnit.MILLISECONDS.toNanos(400) < System.nanoTime() ?
                                    new XFaded(new ForwardTransition<>(mf)) :
                                    new Swiped(new ForwardTransition<>(mf)));
        }
    }


    public interface Params
    {
        AccountDetails accountDetails();

        MicroWizard<AccountDetails> next();
    }
}

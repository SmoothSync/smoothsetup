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
import android.net.Uri;
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

import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.VerificationService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.IndirectServiceIntentIterable;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.bolts.service.elementary.FutureLocalServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.httpessentials.executors.authorizing.AuthScope;
import org.dmfs.httpessentials.executors.authorizing.ServiceScope;
import org.dmfs.httpessentials.executors.authorizing.credentialsstores.SimpleCredentialsStore;
import org.dmfs.httpessentials.executors.authorizing.strategies.UserCredentialsAuthStrategy;
import org.dmfs.iterators.decorators.Filtered;
import org.dmfs.iterators.decorators.Serialized;
import org.dmfs.jems.iterator.decorators.Mapped;

import java.util.Iterator;


/**
 * A {@link MicroFragment} to verify a user's authorization.
 *
 * @author Marten Gajda
 */
public final class ApproveAuthorizationMicroFragment implements MicroFragment<ApproveAuthorizationMicroFragment.Params>
{
    public final static Creator<ApproveAuthorizationMicroFragment> CREATOR = new Creator<ApproveAuthorizationMicroFragment>()
    {
        @Override
        public ApproveAuthorizationMicroFragment createFromParcel(Parcel source)
        {
            return new ApproveAuthorizationMicroFragment(new Unboxed<AccountDetails>(source).value(), new Unboxed<MicroWizard<AccountDetails>>(source).value());
        }


        @Override
        public ApproveAuthorizationMicroFragment[] newArray(int size)
        {
            return new ApproveAuthorizationMicroFragment[size];
        }
    };
    private final AccountDetails mAccountDetails;
    private final MicroWizard<AccountDetails> mNext;


    public ApproveAuthorizationMicroFragment(@NonNull AccountDetails accountDetails, MicroWizard<AccountDetails> next)
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


    public static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Boolean>
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private final Runnable mShowWaitMessage = () -> getView().findViewById(android.R.id.message).animate().alpha(1f).start();
        private Handler mHandler = new Handler();
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Params mParams;


        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mParams = mMicroFragmentEnvironment.microFragment().parameter();

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
            final Context context = getContext();
            new ThrowingAsyncTask<Void, Void, Boolean>(this)
            {
                @Override
                protected Boolean doInBackgroundWithException(Void[] params) throws Exception
                {
                    Iterator<FutureServiceConnection<VerificationService>> serviceConnections =
                            new Mapped<>(
                                    element -> new FutureLocalServiceConnection<>(context, element),
                                    new Serialized<>(
                                            new Mapped<Service, Iterator<Intent>>(
                                                    element -> new IndirectServiceIntentIterable(context,
                                                            new Intent(VerificationService.ACTION)
                                                                    .setData(Uri.fromParts(element.serviceType(), element.uri().toASCIIString(), null))
                                                                    .setPackage(context.getPackageName())).iterator(),
                                                    new Filtered<>(
                                                            mParams.accountDetails().account().provider().services(),
                                                            element -> "com.smoothsync.authenticate".equals(element.serviceType()))
                                            ))
                            );
                    if (!serviceConnections.hasNext())
                    {
                        throw new RuntimeException("No verification service found");
                    }
                    return serviceConnections.next()
                            .service(1000)
                            .verify(mParams.accountDetails().account().provider(),
                                    new UserCredentialsAuthStrategy(new SimpleCredentialsStore<>(new ServiceScope()
                                    {
                                        @Override
                                        public boolean contains(AuthScope authScope)
                                        {
                                            // Consider creating a ServiceScope which covers all service-URLs of the given provider
                                            return true;
                                        }
                                    }, mParams.accountDetails().credentials())));
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


        @Override
        public void onDestroyView()
        {
            mHandler.removeCallbacks(mShowWaitMessage);
            super.onDestroyView();
        }


        @Override
        public void onResult(final AsyncTaskResult<Boolean> result)
        {
            if (isResumed())
            {
                try
                {
                    if (result.value())
                    {
                        mMicroFragmentEnvironment.host()
                                .execute(getActivity(),
                                        new XFaded(
                                                new ForwardTransition<>(mParams.next().microFragment(getActivity(), mParams.accountDetails()))));
                    }
                    else
                    {
                        mMicroFragmentEnvironment.host()
                                .execute(getActivity(),
                                        new XFaded(
                                                new ForwardTransition<>(
                                                        new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_authentication), null,
                                                                getString(R.string.smoothsetup_wizard_title_authentication_error)))));
                    }
                }
                catch (Exception e)
                {
                    mMicroFragmentEnvironment.host()
                            .execute(getActivity(),
                                    new XFaded(
                                            new ForwardTransition<>(
                                                    new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_network)))));
                }
            }
        }
    }


    public interface Params
    {
        AccountDetails accountDetails();

        MicroWizard<AccountDetails> next();
    }
}

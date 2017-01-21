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
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.HttpAuthorizationFactory;
import com.smoothsync.smoothsetup.services.FutureLocalServiceConnection;
import com.smoothsync.smoothsetup.services.FutureServiceConnection;
import com.smoothsync.smoothsetup.services.VerificationService;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.IndirectServiceIntentIterable;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.iterators.AbstractConvertedIterator;
import org.dmfs.iterators.AbstractFilteredIterator;
import org.dmfs.iterators.ConvertedIterator;
import org.dmfs.iterators.FilteredIterator;
import org.dmfs.iterators.SerialIteratorIterator;

import java.util.Iterator;


/**
 * A {@link MicroFragment} to verify a user's authorization.
 *
 * @author Marten Gajda
 */
public final class ApproveAuthorizationMicroFragment implements MicroFragment<ApproveAuthorizationMicroFragment.LoadFragment.Params>
{
    public final static Creator<ApproveAuthorizationMicroFragment> CREATOR = new Creator<ApproveAuthorizationMicroFragment>()
    {
        @Override
        public ApproveAuthorizationMicroFragment createFromParcel(Parcel source)
        {
            ClassLoader classLoader = getClass().getClassLoader();
            return new ApproveAuthorizationMicroFragment((Account) source.readParcelable(classLoader),
                    (HttpAuthorizationFactory) source.readParcelable(classLoader));
        }


        @Override
        public ApproveAuthorizationMicroFragment[] newArray(int size)
        {
            return new ApproveAuthorizationMicroFragment[size];
        }
    };
    private final Account mAccount;
    private final HttpAuthorizationFactory mHttpAuthorizationFactory;


    public ApproveAuthorizationMicroFragment(@NonNull Account account, @NonNull HttpAuthorizationFactory httpAuthorizationFactory)
    {
        mAccount = account;
        mHttpAuthorizationFactory = httpAuthorizationFactory;
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
    public LoadFragment.Params parameter()
    {
        return new LoadFragment.Params()
        {
            @Override
            public Account account()
            {
                return mAccount;
            }


            @Override
            public HttpAuthorizationFactory httpAuthorizationFactory()
            {
                return mHttpAuthorizationFactory;
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
        dest.writeParcelable(mAccount, 0);
        dest.writeParcelable(mHttpAuthorizationFactory, 0);
    }


    public static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Boolean>
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
                            new ConvertedIterator<>(
                                    new SerialIteratorIterator<>(
                                            new ConvertedIterator<>(
                                                    new FilteredIterator<>(
                                                            mParams.account().provider().services(),
                                                            new AbstractFilteredIterator.IteratorFilter<Service>()
                                                            {
                                                                @Override
                                                                public boolean iterate(Service element)
                                                                {
                                                                    return "com.smoothsync.authenticate".equals(element.serviceType());
                                                                }
                                                            }),
                                                    new AbstractConvertedIterator.Converter<Iterator<Intent>, Service>()
                                                    {
                                                        @Override
                                                        public Iterator<Intent> convert(Service element)
                                                        {
                                                            return new IndirectServiceIntentIterable(context,
                                                                    new Intent(VerificationService.ACTION)
                                                                            .setData(Uri.fromParts(element.serviceType(), element.uri().toASCIIString(), null))
                                                                            .setPackage(context.getPackageName())).iterator();
                                                        }
                                                    })),
                                    new AbstractConvertedIterator.Converter<FutureServiceConnection<VerificationService>, Intent>()
                                    {
                                        @Override
                                        public FutureServiceConnection<VerificationService> convert(Intent element)
                                        {
                                            return new FutureLocalServiceConnection<>(context, element);
                                        }
                                    });
                    if (!serviceConnections.hasNext())
                    {
                        throw new RuntimeException("No verification service found");
                    }
                    return serviceConnections.next().service(1000).verify(mParams.account().provider(), mParams.httpAuthorizationFactory());
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
                                                new ForwardTransition<>(
                                                        new CreateAccountMicroFragment(mParams.account(), mParams.httpAuthorizationFactory()))));
                    }
                    else
                    {
                        mMicroFragmentEnvironment.host()
                                .execute(getActivity(),
                                        new XFaded(
                                                new ForwardTransition<>(
                                                        new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_authentication)))));
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


        interface Params
        {
            Account account();

            HttpAuthorizationFactory httpAuthorizationFactory();
        }
    }
}

/*
 * Copyright (c) 2018 dmfs GmbH
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
import com.smoothsync.smoothsetup.services.WizardService;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.StringMeta;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.bolts.service.elementary.FutureLocalServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardResetTransition;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.FragmentTransition;


/**
 * A {@link MicroFragment} to load a wizard.
 *
 * @author Marten Gajda
 */
public final class InitWizardMicroFragment implements MicroFragment<InitWizardMicroFragment.Params>
{
    public final static Creator<InitWizardMicroFragment> CREATOR = new Creator<InitWizardMicroFragment>()
    {
        @Override
        public InitWizardMicroFragment createFromParcel(Parcel source)
        {
            return new InitWizardMicroFragment(source.readString(), source.readParcelable(getClass().getClassLoader()));
        }


        @Override
        public InitWizardMicroFragment[] newArray(int size)
        {
            return new InitWizardMicroFragment[size];
        }
    };


    interface Params
    {
        String metaKey();

        Intent intent();
    }


    private final String mWizardServiceMetaKey;
    private final Intent mIntent;


    public InitWizardMicroFragment(String wizardServiceMetaKey, Intent intent)
    {
        mWizardServiceMetaKey = wizardServiceMetaKey;
        mIntent = intent;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        // this step should be fairly quick, showing a title for a few milliseconds could lead to disturbing UI glitches
        return "";
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
        return new LoadWizardFragment();
    }


    @NonNull
    @Override
    public Params parameter()
    {
        return new Params()
        {
            @Override
            public String metaKey()
            {
                return mWizardServiceMetaKey;
            }


            @Override
            public Intent intent()
            {
                return mIntent;
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
        dest.writeString(mWizardServiceMetaKey);
        dest.writeParcelable(mIntent, flags);
    }


    public final static class LoadWizardFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<MicroFragment<?>>
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private final Timestamp mTimestamp = new UiTimestamp();
        private final Runnable mShowWaitMessage = () -> getView().findViewById(android.R.id.message).animate().alpha(1f).start();
        private Handler mHandler = new Handler();
        private FutureServiceConnection<WizardService> mWizardService;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private FragmentTransition mFragmentTransition;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mWizardService = new FutureLocalServiceConnection<>(getContext(),
                    new Intent(
                            new StringMeta(getContext(), mMicroFragmentEnvironment.microFragment().parameter().metaKey()).value())
                            .setPackage(getContext().getPackageName()));
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
                new ThrowingAsyncTask<Void, Void, MicroFragment<?>>(this)
                {
                    @Override
                    protected MicroFragment<?> doInBackgroundWithException(Void... params) throws Exception
                    {
                        return mWizardService.service(1000).initialMicroFragment(getContext(), mMicroFragmentEnvironment.microFragment().parameter().intent());
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
            mWizardService.disconnect();
            super.onDestroy();
        }


        @Override
        public void onResult(AsyncTaskResult<MicroFragment<?>> result)
        {
            FragmentTransition transition;
            Context context = getContext();
            try
            {
                // Go to the next step
                // since this is always the first step and it's very fast, we don't use any animation for the transition
                transition = new ForwardTransition<>(result.value(), mTimestamp);
            }
            catch (Exception e)
            {
                transition = new ForwardResetTransition<>(new ErrorRetryMicroFragment("Unexpected Exception:\n\n" + e.getMessage()), mTimestamp);
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

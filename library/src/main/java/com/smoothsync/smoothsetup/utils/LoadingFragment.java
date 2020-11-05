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

package com.smoothsync.smoothsetup.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.FragmentTransition;
import org.dmfs.jems.function.Function;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * An abstract {@link Fragment} which executes a background operation for a result and proceeds with another {@link MicroFragment}.
 *
 * @param <Params>
 *         The type of the {@link MicroFragment} parameter.
 * @param <Result>
 *         The type of the result of the background operation.
 */
public abstract class LoadingFragment<Params, Result> extends Fragment implements ThrowingAsyncTask.OnResultCallback<Result>
{
    /**
     * The loader which is run in the background.
     *
     * @param <Params>
     * @param <Result>
     */
    public interface Loader<Params, Result>
    {
        Result result(Context context, MicroFragmentEnvironment<Params> env) throws Exception;
    }


    private final static int DELAY_WAIT_MESSAGE = 2500;
    private final Timestamp mTimestamp = new UiTimestamp();
    private FragmentTransition mFragmentTransition;

    private final Loader<Params, Result> mLoader;
    private final Function<AsyncTaskResult<Result>, FragmentTransition> mResultFunction;


    protected LoadingFragment(Loader<Params, Result> loader, Function<AsyncTaskResult<Result>, FragmentTransition> resultFunction)
    {
        mLoader = loader;
        mResultFunction = resultFunction;
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
        MicroFragmentEnvironment<Params> mMicroFragmentEnvironment = new FragmentEnvironment<>(this);

        if (mFragmentTransition != null)
        {
            // the operation completed in the background
            mMicroFragmentEnvironment.host().execute(getActivity(), mFragmentTransition);
        }
        else
        {
            new LoaderTask<>(getContext(), mMicroFragmentEnvironment, mLoader, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    @Override
    public void onResult(AsyncTaskResult<Result> result)
    {
        // override the transition timestamp with ours
        mFragmentTransition = new Timestamped(mTimestamp, mResultFunction.value(result));
        if (isResumed())
        {
            new FragmentEnvironment<>(this).host().execute(getContext(), mFragmentTransition);
            mFragmentTransition = null;
        }
    }


    private final static class LoaderTask<Params, Result> extends ThrowingAsyncTask<Void, Void, Result>
    {
        private final Context mContext;
        private final MicroFragmentEnvironment<Params> mMicroFragment;
        private final Loader<Params, Result> mLoader;


        public LoaderTask(Context context, MicroFragmentEnvironment<Params> microFragment, Loader<Params, Result> loader, ThrowingAsyncTask.OnResultCallback<Result> callback)
        {
            super(callback);
            mContext = context.getApplicationContext();
            mMicroFragment = microFragment;
            mLoader = loader;
        }


        @Override
        protected Result doInBackgroundWithException(Void... voids) throws Exception
        {
            return mLoader.result(mContext, mMicroFragment);
        }
    }
}
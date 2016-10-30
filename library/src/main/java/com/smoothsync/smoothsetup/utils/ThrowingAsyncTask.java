/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.utils;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;


/**
 * An AsyncTask that returns results and Exceptions of a background operation to a callback.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class ThrowingAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, AsyncTaskResult<Result>>
{
    public interface OnResultCallback<Result>
    {
        public void onResult(AsyncTaskResult<Result> result);
    }


    private final WeakReference<OnResultCallback<Result>> mCallbackReference;


    public ThrowingAsyncTask(OnResultCallback<Result> callback)
    {
        mCallbackReference = new WeakReference<OnResultCallback<Result>>(callback);
    }


    @Override
    protected final AsyncTaskResult<Result> doInBackground(Params... params)
    {
        try
        {
            return new ValueAsyncTaskResult<Result>(doInBackgroundWithException(params));
        }
        catch (Exception e)
        {
            return new ThrowingAsyncTaskResult<Result>(e);
        }
    }


    protected abstract Result doInBackgroundWithException(Params... params) throws Exception;


    @Override
    protected final void onPostExecute(AsyncTaskResult<Result> asyncTaskResult)
    {
        OnResultCallback<Result> callback = mCallbackReference.get();
        if (callback != null)
        {
            callback.onResult(asyncTaskResult);
        }
    }
}

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

package com.smoothsync.smoothsetup;

import android.os.AsyncTask;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.requests.ProviderGet;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.io.IOException;


/**
 * An {@link AsyncTask} that loads a Provider.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ProviderLoadTask extends ThrowingAsyncTask<String, Void, Provider>
{

    private final SmoothSyncApi mApi;


    public ProviderLoadTask(SmoothSyncApi api, OnResultCallback callback)
    {
        super(callback);
        mApi = api;
    }


    @Override
    protected Provider doInBackgroundWithException(String... params) throws ProtocolException, ProtocolError, IOException
    {
        return mApi.resultOf(new ProviderGet(params[0]));
    }
}

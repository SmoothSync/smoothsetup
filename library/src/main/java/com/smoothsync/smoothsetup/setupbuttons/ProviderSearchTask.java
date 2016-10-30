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

package com.smoothsync.smoothsetup.setupbuttons;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.requests.ProviderSearch;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;


/**
 * An ThrowingAsyncTask that searches providers by their domain.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ProviderSearchTask extends ThrowingAsyncTask<String, Void, List<Provider>>
{
    private final SmoothSyncApi mApi;


    public ProviderSearchTask(SmoothSyncApi api, OnResultCallback callback)
    {
        super(callback);
        mApi = api;
    }


    @Override
    protected List<Provider> doInBackgroundWithException(String... params) throws IOException, ProtocolException, ProtocolError
    {
        // don't try to hit the API if we can't resolve the hostname
        InetAddress address = InetAddress.getByName(params[0]);
        return mApi.resultOf(new ProviderSearch(params[0]));
    }
}

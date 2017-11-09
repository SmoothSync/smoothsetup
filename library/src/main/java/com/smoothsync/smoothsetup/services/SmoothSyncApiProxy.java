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

package com.smoothsync.smoothsetup.services;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.SmoothSyncApiRequest;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.bolts.service.exceptions.BindFailed;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * A {@link SmoothSyncApi} proxy that forwards all request to a {@link FutureServiceConnection}.
 *
 * @author Marten Gajda
 */
public final class SmoothSyncApiProxy implements SmoothSyncApi
{
    private final FutureServiceConnection<SmoothSyncApi> mConnection;


    public SmoothSyncApiProxy(FutureServiceConnection<SmoothSyncApi> connection)
    {
        this.mConnection = connection;
    }


    @Override
    public <T> T resultOf(SmoothSyncApiRequest<T> smoothSyncApiRequest) throws IOException, ProtocolError, ProtocolException
    {
        try
        {
            // Note, if the service takes longer than 2 seconds to connect there is something wrong
            return mConnection.service(2 * 1000L).resultOf(smoothSyncApiRequest);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException();
        }
        catch (TimeoutException e)
        {
            throw new RuntimeException("Couldn't connect to SmoothSyncApiService within a reasonable time.", e);
        }
        catch (BindFailed bindFailed)
        {
            throw new RuntimeException("Couldn't connect to SmoothSyncApiService.", bindFailed);
        }
    }
}

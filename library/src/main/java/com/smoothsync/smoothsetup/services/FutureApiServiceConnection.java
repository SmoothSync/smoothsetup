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

import android.content.Context;
import android.content.Intent;

import com.smoothsync.api.SmoothSyncApi;

import java.util.concurrent.TimeoutException;


/**
 * A {@link FutureServiceConnection} to the {@link SmoothSyncApi}.
 *
 * @author Marten Gajda
 */
public final class FutureApiServiceConnection implements FutureServiceConnection<SmoothSyncApi>
{
    private final FutureServiceConnection<SmoothSyncApi> mDelegate;


    public FutureApiServiceConnection(Context context)
    {
        mDelegate = new FutureLocalServiceConnection<>(context,
                new Intent("com.smoothsync.action.BIND_API").setPackage(context.getPackageName()));
    }


    @Override
    public boolean isConnected()
    {
        return mDelegate.isConnected();
    }


    @Override
    public SmoothSyncApi service(long timeout) throws TimeoutException, InterruptedException
    {
        return mDelegate.service(timeout);
    }


    @Override
    public void disconnect()
    {
        mDelegate.disconnect();
    }
}

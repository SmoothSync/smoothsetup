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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.TimeoutException;


/**
 * A {@link FutureServiceConnection} to connect to local services.
 *
 * @author Marten Gajda
 */
public final class FutureLocalServiceConnection<T> implements FutureServiceConnection<T>
{
    private final Context mContext;
    private final boolean mBindSucceeded;
    private boolean mIsConnected;
    private T mService;

    private final ServiceConnection mConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            synchronized (this)
            {
                mIsConnected = true;
                mService = (T) service;
                notify();
            }
        }


        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            synchronized (this)
            {
                mIsConnected = false;
                mService = null;
                notify();
            }
        }
    };


    /**
     * Binds the service identified by the given Intent.
     *
     * @param context
     *         A {@link Context}.
     * @param intent
     *         The {@link Intent} to bind the service.
     */
    public FutureLocalServiceConnection(Context context, Intent intent)
    {
        mContext = context.getApplicationContext();
        mBindSucceeded = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public boolean isConnected()
    {
        synchronized (mConnection)
        {
            return mIsConnected;
        }
    }


    @Override
    public T service(long timeout) throws TimeoutException, InterruptedException
    {
        synchronized (mConnection)
        {
            if (mIsConnected)
            {
                return mService;
            }

            long now = System.currentTimeMillis();
            long end = now + timeout;
            while (now < end)
            {
                mConnection.wait(end - now);
                if (mIsConnected)
                {
                    return mService;
                }
                now = System.currentTimeMillis();
            }
        }
        throw new TimeoutException();
    }


    @Override
    public void disconnect()
    {
        synchronized (mConnection)
        {
            if (mBindSucceeded)
            {
                mIsConnected = false;
                mContext.unbindService(mConnection);
            }
        }
    }
}
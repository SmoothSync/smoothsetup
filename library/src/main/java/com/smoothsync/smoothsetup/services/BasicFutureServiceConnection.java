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

package com.smoothsync.smoothsetup.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A basic implementation of {@link FutureServiceConnection}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class BasicFutureServiceConnection<T> implements FutureServiceConnection<T>, ServiceConnection
{
	private final Context mContext;
	private boolean mIsConnected;
	private T mService;


	/**
	 * Binds the service identified by the given Intent.
	 * 
	 * @param context
	 *            A {@link Context}.
	 * @param intent
	 *            The {@link Intent} to bind the service.
	 */
	public BasicFutureServiceConnection(Context context, Intent intent)
	{
		mContext = context.getApplicationContext();
		mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}


	@Override
	public boolean isConnected()
	{
		return mIsConnected;
	}


	@Override
	public T service() throws InterruptedException
	{
		try
		{
			return service(TimeUnit.DAYS.toMillis(100000));
		}
		catch (TimeoutException e)
		{
			throw new RuntimeException("");
		}
	}


	@Override
	public T service(long timeout) throws TimeoutException, InterruptedException
	{
		synchronized (this)
		{
			if (mIsConnected)
			{
				return mService;
			}

			long now = System.currentTimeMillis();
			long end = now + timeout;
			while (now < end)
			{
				wait(end - now);
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
		mContext.unbindService(this);
	}


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
}

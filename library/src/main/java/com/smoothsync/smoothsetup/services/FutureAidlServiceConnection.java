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

import java.util.concurrent.TimeoutException;


/**
 * An implementation of {@link FutureServiceConnection} to connect aidl based services.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class FutureAidlServiceConnection<T extends android.os.IInterface> implements FutureServiceConnection<T>
{
	public interface StubProxy<T extends android.os.IInterface>
	{
		/**
		 * Returns a stub object that connects to the service.
		 *
		 * This method needs to contain only one line like:
		 * 
		 * <pre>
		 * <code>
		 * return &lt;T>.Stub.asInterface(service);
		 * </code>
		 * </pre>
		 * 
		 * Where &lt;T> is the interface of the service.
		 *
		 * @param service
		 * @return
		 */
		T asInterface(IBinder service);
	}

	private final Context mContext;
	private final StubProxy<T> mStubProxy;
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
				mService = mStubProxy.asInterface(service);
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
	 *            A {@link Context}.
	 * @param intent
	 *            The {@link Intent} to bind the service.
	 * @param stubProxy
	 *            A StubProxy to convert the IBinder to the service interface.
	 */
	public FutureAidlServiceConnection(Context context, Intent intent, StubProxy<T> stubProxy)
	{
		mContext = context.getApplicationContext();
		mStubProxy = stubProxy;
		mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}


	@Override
	public boolean isConnected()
	{
		return mIsConnected;
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
		mContext.unbindService(mConnection);
	}

}

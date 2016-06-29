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

import android.os.AsyncTask;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.requests.ProviderSearchRequest;

import org.dmfs.httpclient.exceptions.ProtocolError;
import org.dmfs.httpclient.exceptions.ProtocolException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;


/**
 * Created by marten on 12.06.16.
 */
public final class ProviderSearchTask extends AsyncTask<String, Void, List<Provider>>
{
	public interface LoaderCallback
	{
		public void onLoad(List<Provider> providers);
	}

	private final LoaderCallback mLoader;
	private final SmoothSyncApi mApi;


	public ProviderSearchTask(SmoothSyncApi api, LoaderCallback loader)
	{
		mApi = api;
		mLoader = loader;
	}


	@Override
	protected List<Provider> doInBackground(String... params)
	{
		try
		{
			// don't try to hit the API if we can't resolve the hostname
			InetAddress address = InetAddress.getByName(params[0]);
			return mApi.resultOf(new ProviderSearchRequest(params[0]));
		}
		catch (IOException | ProtocolException | ProtocolError e)
		{
			return Collections.EMPTY_LIST;
		}
	}


	@Override
	protected void onPostExecute(List<Provider> providers)
	{
		mLoader.onLoad(providers);
	}
}

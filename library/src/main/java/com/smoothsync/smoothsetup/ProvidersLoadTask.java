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

package com.smoothsync.smoothsetup;

import android.os.AsyncTask;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.requests.ProvidersRequest;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * An {@link AsyncTask} that loads all Providers this client has access to.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ProvidersLoadTask extends AsyncTask<String, Void, List<Provider>>
{
	public interface LoaderCallback
	{
		public void onLoad(List<Provider> providers);
	}

	private final WeakReference<LoaderCallback> mLoaderReference;
	private final SmoothSyncApi mApi;


	public ProvidersLoadTask(SmoothSyncApi api, LoaderCallback loader)
	{
		mApi = api;
		mLoaderReference = new WeakReference<LoaderCallback>(loader);
	}


	@Override
	protected List<Provider> doInBackground(String... params)
	{
		try
		{
			return mApi.resultOf(new ProvidersRequest());
		}
		catch (Exception e)
		{
			return null;
		}
	}


	@Override
	protected void onPostExecute(List<Provider> providers)
	{
		LoaderCallback loader = mLoaderReference.get();
		if (loader != null)
		{
			loader.onLoad(providers);
		}
	}
}

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

package com.smoothsync.smoothsetup.autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.AutoCompleteResult;

import android.util.LruCache;
import android.widget.Filter;


/**
 * An auto-complete adapter that's backed by the SmoothSync API.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ApiAutoCompleteAdapter extends AbstractAutoCompleteAdapter
{
	private final static int MAX_RESULTS = 10;

	private final List<String> mValues;
	private final Filter mFilter = new ResultFilter();
	private final LruCache<String, AutoCompleteResult> mResultCache;


	/**
	 * Creates an auto-complete adapter using the given {@link SmoothSyncApi} instance.
	 * 
	 * @param api
	 *            A SmoothSyncApi instance.
	 */
	public ApiAutoCompleteAdapter(SmoothSyncApi api)
	{
		mResultCache = new AutoCompleteLruCache(api, 64);
		mValues = Collections.synchronizedList(new ArrayList<String>(MAX_RESULTS));
	}


	@Override
	public int getCount()
	{
		return mValues.size();
	}


	@Override
	public String getItem(int position)
	{
		return mValues.get(position);
	}


	@Override
	public Filter getFilter()
	{
		return mFilter;
	}

	private final class ResultFilter extends Filter
	{

		@Override
		protected FilterResults performFiltering(final CharSequence prefix)
		{
			FilterResults results = new FilterResults();

			// by default we don't have any results;
			results.values = Collections.emptyList();
			results.count = 0;

			if (prefix == null || prefix.length() == 0)
			{
				return results;
			}

			String prefixStr = prefix.toString();
			final int atPos = prefixStr.indexOf('@');
			if (atPos < 0 || atPos == prefixStr.length() - 1)
			{
				// no domain, no results either
				return results;
			}
			String localPart = prefixStr.substring(0, atPos);
			String domainPart = prefixStr.substring(atPos + 1);

			// fetch the auto-complete result
			AutoCompleteResult autoCompleteResult = mResultCache.get(domainPart);

			if (domainPart.length() < 2)
			{
				// require at least two characters of the domain before presenting auto-completion results.
				// note: we still run the request with a single char prior to this check, to cache the result as early as possible
				return results;
			}

			if (autoCompleteResult == null)
			{
				// no result, no results either
				return results;
			}

			Iterator<String> domainIterator = new AutoCompleteArrayIterator(autoCompleteResult.autoComplete(), localPart, domainPart);
			List values = new ArrayList(autoCompleteResult.autoComplete().length);
			while (domainIterator.hasNext())
			{
				values.add(domainIterator.next());
			}

			if (values.size() == 1 && values.contains(prefixStr) || values.size() > MAX_RESULTS)
			{
				// only one result that matches exactly or there are too many result, don't show anything
				return results;
			}

			results.values = values;
			results.count = values.size();

			return results;
		}


		@Override
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			mValues.clear();

			if (results != null && results.values != null)
			{
				mValues.addAll((List<String>) results.values);
			}

			notifyDataSetChanged();
		}
	}

}

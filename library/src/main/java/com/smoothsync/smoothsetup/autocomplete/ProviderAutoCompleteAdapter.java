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

import android.widget.Filter;

import com.smoothsync.api.model.Provider;

import org.dmfs.httpclient.exceptions.ProtocolException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * An auto-complete adapter that supports a single provider only.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ProviderAutoCompleteAdapter extends AbstractAutoCompleteAdapter
{
	private final Provider mProvider;
	private final List<String> mValues;
	private final Filter mFilter = new ArrayFilter();


	/**
	 * Creates an auto-complete adapter that completes to the domains of the given provider.
	 * 
	 * @param provider
	 */
	public ProviderAutoCompleteAdapter(Provider provider)
	{
		mProvider = provider;
		mValues = Collections.synchronizedList(new ArrayList<String>(16));
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

	private final class ArrayFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence prefix)
		{
			FilterResults results = new FilterResults();
			results.values = Collections.emptyList();
			results.count = 0;

			if (prefix == null || prefix.length() == 0)
			{
				// no prefix, no results
				return results;
			}

			String prefixStr = prefix.toString();
			final int atPos = prefixStr.indexOf('@');

			if (atPos < 0)
			{
				// no prefix, no results
				return results;
			}

			String localPart = prefixStr.substring(0, atPos);
			String domainPart = prefixStr.substring(atPos + 1);

			try
			{
				Iterator<String> domainIterator = new AutoCompleteArrayIterator(mProvider.domains(), localPart, domainPart);
				List result = new ArrayList(mProvider.domains().length);
				while (domainIterator.hasNext())
				{
					result.add(domainIterator.next());
				}

				if (result.size() == 1 && result.contains(prefixStr))
				{
					return results;
				}

				results.values = result;
				results.count = result.size();
			}
			catch (ProtocolException e)
			{
				// no response, no results either
				results.values = new String[0];
				results.count = 0;
				return results;
			}

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

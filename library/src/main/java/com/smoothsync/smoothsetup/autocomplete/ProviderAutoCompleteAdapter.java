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

package com.smoothsync.smoothsetup.autocomplete;

import android.util.LruCache;
import android.widget.Filter;

import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.AutoCompleteIterable;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.jems.single.elementary.Collected;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;


/**
 * An auto-complete adapter that's backed by the SmoothSync API.
 *
 * @author Marten Gajda
 */
public final class ProviderAutoCompleteAdapter extends AbstractAutoCompleteAdapter
{
    private final static int MAX_RESULTS = 5;

    private final Filter mFilter = new ResultFilter();
    private final List<AutoCompleteItem> mValues;
    private final LruCache<String, List<String>> mResultCache;


    /**
     * Creates an auto-complete adapter using the given {@link ProviderService} instance.
     *
     * @param providerService
     *         A {@link Single} {@link ProviderService}.
     */
    public ProviderAutoCompleteAdapter(Single<ProviderService> providerService)
    {
        mResultCache = new AutoCompleteLruCache(providerService, 64);
        mValues = Collections.synchronizedList(new ArrayList<AutoCompleteItem>(MAX_RESULTS));
    }


    @Override
    public int getCount()
    {
        return mValues.size();
    }


    @Override
    public AutoCompleteItem getItem(int position)
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
                // no prefix, no results
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
            List<String> autoCompleteResult = mResultCache.get(domainPart);

            if (autoCompleteResult == null)
            {
                // no result, no results either
                return results;
            }

            List<String> values = new Collected<>(ArrayList::new, new AutoCompleteArrayIterable(autoCompleteResult, localPart, domainPart)).value();

            if (values.contains(prefixStr))
            {
                // don't show autocomplete if we have an exact result
                return results;
            }

            if (values.size() <= MAX_RESULTS)
            {
                List<AutoCompleteItem> result = new ArrayList<>(values.size());
                for (String value : values)
                {
                    result.add(new AutoCompleteItem()
                    {
                        @Override
                        public String autoComplete()
                        {
                            return value;
                        }


                        @Override
                        public Iterable<String> extensions()
                        {
                            return EmptyIterable.instance();
                        }
                    });
                }
                results.values = result;
                results.count = result.size();
            }
            else
            {
                // try to find common prefixes, first sort the list
                Collections.sort(values);
                List<AutoCompleteItem> result = new ArrayList<>(values.size());
                for (AutoCompleteItem item : new AutoCompleteIterable(values, prefixStr))
                {
                    result.add(item);
                }
                results.values = result;
                results.count = result.size();

            }

            return results;
        }


        @Override
        public CharSequence convertResultToString(Object resultValue)
        {
            return ((AutoCompleteItem) resultValue).autoComplete();
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            mValues.clear();

            if (results != null && results.values != null)
            {
                mValues.addAll((List<AutoCompleteItem>) results.values);
            }

            notifyDataSetChanged();
        }
    }

}

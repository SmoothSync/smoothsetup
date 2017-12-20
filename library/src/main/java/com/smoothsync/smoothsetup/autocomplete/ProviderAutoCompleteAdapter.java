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

import android.widget.Filter;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.utils.AutoCompleteIterable;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.iterables.EmptyIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An auto-complete adapter that supports a single provider only.
 * <p>
 * TODO: consolidate with {@link ApiAutoCompleteAdapter}
 *
 * @author Marten Gajda
 */
public final class ProviderAutoCompleteAdapter extends AbstractAutoCompleteAdapter
{
    private final static int MAX_RESULTS = 5;

    private final Filter mFilter = new ResultFilter();
    private final Provider mProvider;
    private final List<AutoCompleteItem> mValues;


    /**
     * Creates an auto-complete adapter that completes to the domains of the given provider.
     *
     * @param provider
     */
    public ProviderAutoCompleteAdapter(Provider provider)
    {
        mProvider = provider;
        mValues = Collections.synchronizedList(new ArrayList<AutoCompleteItem>(16));
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
        protected FilterResults performFiltering(CharSequence prefix)
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
            if (atPos < 0)
            {
                // no prefix, no results
                return results;
            }
            String localPart = prefixStr.substring(0, atPos);
            String domainPart = prefixStr.substring(atPos + 1);

            try
            {
                List<String> values = new ArrayList<>(mProvider.domains().length);
                for (String autoComplete : new AutoCompleteArrayIterable(mProvider.domains(), localPart, domainPart))
                {
                    values.add(autoComplete);
                }

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

            }
            catch (ProtocolException e)
            {
                // no response, no results either
                results.values = Collections.emptyList();
                results.count = 0;
                return results;
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

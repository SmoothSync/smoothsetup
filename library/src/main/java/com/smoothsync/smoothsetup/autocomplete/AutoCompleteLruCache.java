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

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.AutoCompleteResult;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;


/**
 * A {@link LruCache} of {@link AutoCompleteResult}s.
 * <p>
 * Note: The cache tries to reduce network traffic and thus may return responses that contain more results than they should. Make sure you always filter the
 * result unsing an {@link AutoCompleteArrayIterable}.
 *
 * @author Marten Gajda
 */
public final class AutoCompleteLruCache extends LruCache<String, List<String>>
{
    private final Single<ProviderService> mProviderService;


    /**
     * @param providerService
     *         A {@link SmoothSyncApi}.
     * @param maxSize
     *         for caches that do not override {@link #sizeOf}, this is the maximum number of entries in the cache. For all other caches, this is the maximum
     *         sum of the sizes of the entries in this cache.
     */
    public AutoCompleteLruCache(Single<ProviderService> providerService, int maxSize)
    {
        super(maxSize);
        mProviderService = providerService;
    }


    @Override
    protected List<String> create(String key)
    {
        try
        {
            // if the key doesn't have a . at the last position, we can use the result of the key minus one char and filter on the client
            if (key.length() > 1 && key.charAt(key.length() - 1) != '.')
            {
                return get(key.substring(0, key.length() - 1));
            }
            return mProviderService
                    .flatMapObservable(s -> s.autoComplete(key))
                    .collect((Supplier<ArrayList<String>>) ArrayList::new, ArrayList::add).blockingGet();
        }
        catch (Exception e)
        {
            // ignore any error and just don't auto-complete
            return null;
        }
    }
}

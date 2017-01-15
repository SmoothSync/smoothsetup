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
import com.smoothsync.api.requests.AutoComplete;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.io.IOException;


/**
 * A {@link LruCache} of {@link AutoCompleteResult}s.
 * <p>
 * Note: The cache tries to reduce network traffic and thus may return responses that contain more results than they should. Make sure you always filter the
 * result unsing an {@link AutoCompleteArrayIterator}.
 */
public final class AutoCompleteLruCache extends LruCache<String, AutoCompleteResult>
{
    private final SmoothSyncApi mApi;


    /**
     * @param api
     *         A {@link SmoothSyncApi}.
     * @param maxSize
     *         for caches that do not override {@link #sizeOf}, this is the maximum number of entries in the cache. For all other caches, this is the maximum
     *         sum of the sizes of the entries in this cache.
     */
    public AutoCompleteLruCache(SmoothSyncApi api, int maxSize)
    {
        super(maxSize);
        mApi = api;
    }


    @Override
    protected AutoCompleteResult create(String key)
    {
        try
        {
            // if the key doesn't have a . at the last position, we can use the result of the key minus one char and filter on the client
            if (key.length() > 2 && key.charAt(key.length() - 1) != '.')
            {
                return get(key.substring(0, key.length() - 1));
            }
            return mApi.resultOf(new AutoComplete(key));
        }
        catch (IOException | ProtocolException | ProtocolError e)
        {
            // ignore any error and just don't auto-complete
            return null;
        }
    }
}

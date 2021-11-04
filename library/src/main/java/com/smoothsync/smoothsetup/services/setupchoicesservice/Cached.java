/*
 * Copyright (c) 2021 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.setupchoicesservice;

import android.util.Log;
import android.util.LruCache;

import com.smoothsync.smoothsetup.services.SetupChoiceService;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public final class Cached implements SetupChoiceService
{
    private final SetupChoiceService mDelegate;

    private final LruCache<String, Iterable<String>> autoCompleteCache = new LruCache<>(50);

    private final LruCache<String, Iterable<SetupChoice>> choicesCache = new LruCache<>(50);


    public Cached(@NonNull SetupChoiceService delegate)
    {
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public Flowable<Iterable<String>> autoComplete(@NonNull String name)
    {
        Iterable<String> result = autoCompleteCache.get(name);

        if (result != null)
        {
            return Flowable.just(result);
        }
        else
        {
            Flowable<Iterable<String>> response = mDelegate.autoComplete(name).cache().doOnNext(l-> Log.v("333333", ""+l));
            // only cache the very last element
            response.lastElement().observeOn(Schedulers.computation()).subscribe(r -> autoCompleteCache.put(name, r), error -> { /* ignore errors */});
            return response;
        }
    }


    @NonNull
    @Override
    public Flowable<Iterable<SetupChoice>> choices(@NonNull String domain)
    {
        Iterable<SetupChoice> result = choicesCache.get(domain);

        if (result != null)
        {
            return Flowable.just(result);
        }
        else
        {
            Flowable<Iterable<SetupChoice>> response = mDelegate.choices(domain).cache();
            // only cache the very last element
            response.lastElement().observeOn(Schedulers.computation()).subscribe(r -> choicesCache.put(domain, r), error -> { /* ignore errors */});
            return response;
        }
    }
}

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

import com.smoothsync.smoothsetup.services.SetupChoiceService;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;

import static org.dmfs.jems2.iterable.EmptyIterable.emptyIterable;


public final class Slow implements SetupChoiceService
{
    private final SetupChoiceService mDelegate;


    public Slow(@NonNull SetupChoiceService delegate)
    {
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public Flowable<Iterable<String>> autoComplete(@NonNull String name)
    {
        return Flowable.<Iterable<String>>just(emptyIterable()).concatWith(mDelegate.autoComplete(name)).doOnNext(l -> Log.v("22222222222222", "" + l));
    }


    @NonNull
    @Override
    public Flowable<Iterable<SetupChoice>> choices(@NonNull String domain)
    {
        return Flowable.<Iterable<SetupChoice>>just(emptyIterable()).concatWith(mDelegate.choices(domain));

    }
}
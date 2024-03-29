/*
 * Copyright (c) 2020 dmfs GmbH
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

package com.smoothsync.smoothsetup.utils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;


public abstract class DelegatingFlowable<T> extends Flowable<T>
{
    private final Publisher<T> mDelegate;


    public DelegatingFlowable(Publisher<T> mDelegate)
    {
        this.mDelegate = mDelegate;
    }


    @Override
    protected void subscribeActual(@NonNull Subscriber<? super T> subscriber)
    {
        mDelegate.subscribe(subscriber);
    }
}
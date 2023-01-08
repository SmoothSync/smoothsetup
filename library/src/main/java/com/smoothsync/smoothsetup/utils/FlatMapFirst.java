/*
 * Copyright (c) 2023 dmfs GmbH
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

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.functions.Function;


public final class FlatMapFirst<Upstream, Downstream> implements FlowableTransformer<Upstream, Downstream>
{
    private static final Exception TERMINATE = new Exception();

    private final Function<? super Upstream, ? extends Flowable<Downstream>> mFunction;


    public FlatMapFirst(Function<? super Upstream, ? extends Flowable<Downstream>> function)
    {
        mFunction = function;
    }


    @Override
    public @NonNull Publisher<Downstream> apply(@NonNull Flowable<Upstream> upstream)
    {
        return upstream.flatMap(up -> mFunction.apply(up).concatWith(Completable.error(TERMINATE)))
            .onErrorComplete(TERMINATE::equals);
    }
}

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

import com.smoothsync.smoothsetup.services.SetupChoiceService;

import org.dmfs.jems2.iterable.Distinct;
import org.dmfs.jems2.iterable.Joined;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Seq;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;


public final class Composite implements SetupChoiceService
{
    private final Iterable<SetupChoiceService> mDelegates;


    public Composite(@NonNull SetupChoiceService... delegates)
    {
        this(new Seq<>(delegates));
    }


    public Composite(@NonNull Iterable<SetupChoiceService> delegates)
    {
        mDelegates = delegates;
    }


    @NonNull
    @Override
    public Flowable<Iterable<String>> autoComplete(@NonNull String name)
    {
        return Flowable.combineLatest(
            new Mapped<>(d -> d.autoComplete(name), mDelegates),
            choices -> new Distinct<>(new Joined<>(new Mapped<>(o -> ((Iterable<String>) o), new Seq<>(choices)))));
    }


    @NonNull
    @Override
    public Flowable<Iterable<SetupChoice>> choices(@NonNull String domain)
    {
        return Flowable.combineLatest(
            new Mapped<>(d -> d.choices(domain), mDelegates),
            choices -> new Distinct<>(SetupChoice::id, new Joined<>(new Mapped<>(o -> ((Iterable<SetupChoice>) o), new Seq<>(choices)))));
    }
}

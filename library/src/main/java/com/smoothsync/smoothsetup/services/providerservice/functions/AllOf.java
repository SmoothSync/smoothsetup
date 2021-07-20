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

package com.smoothsync.smoothsetup.services.providerservice.functions;

import android.content.Context;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;

import org.dmfs.jems2.Function;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Seq;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;


/**
 * A {@link ProviderService} {@link Function} to combine the results of other {@link ProviderService} {@link Function}s.
 */
public final class AllOf implements Function<Context, ProviderService>
{
    private final Iterable<? extends Function<? super Context, ? extends ProviderService>> mDelegates;


    @SafeVarargs
    public AllOf(Function<? super Context, ? extends ProviderService>... delegates)
    {
        this(new Seq<>(delegates));
    }


    public AllOf(Iterable<? extends Function<? super Context, ? extends ProviderService>> delegates)
    {
        mDelegates = delegates;
    }


    @Override
    public ProviderService value(Context context)
    {
        Iterable<ProviderService> services = new Mapped<>(f -> f.value(context), mDelegates);
        return new ProviderService()
        {
            @Override
            public Maybe<Provider> byId(String id)
            {
                return Maybe.concat(new Mapped<>(s -> s.byId(id), services)).firstElement();
            }


            @Override
            public Observable<Provider> byDomain(String domain)
            {
                return Observable.concat(new Mapped<>(s -> s.byDomain(domain), services)).distinct(Provider::id);
            }


            @Override
            public Observable<Provider> all()
            {
                return Observable.concat(new Mapped<>(ProviderService::all, services)).distinct(Provider::id);
            }


            @Override
            public Observable<String> autoComplete(String domainFragment)
            {
                return Observable.concat(new Mapped<>(s -> s.autoComplete(domainFragment), services)).distinct();
            }
        };
    }
}

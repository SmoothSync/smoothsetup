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

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.AutoCompleteResult;
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.requests.AutoComplete;
import com.smoothsync.api.requests.ProviderGet;
import com.smoothsync.api.requests.ProviderMultiget;
import com.smoothsync.api.requests.ProviderSearch;
import com.smoothsync.smoothsetup.services.binders.ApiServiceBinder;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.FlatMapFirst;
import com.smoothsync.smoothsetup.utils.WithIdPrefix;

import org.dmfs.httpessentials.exceptions.NotFoundException;
import org.dmfs.jems2.Function;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * A {@link ProviderService} {@link Function} to return results from the SmoothSync API.
 */
public final class ApiProviders implements Function<Context, ProviderService>
{
    public final static String PREFIX = "com.smoothsync.api:";
    private final io.reactivex.rxjava3.functions.Function<? super Provider, ? extends Provider> prefixFunction =
        provider -> new WithIdPrefix(PREFIX, provider);


    @Override
    public ProviderService value(Context context)
    {
        Flowable<SmoothSyncApi> apiService = new ApiServiceBinder(context);
        return new ProviderService()
        {
            @Override
            public Maybe<Provider> byId(String id)
            {
                return apiService
                    .observeOn(Schedulers.io())
                    .filter(ignored -> id.startsWith(PREFIX) || !id.startsWith("com.smoothsync") /* for backwards compatibility */)
                    .compose(new FlatMapFirst<>(
                        api -> Flowable.fromCallable(() -> api.resultOf(new ProviderGet(id.startsWith(PREFIX) ? id.substring(PREFIX.length()) : id)))
                            .subscribeOn(Schedulers.io())
                            .onErrorComplete(NotFoundException.class::isInstance)
                    ))
                    .firstElement()
                    .map(prefixFunction);
            }


            @Override
            public Observable<Provider> byDomain(String domain)
            {
                return apiService
                    .observeOn(Schedulers.io())
                    .compose(new FlatMapFirst<>(api -> Flowable.fromCallable(() -> api.resultOf(new ProviderSearch(domain)))
                        .subscribeOn(Schedulers.io())))
                    .flatMapIterable(list -> list)
                    .toObservable()
                    .map(prefixFunction);
            }


            @Override
            public Observable<Provider> all()
            {
                return apiService
                    .compose(new FlatMapFirst<>(api -> Flowable.fromCallable(() -> api.resultOf(new ProviderMultiget()))
                        .subscribeOn(Schedulers.io())))
                    .flatMapIterable(list -> list)
                    .toObservable()
                    .map(prefixFunction);
            }


            @Override
            public Observable<String> autoComplete(String domainFragment)
            {
                return apiService
                    .compose(new FlatMapFirst<>(api -> Flowable.fromCallable(() -> api.resultOf(new AutoComplete(domainFragment)))
                        .subscribeOn(Schedulers.io())))
                    .flatMapIterable(AutoCompleteResult::autoComplete)
                    .toObservable();
            }
        };
    }
}

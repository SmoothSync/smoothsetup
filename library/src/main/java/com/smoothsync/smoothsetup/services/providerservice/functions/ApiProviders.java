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
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.requests.AutoComplete;
import com.smoothsync.api.requests.ProviderGet;
import com.smoothsync.api.requests.ProviderMultiget;
import com.smoothsync.api.requests.ProviderSearch;
import com.smoothsync.smoothsetup.services.binders.ApiServiceBinder;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.services.providerservice.WithIdPrefix;

import org.dmfs.httpessentials.exceptions.NotFoundException;
import org.dmfs.jems.function.Function;
import org.dmfs.jems.iterable.elementary.Seq;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
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
        Single<SmoothSyncApi> apiSingle = new ApiServiceBinder(context).wrapped();
        return new ProviderService()
        {
            @Override
            public Maybe<Provider> byId(String id)
            {
                return apiSingle
                        .observeOn(Schedulers.io())
                        .filter(ignored -> id.startsWith(PREFIX) || !id.startsWith("com.smoothsync") /* for backwards compatibility */)
                        .flatMap(api -> (MaybeSource<Provider>) observer -> {
                            try
                            {
                                observer.onSuccess(api.resultOf(new ProviderGet(id.startsWith(PREFIX) ? id.substring(PREFIX.length()) : id)));
                            }
                            catch (NotFoundException e)
                            {
                                observer.onComplete();
                            }
                            catch (Exception exception)
                            {
                                observer.onError(exception);
                            }
                        })
                        .map(prefixFunction);
            }


            @Override
            public Observable<Provider> byDomain(String domain)
            {
                return apiSingle
                        .observeOn(Schedulers.io())
                        .flattenAsObservable(api -> api.resultOf(new ProviderSearch(domain)))
                        .map(prefixFunction);
            }


            @Override
            public Observable<Provider> all()
            {
                return apiSingle
                        .observeOn(Schedulers.io())
                        .flattenAsObservable(api -> api.resultOf(new ProviderMultiget()))
                        .map(prefixFunction);
            }


            @Override
            public Observable<String> autoComplete(String domainFragment)
            {
                return apiSingle
                        .observeOn(Schedulers.io())
                        .map(api -> api.resultOf(new AutoComplete(domainFragment)))
                        .flattenAsObservable(result -> new Seq<>(result.autoComplete()));
            }
        };
    }
}

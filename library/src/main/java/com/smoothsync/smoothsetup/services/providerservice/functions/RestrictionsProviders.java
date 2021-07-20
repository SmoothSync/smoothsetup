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
import android.os.Build;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.restrictions.ProviderRestrictions;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.WithIdPrefix;

import org.dmfs.jems2.Function;
import org.dmfs.jems2.Predicate;
import org.dmfs.jems2.iterable.Seq;
import org.dmfs.jems2.optional.First;
import org.dmfs.jems2.predicate.AnyOf;

import androidx.annotation.RequiresApi;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;


/**
 * A {@link ProviderService} {@link Function} to return the providers configured in the restrictions.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public final class RestrictionsProviders implements Function<Context, ProviderService>
{
    public final static String PREFIX = "com.smoothsync.restrictions:";
    private final io.reactivex.rxjava3.functions.Function<? super Provider, ? extends Provider> prefixFunction =
            provider -> new WithIdPrefix(PREFIX, provider);


    @Override
    public ProviderService value(Context context)
    {
        Observable<Provider> restrictionProviders = new ProviderRestrictions(context).wrapped().cache();
        return new ProviderService()
        {
            @Override
            public Maybe<Provider> byId(String id)
            {
                return restrictionProviders.filter(provider -> new AnyOf<>(id, PREFIX + id).satisfiedBy(provider.id()))
                        .firstElement()
                        .map(prefixFunction);
            }


            @Override
            public Observable<Provider> byDomain(String domain)
            {
                return restrictionProviders.filter(
                        provider -> new First<>(
                                (Predicate<? super String>) domain::equals,
                                new Seq<>(provider.domains())).isPresent())
                        .map(prefixFunction);
            }


            @Override
            public Observable<Provider> all()
            {
                return restrictionProviders
                        .map(prefixFunction);
            }


            @Override
            public Observable<String> autoComplete(String domainFragment)
            {
                return restrictionProviders
                        .flatMapIterable(p -> new Seq<>(p.domains()))
                        .filter(d -> toString().startsWith(domainFragment));
            }
        };
    }
}

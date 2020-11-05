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

import org.dmfs.jems.function.Function;
import org.dmfs.jems.iterable.elementary.Seq;
import org.dmfs.jems.optional.adapters.First;
import org.dmfs.jems.predicate.Predicate;

import androidx.annotation.RequiresApi;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;


/**
 * A {@link ProviderService} {@link Function} to return the providers configured in the restrictions.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public final class RestrictionsProviders implements Function<Context, ProviderService>
{

    @Override
    public ProviderService value(Context context)
    {
        Observable<Provider> restrictionProviders = new ProviderRestrictions(context).wrapped().cache();
        return new ProviderService()
        {
            @Override
            public Maybe<Provider> byId(String id)
            {
                return restrictionProviders.filter(provider -> id.equals(provider.id())).firstElement();
            }


            @Override
            public Observable<Provider> byDomain(String domain)
            {
                return restrictionProviders.filter(
                        provider -> new First<>(
                                new Seq<>(provider.domains()),
                                (Predicate<? super String>) domain::equals).isPresent());
            }


            @Override
            public Observable<Provider> all()
            {
                return restrictionProviders;
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

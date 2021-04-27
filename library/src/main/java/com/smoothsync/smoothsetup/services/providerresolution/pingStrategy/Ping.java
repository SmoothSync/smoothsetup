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

package com.smoothsync.smoothsetup.services.providerresolution.pingStrategy;

import com.smoothsync.api.model.Provider;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.functions.Predicate;


/**
 * A {@link PingStrategy} that pings a {@link Provider} if it matches a given {@link Predicate}.
 */
public final class Ping implements PingStrategy
{
    private final Predicate<Provider> mProviderPredicate;


    public Ping(@NonNull String providerPrefix)
    {
        this(provider -> provider.id().startsWith(providerPrefix));
    }


    public Ping(@NonNull Predicate<Provider> providerPredicate)
    {
        mProviderPredicate = providerPredicate;
    }


    @NonNull
    @Override
    public Maybe<Provider> pingProvider(@NonNull Provider provider)
    {
        return Maybe.just(provider)
                .filter(mProviderPredicate);
    }
}

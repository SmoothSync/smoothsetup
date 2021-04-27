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
import com.smoothsync.api.model.impl.JsonObjectArrayIterator;
import com.smoothsync.api.model.impl.JsonProvider;
import com.smoothsync.smoothsetup.providerdata.ProviderData;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.WithIdPrefix;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.iterables.decorators.Sieved;
import org.dmfs.jems.function.Function;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.predicate.Predicate;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;


/**
 * A {@link ProviderService} {@link Function} to statically configured providers.
 */
public final class StaticProviders implements Function<Context, ProviderService>
{
    public final static String PREFIX = "com.smoothsync.static:";
    private final io.reactivex.rxjava3.functions.Function<? super Provider, ? extends Provider> prefixFunction =
            provider -> new WithIdPrefix(PREFIX, provider);

    private final ProviderData mData;


    public StaticProviders(ProviderData data)
    {
        mData = data;
    }


    @Override
    public ProviderService value(Context context)
    {
        try
        {
            JSONObject data = mData.providerData(context);
            return new ProviderService()
            {
                @Override
                public Maybe<Provider> byId(String id)
                {
                    return Observable.fromIterable(
                            new Mapped<JSONObject, Provider>(
                                    JsonProvider::new,
                                    new Sieved<>(
                                            (Predicate<? super JSONObject>) o -> id.equals(o.optString("id"))
                                                    || (PREFIX + id).equals(o.optString("id")),
                                            () -> new JsonObjectArrayIterator(data.optJSONArray("providers")))))
                            .firstElement()
                            .map(prefixFunction);
                }


                @Override
                public Observable<Provider> byDomain(String domain)
                {
                    // TODO
                    return Observable.fromIterable(new EmptyIterable<>());
                }


                @Override
                public Observable<Provider> all()
                {
                    return Observable.fromIterable(
                            new Mapped<>(
                                    JsonProvider::new,
                                    () -> new JsonObjectArrayIterator(data.optJSONArray("providers"))))
                            .map(prefixFunction);
                }


                @Override
                public Observable<String> autoComplete(String domainFragment)
                {
                    // TODO
                    return Observable.fromIterable(new EmptyIterable<>());
                }
            };
        }
        catch (IOException | JSONException e)
        {
            throw new RuntimeException("Can't read static provider data", e);
        }
    }
}

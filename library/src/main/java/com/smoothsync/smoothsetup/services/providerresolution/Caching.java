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

package com.smoothsync.smoothsetup.services.providerresolution;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.impl.JsonProvider;
import com.smoothsync.smoothsetup.services.providerservice.functions.ApiProviders;
import com.smoothsync.smoothsetup.services.providerservice.functions.ManualProviders;
import com.smoothsync.smoothsetup.utils.ProviderJson;

import org.dmfs.express.json.elementary.JsonText;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Maybe;


/**
 * A {@link ProviderResolutionStrategy} that caches results from the API
 * <p>
 * TODO: this should be covered by an HTTP cache in future
 */
public final class Caching implements ProviderResolutionStrategy
{
    private final static Duration CACHE_DURATION = new Duration(1, 28, 0); // 4 weeks
    public final static String KEY_CACHE_DATE = "provider.cached_date";
    private final ProviderResolutionStrategy mDelegate;


    public Caching(@NonNull ProviderResolutionStrategy delegate)
    {
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public Maybe<Provider> provider(@NonNull Context context, @NonNull Account account)
    {
        AccountManager am = AccountManager.get(context);
        return Maybe.fromCallable(() -> am.getUserData(account, KEY_CACHE_DATE))
            .map(DateTime::parse)
            .switchIfEmpty(Maybe.just(new DateTime(0)))
            .filter(cached -> cached.addDuration(CACHE_DURATION).after(DateTime.now()))
            .flatMap(cached -> Maybe.fromCallable(() -> am.getUserData(account, ManualProviders.KEY_PROVIDER)))
            .map(provider -> (Provider) new JsonProvider(new JSONObject(provider)))
            .switchIfEmpty(mDelegate.provider(context, account)
                .doOnSuccess(provider ->
                {
                    if (provider.id().startsWith(ApiProviders.PREFIX))
                    {
                        am.setUserData(account, ManualProviders.KEY_PROVIDER, new JsonText(new ProviderJson(provider)).value());
                        am.setUserData(account, KEY_CACHE_DATE, DateTime.now().toString());
                    }
                }));
    }
}

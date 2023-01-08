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
import com.smoothsync.api.model.impl.BasicInstance;
import com.smoothsync.api.requests.Ping;
import com.smoothsync.smoothsetup.services.binders.ApiServiceBinder;
import com.smoothsync.smoothsetup.services.providerresolution.pingStrategy.PingStrategy;
import com.smoothsync.smoothsetup.utils.FlatMapFirst;
import com.smoothsync.smoothsetup.utils.UnPrefixed;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * A {@link ProviderResolutionStrategy} that sends a ping request to the API for the resolved provider if a given {@link PingStrategy} says so.
 */
public final class Accounting implements ProviderResolutionStrategy
{
    final static String KEY_LAST_API_PING = "last-api-ping";
    final static Duration PING_GRACE_PERIOD = new Duration(1, 8);

    private final PingStrategy mPingStrategy;
    private final ProviderResolutionStrategy mDelegate;


    public Accounting(
        @NonNull PingStrategy pingStrategy,
        @NonNull ProviderResolutionStrategy delegate)
    {
        mPingStrategy = pingStrategy;
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public Maybe<Provider> provider(@NonNull Context context, @NonNull Account account)
    {
        AccountManager am = AccountManager.get(context);
        return mDelegate.provider(context, account)
            .flatMap(provider -> mPingStrategy
                // get the provider to ping
                .pingProvider(provider)
                // ping if necessary
                .flatMap(pingProvider ->
                    Maybe.fromCallable(() -> am.getUserData(account, KEY_LAST_API_PING))
                        .map(DateTime::parse)
                        .switchIfEmpty(Maybe.fromCallable(() -> new DateTime(0)))
                        .filter(this::ping)
                        // at this point we have a value if we need to send a ping
                        .flatMap(lastPing ->
                            new ApiServiceBinder(context)
                                .observeOn(Schedulers.io())
                                .compose(new FlatMapFirst<>(smoothSyncApi ->
                                    Flowable.just(smoothSyncApi.resultOf(
                                        new Ping(new BasicInstance(new UnPrefixed(pingProvider), context.getPackageName(),
                                            account.name))))))
                                .firstOrError()
                                .timeout(100, TimeUnit.SECONDS)
                                .doOnSuccess(pingResponse -> am.setUserData(account, KEY_LAST_API_PING, DateTime.now().toString()))
                                .flatMap(r -> r.provider().isPresent() ? Single.just(r.provider().value()) : Single.just(provider))
                                .doOnSuccess(newProvider -> {
                                    // wipe cache date if the provider is updated
                                    if (pingProvider.lastModified().before(newProvider.lastModified()))
                                    {
                                        am.setUserData(account, Caching.KEY_CACHE_DATE, null);
                                    }
                                })
                                // allow ping to fail for a while before we allow the error to bubble up
                                .onErrorComplete(ignored -> DateTime.now().after(lastPing.addDuration(PING_GRACE_PERIOD)))))
                // return the original provider if no ping was done
                .switchIfEmpty(Maybe.just(provider)));
    }


    private boolean ping(DateTime lastPing)
    {
        DateTime now = DateTime.now();
        if (lastPing.addDuration(new Duration(1, 31, 0)).before(now))
        {
            // if last ping is older than 31 days always ping
            return true;
        }
        DateTime firstOfMonth = new DateTime(DateTime.UTC, now.getYear(), now.getMonth(), 1, 0, 0, 0);
        // try to distribute pings, so not all instances ping on the same day, but make sure we ping after the 26th of each month
        double chance = Math.random() * (26L - now.getDayOfMonth()) * 16;
        return firstOfMonth.after(lastPing) && chance < 1;

    }
}

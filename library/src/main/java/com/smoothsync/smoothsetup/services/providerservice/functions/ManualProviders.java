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

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.impl.JsonProvider;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.ProviderJson;
import com.smoothsync.smoothsetup.utils.SharedPrefs;
import com.smoothsync.smoothsetup.utils.WithIdPrefix;

import org.dmfs.express.json.elementary.JsonText;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.iterable.Seq;
import org.json.JSONObject;

import androidx.annotation.StringRes;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * A {@link ProviderService} {@link Function} to manually configured providers.
 */
public final class ManualProviders implements Function<Context, ProviderService>
{
    public final static String PREFIX = "com.smoothsync.manual:";
    public final static String KEY_PROVIDER = "provider";
    public static final String KEY_PROVIDER_ID = "provider_id";
    private static final String PROVIDER_STORE = "manual_providers";

    private final io.reactivex.rxjava3.functions.Function<? super Provider, ? extends Provider> prefixFunction =
        provider -> new WithIdPrefix(PREFIX, provider);

    private final @StringRes
    int accountTypeResource;


    /**
     * Creates a {@link ManualProviders} {@link ProviderService} for the given account type resource.
     */
    public ManualProviders(@StringRes int accountTypeResource)
    {
        this.accountTypeResource = accountTypeResource;
    }


    @Override
    public ProviderService value(Context context)
    {
        AccountManager am = AccountManager.get(context);
        return new ProviderService()
        {
            @Override
            public Maybe<Provider> byId(String id)
            {
                return new SharedPrefs(context, PROVIDER_STORE)
                    .flatMapMaybe(prefs -> Maybe.fromCallable(() -> prefs.getString(id, null)))
                    .map(providerJson -> (Provider) new JsonProvider(new JSONObject(providerJson)))
                    .switchIfEmpty(
                        Observable.fromIterable(new Seq<>(
                                am.getAccountsByTypeForPackage(context.getString(accountTypeResource), context.getPackageName())))
                            .filter(account -> id.equals(am.getUserData(account, KEY_PROVIDER_ID)))
                            .firstElement()
                            .map(account -> new JsonProvider(new JSONObject(am.getUserData(account, KEY_PROVIDER)))));
            }


            @Override
            public Observable<Provider> byDomain(String domain)
            {
                // TODO
                return Observable.empty();
            }


            @Override
            public Observable<Provider> all()
            {
                return new SharedPrefs(context, PROVIDER_STORE)
                    .flattenAsObservable(sharedPreferences -> sharedPreferences.getAll().values())
                    .map(providerJson -> new JsonProvider(new JSONObject(providerJson.toString())));
            }


            @Override
            public Observable<String> autoComplete(String domainFragment)
            {
                // TODO
                return Observable.empty();
            }
        };
    }


    public static final class ProviderStorageProcedure implements Consumer<Provider>
    {

        private final Single<SharedPreferences> mSharedPreferencesSingle;


        public ProviderStorageProcedure(@NonNull Context context)
        {
            this(new SharedPrefs(context, PROVIDER_STORE));
        }


        public ProviderStorageProcedure(@NonNull Single<SharedPreferences> preferencesSingle)
        {
            mSharedPreferencesSingle = preferencesSingle;
        }


        @Override
        public void accept(@NonNull Provider provider)
        {
            mSharedPreferencesSingle.subscribe(sharedPreferences ->
                sharedPreferences
                    .edit()
                    .putString(provider.id(), new JsonText(new ProviderJson(provider)).value())
                    .apply());
        }
    }
}

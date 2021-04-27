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
import com.smoothsync.smoothsetup.services.binders.ProviderServiceBinder;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;


public final class SimpleProviderResolutionStrategy implements ProviderResolutionStrategy
{
    public final static String KEY_PROVIDER_ID = "provider_id";


    @NonNull
    @Override
    public Maybe<Provider> provider(@NonNull Context context, @NonNull Account account)
    {
        return Single.wrap(new ProviderServiceBinder(context))
                .flatMapMaybe(
                        providerService ->
                                Maybe.fromCallable(() -> AccountManager.get(context).getUserData(account, KEY_PROVIDER_ID))
                                        .flatMap(providerService::byId));
    }
}

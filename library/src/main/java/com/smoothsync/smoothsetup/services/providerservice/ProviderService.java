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

package com.smoothsync.smoothsetup.services.providerservice;

import com.smoothsync.api.model.Provider;

import androidx.annotation.Keep;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;


/**
 * A service to provide {@link Provider} information.
 */
@Keep
public interface ProviderService
{
    /**
     * Return a specific provider by its id.
     */
    Maybe<Provider> byId(String id);

    /**
     * Return all providers suporting a given domain.
     */
    Observable<Provider> byDomain(String domain);

    /**
     * Return all providers known to this {@link ProviderService}.
     */
    Observable<Provider> all();

    /**
     * Auto complete the given domain name to known domains.
     */
    Observable<String> autoComplete(String domainFragment);
}

/*
 * Copyright (c) 2019 dmfs GmbH
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

package com.smoothsync.smoothsetup.services;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.httpessentials.executors.authorizing.UserCredentials;


/**
 * A Service which validates a given provider configuration.
 *
 * @author Marten Gajda
 */
public interface ProviderValidationService
{
    String ACTION = "com.smoothsync.action.MANUAL_LOGIN_SERVICE";

    /**
     * Validates the given provider and returns a corrected version or an {@link Exception}.
     */
    AccountDetails providerForUrl(Provider provider, UserCredentials credentials) throws Exception;
}

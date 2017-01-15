/*
 * Copyright (c) 2017 dmfs GmbH
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

package com.smoothsync.smoothsetup.model;

import android.os.Parcelable;

import com.smoothsync.api.model.Provider;


/**
 * Holds information about the account to set up, including the login (account-id) and the provider.
 *
 * @author Marten Gajda <mrten@dmfs.org>
 */
public interface Account extends Parcelable
{

    /**
     * Returns the account identifier that was entered by the user. Usually that's an email address.
     *
     * @return The account identifier.
     */
    public String accountId();

    /**
     * Returns the provider that hosts the user account.
     *
     * @return A {@link Provider}.
     */
    public Provider provider();
}

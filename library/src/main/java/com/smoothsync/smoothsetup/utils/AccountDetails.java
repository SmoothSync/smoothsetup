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

package com.smoothsync.smoothsetup.utils;

import com.smoothsync.smoothsetup.model.Account;

import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.httpessentials.executors.authorizing.UserCredentials;


/**
 * // TODO: remove the dependency on UserCredentials, this should actually take something like a "AuthStrategyFactory".
 *
 * @author Marten Gajda
 */
public interface AccountDetails extends Boxable<AccountDetails>
{
    Account account();

    UserCredentials credentials();
}

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

package com.smoothsync.smoothsetup.demo;

import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.services.delegating.DelegatingValidationService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AccountDetailsBox;

import org.dmfs.android.microwizard.box.Box;
import org.dmfs.httpessentials.executors.authorizing.UserCredentials;


/**
 * @author Marten Gajda
 */
public final class ValidationService extends DelegatingValidationService
{
    public ValidationService()
    {
        super(context -> (provider, credentials) -> {
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return new AccountDetails()
            {
                @Override
                public Account account()
                {
                    return new BasicAccount(                            // TODO: include a domain
                            credentials.userName().toString(),
                            provider);
                }


                @Override
                public UserCredentials credentials()
                {
                    return credentials;
                }


                @Override
                public Box<AccountDetails> boxed()
                {
                    return new AccountDetailsBox(this);
                }
            };
        });
    }
}

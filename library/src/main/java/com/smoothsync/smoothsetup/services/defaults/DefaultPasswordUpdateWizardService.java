/*
 * Copyright (c) 2018 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.defaults;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.WizardService;
import com.smoothsync.smoothsetup.services.delegating.DelegatingWizardService;
import com.smoothsync.smoothsetup.wizard.AuthError;
import com.smoothsync.smoothsetup.wizard.Congratulations;
import com.smoothsync.smoothsetup.wizard.CreateAccount;
import com.smoothsync.smoothsetup.wizard.EnterPassword;
import com.smoothsync.smoothsetup.wizard.LoadAccount;
import com.smoothsync.smoothsetup.wizard.VerifyLogin;


/**
 * The default {@link WizardService} to update an account password.
 *
 * @author Marten Gajda
 */
public final class DefaultPasswordUpdateWizardService extends DelegatingWizardService
{
    public final static String PARAM_ACCOUNT = "account";


    public DefaultPasswordUpdateWizardService()
    {
        super(() -> (context, intent) ->
                new LoadAccount(
                        new AuthError(
                                new EnterPassword(
                                        new VerifyLogin(
                                                // TODO: replace with UpdateCredentialsWizard
                                                new CreateAccount(
                                                        // TODO: replace with simple success message
                                                        new Congratulations(R.string.smoothsetup_message_auth_completed))))))
                        .microFragment(
                                context,
                                intent.getParcelableExtra(PARAM_ACCOUNT))
        );
    }
}

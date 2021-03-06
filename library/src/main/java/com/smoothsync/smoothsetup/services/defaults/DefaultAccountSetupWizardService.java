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

import android.Manifest;
import android.net.Uri;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.services.WizardService;
import com.smoothsync.smoothsetup.services.delegating.DelegatingWizardService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.LoginInfo;
import com.smoothsync.smoothsetup.wizard.ChooseProvider;
import com.smoothsync.smoothsetup.wizard.Congratulations;
import com.smoothsync.smoothsetup.wizard.CreateAccount;
import com.smoothsync.smoothsetup.wizard.Dispatching;
import com.smoothsync.smoothsetup.wizard.EnterPassword;
import com.smoothsync.smoothsetup.wizard.GenericLogin;
import com.smoothsync.smoothsetup.wizard.LoadProvider;
import com.smoothsync.smoothsetup.wizard.LoadProviders;
import com.smoothsync.smoothsetup.wizard.ManualLogin;
import com.smoothsync.smoothsetup.wizard.RequestPermissions;
import com.smoothsync.smoothsetup.wizard.UsernameLogin;
import com.smoothsync.smoothsetup.wizard.ValidateCustomAccountLogin;
import com.smoothsync.smoothsetup.wizard.VerifyLogin;
import com.smoothsync.smoothsetup.wizard.WaitForReferrer;

import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.iterables.elementary.Seq;


/**
 * The default {@link WizardService} to create an account.
 *
 * @author Marten Gajda
 */
public final class DefaultAccountSetupWizardService extends DelegatingWizardService
{
    public DefaultAccountSetupWizardService()
    {
        super(() -> (context, intent) ->
        {
            MicroWizard<AccountDetails> requestPermission = new RequestPermissions<>(
                    new Seq<>(Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS),
                    new CreateAccount(
                            new Congratulations(R.string.smoothsetup_message_setup_completed)));
            MicroWizard<Account> passwordWizard =
                    new EnterPassword(
                            new VerifyLogin(requestPermission));
            MicroWizard<LoginInfo> loginWizard = new UsernameLogin(passwordWizard);
            return new Dispatching(
                    new WaitForReferrer(
                            new LoadProvider(loginWizard),
                            new GenericLogin(
                                    passwordWizard,
                                    new LoadProviders(new ChooseProvider(loginWizard)),
                                    new ManualLogin(new ValidateCustomAccountLogin(requestPermission)))))
                    .microFragment(context, intent.getData() == null ? Uri.EMPTY : intent.getData());
        });
    }
}

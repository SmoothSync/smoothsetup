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

package com.smoothsync.smoothsetup;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.utils.ActivityInfo;
import com.smoothsync.smoothsetup.utils.LoginInfo;
import com.smoothsync.smoothsetup.utils.SimpleLoginRequest;
import com.smoothsync.smoothsetup.wizard.VerifyLogin;
import com.smoothsync.smoothsetup.wizard.ChooseProvider;
import com.smoothsync.smoothsetup.wizard.CreateAccount;
import com.smoothsync.smoothsetup.wizard.GenericLogin;
import com.smoothsync.smoothsetup.wizard.LoadProvider;
import com.smoothsync.smoothsetup.wizard.LoadProviders;
import com.smoothsync.smoothsetup.wizard.UsernameLogin;
import com.smoothsync.smoothsetup.wizard.EnterPassword;
import com.smoothsync.smoothsetup.wizard.Congratulations;
import com.smoothsync.smoothsetup.wizard.WaitForReferrer;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;


/**
 * The launcher activity. It decides how to launch the setup wizard.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class SmoothSetupDispatchActivity extends AppCompatActivity
{
    public final static String META_PROVIDER = "com.smoothsync.PROVIDER";

    public final static String PREF_REFERRER = "referrer";

    public final static String PARAM_PROVIDER = "provider";

    public final static String PARAM_ACCOUNT = "account";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MicroWizard<Account> passwordWizard = new EnterPassword(new VerifyLogin(new CreateAccount(new Congratulations())));
        MicroWizard<LoginInfo> loginWizard = new UsernameLogin(passwordWizard);

        // check if meta data contains a specific provider url, if the url is hard coded, we don't allow to override it
        Optional<Bundle> metaData = new NullSafe<>(new ActivityInfo(this, PackageManager.GET_META_DATA).value().metaData);
        if (metaData.value(Bundle.EMPTY).containsKey(META_PROVIDER))
        {
            Uri uri = Uri.parse(metaData.value().getString(META_PROVIDER));
            launchStep(
                    new LoadProvider(loginWizard)
                            .microFragment(this,
                                    new SimpleLoginRequest(
                                            uri.getQueryParameter(PARAM_PROVIDER),
                                            new NullSafe<>(uri.getQueryParameter(PARAM_ACCOUNT)))));
            return;
        }

        // check if intent data has a provider id
        Uri data = getIntent().getData();
        if (data != null && data.getQueryParameter(PARAM_PROVIDER) != null)
        {
            launchStep(
                    new LoadProvider(loginWizard)
                            .microFragment(this,
                                    new SimpleLoginRequest(
                                            data.getQueryParameter(PARAM_PROVIDER),
                                            new NullSafe<>(data.getQueryParameter(PARAM_ACCOUNT)))));
            return;
        }

        MicroWizard<Void> genericLogin = new GenericLogin(passwordWizard, new LoadProviders(new ChooseProvider(loginWizard)));
        // check if shared preferences contain a provider id
        SharedPreferences pref = getSharedPreferences("com.smoothsync.smoothsetup.prefs", 0);
        if (pref.contains(PREF_REFERRER))
        {
            String referrer = pref.getString(PREF_REFERRER, null);
            if (!TextUtils.isEmpty(referrer))
            {
                Uri uri = Uri.parse(referrer);
                if (uri.getQueryParameter(PARAM_PROVIDER) != null)
                {
                    launchStep(
                            new LoadProvider(loginWizard)
                                    .microFragment(this,
                                            new SimpleLoginRequest(
                                                    uri.getQueryParameter(PARAM_PROVIDER),
                                                    new NullSafe<>(uri.getQueryParameter(PARAM_ACCOUNT)))));
                    return;
                }
            }
            else
            {
                // no referrer
                launchStep(genericLogin.microFragment(this, null));
                return;
            }
        }

        // launch wait fo the referrer braodcast
        launchStep(new WaitForReferrer(new LoadProvider(loginWizard), genericLogin).microFragment(this, null));
    }


    private void launchStep(MicroFragment<?> microFragment)
    {
        MicroFragmentHostActivity.launch(this, microFragment);
        finish();
    }
}

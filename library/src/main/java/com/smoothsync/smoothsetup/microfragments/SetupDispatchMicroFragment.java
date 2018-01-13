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

package com.smoothsync.smoothsetup.microfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.utils.LoginInfo;
import com.smoothsync.smoothsetup.utils.SimpleLoginRequest;
import com.smoothsync.smoothsetup.utils.StringMeta;
import com.smoothsync.smoothsetup.wizard.ChooseProvider;
import com.smoothsync.smoothsetup.wizard.Congratulations;
import com.smoothsync.smoothsetup.wizard.CreateAccount;
import com.smoothsync.smoothsetup.wizard.EnterPassword;
import com.smoothsync.smoothsetup.wizard.GenericLogin;
import com.smoothsync.smoothsetup.wizard.LoadProvider;
import com.smoothsync.smoothsetup.wizard.LoadProviders;
import com.smoothsync.smoothsetup.wizard.UsernameLogin;
import com.smoothsync.smoothsetup.wizard.VerifyLogin;
import com.smoothsync.smoothsetup.wizard.WaitForReferrer;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;


/**
 * A {@link MicroFragment} which dispatches the setup intent to the appropriate wizard step.
 *
 * @author Marten Gajda
 */
public final class SetupDispatchMicroFragment implements MicroFragment<Void>
{
    public SetupDispatchMicroFragment()
    {
        // nothing to do
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        // this step should be fairly quick, showing a title for a few milliseconds could lead to disturbing UI glitches
        return "";
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost microFragmentHost)
    {
        return new DispatchFragment();
    }


    @NonNull
    @Override
    public Void parameter()
    {
        return null;
    }


    @Override
    public boolean skipOnBack()
    {
        return true;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
    }


    public final static Creator<SetupDispatchMicroFragment> CREATOR = new Creator<SetupDispatchMicroFragment>()
    {
        @Override
        public SetupDispatchMicroFragment createFromParcel(Parcel source)
        {
            return new SetupDispatchMicroFragment();
        }


        @Override
        public SetupDispatchMicroFragment[] newArray(int size)
        {
            return new SetupDispatchMicroFragment[size];
        }
    };


    public final static class DispatchFragment extends Fragment
    {
        public final static String META_PROVIDER = "com.smoothsync.PROVIDER";

        public final static String PREF_REFERRER = "referrer";

        public final static String PARAM_PROVIDER = "provider";

        public final static String PARAM_ACCOUNT = "account";


        @Override
        public void onResume()
        {
            super.onResume();

            new Handler().post(() ->
            {

                MicroWizard<Account> passwordWizard = new EnterPassword(new VerifyLogin(new CreateAccount(new Congratulations())));
                MicroWizard<LoginInfo> loginWizard = new UsernameLogin(passwordWizard);

                // check if meta data contains a specific provider url, if the url is hard coded, we don't allow to override it
                Optional<String> metaData = new NullSafe<>(new StringMeta(getActivity(), META_PROVIDER).value());
                if (metaData.isPresent())
                {
                    Uri uri = Uri.parse(metaData.value());
                    launchWizard(
                            new LoadProvider(loginWizard),
                            new SimpleLoginRequest(
                                    uri.getQueryParameter(PARAM_PROVIDER),
                                    new NullSafe<>(uri.getQueryParameter(PARAM_ACCOUNT))));
                    return;
                }

                // check if intent data has a provider id
                Uri data = getActivity().getIntent().getData();
                if (data != null && data.getQueryParameter(PARAM_PROVIDER) != null)
                {
                    launchWizard(
                            new LoadProvider(loginWizard),
                            new SimpleLoginRequest(
                                    data.getQueryParameter(PARAM_PROVIDER),
                                    new NullSafe<>(data.getQueryParameter(PARAM_ACCOUNT))));
                    return;
                }

                MicroWizard<Void> genericLogin = new GenericLogin(passwordWizard, new LoadProviders(new ChooseProvider(loginWizard)));
                // check if shared preferences contain a provider id
                SharedPreferences pref = getContext().getSharedPreferences("com.smoothsync.smoothsetup.prefs", 0);
                if (pref.contains(PREF_REFERRER))
                {
                    String referrer = pref.getString(PREF_REFERRER, null);
                    if (!TextUtils.isEmpty(referrer))
                    {
                        Uri uri = Uri.parse(referrer);
                        if (uri.getQueryParameter(PARAM_PROVIDER) != null)
                        {
                            launchWizard(
                                    new LoadProvider(loginWizard),
                                    new SimpleLoginRequest(
                                            uri.getQueryParameter(PARAM_PROVIDER),
                                            new NullSafe<>(uri.getQueryParameter(PARAM_ACCOUNT))));
                            return;
                        }
                    }
                    else
                    {
                        // no referrer
                        launchWizard(genericLogin, null);
                        return;
                    }
                }

                // launch wait fo the referrer broadcast
                launchWizard(new WaitForReferrer(new LoadProvider(loginWizard), genericLogin), null);
            });
        }


        private <T> void launchWizard(MicroWizard<T> microWizard, T argument)
        {
            new FragmentEnvironment<>(this).host().execute(getActivity(), new XFaded(
                    new ForwardTransition<>(microWizard.microFragment(getContext(), argument))));
        }
    }
}

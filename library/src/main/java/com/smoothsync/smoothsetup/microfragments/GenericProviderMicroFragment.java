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

package com.smoothsync.smoothsetup.microfragments;

import android.content.Context;
import android.os.Parcel;
import android.text.TextUtils;
import android.widget.Adapter;
import android.widget.Filterable;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.ApiAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.setupbuttons.ApiSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.FixedButtonSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.generator.Generator;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.Conditional;
import org.dmfs.jems.predicate.composite.Not;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import static org.dmfs.optional.Absent.absent;


/**
 * A {@link MicroFragment} that tries to find the provider based on the domain part of the user login.
 *
 * @author Marten Gajda
 */
public final class GenericProviderMicroFragment implements MicroFragment<LoginFragment.Params>
{
    public final static Creator<GenericProviderMicroFragment> CREATOR = new Creator<GenericProviderMicroFragment>()
    {
        @Override
        public GenericProviderMicroFragment createFromParcel(Parcel source)
        {
            return new GenericProviderMicroFragment(new Unboxed<MicroWizard<Account>>(source).value(),
                    new Unboxed<MicroWizard<Optional<String>>>(source).value(),
                    new Unboxed<MicroWizard<Optional<String>>>(source).value());
        }


        @Override
        public GenericProviderMicroFragment[] newArray(int size)
        {
            return new GenericProviderMicroFragment[size];
        }
    };

    private final MicroWizard<Account> mNext;
    private final MicroWizard<Optional<String>> mChooser;
    private final MicroWizard<Optional<String>> mManual;


    public GenericProviderMicroFragment(MicroWizard<Account> next, MicroWizard<Optional<String>> chooser, MicroWizard<Optional<String>> manual)
    {
        mNext = next;
        mChooser = chooser;
        mManual = manual;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_login);
    }


    @Override
    public boolean skipOnBack()
    {
        return false;
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost host)
    {
        return new LoginFragment();
    }


    @NonNull
    @Override
    public LoginFragment.Params parameter()
    {
        return new LoginFragment.Params()
        {
            @Override
            public LoginFragment.LoginFormAdapterFactory loginFormAdapterFactory()
            {
                return new ApiLoginFormAdapterFactory(mNext, mChooser, mManual);
            }


            @Override
            public Optional<String> username()
            {
                return absent();
            }

        };
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mNext.boxed(), flags);
        dest.writeParcelable(mChooser.boxed(), flags);
        dest.writeParcelable(mManual.boxed(), flags);
    }


    private final static class ApiLoginFormAdapterFactory implements LoginFragment.LoginFormAdapterFactory
    {
        private final MicroWizard<Account> mAccountSetup;
        private final MicroWizard<Optional<String>> mChooserSetup;
        private final MicroWizard<Optional<String>> mManualSetup;


        private ApiLoginFormAdapterFactory(MicroWizard<Account> accountSetup, MicroWizard<Optional<String>> chooserSetup, MicroWizard<Optional<String>> manualSetup)
        {
            mAccountSetup = accountSetup;
            mChooserSetup = chooserSetup;
            mManualSetup = manualSetup;
        }


        @NonNull
        @Override
        public <T extends RecyclerView.Adapter<BasicButtonViewHolder> & SetupButtonAdapter> T setupButtonAdapter(@NonNull Context context,
                                                                                                                 @NonNull MicroFragmentHost host,
                                                                                                                 @NonNull SmoothSyncApi api,
                                                                                                                 @NonNull Generator<String> name)
        {
            T adapter = (T) new ApiSmoothSetupAdapter(api,
                    provider -> host.execute(
                            context,
                            new Swiped(
                                    new ForwardTransition<>(
                                            mAccountSetup.microFragment(
                                                    context,
                                                    new BasicAccount(name.next(), provider))))));

            adapter = (T) new FixedButtonSetupAdapter<>(adapter,
                    () -> host.execute(
                            context,
                            new Swiped(
                                    new ForwardTransition<>(
                                            mChooserSetup.microFragment(context, new Conditional<String>(new Not<>(TextUtils::isEmpty), name::next))))),
                    () -> R.string.smoothsetup_button_choose_provider);

            if (context.getResources().getBoolean(R.bool.smoothsetup_allow_manual_setup))
            {
                adapter = (T) new FixedButtonSetupAdapter<>(adapter,
                        () -> host.execute(
                                context,
                                new Swiped(
                                        new ForwardTransition<>(
                                                mManualSetup.microFragment(context, new Conditional<String>(new Not<>(TextUtils::isEmpty), name::next))))),
                        () -> R.string.smoothsetup_button_manual_setup);
            }
            return adapter;
        }


        @NonNull
        @Override
        public <T extends Adapter & Filterable> T autoCompleteAdapter(@NonNull Context context, @NonNull SmoothSyncApi api)
        {
            return (T) new ApiAutoCompleteAdapter(api);
        }


        @NonNull
        @Override
        public String promptText(@NonNull Context context)
        {
            return context.getString(R.string.smoothsetup_prompt_login);
        }


        @Override
        public Optional<Provider> provider()
        {
            return absent();
        }
    }
}

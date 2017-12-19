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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.Adapter;
import android.widget.Filterable;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.ApiAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.setupbuttons.ApiSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.FixedButtonSetupAdapter;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.FactoryBox;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.optional.Optional;

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
                    new Unboxed<MicroWizard<Optional<String>>>(source).value());
        }


        @Override
        public GenericProviderMicroFragment[] newArray(int size)
        {
            return new GenericProviderMicroFragment[size];
        }
    };

    private final MicroWizard<Account> mNext;
    private final MicroWizard<Optional<String>> mFallback;


    public GenericProviderMicroFragment(MicroWizard<Account> next, MicroWizard<Optional<String>> fallback)
    {
        mNext = next;
        mFallback = fallback;
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
                return new ApiLoginFormAdapterFactory();
            }


            @Override
            public Optional<String> username()
            {
                return absent();
            }


            @Override
            public MicroWizard<Account> next()
            {
                return mNext;
            }


            @Override
            public MicroWizard<Optional<String>> fallback()
            {
                return mFallback;
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
        dest.writeParcelable(mFallback.boxed(), flags);
    }


    private final static class ApiLoginFormAdapterFactory implements LoginFragment.LoginFormAdapterFactory
    {
        public final static Creator<LoginFragment.LoginFormAdapterFactory> CREATOR = new FactoryBox.FactoryBoxCreator<>(ApiLoginFormAdapterFactory::new,
                ApiLoginFormAdapterFactory[]::new);


        @NonNull
        @Override
        public <T extends RecyclerView.Adapter<BasicButtonViewHolder>, SetupButtonAdapter> T setupButtonAdapter(@NonNull Context context,
                                                                                                                @NonNull com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter.OnProviderSelectListener providerSelectListener, SmoothSyncApi api)
        {
            return (T) new FixedButtonSetupAdapter(new ApiSmoothSetupAdapter(api, providerSelectListener), providerSelectListener);
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
        public int describeContents()
        {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
        }
    }
}

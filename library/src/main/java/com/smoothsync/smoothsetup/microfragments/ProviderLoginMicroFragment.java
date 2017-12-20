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
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.ProviderAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.ProviderSmoothSetupAdapter;
import com.smoothsync.smoothsetup.utils.LoginInfo;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.optional.Optional;


/**
 * A {@link MicroFragment} to set up a specific provider.
 *
 * @author Marten Gajda
 */
public final class ProviderLoginMicroFragment implements MicroFragment<LoginFragment.Params>
{
    public final static Creator<ProviderLoginMicroFragment> CREATOR = new Creator<ProviderLoginMicroFragment>()
    {
        @Override
        public ProviderLoginMicroFragment createFromParcel(Parcel source)
        {
            return new ProviderLoginMicroFragment(new Unboxed<LoginInfo>(source).value(), new Unboxed<MicroWizard<Account>>(source).value());
        }


        @Override
        public ProviderLoginMicroFragment[] newArray(int size)
        {
            return new ProviderLoginMicroFragment[size];
        }
    };
    @NonNull
    private final LoginInfo mLoginInfo;
    @NonNull
    private final MicroWizard<Account> mNext;


    public ProviderLoginMicroFragment(@NonNull LoginInfo loginInfo, MicroWizard<Account> next)
    {
        mLoginInfo = loginInfo;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        try
        {
            return mLoginInfo.provider().name();
        }
        catch (ProtocolException e)
        {
            throw new RuntimeException("Can't load provider title", e);
        }
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
            @NonNull
            @Override
            public LoginFragment.LoginFormAdapterFactory loginFormAdapterFactory()
            {
                return new ProviderLoginFormAdapterFactory(mLoginInfo.provider());
            }


            @Override
            public Optional<String> username()
            {
                return mLoginInfo.username();
            }


            @Override
            public MicroWizard<Account> next()
            {
                return mNext;
            }


            @Override
            public MicroWizard<Optional<String>> fallback()
            {
                throw new RuntimeException("This should not be called");
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
        dest.writeParcelable(mLoginInfo.boxed(), flags);
        dest.writeParcelable(mNext.boxed(), flags);
    }


    private final static class ProviderLoginFormAdapterFactory implements LoginFragment.LoginFormAdapterFactory
    {
        private final Provider mProvider;
        private MicroFragmentEnvironment<ChooseProviderMicroFragment.ProviderListFragment.Params> mMicroFragmentEnvironment;


        private ProviderLoginFormAdapterFactory(Provider provider)
        {
            mProvider = provider;
        }


        @NonNull
        @Override
        public <T extends RecyclerView.Adapter<BasicButtonViewHolder>, SetupButtonAdapter> T setupButtonAdapter(@NonNull Context context,
                                                                                                                @NonNull com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter.OnProviderSelectListener providerSelectListener, SmoothSyncApi api)
        {
            return (T) new ProviderSmoothSetupAdapter(mProvider, providerSelectListener);
        }


        @NonNull
        @Override
        public <T extends Adapter & Filterable> T autoCompleteAdapter(@NonNull Context context, @NonNull SmoothSyncApi api)
        {
            return (T) new ProviderAutoCompleteAdapter(mProvider);
        }


        @NonNull
        @Override
        public String promptText(@NonNull Context context)
        {
            try
            {
                return context.getString(R.string.smoothsetup_prompt_login_at_provider, mProvider.name());
            }
            catch (ProtocolException e)
            {
                throw new RuntimeException("Can't retrieve provider name", e);
            }
        }
    }
}

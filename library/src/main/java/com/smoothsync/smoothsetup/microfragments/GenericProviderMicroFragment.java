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
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.Adapter;
import android.widget.Filterable;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.ApiAutoCompleteAdapter;
import com.smoothsync.smoothsetup.setupbuttons.ApiSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.FixedButtonSetupAdapter;

import org.dmfs.android.microfragments.BasicMicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;


/**
 * A {@link MicroFragment} that tries to find the provider based on the domain part of the user login.
 *
 * @author Marten Gajda
 */
public final class GenericProviderMicroFragment implements MicroFragment<LoginFragment.Params>
{
    public GenericProviderMicroFragment()
    {
        // nothing to do
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
        Fragment result = new LoginFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(MicroFragment.ARG_ENVIRONMENT, new BasicMicroFragmentEnvironment<>(this, host));
        result.setArguments(arguments);
        return result;
    }


    @NonNull
    @Override
    public LoginFragment.Params parameters()
    {
        return new LoginFragment.Params()
        {
            @Override
            public LoginFragment.LoginFormAdapterFactory loginFormAdapterFactory()
            {
                return new ApiLoginFormAdapterFactory();
            }


            @Override
            public String accountName()
            {
                return "";
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
    }


    public final static Creator<GenericProviderMicroFragment> CREATOR = new Creator<GenericProviderMicroFragment>()
    {
        @Override
        public GenericProviderMicroFragment createFromParcel(Parcel source)
        {
            return new GenericProviderMicroFragment();
        }


        @Override
        public GenericProviderMicroFragment[] newArray(int size)
        {
            return new GenericProviderMicroFragment[size];
        }
    };


    private final static class ApiLoginFormAdapterFactory implements LoginFragment.LoginFormAdapterFactory
    {
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


        public final static Creator<LoginFragment.LoginFormAdapterFactory> CREATOR = new Creator<LoginFragment.LoginFormAdapterFactory>()
        {
            @Override
            public LoginFragment.LoginFormAdapterFactory createFromParcel(Parcel source)
            {
                return new ApiLoginFormAdapterFactory();
            }


            @Override
            public LoginFragment.LoginFormAdapterFactory[] newArray(int size)
            {
                return new ApiLoginFormAdapterFactory[size];
            }
        };
    }
}

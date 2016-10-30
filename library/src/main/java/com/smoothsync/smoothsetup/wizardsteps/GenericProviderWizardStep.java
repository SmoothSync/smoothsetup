/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.wizardsteps;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.widget.Adapter;
import android.widget.Filterable;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.ApiAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.setupbuttons.ApiSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.FixedButtonSetupAdapter;


/**
 * A WizardStep that tries to find the provider based on the domain part of the user login.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class GenericProviderWizardStep implements WizardStep
{
    private final LoginFragment.LoginFormAdapterFactory loginFormAdapterFactory = new ApiLoginFormAdapterFactory();


    @Override
    public String title(Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_login);
    }


    @Override
    public boolean skipOnBack()
    {
        return false;
    }


    @Override
    public Fragment fragment(Context context)
    {
        return LoginFragment.newInstance(this, loginFormAdapterFactory, "");
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


    public final static Creator CREATOR = new Creator()
    {
        @Override
        public Object createFromParcel(Parcel source)
        {
            return new GenericProviderWizardStep();
        }


        @Override
        public Object[] newArray(int size)
        {
            return new GenericProviderWizardStep[size];
        }
    };


    private static class ApiLoginFormAdapterFactory implements LoginFragment.LoginFormAdapterFactory
    {
        @Override
        public <T extends RecyclerView.Adapter<BasicButtonViewHolder>, SetupButtonAdapter> T setupButtonAdapter(Context context,
                                                                                                                com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter.OnProviderSelectListener providerSelectListener, SmoothSyncApi api)
        {
            return (T) new FixedButtonSetupAdapter(new ApiSmoothSetupAdapter(api, providerSelectListener), providerSelectListener);
        }


        @Override
        public <T extends Adapter & Filterable> T autoCompleteAdapter(Context context, SmoothSyncApi api)
        {
            return (T) new ApiAutoCompleteAdapter(api);
        }


        @Override
        public String promptText(Context context)
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

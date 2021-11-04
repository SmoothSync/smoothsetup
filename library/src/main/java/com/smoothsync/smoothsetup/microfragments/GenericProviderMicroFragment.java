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
import android.content.Intent;
import android.os.Parcel;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.optional.Optional;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static org.dmfs.optional.Absent.absent;


/**
 * A {@link MicroFragment} that tries to find the provider based on the domain part of the user login.
 *
 * @author Marten Gajda
 */
public final class GenericProviderMicroFragment implements MicroFragment<GenericLoginFragment.Params>
{
    public final static Creator<GenericProviderMicroFragment> CREATOR = new Creator<GenericProviderMicroFragment>()
    {
        @Override
        public GenericProviderMicroFragment createFromParcel(Parcel source)
        {
            return new GenericProviderMicroFragment(new Unboxed<MicroWizard<AccountDetails>>(source).value(),
                source.readParcelable(getClass().getClassLoader()));
        }


        @Override
        public GenericProviderMicroFragment[] newArray(int size)
        {
            return new GenericProviderMicroFragment[size];
        }
    };

    private final MicroWizard<AccountDetails> mNext;
    private final Intent mSetupChoicesService;


    public GenericProviderMicroFragment(MicroWizard<AccountDetails> next, Intent setupChoicesService)
    {
        mNext = next;
        mSetupChoicesService = setupChoicesService;
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
        return new GenericLoginFragment();
    }


    @NonNull
    @Override
    public GenericLoginFragment.Params parameter()
    {
        return new GenericLoginFragment.Params()
        {
            @NonNull
            @Override
            public Optional<String> username()
            {
                return absent();
            }


            @NonNull
            @Override
            public MicroWizard<AccountDetails> next()
            {
                return mNext;
            }


            @NonNull
            @Override
            public Intent setupChoicesService()
            {
                return mSetupChoicesService;
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
        dest.writeParcelable(mSetupChoicesService, flags);
    }
}

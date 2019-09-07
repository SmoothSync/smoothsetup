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

import com.smoothsync.smoothsetup.microfragments.appspecificpassword.AppSpecificWebviewFragment;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.pigeonpost.Cage;

import java.net.URI;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} to present a web site to create an app-specific password.
 *
 * @author Marten Gajda
 */
public final class CreateAppSpecificPasswordMicroFragment implements MicroFragment<AppSpecificWebviewFragment.Params>
{
    public final static Creator<CreateAppSpecificPasswordMicroFragment> CREATOR = new Creator<CreateAppSpecificPasswordMicroFragment>()
    {
        @Override
        public CreateAppSpecificPasswordMicroFragment createFromParcel(Parcel source)
        {
            return new CreateAppSpecificPasswordMicroFragment(source.readString(),
                    (Cage<AppSpecificWebviewFragment.PasswordResult>) source.readParcelable(getClass().getClassLoader()), (URI) source.readSerializable());
        }


        @Override
        public CreateAppSpecificPasswordMicroFragment[] newArray(int size)
        {
            return new CreateAppSpecificPasswordMicroFragment[size];
        }
    };
    @NonNull
    private final String mTitle;
    @NonNull
    private final Cage<AppSpecificWebviewFragment.PasswordResult> mCage;
    @NonNull
    private final URI mUrl;


    public CreateAppSpecificPasswordMicroFragment(@NonNull String title, @NonNull Cage<AppSpecificWebviewFragment.PasswordResult> cage, @NonNull URI url)
    {
        mTitle = title;
        mCage = cage;
        mUrl = url;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return mTitle;
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost host)
    {
        return new AppSpecificWebviewFragment();
    }


    @NonNull
    @Override
    public AppSpecificWebviewFragment.Params parameter()
    {
        return new AppSpecificWebviewFragment.Params()
        {
            @NonNull
            @Override
            public URI uri()
            {
                return mUrl;
            }


            @NonNull
            @Override
            public Cage<AppSpecificWebviewFragment.PasswordResult> cage()
            {
                return mCage;
            }
        };
    }


    @Override
    public boolean skipOnBack()
    {
        return false;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mTitle);
        dest.writeParcelable(mCage, flags);
        dest.writeSerializable(mUrl);
    }
}

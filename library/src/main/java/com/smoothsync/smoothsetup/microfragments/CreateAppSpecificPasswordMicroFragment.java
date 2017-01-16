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

import com.smoothsync.smoothsetup.microfragments.appspecificpassword.AppSpecificWebviewFragment;

import org.dmfs.android.microfragments.BasicMicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;

import java.net.URI;


/**
 * A {@link MicroFragment} to present a web site to create an app-specific password.
 *
 * @author Marten Gajda
 */
public final class CreateAppSpecificPasswordMicroFragment implements MicroFragment<URI>
{
    public final static Creator<CreateAppSpecificPasswordMicroFragment> CREATOR = new Creator<CreateAppSpecificPasswordMicroFragment>()
    {
        @Override
        public CreateAppSpecificPasswordMicroFragment createFromParcel(Parcel source)
        {
            return new CreateAppSpecificPasswordMicroFragment(source.readString(), (URI) source.readSerializable());
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
    private final URI mUrl;


    public CreateAppSpecificPasswordMicroFragment(@NonNull String title, @NonNull URI url)
    {
        mTitle = title;
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
        Fragment result = new AppSpecificWebviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(MicroFragment.ARG_ENVIRONMENT, new BasicMicroFragmentEnvironment<>(this, host));
        result.setArguments(args);
        return result;
    }


    @NonNull
    @Override
    public URI parameters()
    {
        return mUrl;
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
        dest.writeSerializable(mUrl);
    }
}

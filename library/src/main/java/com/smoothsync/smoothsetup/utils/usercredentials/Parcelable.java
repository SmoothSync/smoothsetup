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

package com.smoothsync.smoothsetup.utils.usercredentials;

import android.os.Parcel;

import org.dmfs.httpessentials.executors.authorizing.UserCredentials;


/**
 * @author Marten Gajda
 */
public final class Parcelable implements UserCredentials, android.os.Parcelable
{
    private final UserCredentials mDelegate;


    public Parcelable(UserCredentials delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mDelegate.userName().toString());
        dest.writeString(mDelegate.password().toString());
    }


    @Override
    public CharSequence userName()
    {
        return mDelegate.userName();
    }


    @Override
    public CharSequence password()
    {
        return mDelegate.password();
    }


    public final static Creator<Parcelable> CREATOR = new Creator<Parcelable>()
    {
        @Override
        public Parcelable createFromParcel(Parcel source)
        {
            final String username = source.readString();
            final String password = source.readString();
            return new Parcelable(new UserCredentials()
            {
                @Override
                public CharSequence userName()
                {
                    return username;
                }


                @Override
                public CharSequence password()
                {
                    return password;
                }
            });
        }


        @Override
        public Parcelable[] newArray(int size)
        {
            return new Parcelable[size];
        }
    };
}

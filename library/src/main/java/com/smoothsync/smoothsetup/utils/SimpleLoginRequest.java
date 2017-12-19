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

package com.smoothsync.smoothsetup.utils;

import android.os.Parcel;

import org.dmfs.android.microwizard.box.Box;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;


/**
 * @author Marten Gajda
 */
public final class SimpleLoginRequest implements LoginRequest
{
    private final String mProviderId;
    private final Optional<String> mUsername;


    public SimpleLoginRequest(String providerId, Optional<String> username)
    {
        mProviderId = providerId;
        mUsername = username;
    }


    @Override
    public String providerId()
    {
        return mProviderId;
    }


    @Override
    public Optional<String> username()
    {
        return mUsername;
    }


    @Override
    public Box<LoginRequest> boxed()
    {
        return new LoginRequestBox(this);
    }


    private final static class LoginRequestBox implements Box<LoginRequest>
    {
        private final SimpleLoginRequest mLoginRequest;


        private LoginRequestBox(SimpleLoginRequest loginRequest)
        {
            mLoginRequest = loginRequest;
        }


        @Override
        public int describeContents()
        {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            dest.writeString(mLoginRequest.mProviderId);
            dest.writeString(mLoginRequest.mUsername.value(null));
        }


        @Override
        public LoginRequest value()
        {
            return mLoginRequest;
        }


        public final static Creator<Box<LoginRequest>> CREATOR = new Creator<Box<LoginRequest>>()
        {
            @Override
            public Box<LoginRequest> createFromParcel(Parcel source)
            {
                return new LoginRequestBox(new SimpleLoginRequest(source.readString(), new NullSafe<>(source.readString())));
            }


            @Override
            public Box<LoginRequest>[] newArray(int size)
            {
                return new LoginRequestBox[size];
            }
        };
    }
}

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

package com.smoothsync.smoothsetup.microfragments.appspecificpassword;

import android.os.Parcel;
import android.support.annotation.NonNull;


/**
 * The result of an {@link AppSpecificWebviewFragment}.
 *
 * @author Marten Gajda
 */
public class SimplePasswordResult implements AppSpecificWebviewFragment.PasswordResult
{
    public final static Creator<SimplePasswordResult> CREATOR = new Creator<SimplePasswordResult>()
    {
        @Override
        public SimplePasswordResult createFromParcel(Parcel source)
        {
            return new SimplePasswordResult(source.readString());
        }


        @Override
        public SimplePasswordResult[] newArray(int size)
        {
            return new SimplePasswordResult[size];
        }
    };
    private final String mResult;


    public SimplePasswordResult(@NonNull String result)
    {
        mResult = result;
    }


    @NonNull
    @Override
    public String password()
    {
        return mResult;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mResult);
    }
}

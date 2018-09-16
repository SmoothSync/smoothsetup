/*
 * Copyright (c) 2018 dmfs GmbH
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
import org.dmfs.android.microwizard.box.Boxable;


/**
 * @author Marten Gajda
 */
public final class StringBox implements Box<String>, Boxable<String>
{
    private final String mValue;


    public StringBox(String value)
    {
        mValue = value;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(mValue);
    }


    @Override
    public String value()
    {
        return mValue;
    }


    public final static Creator<Box<String>> CREATOR = new Creator<Box<String>>()
    {
        @Override
        public Box<String> createFromParcel(Parcel parcel)
        {
            return new StringBox(parcel.readString());
        }


        @Override
        public Box<String>[] newArray(int i)
        {
            return new StringBox[i];
        }
    };


    @Override
    public Box<String> boxed()
    {
        return this;
    }
}

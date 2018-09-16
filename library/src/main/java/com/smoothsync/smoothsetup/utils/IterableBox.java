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

import java.util.LinkedList;
import java.util.List;


/**
 * A {@link Box} for {@link Iterable}s of {@link Boxable}s.
 *
 * @author Marten Gajda
 */
public final class IterableBox<T extends Boxable<?>> implements Box<Iterable<T>>, Boxable<Iterable<T>>
{
    private final Iterable<T> mValue;


    public IterableBox(Iterable<T> value)
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
        int pos = parcel.dataPosition();
        parcel.writeInt(0);
        int count = 0;
        for (Boxable<?> boxable : mValue)
        {
            parcel.writeParcelable(boxable.boxed(), i);
            count += 1;
        }
        parcel.setDataPosition(pos);
        parcel.writeInt(count);
        parcel.setDataPosition(parcel.dataSize());
    }


    @Override
    public Iterable<T> value()
    {
        return mValue;
    }


    public final static Creator<IterableBox<?>> CREATOR = new Creator<IterableBox<?>>()
    {
        @Override
        public IterableBox<?> createFromParcel(Parcel parcel)
        {
            ClassLoader classLoader = getClass().getClassLoader();
            List values = new LinkedList<>();
            int count = parcel.readInt();
            for (int i = 0; i < count; ++i)
            {
                final Box box = parcel.readParcelable(classLoader);
                values.add((Boxable<?>) () -> box);
            }
            return new IterableBox(values);
        }


        @Override
        public IterableBox<?>[] newArray(int i)
        {
            return new IterableBox[i];
        }
    };


    @Override
    public Box<Iterable<T>> boxed()
    {
        return this;
    }
}

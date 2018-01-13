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

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.transitions.FragmentTransition;


/**
 * {@link FragmentTransition} decorator overriding the timestamp.
 *
 * @author Marten Gajda
 */
public final class Timestamped implements FragmentTransition
{
    private final Timestamp mTimestamp;
    private final FragmentTransition mDelegate;


    public Timestamped(Timestamp timestamp, FragmentTransition delegate)
    {
        mTimestamp = timestamp;
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public Timestamp timestamp()
    {
        return mTimestamp;
    }


    @Override
    public void prepare(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull MicroFragmentHost microFragmentHost, @NonNull MicroFragment<?> microFragment)
    {
        mDelegate.prepare(context, fragmentManager, microFragmentHost, microFragment);
    }


    @NonNull
    @Override
    public FragmentTransaction updateTransaction(@NonNull Context context, @NonNull FragmentTransaction fragmentTransaction, @NonNull FragmentManager fragmentManager, @NonNull MicroFragmentHost microFragmentHost, @NonNull MicroFragment<?> microFragment)
    {
        return mDelegate.updateTransaction(context, fragmentTransaction, fragmentManager, microFragmentHost, microFragment);
    }


    @Override
    public void cleanup(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull MicroFragmentHost microFragmentHost, @NonNull MicroFragment<?> microFragment)
    {
        mDelegate.cleanup(context, fragmentManager, microFragmentHost, microFragment);
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mTimestamp, flags);
        dest.writeParcelable(mDelegate, flags);
    }


    public final static Creator<Timestamped> CREATOR = new Creator<Timestamped>()
    {
        @Override
        public Timestamped createFromParcel(Parcel source)
        {
            ClassLoader cl = getClass().getClassLoader();
            return new Timestamped(source.readParcelable(cl), source.readParcelable(cl));
        }


        @Override
        public Timestamped[] newArray(int size)
        {
            return new Timestamped[size];
        }
    };
}

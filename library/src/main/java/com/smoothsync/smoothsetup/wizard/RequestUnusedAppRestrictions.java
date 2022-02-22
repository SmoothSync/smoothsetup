/*
 * Copyright (c) 2022 dmfs GmbH
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

package com.smoothsync.smoothsetup.wizard;

import android.content.Context;
import android.os.Parcel;

import com.smoothsync.smoothsetup.microfragments.UnusedAppRestrictionsRequestMicroFragment;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;


public final class RequestUnusedAppRestrictions<T extends Boxable<T>> implements MicroWizard<T>
{

    private final MicroWizard<T> mNext;


    public RequestUnusedAppRestrictions(MicroWizard<T> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, T params)
    {
        return new UnusedAppRestrictionsRequestMicroFragment<>(params, mNext);
    }


    @Override
    public Box<MicroWizard<T>> boxed()
    {
        return new WizardBox<>(mNext);
    }


    public final static class WizardBox<T extends Boxable<T>> implements Box<MicroWizard<T>>
    {
        private final MicroWizard<T> mNext;


        public WizardBox( MicroWizard<T> next)
        {
            mNext = next;
        }


        @Override
        public int describeContents()
        {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel parcel, int i)
        {
            parcel.writeParcelable(mNext.boxed(), i);
        }


        @Override
        public MicroWizard<T> value()
        {
            return new RequestUnusedAppRestrictions<>( mNext);
        }


        public final static Creator<RequestUnusedAppRestrictions.WizardBox<?>> CREATOR = new Creator<RequestUnusedAppRestrictions.WizardBox<?>>()
        {
            @SuppressWarnings("unchecked") // in static context we don't know about the generic type
            @Override
            public RequestUnusedAppRestrictions.WizardBox<?> createFromParcel(Parcel parcel)
            {
                return new WizardBox( new RequestUnusedAppRestrictions( new Unboxed<MicroWizard<?>>(parcel).value()));
            }


            @Override
            public RequestUnusedAppRestrictions.WizardBox<?>[] newArray(int i)
            {
                return new RequestUnusedAppRestrictions.WizardBox[i];
            }
        };
    }
}

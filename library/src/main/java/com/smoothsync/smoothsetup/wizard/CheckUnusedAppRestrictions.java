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

import com.smoothsync.smoothsetup.microfragments.UnusedAppRestrictionsCheckMicroFragment;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.iterable.decorators.Mapped;


public final class CheckUnusedAppRestrictions<T extends Boxable<T>> implements MicroWizard<T>
{

    private final MicroWizard<T> mNext;


    public CheckUnusedAppRestrictions(MicroWizard<T> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, T params)
    {
        return new UnusedAppRestrictionsCheckMicroFragment<>(params, mNext);
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
            return new CheckUnusedAppRestrictions<>( mNext);
        }


        public final static Creator<CheckUnusedAppRestrictions.WizardBox<?>> CREATOR = new Creator<CheckUnusedAppRestrictions.WizardBox<?>>()
        {
            @SuppressWarnings("unchecked") // in static context we don't know about the generic type
            @Override
            public CheckUnusedAppRestrictions.WizardBox<?> createFromParcel(Parcel parcel)
            {
                Iterable<Boxable<String>> boxablePermissions = new Unboxed<Iterable<Boxable<String>>>(parcel).value();
                Iterable<String> permissions = new Mapped<>(b -> b.boxed().value(), boxablePermissions);
                return new CheckUnusedAppRestrictions.WizardBox( new CheckUnusedAppRestrictions( new Unboxed<MicroWizard<?>>(parcel).value()));
            }


            @Override
            public CheckUnusedAppRestrictions.WizardBox<?>[] newArray(int i)
            {
                return new CheckUnusedAppRestrictions.WizardBox[i];
            }
        };
    }

}

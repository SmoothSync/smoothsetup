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

package com.smoothsync.smoothsetup.wizard;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;

import com.smoothsync.smoothsetup.microfragments.PermissionMicroFragment;
import com.smoothsync.smoothsetup.utils.Denied;
import com.smoothsync.smoothsetup.utils.IterableBox;
import com.smoothsync.smoothsetup.utils.StringBox;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.iterable.decorators.Mapped;


/**
 * A MicroWizard which asks the user about one or multiple permission.
 * <p>
 * If running on Android 5 or lower this step is always skipped.
 *
 * @author Marten Gajda
 */
public final class RequestPermissions<T extends Boxable<T>> implements MicroWizard<T>
{
    private final Iterable<String> mPermissions;
    private final MicroWizard<T> mNext;


    public RequestPermissions(Iterable<String> permissions, MicroWizard<T> next)
    {
        mPermissions = permissions;
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, T argument)
    {
        // skip this on Android 5 and below or if all permissions have been granted before
        return !new Denied(context, mPermissions).iterator().hasNext() ?
                mNext.microFragment(context, argument) : new PermissionMicroFragment<>(mPermissions, argument, mNext);
    }


    @Override
    public Box<MicroWizard<T>> boxed()
    {
        return new WizardBox<>(mPermissions, mNext);
    }


    public final static class WizardBox<T extends Boxable<T>> implements Box<MicroWizard<T>>
    {
        private final Iterable<String> mPermissions;
        private final MicroWizard<T> mNext;


        public WizardBox(Iterable<String> permissions, MicroWizard<T> next)
        {
            mPermissions = permissions;
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
            parcel.writeParcelable(new IterableBox<>(new Mapped<>(StringBox::new, mPermissions)), i);
            parcel.writeParcelable(mNext.boxed(), i);
        }


        @Override
        public MicroWizard<T> value()
        {
            return new RequestPermissions<>(mPermissions, mNext);
        }


        public final static Creator<WizardBox<?>> CREATOR = new Creator<WizardBox<?>>()
        {
            @SuppressWarnings("unchecked") // in static context we don't know about the generic type
            @Override
            public WizardBox<?> createFromParcel(Parcel parcel)
            {
                Iterable<Boxable<String>> boxablePermissions = new Unboxed<Iterable<Boxable<String>>>(parcel).value();
                Iterable<String> permissions = new Mapped<>(b -> b.boxed().value(), boxablePermissions);
                return new WizardBox(permissions, new RequestPermissions(permissions, new Unboxed<MicroWizard<?>>(parcel).value()));
            }


            @Override
            public WizardBox<?>[] newArray(int i)
            {
                return new WizardBox[i];
            }
        };
    }
}

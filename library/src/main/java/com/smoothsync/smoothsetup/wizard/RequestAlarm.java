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

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;

import com.smoothsync.smoothsetup.microfragments.AllowAlarmsRequestMicroFragment;
import com.smoothsync.smoothsetup.utils.IterableBox;
import com.smoothsync.smoothsetup.utils.StringBox;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.First;

import androidx.core.app.AlarmManagerCompat;


public final class RequestAlarm<T extends Boxable<T>> implements MicroWizard<T>
{
    private final Iterable<String> mPermissions;
    private final MicroWizard<T> mNext;


    public RequestAlarm(Iterable<String> permissions, MicroWizard<T> next)
    {
        mPermissions = permissions;
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, T params)
    {
        return
            Build.VERSION.SDK_INT >= 31
                && new First<>(Manifest.permission.SCHEDULE_EXACT_ALARM::equals, mPermissions).isPresent()
                && !AlarmManagerCompat.canScheduleExactAlarms(context.getSystemService(AlarmManager.class))
                ? new AllowAlarmsRequestMicroFragment<>(params, mNext)
                : mNext.microFragment(context, params);
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
            return new RequestAlarm<>(mPermissions, mNext);
        }


        public final static Creator<RequestAlarm.WizardBox<?>> CREATOR = new Creator<RequestAlarm.WizardBox<?>>()
        {
            @SuppressWarnings("unchecked") // in static context we don't know about the generic type
            @Override
            public RequestAlarm.WizardBox<?> createFromParcel(Parcel parcel)
            {
                Iterable<Boxable<String>> boxablePermissions = new Unboxed<Iterable<Boxable<String>>>(parcel).value();
                Iterable<String> permissions = new Mapped<>(b -> b.boxed().value(), boxablePermissions);
                return new WizardBox(permissions, new RequestAlarm(permissions, new Unboxed<MicroWizard<?>>(parcel).value()));
            }


            @Override
            public RequestAlarm.WizardBox<?>[] newArray(int i)
            {
                return new RequestAlarm.WizardBox[i];
            }
        };
    }
}

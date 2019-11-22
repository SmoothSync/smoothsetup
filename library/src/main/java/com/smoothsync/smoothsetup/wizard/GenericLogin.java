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

package com.smoothsync.smoothsetup.wizard;

import android.content.Context;
import android.os.Parcel;

import com.smoothsync.smoothsetup.microfragments.GenericProviderMicroFragment;
import com.smoothsync.smoothsetup.model.Account;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.optional.Optional;


/**
 * @author Marten Gajda
 */
public final class GenericLogin implements MicroWizard<Void>
{
    private final MicroWizard<Account> mNext;
    private final MicroWizard<Optional<String>> mFallback;
    private final MicroWizard<Optional<String>> mManual;


    public GenericLogin(MicroWizard<Account> next, MicroWizard<Optional<String>> chooser, MicroWizard<Optional<String>> manual)
    {
        mNext = next;
        mFallback = chooser;
        mManual = manual;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Void dummy)
    {
        return new GenericProviderMicroFragment(mNext, mFallback, mManual);
    }


    @Override
    public Box<MicroWizard<Void>> boxed()
    {
        return new WizardBox(mNext, mFallback, mManual);
    }


    private final static class WizardBox implements Box<MicroWizard<Void>>
    {
        private final MicroWizard<Account> mNext;
        private final MicroWizard<Optional<String>> mChooser;
        private final MicroWizard<Optional<String>> mManual;


        protected WizardBox(MicroWizard<Account> next, MicroWizard<Optional<String>> chooser, MicroWizard<Optional<String>> manual)
        {
            mNext = next;
            mChooser = chooser;
            mManual = manual;
        }


        public final int describeContents()
        {
            return 0;
        }


        public final void writeToParcel(Parcel dest, int flags)
        {
            dest.writeParcelable(mNext.boxed(), flags);
            dest.writeParcelable(mChooser.boxed(), flags);
            dest.writeParcelable(mManual.boxed(), flags);
        }


        public final MicroWizard<Void> value()
        {
            return new GenericLogin(mNext, mChooser, mManual);
        }


        public final static Creator<WizardBox> CREATOR = new Creator<WizardBox>()
        {
            @Override
            public WizardBox createFromParcel(Parcel parcel)
            {
                return new WizardBox(
                        new Unboxed<MicroWizard<Account>>(parcel).value(),
                        new Unboxed<MicroWizard<Optional<String>>>(parcel).value(),
                        new Unboxed<MicroWizard<Optional<String>>>(parcel).value());
            }


            @Override
            public WizardBox[] newArray(int i)
            {
                return new WizardBox[0];
            }
        };
    }
}

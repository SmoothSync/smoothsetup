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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import com.smoothsync.smoothsetup.microfragments.GenericProviderMicroFragment;
import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Unboxed;

import androidx.annotation.StringRes;


/**
 * @author Marten Gajda
 */
public final class GenericLogin implements MicroWizard<Void>
{
    private final MicroWizard<AccountDetails> mNext;
    @StringRes
    private final int mSetupChoicesService;


    public GenericLogin(MicroWizard<AccountDetails> next, @StringRes int setupChoicesService)
    {
        mNext = next;
        mSetupChoicesService = setupChoicesService;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Void dummy)
    {
        return new GenericProviderMicroFragment(mNext, new Intent().setComponent(new ComponentName(context, context.getString(mSetupChoicesService))));
    }


    @Override
    public Box<MicroWizard<Void>> boxed()
    {
        return new WizardBox(mNext, mSetupChoicesService);
    }


    private final static class WizardBox implements Box<MicroWizard<Void>>
    {
        private final MicroWizard<AccountDetails> mNext;
        private final int mSetupChoiceComponent;


        protected WizardBox(MicroWizard<AccountDetails> next, int setupChoiceComponent)
        {
            mNext = next;
            mSetupChoiceComponent = setupChoiceComponent;
        }


        public final int describeContents()
        {
            return 0;
        }


        public final void writeToParcel(Parcel dest, int flags)
        {
            dest.writeParcelable(mNext.boxed(), flags);
            dest.writeInt(mSetupChoiceComponent);
        }


        public final MicroWizard<Void> value()
        {
            return new GenericLogin(mNext, mSetupChoiceComponent);
        }


        public final static Creator<WizardBox> CREATOR = new Creator<WizardBox>()
        {
            @Override
            public WizardBox createFromParcel(Parcel parcel)
            {
                return new WizardBox(
                    new Unboxed<MicroWizard<AccountDetails>>(parcel).value(),
                    parcel.readInt());
            }


            @Override
            public WizardBox[] newArray(int i)
            {
                return new WizardBox[0];
            }
        };
    }
}

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

import com.smoothsync.smoothsetup.microfragments.TerminalMicroFragment;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.utils.AppLabel;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;


/**
 * The final step of a setup wizard.
 *
 * @author Marten Gajda
 */
public final class Congratulations implements MicroWizard<Account>
{
    private final int mMessageResource;


    public Congratulations(int messageResource)
    {
        mMessageResource = messageResource;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Account account)
    {
        return new TerminalMicroFragment(context.getString(mMessageResource, new AppLabel(context).value()));
    }


    @Override
    public Box<MicroWizard<Account>> boxed()
    {
        return new WizardBox(mMessageResource);
    }


    private final static class WizardBox implements Box<MicroWizard<Account>>
    {
        private final int mMessageResource;


        public WizardBox(int messageResource)
        {
            mMessageResource = messageResource;
        }


        @Override
        public int describeContents()
        {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel parcel, int i)
        {
            parcel.writeInt(mMessageResource);
        }


        @Override
        public MicroWizard<Account> value()
        {
            return new Congratulations(mMessageResource);
        }


        public final static Creator<WizardBox> CREATOR = new Creator<WizardBox>()
        {
            @Override
            public WizardBox createFromParcel(Parcel parcel)
            {
                return new WizardBox(parcel.readInt());
            }


            @Override
            public WizardBox[] newArray(int i)
            {
                return new WizardBox[i];
            }
        };
    }
}

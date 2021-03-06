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

import com.smoothsync.smoothsetup.microfragments.ApproveAuthorizationMicroFragment;
import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractSingleBox;
import org.dmfs.android.microwizard.box.Box;


/**
 * @author Marten Gajda
 */
public final class VerifyLogin implements MicroWizard<AccountDetails>
{
    private final MicroWizard<AccountDetails> mNext;


    public VerifyLogin(MicroWizard<AccountDetails> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, AccountDetails accountDetails)
    {
        return new ApproveAuthorizationMicroFragment(accountDetails, mNext);
    }


    @Override
    public Box<MicroWizard<AccountDetails>> boxed()
    {
        return new WizardBox(mNext);
    }


    private final static class WizardBox extends AbstractSingleBox<MicroWizard<AccountDetails>, MicroWizard<AccountDetails>>
    {

        WizardBox(MicroWizard<AccountDetails> next)
        {
            super(next, VerifyLogin::new);
        }


        public final static Creator<Box<MicroWizard<AccountDetails>>> CREATOR = new SingleBoxableBoxCreator<>(WizardBox::new, WizardBox[]::new);

    }
}

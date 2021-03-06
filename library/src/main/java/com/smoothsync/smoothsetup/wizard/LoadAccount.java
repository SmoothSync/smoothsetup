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

import android.accounts.Account;
import android.content.Context;

import com.smoothsync.smoothsetup.microfragments.AccountLoadMicroFragment;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractSingleBox;
import org.dmfs.android.microwizard.box.Box;


/**
 * @author Marten Gajda
 */
public final class LoadAccount implements MicroWizard<Account>
{
    private final MicroWizard<com.smoothsync.smoothsetup.model.Account> mNext;


    public LoadAccount(MicroWizard<com.smoothsync.smoothsetup.model.Account> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Account account)
    {
        return new AccountLoadMicroFragment(account, mNext);
    }


    @Override
    public Box<MicroWizard<Account>> boxed()
    {
        return new WizardBox(mNext);
    }


    private final static class WizardBox extends AbstractSingleBox<MicroWizard<com.smoothsync.smoothsetup.model.Account>, MicroWizard<Account>>
    {

        WizardBox(MicroWizard<com.smoothsync.smoothsetup.model.Account> next)
        {
            super(next, LoadAccount::new);
        }


        public final static Creator<Box<MicroWizard<Account>>> CREATOR = new SingleBoxableBoxCreator<>(WizardBox::new, WizardBox[]::new);
    }
}

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

import com.smoothsync.smoothsetup.microfragments.AuthErrorMicroFragment;
import com.smoothsync.smoothsetup.model.Account;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractSingleBox;
import org.dmfs.android.microwizard.box.Box;


/**
 * @author Marten Gajda
 */
public final class AuthError implements MicroWizard<Account>
{
    private final MicroWizard<Account> mNext;


    public AuthError(MicroWizard<Account> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Account loginRequest)
    {
        return new AuthErrorMicroFragment(loginRequest, mNext);
    }


    @Override
    public Box<MicroWizard<Account>> boxed()
    {
        return new WizardBox(mNext);
    }


    private final static class WizardBox extends AbstractSingleBox<MicroWizard<Account>, MicroWizard<Account>>
    {

        WizardBox(MicroWizard<Account> next)
        {
            super(next, AuthError::new);
        }


        public final static Creator<Box<MicroWizard<Account>>> CREATOR = new SingleBoxableBoxCreator<>(WizardBox::new, WizardBox[]::new);
    }
}

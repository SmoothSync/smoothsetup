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

import com.smoothsync.smoothsetup.microfragments.GenericProviderMicroFragment;
import com.smoothsync.smoothsetup.model.Account;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractDoubleBox;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.optional.Optional;


/**
 * @author Marten Gajda
 */
public final class GenericLogin implements MicroWizard<Void>
{
    private final MicroWizard<Account> mNext;
    private final MicroWizard<Optional<String>> mFallback;


    public GenericLogin(MicroWizard<Account> next, MicroWizard<Optional<String>> fallback)
    {
        mNext = next;
        mFallback = fallback;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Void dummy)
    {
        return new GenericProviderMicroFragment(mNext, mFallback);
    }


    @Override
    public Box<MicroWizard<Void>> boxed()
    {
        return new WizardBox(mNext, mFallback);
    }


    private final static class WizardBox extends AbstractDoubleBox<MicroWizard<Account>, MicroWizard<Optional<String>>, MicroWizard<Void>>
    {

        private WizardBox(MicroWizard<Account> next, MicroWizard<Optional<String>> fallback)
        {
            super(next, fallback, GenericLogin::new);
        }


        public final static Creator<Box<MicroWizard<Void>>> CREATOR = new DoubleBoxableBoxCreator<>(WizardBox::new, WizardBox[]::new);
    }
}

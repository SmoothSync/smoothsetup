/*
 * Copyright (c) 2019 dmfs GmbH
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

import com.smoothsync.smoothsetup.microfragments.ManualSetupSimpleMicroFragment;
import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractSingleBox;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.jems.optional.Optional;


/**
 * @author Marten Gajda
 */
public final class ManualLogin implements MicroWizard<Optional<String>>
{
    private final MicroWizard<AccountDetails> mNext;


    public ManualLogin(MicroWizard<AccountDetails> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Optional<String> username)
    {
        return new ManualSetupSimpleMicroFragment(username, mNext);
    }


    @Override
    public Box<MicroWizard<Optional<String>>> boxed()
    {
        return new WizardBox(mNext);
    }


    private final static class WizardBox extends AbstractSingleBox<MicroWizard<AccountDetails>, MicroWizard<Optional<String>>>
    {

        private WizardBox(MicroWizard<AccountDetails> next)
        {
            super(next, ManualLogin::new);
        }


        public final static Creator<Box<MicroWizard<Optional<String>>>> CREATOR = new SingleBoxableBoxCreator<>(WizardBox::new, WizardBox[]::new);
    }
}

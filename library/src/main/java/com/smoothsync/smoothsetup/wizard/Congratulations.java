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

import com.smoothsync.smoothsetup.microfragments.SetupCompleteMicroFragment;
import com.smoothsync.smoothsetup.model.Account;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.FactoryBox;


/**
 * The final step of a setup wizard.
 *
 * @author Marten Gajda
 */
public final class Congratulations implements MicroWizard<Account>
{
    @Override
    public MicroFragment<?> microFragment(Context context, Account account)
    {
        return new SetupCompleteMicroFragment(account);
    }


    @Override
    public Box<MicroWizard<Account>> boxed()
    {
        return new WizardBox();
    }


    private final static class WizardBox extends FactoryBox<MicroWizard<Account>>
    {
        public WizardBox()
        {
            super(Congratulations::new);
        }


        public final static Creator<WizardBox> CREATOR = new FactoryBox.FactoryBoxCreator<>(WizardBox::new, WizardBox[]::new);
    }
}

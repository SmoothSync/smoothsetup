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

import com.smoothsync.smoothsetup.microfragments.ChooseProviderMicroFragment;
import com.smoothsync.smoothsetup.utils.LoginInfo;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractSingleBox;
import org.dmfs.android.microwizard.box.Box;


/**
 * @author Marten Gajda
 */
public final class ChooseProvider implements MicroWizard<ChooseProviderMicroFragment.ProviderSelection>
{
    private final MicroWizard<LoginInfo> mNext;


    public ChooseProvider(MicroWizard<LoginInfo> next)
    {
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, ChooseProviderMicroFragment.ProviderSelection selection)
    {
        return new ChooseProviderMicroFragment(selection, mNext);
    }


    @Override
    public Box<MicroWizard<ChooseProviderMicroFragment.ProviderSelection>> boxed()
    {
        return new WizardBox(mNext);
    }


    private final static class WizardBox extends AbstractSingleBox<MicroWizard<LoginInfo>, MicroWizard<ChooseProviderMicroFragment.ProviderSelection>>
    {

        WizardBox(MicroWizard<LoginInfo> next)
        {
            super(next, ChooseProvider::new);
        }


        public final static Creator<Box<MicroWizard<ChooseProviderMicroFragment.ProviderSelection>>> CREATOR =
                new SingleBoxableBoxCreator<>(
                        WizardBox::new,
                        WizardBox[]::new);
    }
}

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

import com.smoothsync.smoothsetup.microfragments.WaitForReferrerMicroFragment;
import com.smoothsync.smoothsetup.utils.LoginRequest;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.AbstractDoubleBox;
import org.dmfs.android.microwizard.box.Box;


/**
 * @author Marten Gajda
 */
public final class WaitForReferrer implements MicroWizard<Void>
{
    private final MicroWizard<LoginRequest> mNext;
    private final MicroWizard<Void> mFallback;


    public WaitForReferrer(MicroWizard<LoginRequest> next, MicroWizard<Void> fallback)
    {
        mNext = next;
        mFallback = fallback;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, Void dummy)
    {
        return new WaitForReferrerMicroFragment(mNext, mFallback);
    }


    @Override
    public Box<MicroWizard<Void>> boxed()
    {
        return new WizardBox(mNext, mFallback);
    }


    private final static class WizardBox extends AbstractDoubleBox<MicroWizard<LoginRequest>, MicroWizard<Void>, MicroWizard<Void>>
    {
        private WizardBox(MicroWizard<LoginRequest> next, MicroWizard<Void> fallback)
        {
            super(next, fallback, WaitForReferrer::new);
        }


        public final static Creator<Box<MicroWizard<Void>>> CREATOR = new DoubleBoxableBoxCreator<>(WizardBox::new, WizardBox[]::new);
    }
}

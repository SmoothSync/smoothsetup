/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.wizardtransitions;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.WizardStep;

/**
 * A WizardTransition that just moves on to the next WizardStep using a swipe animation.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ForwardWizardTransition extends AbstractWizardTransition
{
    private final WizardStep mNextStep;


    public ForwardWizardTransition(WizardStep nextStep)
    {
        this.mNextStep = nextStep;
    }


    @Override
    public void apply(Context context, FragmentManager fragmentManager, WizardStep previousStep)
    {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (useRtl(context))
        {
            transaction.setCustomAnimations(R.anim.smoothsetup_enter_left, R.anim.smoothsetup_exit_right, R.anim.smoothsetup_enter_right,
                R.anim.smoothsetup_exit_left);
        }
        else
        {
            transaction.setCustomAnimations(R.anim.smoothsetup_enter_right, R.anim.smoothsetup_exit_left, R.anim.smoothsetup_enter_left,
                R.anim.smoothsetup_exit_right);
        }
        transaction.replace(R.id.wizards, mNextStep.fragment(context));
        if (fragmentManager.getBackStackEntryCount() > 0 || !previousStep.skipOnBack())
        {
            transaction.addToBackStack(mNextStep.skipOnBack() ? "skip" : null);
        }
        transaction.commit();
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mNextStep, flags);
    }

    public final static Creator<ForwardWizardTransition> CREATOR = new Creator<ForwardWizardTransition>()
    {
        @Override
        public ForwardWizardTransition createFromParcel(Parcel source)
        {
            return new ForwardWizardTransition((WizardStep) source.readParcelable(getClass().getClassLoader()));
        }


        @Override
        public ForwardWizardTransition[] newArray(int size)
        {
            return new ForwardWizardTransition[size];
        }
    };
}

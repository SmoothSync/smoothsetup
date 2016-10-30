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
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;


/**
 * An abstract WizardTransition that sends itself to a WizardActivity using a local broadcast.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractWizardTransition implements WizardTransition, Parcelable
{
    /**
     * The broadcast action that this transition uses.
     */
    public final static String ACTION_WIZARD_TRANSITION = "action-wizard-transition";

    /**
     * The name of the Parcelable extra that contains this transition.
     */
    public final static String EXTRA_WIZARD_TRANSITION = "wizard-transition";


    @Override
    public final void execute(Context context)
    {
        Intent intent = new Intent(ACTION_WIZARD_TRANSITION);
        intent.putExtra(EXTRA_WIZARD_TRANSITION, this);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}

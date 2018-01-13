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

package com.smoothsync.smoothsetup.services;

import android.content.Context;
import android.content.Intent;

import org.dmfs.android.microfragments.MicroFragment;


/**
 * Interface of a service which returns the initial {@link MicroFragment} of a wizard.
 *
 * @author Marten Gajda
 */
public interface WizardService
{
    /**
     * Returns the initial {@link MicroFragment} of the wizard.
     *
     * @param context
     *         A {@link Context}
     * @param intent
     *         The intent which was used to launch the wizard.
     *
     * @return A {@link MicroFragment}.
     */
    MicroFragment<?> initialMicroFragment(Context context, Intent intent);
}

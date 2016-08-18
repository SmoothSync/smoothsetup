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

package com.smoothsync.smoothsetup.model;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;


/**
 * Interface of a specific step of the wizard.
 */
public interface WizardStep extends Parcelable
{
	/**
	 * The Fragment argument that contains the WizardStep that a Fragment belongs to. This must be set on a wizard fragment.
	 */
	String ARG_WIZARD_STEP = "WIZARD_STEP";


	/**
	 * Returns the title of this step.
	 * 
	 * @param context
	 *            A {@link Context}.
	 * @return The localized wizard step title.
	 */
	String title(Context context);


	/**
	 * Returns the Fragment that represents this WizardStep.
	 *
	 * @param context
	 *            A {@link Context}.
	 * @return A {@link Fragment}.
	 */
	Fragment fragment(Context context);


	/**
	 * True if this step should be skipped when going back to the previous step.
	 * 
	 * @return
	 */
	boolean skipOnBack();

}

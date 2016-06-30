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
import android.support.v4.app.FragmentManager;

import com.smoothsync.smoothsetup.model.WizardStep;


/**
 * Represents an operation that switches from one WizardStep to another one.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface WizardTransition
{

	/**
	 * Executes this transition, switching to another WizardStep.
	 * 
	 * @param context
	 *            A Context.
	 */
	public void execute(Context context);


	/**
	 * Applies this transition to the given FragmentManager.
	 * 
	 * @param context
	 *            A Context.
	 * @param fragmentManager
	 *            The FragmentManager on which to perform the transition.
	 * @param previousStep
	 *            The previous WizardStep.
	 */
	public void apply(Context context, FragmentManager fragmentManager, WizardStep previousStep);

}

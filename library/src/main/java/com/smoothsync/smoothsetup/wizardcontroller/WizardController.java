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

package com.smoothsync.smoothsetup.wizardcontroller;

import com.smoothsync.smoothsetup.model.WizardStep;


/**
 * Interface of an object that controls the wizard.
 */
public interface WizardController
{
	/**
	 * Move on to the next step.
	 * 
	 * @param step
	 *            The next WizardStep to present to the user.
	 * @param canReturn
	 *            {@code true} to allow to return to the current step, {@code false} otherwise.
	 * @param isAutomatic
	 *            {@code true} if this is an automatic transition, {@code false} otherwise.
	 */
	public void forward(WizardStep step, boolean canReturn, boolean isAutomatic);


	/**
	 * Return to the previous step in the back stack.
	 */
	public void back();


	/**
	 * Restart the wizard with the given first step.
	 * 
	 * @param step
	 *            The first step to show.
	 * @param isAutomatic
	 *            {@code true} if this is an automatic transition, {@code false} otherwise.
	 */
	public void reset(WizardStep step, boolean isAutomatic);
}

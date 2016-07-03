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

import com.smoothsync.smoothsetup.model.WizardStep;


/**
 * A WizardTransition that returns to the previous step.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class BackWizardTransition extends AbstractWizardTransition
{

	/**
	 * Creates a WizardTransition that goes back to the previous step.
	 */
	public BackWizardTransition()
	{
	}


	@Override
	public void apply(Context context, FragmentManager fragmentManager, WizardStep previousStep)
	{
		fragmentManager.popBackStackImmediate();
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags)
	{

	}

	public final static Creator<BackWizardTransition> CREATOR = new Creator<BackWizardTransition>()
	{
		@Override
		public BackWizardTransition createFromParcel(Parcel source)
		{
			return new BackWizardTransition();
		}


		@Override
		public BackWizardTransition[] newArray(int size)
		{
			return new BackWizardTransition[size];
		}
	};
}

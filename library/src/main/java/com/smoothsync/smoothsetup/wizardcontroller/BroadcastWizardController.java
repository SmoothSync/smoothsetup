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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.smoothsync.smoothsetup.model.WizardStep;


/**
 * A simple {@link WizardController} that uses local broadcasts to send commands to the wizard host.
 */
public final class BroadcastWizardController implements WizardController
{
	public final static String NEXT_STEP_ACTION = "org.dmfs.NEXT_STEP";
	public final static String PREV_STEP_ACTION = "org.dmfs.PREV_STEP";
	public final static String RESTART_ACTION = "org.dmfs.RESTART";
	public final static String EXTRA_WIZARDSTEP = "org.dmfs.WIZARD_STEP";
	public final static String EXTRA_CAN_RETURN = "org.dmfs.CAN_RETURN";
	public final static String EXTRA_IS_AUTOMATIC = "org.dmfs.IS_AUTOMATIC";

	private final LocalBroadcastManager mLocalBroadcastManager;


	public BroadcastWizardController(Context context)
	{
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
	}


	@Override
	public void forward(WizardStep step, boolean canReturn, boolean isAutomatic)
	{
		Intent intent = new Intent(NEXT_STEP_ACTION);
		intent.putExtra(EXTRA_WIZARDSTEP, step);
		intent.putExtra(EXTRA_CAN_RETURN, canReturn);
		intent.putExtra(EXTRA_IS_AUTOMATIC, isAutomatic);
		mLocalBroadcastManager.sendBroadcast(intent);
	}


	@Override
	public void back()
	{
		Intent intent = new Intent(PREV_STEP_ACTION);
		mLocalBroadcastManager.sendBroadcast(intent);
	}


	@Override
	public void reset(WizardStep step, boolean isAutomatic)
	{
		Intent intent = new Intent(RESTART_ACTION);
		intent.putExtra(EXTRA_WIZARDSTEP, step);
		intent.putExtra(EXTRA_IS_AUTOMATIC, isAutomatic);
		mLocalBroadcastManager.sendBroadcast(intent);
	}
}

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

package com.smoothsync.smoothsetup.wizardsteps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.WizardStep;


/**
 * A WizardStep that shows a "setup complete" message.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class SetupCompleteWizardStep implements WizardStep
{

	public SetupCompleteWizardStep()
	{
	}


	@Override
	public String title(Context context)
	{
		return context.getString(R.string.smoothsetup_wizard_title_setup_completed);
	}


	@Override
	public boolean skipOnBack()
	{
		return false;
	}


	@Override
	public Fragment fragment(Context context)
	{
		Fragment result = new MessageFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(ARG_WIZARD_STEP, this);
		result.setArguments(arguments);
		result.setRetainInstance(true);
		return result;
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

	public final static Creator<SetupCompleteWizardStep> CREATOR = new Creator<SetupCompleteWizardStep>()
	{
		@Override
		public SetupCompleteWizardStep createFromParcel(Parcel source)
		{
			return new SetupCompleteWizardStep();
		}


		@Override
		public SetupCompleteWizardStep[] newArray(int size)
		{
			return new SetupCompleteWizardStep[size];
		}
	};

	/**
	 * A Fragment that shows a message.
	 */
	public static class MessageFragment extends Fragment implements View.OnClickListener
	{

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			View result = inflater.inflate(R.layout.smoothsetup_wizard_fragment_setup_completed, container, false);

			((TextView) result.findViewById(android.R.id.message))
				.setText(getString(R.string.smoothsetup_message_setup_completed, getString(getContext().getApplicationInfo().labelRes)));

			Button button = ((Button) result.findViewById(android.R.id.button1));
			button.setOnClickListener(this);

			return result;
		}


		@Override
		public void onClick(View v)
		{
			Activity activity = getActivity();
			activity.setResult(Activity.RESULT_OK);
			activity.finish();
		}
	}
}

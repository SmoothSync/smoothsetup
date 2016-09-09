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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.BasicHttpAuthorizationFactory;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardtransitions.ForwardWizardTransition;

import org.dmfs.httpessentials.exceptions.ProtocolException;


/**
 * A WizardStep that prompts the user to enter a password after Account has been chosen.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class PasswordWizardStep implements WizardStep
{
	private final static String ARG_ACCOUNT = "account";

	private final Account mAccount;


	public PasswordWizardStep(Account account)
	{
		mAccount = account;
	}


	@Override
	public String title(Context context)
	{
		return context.getString(R.string.smoothsetup_wizard_title_enter_password);
	}


	@Override
	public boolean skipOnBack()
	{
		return false;
	}


	@Override
	public Fragment fragment(Context context)
	{
		Fragment result = new PasswordFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(ARG_ACCOUNT, mAccount);
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
		dest.writeParcelable(mAccount, flags);
	}

	public final static Creator<PasswordWizardStep> CREATOR = new Creator<PasswordWizardStep>()
	{
		@Override
		public PasswordWizardStep createFromParcel(Parcel source)
		{
			return new PasswordWizardStep((Account) source.readParcelable(getClass().getClassLoader()));
		}


		@Override
		public PasswordWizardStep[] newArray(int size)
		{
			return new PasswordWizardStep[0];
		}
	};

	/**
	 * A Fragment that prompts the user for his or her password.
	 */
	public final static class PasswordFragment extends Fragment implements View.OnClickListener
	{

		private Account mAccount;
		private EditText mPassword;
		private Button mButton;


		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			View result = inflater.inflate(R.layout.smoothsetup_wizard_fragment_password, container, false);

			mAccount = getArguments().getParcelable(ARG_ACCOUNT);

			try
			{
				((TextView) result.findViewById(android.R.id.message))
					.setText(getContext().getString(R.string.smoothsetup_prompt_enter_password, mAccount.provider().name()));
			}
			catch (ProtocolException e)
			{
				throw new RuntimeException("can't get provider name", e);
			}

			mButton = (Button) result.findViewById(R.id.button);
			mButton.setOnClickListener(this);

			mPassword = (EditText) result.findViewById(android.R.id.input);
			mPassword.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
					// nothing to do
				}


				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					// nothing to do
				}


				@Override
				public void afterTextChanged(Editable s)
				{
					mButton.setEnabled(!s.toString().isEmpty());
				}
			});

			mButton.setEnabled(!mPassword.getText().toString().isEmpty());
			return result;
		}


		@Override
		public void onClick(View v)
		{
			if (v.getId() == R.id.button)
			{
				// verify entered password
				new ForwardWizardTransition(
					(new ApproveAuthorizationWizardStep(mAccount, new BasicHttpAuthorizationFactory(mAccount.accountId(), mPassword.getText().toString()))))
						.execute(getContext());
			}
		}

	}

}

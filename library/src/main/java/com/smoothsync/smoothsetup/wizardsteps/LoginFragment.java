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
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.TextView;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.AbstractAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.setupbuttons.AbstractSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;
import com.smoothsync.smoothsetup.wizardtransitions.ForwardWizardTransition;

import org.dmfs.httpclient.exceptions.ProtocolException;


/**
 * A generic Login form with a text edit field and one or more buttons.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class LoginFragment extends Fragment implements SetupButtonAdapter.OnProviderSelectListener
{
	public final static String ARG_LOGIN_FORM_ADAPTER_FACTORY = "login-form-adapter-factory";
	public final static String ARG_ACCOUNT = "account";

	public interface LoginFormAdapterFactory extends Parcelable
	{
		public <T extends Adapter & Filterable> T autoCompleteAdapter(Context context);


		public <T extends RecyclerView.Adapter<BasicButtonViewHolder>, SetupButtonAdapter> T setupButtonAdapter(Context context,
			com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter.OnProviderSelectListener providerSelectListener);


		public String promptText(Context context);
	}

	AutoCompleteTextView mLogin;


	public static Fragment newInstance(WizardStep wizardStep, LoginFormAdapterFactory loginFormAdapterFactory, String account)
	{
		Fragment result = new LoginFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(WizardStep.ARG_WIZARD_STEP, wizardStep);
		arguments.putParcelable(ARG_LOGIN_FORM_ADAPTER_FACTORY, loginFormAdapterFactory);
		arguments.putString(ARG_ACCOUNT, account);
		result.setArguments(arguments);
		result.setRetainInstance(true);
		return result;
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View result = inflater.inflate(R.layout.smoothsetup_wizard_fragment_login, container, false);

		mLogin = (AutoCompleteTextView) result.findViewById(android.R.id.input);

		LoginFormAdapterFactory loginFormAdapterFactory = (LoginFormAdapterFactory) getArguments().getParcelable(ARG_LOGIN_FORM_ADAPTER_FACTORY);
		AbstractAutoCompleteAdapter autoCompleteAdapter = loginFormAdapterFactory.autoCompleteAdapter(getContext());
		mLogin.setAdapter(autoCompleteAdapter);

		RecyclerView list = (RecyclerView) result.findViewById(android.R.id.list);

		list.setHasFixedSize(true);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		llm.setOrientation(LinearLayoutManager.VERTICAL);
		list.setLayoutManager(llm);

		final AbstractSmoothSetupAdapter adapter = loginFormAdapterFactory.setupButtonAdapter(getContext(), this);
		list.setAdapter(adapter);

		mLogin.addTextChangedListener(new TextWatcher()
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
				String login = s.toString();
				final int atPos = login.indexOf('@');
				if (atPos > 0 && atPos < login.length() - 1)
				{
					try
					{
						adapter.update(login.substring(atPos + 1));
					}
					catch (ProtocolException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		mLogin.setText(getArguments().getString(ARG_ACCOUNT));

		((TextView) result.findViewById(android.R.id.message)).setText(loginFormAdapterFactory.promptText(getContext()));

		return result;
	}


	@Override
	public void onProviderSelected(Provider provider)
	{
		new ForwardWizardTransition(new PasswordWizardStep(new BasicAccount(mLogin.getText().toString(), provider))).execute(getContext());
	}
}

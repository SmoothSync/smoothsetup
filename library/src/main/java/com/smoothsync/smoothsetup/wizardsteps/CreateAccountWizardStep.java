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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.BasicHttpAuthorizationFactory;
import com.smoothsync.smoothsetup.model.HttpAuthorizationFactory;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.services.AccountService;
import com.smoothsync.smoothsetup.services.FutureAidlServiceConnection;
import com.smoothsync.smoothsetup.services.FutureServiceConnection;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;
import com.smoothsync.smoothsetup.wizardtransitions.AutomaticWizardTransition;


/**
 * A {@link WizardStep} to create the account and finish the setup procedure.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class CreateAccountWizardStep implements WizardStep
{
	private final static String ARG_ACCOUNT = "account";
	private final static String ARG_AUTH_FACTORY = "auth_factory";

	private final Account mAccount;
	private final HttpAuthorizationFactory mHttpAuthorizationFactory;


	public CreateAccountWizardStep(Account account, HttpAuthorizationFactory httpAuthorizationFactory)
	{
		mAccount = account;
		mHttpAuthorizationFactory = httpAuthorizationFactory;
	}


	@Override
	public String title(Context context)
	{
		return context.getString(R.string.smoothsetup_completing_setup);
	}


	@Override
	public boolean skipOnBack()
	{
		return true;
	}


	@Override
	public Fragment fragment(Context context)
	{
		Fragment result = new LoadFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_ACCOUNT, mAccount);
		args.putParcelable(ARG_AUTH_FACTORY, mHttpAuthorizationFactory);
		args.putParcelable(ARG_WIZARD_STEP, this);
		result.setArguments(args);
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
		dest.writeParcelable(mAccount, 0);
		dest.writeParcelable(mHttpAuthorizationFactory, 0);
	}

	public final static Creator CREATOR = new Creator()
	{
		@Override
		public CreateAccountWizardStep createFromParcel(Parcel source)
		{
			ClassLoader classLoader = getClass().getClassLoader();
			return new CreateAccountWizardStep((Account) source.readParcelable(classLoader), (HttpAuthorizationFactory) source.readParcelable(classLoader));
		}


		@Override
		public CreateAccountWizardStep[] newArray(int size)
		{
			return new CreateAccountWizardStep[size];
		}
	};

	public static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Boolean>
	{
		private final static int DELAY_WAIT_MESSAGE = 2500;

		private View mWaitMessage;
		private Handler mHandler = new Handler();
		private FutureServiceConnection<AccountService> mAccountService;


		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			View result = inflater.inflate(R.layout.smoothsetup_wizard_fragment_loading, container, false);
			mWaitMessage = result.findViewById(android.R.id.message);
			mHandler.postDelayed(mShowWaitMessage, DELAY_WAIT_MESSAGE);
			return result;
		}


		@Override
		public void onCreate(@Nullable Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			mAccountService = new FutureAidlServiceConnection<AccountService>(getContext(),
				new Intent("com.smoothsync.action.ACCOUNT_SERVICE").setPackage(getContext().getPackageName()),
				new FutureAidlServiceConnection.StubProxy<AccountService>()
				{
					@Override
					public AccountService asInterface(IBinder service)
					{
						return AccountService.Stub.asInterface(service);
					}
				});
			new ThrowingAsyncTask<Void, Void, Boolean>(this)
			{
				@Override
				protected Boolean doInBackgroundWithException(Void... params) throws Exception
				{
					AccountService service = mAccountService.service(10000);
					Bundle bundle = new Bundle();
					bundle.putParcelable("account", ((Account) getArguments().getParcelable(ARG_ACCOUNT)));
					bundle.putParcelable("auth_factory", ((BasicHttpAuthorizationFactory) getArguments().getParcelable(ARG_AUTH_FACTORY)));
					service.createAccount(bundle);
					return true;
				}
			}.execute();
		}


		@Override
		public void onDestroy()
		{
			mAccountService.disconnect();
			super.onDestroy();
		}


		@Override
		public void onDetach()
		{
			mHandler.removeCallbacks(mShowWaitMessage);
			super.onDetach();
		}

		private final Runnable mShowWaitMessage = new Runnable()
		{
			@Override
			public void run()
			{
				mWaitMessage.animate().alpha(1f).start();
			}
		};


		@Override
		public void onResult(AsyncTaskResult<Boolean> result)
		{
			if (isAdded())
			{
				try
				{
					result.value();
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();
				}
				catch (Exception e)
				{
					new AutomaticWizardTransition(new ErrorRetryWizardStep("Unexpected Exception:\n\n" + e.getMessage())).execute(getContext());
				}
			}
		}
	}
}

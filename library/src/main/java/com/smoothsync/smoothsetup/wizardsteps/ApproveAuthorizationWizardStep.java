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
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.HttpAuthorizationFactory;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;
import com.smoothsync.smoothsetup.wizardtransitions.AutomaticWizardTransition;

import org.dmfs.httpessentials.HttpMethod;
import org.dmfs.httpessentials.HttpStatus;
import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.client.HttpRequestEntity;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.client.HttpResponse;
import org.dmfs.httpessentials.client.HttpResponseHandler;
import org.dmfs.httpessentials.entities.EmptyHttpRequestEntity;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.FollowRedirectPolicy;
import org.dmfs.httpessentials.executors.following.policies.Secure;
import org.dmfs.httpessentials.executors.retrying.Retrying;
import org.dmfs.httpessentials.executors.retrying.policies.DefaultRetryPolicy;
import org.dmfs.httpessentials.headers.EmptyHeaders;
import org.dmfs.httpessentials.headers.Headers;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.httpessentials.httpurlconnection.factories.DefaultHttpUrlConnectionFactory;
import org.dmfs.httpessentials.httpurlconnection.factories.decorators.Finite;
import org.dmfs.httpessentials.responsehandlers.TrivialResponseHandler;
import org.dmfs.iterators.AbstractFilteredIterator;
import org.dmfs.iterators.FilteredIterator;

import java.io.IOException;


/**
 * A {@link WizardStep} to verify a user's authorization.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ApproveAuthorizationWizardStep implements WizardStep
{
	private final static String ARG_ACCOUNT = "account";
	private final static String ARG_AUTH_FACTORY = "auth_factory";

	private final Account mAccount;
	private final HttpAuthorizationFactory mHttpAuthorizationFactory;


	public ApproveAuthorizationWizardStep(Account account, HttpAuthorizationFactory httpAuthorizationFactory)
	{
		mAccount = account;
		mHttpAuthorizationFactory = httpAuthorizationFactory;
	}


	@Override
	public String title(Context context)
	{
		return context.getString(R.string.smoothsetup_authenticating);
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
		public ApproveAuthorizationWizardStep createFromParcel(Parcel source)
		{
			ClassLoader classLoader = getClass().getClassLoader();
			return new ApproveAuthorizationWizardStep((Account) source.readParcelable(classLoader),
				(HttpAuthorizationFactory) source.readParcelable(classLoader));
		}


		@Override
		public ApproveAuthorizationWizardStep[] newArray(int size)
		{
			return new ApproveAuthorizationWizardStep[size];
		}
	};

	public static class LoadFragment extends Fragment implements ThrowingAsyncTask.OnResultCallback<Boolean>
	{
		private final static int DELAY_WAIT_MESSAGE = 2500;

		private View mWaitMessage;
		private Handler mHandler = new Handler();


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
			Context context = getContext();
			new ThrowingAsyncTask<Void, Void, Boolean>(this)
			{
				@Override
				protected Boolean doInBackgroundWithException(Void[] params) throws Exception
				{
					HttpRequestExecutor executor = new Following(
						new Retrying(new HttpUrlConnectionExecutor(new Finite(new DefaultHttpUrlConnectionFactory(), 10000, 30000)), new DefaultRetryPolicy(3)),
						new Secure(new FollowRedirectPolicy(5)));

					return executor.execute(new FilteredIterator<>(((Account) getArguments().getParcelable(ARG_ACCOUNT)).provider().services(),
						new AbstractFilteredIterator.IteratorFilter<Service>()
						{
							@Override
							public boolean iterate(Service element)
							{
								return "com.smoothsync.authenticate".equals(element.serviceType());
							}
						}).next().uri(), ((HttpAuthorizationFactory) getArguments().getParcelable(ARG_AUTH_FACTORY)).authenticate(new HttpRequest<Boolean>()
						{
							@Override
							public HttpMethod method()
							{
								return HttpMethod.GET;
							}


							@Override
							public Headers headers()
							{
								return EmptyHeaders.INSTANCE;
							}


							@Override
							public HttpRequestEntity requestEntity()
							{
								return EmptyHttpRequestEntity.INSTANCE;
							}


							@Override
							public HttpResponseHandler<Boolean> responseHandler(HttpResponse response) throws IOException, ProtocolError, ProtocolException
							{
								return new TrivialResponseHandler<Boolean>(!HttpStatus.UNAUTHORIZED.equals(response.status()));
							}
						}));
				}
			}.execute();
		}


		@Override
		public void onDetach()
		{
			mHandler.removeCallbacks(mShowWaitMessage);
			super.onDetach();
		}


		@Override
		public void onResult(final AsyncTaskResult<Boolean> result)
		{
			if (isAdded())
			{
				try
				{
					if (result.value())
					{
						new AutomaticWizardTransition(new CreateAccountWizardStep(((Account) getArguments().getParcelable(ARG_ACCOUNT)),
							((HttpAuthorizationFactory) getArguments().getParcelable(ARG_AUTH_FACTORY)))).execute(getContext());
					}
					else
					{
						new AutomaticWizardTransition(new ErrorRetryWizardStep(getString(R.string.smoothsetup_authentication_error))).execute(getContext());
					}
				}
				catch (Exception e)
				{
					new AutomaticWizardTransition(new ErrorRetryWizardStep(getString(R.string.smoothsetup_authentication_error))).execute(getContext());
				}
			}
		}

		private final Runnable mShowWaitMessage = new Runnable()
		{
			@Override
			public void run()
			{
				mWaitMessage.animate().alpha(1f).start();
			}
		};
	}
}

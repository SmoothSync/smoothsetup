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

package com.smoothsync.smoothsetup.demo;

import android.content.Context;

import com.smoothsync.api.ProductionApi;
import com.smoothsync.api.ProductionApiClient;
import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.smoothsetup.services.AbstractSmoothSyncApiService;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.NeverFollowRedirectPolicy;
import org.dmfs.httpessentials.executors.retrying.Retrying;
import org.dmfs.httpessentials.executors.retrying.policies.DefaultRetryPolicy;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.httpessentials.httpurlconnection.factories.DefaultHttpUrlConnectionFactory;
import org.dmfs.httpessentials.httpurlconnection.factories.decorators.Finite;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2ClientCredentials;


/**
 * A Service that provides the SmoothSync API.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class SmoothSyncApiService extends AbstractSmoothSyncApiService
{
	public SmoothSyncApiService()
	{
		super(new SmoothSyncApiFactory()
		{
			@Override
			public SmoothSyncApi smoothSyncApi(Context context)
			{
				HttpRequestExecutor executor = new Following(
					new Retrying(new HttpUrlConnectionExecutor(new Finite(new DefaultHttpUrlConnectionFactory(), 10000, 30000)), new DefaultRetryPolicy(3)),
					new NeverFollowRedirectPolicy());

				OAuth2ClientCredentials clientCreds = new BasicOAuth2ClientCredentials("c5afc71ab8d046229d05275f0f01c03a",
					"c1b7aa8d571c4975b6a4e8099ca052c05c239015a24845f7bf7f4c8221cfafa3");

				ProductionApiClient client = new ProductionApiClient(clientCreds);

				return new ProductionApi(executor, client);
			}
		});
	}
}

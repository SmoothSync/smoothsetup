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

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.smoothsetup.services.AbstractSmoothSyncApiService;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.FollowRedirectPolicy;
import org.dmfs.httpessentials.executors.following.policies.Secure;
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
 * @author Marten Gajda
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
                        new Secure(new FollowRedirectPolicy(5)));

                OAuth2ClientCredentials clientCreds = new BasicOAuth2ClientCredentials("e71c750d1e544665ad0ebfd598260b51",
                        "f7cb392dd43945de8fd332f80a7885db96851e6e67c64d5a82f8fc646bd25e8e");

                DemoApiClient client = new DemoApiClient(clientCreds);

                return new DemoApi(executor, client);
            }
        });
    }
}

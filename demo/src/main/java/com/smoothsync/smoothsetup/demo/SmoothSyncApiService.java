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

import com.smoothsync.smoothsetup.services.delegating.DelegatingSmoothSyncApiService;
import com.smoothsync.smoothsetup.utils.DefaultOkHttpGenerator;
import com.smoothsync.smoothsetup.utils.Finite;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.FollowPolicy;
import org.dmfs.httpessentials.executors.following.policies.Limited;
import org.dmfs.httpessentials.executors.following.policies.Secure;
import org.dmfs.httpessentials.executors.retrying.Retrying;
import org.dmfs.httpessentials.executors.retrying.policies.DefaultRetryPolicy;
import org.dmfs.httpessentials.okhttp.OkHttpExecutor;
import org.dmfs.jems.single.elementary.Frozen;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2ClientCredentials;


/**
 * A Service that provides the SmoothSync API.
 *
 * @author Marten Gajda
 */
public class SmoothSyncApiService extends DelegatingSmoothSyncApiService
{
    public SmoothSyncApiService()
    {
        super(context ->
        {
            HttpRequestExecutor executor = new Following(
                new Retrying(
                    new OkHttpExecutor(
                        new Frozen<>(
                            new Finite(new
                                DefaultOkHttpGenerator(), 10000, 30000).next()::build)),
                    new DefaultRetryPolicy(3)),
                new Limited(5, new Secure(new FollowPolicy())));

            OAuth2ClientCredentials clientCreds = new BasicOAuth2ClientCredentials("e71c750d1e544665ad0ebfd598260b51",
                "f7cb392dd43945de8fd332f80a7885db96851e6e67c64d5a82f8fc646bd25e8e");

            DemoApiClient client = new DemoApiClient(clientCreds);

            return new DemoApi(executor, client);
        });
        //super(new StaticApiFactory(new ProfileProviderData()));
    }
}

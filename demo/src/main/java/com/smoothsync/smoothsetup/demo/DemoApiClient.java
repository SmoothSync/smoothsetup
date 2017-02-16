/*
 * Copyright (c) 2017 dmfs GmbH
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
 */

package com.smoothsync.smoothsetup.demo;

import com.smoothsync.api.AbstractApiClient;

import org.dmfs.oauth2.client.BasicOAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.OAuth2AuthorizationProvider;
import org.dmfs.oauth2.client.OAuth2ClientCredentials;
import org.dmfs.rfc5545.Duration;

import java.net.URI;


/**
 * A client to the demo API.
 *
 * @author Marten Gajda
 */
public final class DemoApiClient extends AbstractApiClient
{
    private final static OAuth2AuthorizationProvider AUTH_PROVIDER = new BasicOAuth2AuthorizationProvider(null,
            URI.create("https://smoothsync-services-test.appspot.com/auth/token"), new Duration(1, 0, 3600));


    public DemoApiClient(OAuth2ClientCredentials credentials)
    {
        super(AUTH_PROVIDER, credentials);
    }

}

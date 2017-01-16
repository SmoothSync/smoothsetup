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

import com.smoothsync.api.AbstractSmoothSyncApi;

import org.dmfs.httpessentials.client.HttpRequestExecutor;

import java.net.URI;


/**
 * A demo API that might support upcoming features in an early version.
 *
 * @author Marten Gajda
 */
public final class DemoApi extends AbstractSmoothSyncApi
{
    private final static URI API_URI = URI.create("https://smoothsync-services-test.appspot.com/api/v1/");


    public DemoApi(HttpRequestExecutor executor, DemoApiClient apiClient)
    {
        super(executor, apiClient, API_URI);
    }

}

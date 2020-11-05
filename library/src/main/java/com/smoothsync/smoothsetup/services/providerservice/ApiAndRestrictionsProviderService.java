/*
 * Copyright (c) 2020 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.providerservice;

import android.annotation.TargetApi;
import android.os.Build;

import com.smoothsync.smoothsetup.services.delegating.DelegatingProviderService;
import com.smoothsync.smoothsetup.services.providerservice.functions.AllOf;
import com.smoothsync.smoothsetup.services.providerservice.functions.ApiProviders;
import com.smoothsync.smoothsetup.services.providerservice.functions.EmptyProviders;
import com.smoothsync.smoothsetup.services.providerservice.functions.RestrictionsProviders;
import com.smoothsync.smoothsetup.utils.Alternative;


/**
 * @author Marten Gajda
 */
public final class ApiAndRestrictionsProviderService extends DelegatingProviderService
{
    @TargetApi(23)
    public ApiAndRestrictionsProviderService()
    {
        super(new AllOf(
                new Alternative<>(c -> Build.VERSION.SDK_INT >= 23, new RestrictionsProviders(), new EmptyProviders()),
                new ApiProviders()));
    }
}

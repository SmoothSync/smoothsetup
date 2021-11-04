/*
 * Copyright (c) 2021 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.defaults;

import com.smoothsync.smoothsetup.services.delegating.DelegatingSetupChoicesService;
import com.smoothsync.smoothsetup.services.setupchoicesservice.Cached;
import com.smoothsync.smoothsetup.services.setupchoicesservice.Composite;
import com.smoothsync.smoothsetup.services.setupchoicesservice.DiscoveryServices;
import com.smoothsync.smoothsetup.services.setupchoicesservice.ManualSetup;
import com.smoothsync.smoothsetup.services.setupchoicesservice.ProviderSelection;
import com.smoothsync.smoothsetup.services.setupchoicesservice.Slow;


public final class DefaultSetupChoicesService extends DelegatingSetupChoicesService
{
    public DefaultSetupChoicesService()
    {
        super(context ->
            new Composite(
                new Cached(new Slow(new DiscoveryServices(context))),
                new ProviderSelection(context),
                new ManualSetup(context)));
    }
}

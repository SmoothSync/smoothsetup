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

package com.smoothsync.smoothsetup.microfragments.appspecificpassword;

import com.smoothsync.smoothsetup.microfragments.appspecificpassword.providers.FastmailPasswordProbe;
import com.smoothsync.smoothsetup.microfragments.appspecificpassword.providers.ICloudPasswordProbe;

import java.net.URI;


/**
 * A {@link PasswordProbeFactory}.
 *
 * @author Marten Gajda
 */
public class DefaultPasswordProbeFactory implements PasswordProbeFactory
{
    @Override
    public AppSpecificPasswordProbe forUrl(URI url)
    {
        switch (url.getHost())
        {
            case "appleid.apple.com":
                return new ICloudPasswordProbe();
            case "www.fastmail.com":
                return new FastmailPasswordProbe();
            default:
                return null;
        }
    }
}

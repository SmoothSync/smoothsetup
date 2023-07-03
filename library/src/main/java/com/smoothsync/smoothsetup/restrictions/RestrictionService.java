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

package com.smoothsync.smoothsetup.restrictions;

import android.os.Bundle;

import com.smoothsync.api.model.Service;

import org.dmfs.jems2.Optional;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.single.Backed;

import java.net.URI;
import java.security.KeyStore;

import static org.dmfs.jems2.optional.Absent.absent;


public final class RestrictionService implements Service
{
    private final Bundle mBundle;


    public RestrictionService(Bundle bundle)
    {
        mBundle = bundle;
    }


    @Override
    public String name()
    {
        return new Backed<>(new NullSafe<>(mBundle.getString("name")), "").value();
    }


    @Override
    public String serviceType()
    {
        return mBundle.getString("service-type");
    }


    @Override
    public URI uri()
    {
        return URI.create(mBundle.getString("uri"));
    }


    @Override
    public Optional<KeyStore> keyStore()
    {
        return absent();
    }
}

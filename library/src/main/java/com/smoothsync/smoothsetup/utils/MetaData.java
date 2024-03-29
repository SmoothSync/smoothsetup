/*
 * Copyright (c) 2018 dmfs GmbH
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

package com.smoothsync.smoothsetup.utils;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import org.dmfs.jems2.Single;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.single.Backed;


/**
 * A {@link Single} app meta data {@link Bundle}.
 *
 * @author Marten Gajda
 */
public final class MetaData implements Single<Bundle>
{
    private final Single<android.content.pm.ApplicationInfo> mApplicationInfo;


    public MetaData(Single<ApplicationInfo> applicationInfo)
    {
        mApplicationInfo = applicationInfo;
    }


    @Override
    public Bundle value()
    {
        return new Backed<>(new NullSafe<>(mApplicationInfo.value().metaData), Bundle.EMPTY).value();
    }
}

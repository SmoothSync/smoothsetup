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

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.dmfs.jems2.Single;


/**
 * @author Marten Gajda
 */
public final class StringMeta implements Single<String>
{
    private final String mKey;
    private final Single<Bundle> mMetaData;


    public StringMeta(Context context, String key)
    {
        this(key, new ApplicationInfo(context, PackageManager.GET_META_DATA));
    }


    private StringMeta(String key, ApplicationInfo mAppInfo)
    {
        this(key, new MetaData(mAppInfo));
    }


    private StringMeta(String key, Single<Bundle> metaData)
    {
        mKey = key;
        mMetaData = metaData;
    }


    @Override
    public String value()
    {
        return mMetaData.value().getString(mKey);
    }
}

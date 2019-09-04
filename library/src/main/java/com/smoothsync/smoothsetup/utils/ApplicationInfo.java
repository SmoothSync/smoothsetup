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

import org.dmfs.jems.single.Single;

import androidx.annotation.NonNull;


/**
 * @author Marten Gajda
 */
public final class ApplicationInfo implements Single<android.content.pm.ApplicationInfo>
{
    private final Context mContext;
    private final int mFlags;


    public ApplicationInfo(@NonNull Context context, int flags)
    {
        mContext = context.getApplicationContext();
        mFlags = flags;
    }


    @NonNull
    @Override
    public android.content.pm.ApplicationInfo value()
    {
        try
        {
            return mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), mFlags);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("Couldn't find own package name.");
        }
    }
}

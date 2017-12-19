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

package com.smoothsync.smoothsetup.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import org.dmfs.jems.single.Single;

import java.util.Locale;


/**
 * @author Marten Gajda
 */
public class ActivityInfo implements Single<android.content.pm.ActivityInfo>
{
    private final Activity mActivity;
    private final int mFlags;


    public ActivityInfo(Activity activity, int flags)
    {
        mActivity = activity;
        mFlags = flags;
    }


    @Override
    public android.content.pm.ActivityInfo value()
    {
        try
        {
            return mActivity.getPackageManager().getActivityInfo(new ComponentName(mActivity, mActivity.getClass()), mFlags);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException(String.format(Locale.ENGLISH, "Could not load ActivityInfo of activity %s.", mActivity.getClass().getCanonicalName()));
        }
    }
}

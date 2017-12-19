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

import android.content.Context;

import org.dmfs.jems.single.Single;


/**
 * A {@link Single} of the app label of the current app.
 *
 * @author Marten Gajda
 */
public final class AppLabel implements Single<String>
{
    private final Context mContext;


    public AppLabel(Context context)
    {
        // retain application context to avoid context leaks
        mContext = context.getApplicationContext();
    }


    @Override
    public String value()
    {
        return mContext.getString(mContext.getApplicationInfo().labelRes);
    }
}

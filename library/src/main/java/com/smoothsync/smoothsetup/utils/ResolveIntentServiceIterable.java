/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.dmfs.iterators.EmptyIterator;

import java.util.Iterator;
import java.util.List;


/**
 * An {@link Iterable} that iterates all {@link ResolveInfo} elements of a given service {@link Intent}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ResolveIntentServiceIterable implements Iterable<ResolveInfo>
{
    private final Context mContext;
    private final Intent mIntent;


    public ResolveIntentServiceIterable(Context context, Intent intent)
    {
        mContext = context;
        mIntent = new Intent(intent);
    }


    @Override
    public Iterator<ResolveInfo> iterator()
    {
        List<ResolveInfo> resolveInfos = mContext.getPackageManager()
                .queryIntentServices(mIntent, PackageManager.GET_META_DATA | PackageManager.GET_RESOLVED_FILTER);
        return resolveInfos == null ? EmptyIterator.<ResolveInfo>instance() : resolveInfos.iterator();
    }
}

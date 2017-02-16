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
import android.content.Intent;
import android.content.pm.ResolveInfo;

import org.dmfs.iterators.AbstractConvertedIterator;
import org.dmfs.iterators.ConvertedIterator;

import java.util.Iterator;


/**
 * An {@link Iterable} of direct service {@link Intent}s for the given indirect service intent.
 *
 * @author Marten Gajda
 */
public final class IndirectServiceIntentIterable implements Iterable<Intent>
{
    private final Intent mIntent;
    private final Iterable<ResolveInfo> mResolveInfo;


    public IndirectServiceIntentIterable(Context context, Intent intent)
    {
        mIntent = new Intent(intent);
        mResolveInfo = new ResolveIntentServiceIterable(context, intent);
    }


    @Override
    public Iterator<Intent> iterator()
    {
        return new ConvertedIterator<>(mResolveInfo.iterator(), new AbstractConvertedIterator.Converter<Intent, ResolveInfo>()
        {
            @Override
            public Intent convert(ResolveInfo element)
            {
                if (element.serviceInfo == null)
                {
                    throw new RuntimeException("ResolveInfo for Service Intent has no serviceInfo.");
                }
                return new Intent(mIntent).setClassName(element.serviceInfo.packageName, element.serviceInfo.name);
            }
        });
    }
}

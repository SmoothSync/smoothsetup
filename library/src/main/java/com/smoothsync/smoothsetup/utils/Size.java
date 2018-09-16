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

import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.elementary.Frozen;
import org.dmfs.jems.single.elementary.Reduced;


/**
 * @author Marten Gajda
 */
public final class Size extends Number
{
    private final Single<Integer> mCount;


    public Size(Iterable<?> iterable)
    {
        mCount = new Frozen<>(new Reduced<>(0, (count, element) -> count + 1, iterable));
    }


    @Override
    public int intValue()
    {
        return mCount.value();
    }


    @Override
    public long longValue()
    {
        return mCount.value();
    }


    @Override
    public float floatValue()
    {
        return mCount.value();
    }


    @Override
    public double doubleValue()
    {
        return mCount.value();
    }
}

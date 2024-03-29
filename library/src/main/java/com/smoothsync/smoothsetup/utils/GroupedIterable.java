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

import org.dmfs.jems2.Function;

import java.util.Iterator;

import androidx.annotation.NonNull;


/**
 * @author Marten Gajda
 */
public final class GroupedIterable<T, V> implements Iterable<Iterable<T>>
{
    private final Iterable<T> mDelegate;
    private final Function<T, V> mCriterionFunction;


    public GroupedIterable(Iterable<T> mDelegate, Function<T, V> mCriterionFunction)
    {
        this.mDelegate = mDelegate;
        this.mCriterionFunction = mCriterionFunction;
    }


    @NonNull
    @Override
    public Iterator<Iterable<T>> iterator()
    {
        return new GroupedIterator<>(mDelegate.iterator(), mCriterionFunction);
    }
}

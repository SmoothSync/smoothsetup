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

import org.dmfs.iterators.AbstractBaseIterator;
import org.dmfs.jems.function.Function;
import org.dmfs.optional.Next;
import org.dmfs.optional.Optional;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * @author Marten Gajda
 */
public final class GroupedIterator<T, V> extends AbstractBaseIterator<Iterable<T>>
{
    private final Iterator<T> mDelegate;
    private final Function<T, V> mCriterionFunction;
    private Optional<T> mNext;


    public GroupedIterator(Iterator<T> delegate, Function<T, V> criterionFunction)
    {
        mDelegate = delegate;
        mCriterionFunction = criterionFunction;
        mNext = new Next<>(delegate);
    }


    @Override
    public boolean hasNext()
    {
        return mNext.isPresent();
    }


    @Override
    public Iterable<T> next()
    {
        if (!mNext.isPresent())
        {
            throw new NoSuchElementException("No more element to iterate");
        }
        T prev = mNext.value();
        List<T> group = new LinkedList<>();
        group.add(prev);

        mNext = new Next<>(mDelegate);
        while (mNext.isPresent() && mCriterionFunction.value(prev).equals(mCriterionFunction.value(mNext.value())))
        {
            group.add(mNext.value());
            mNext = new Next<>(mDelegate);
        }

        return group;
    }
}

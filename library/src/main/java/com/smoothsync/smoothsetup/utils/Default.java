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

import org.dmfs.iterators.SingletonIterator;
import org.dmfs.jems2.iterator.BaseIterator;

import java.util.Iterator;


/**
 * An {@link Iterator} decorator that always returns at least one element.
 *
 * @author Marten Gajda
 */
public final class Default<E> extends BaseIterator<E>
{
    private final Iterator<E> mDelegate;


    public Default(Iterator<E> delegate, E defaultValue)
    {
        mDelegate = delegate.hasNext() ? delegate : new SingletonIterator<>(defaultValue);
    }


    @Override
    public boolean hasNext()
    {
        return mDelegate.hasNext();
    }


    @Override
    public E next()
    {
        return mDelegate.next();
    }
}

/*
 * Copyright (c) 2020 dmfs GmbH
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

import org.dmfs.jems2.Predicate;
import org.dmfs.jems2.iterable.Sieved;
import org.dmfs.jems2.optional.First;


/**
 * @author Marten Gajda
 */
public final class HasAny<T> implements Predicate<Iterable<? extends T>>
{
    private final Predicate<? super T> mDelegate;


    public HasAny(Predicate<? super T> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public boolean satisfiedBy(Iterable<? extends T> testedInstance)
    {
        return new First<>(new Sieved<>(mDelegate, testedInstance)).isPresent();
    }
}

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

import org.dmfs.jems.function.Function;
import org.dmfs.jems.predicate.Predicate;


/**
 * @author Marten Gajda
 */
public final class Alternative<Argument, Result> implements Function<Argument, Result>
{
    private final Predicate<? super Argument> mPredicate;
    private final Function<? super Argument, ? extends Result> mDelegate;
    private final Function<? super Argument, ? extends Result> mAlternative;


    public Alternative(
            Predicate<? super Argument> predicate,
            Function<? super Argument, ? extends Result> delegate,
            Function<? super Argument, ? extends Result> alternative)
    {
        mPredicate = predicate;
        mDelegate = delegate;
        mAlternative = alternative;
    }


    @Override
    public Result value(Argument argument)
    {
        return mPredicate.satisfiedBy(argument) ? mDelegate.value(argument) : mAlternative.value(argument);
    }
}

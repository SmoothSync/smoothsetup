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

import org.dmfs.httpessentials.types.Link;
import org.dmfs.iterators.AbstractBaseIterator;
import org.dmfs.iterators.Filter;
import org.dmfs.iterators.decorators.Filtered;

import java.util.Iterator;


/**
 * An Iterator that only returns {@link Link}s with a specific rel-type.
 *
 * @author Marten Gajda
 */
public final class Related extends AbstractBaseIterator<Link>
{
    private final Iterator<Link> mDelegate;


    public Related(Iterator<Link> delegate, final String rel)
    {
        mDelegate = new Filtered<>(delegate, new Filter<Link>()
        {
            @Override
            public boolean iterate(Link element)
            {
                return element.relationTypes().contains(rel);
            }
        });
    }


    @Override
    public boolean hasNext()
    {
        return mDelegate.hasNext();
    }


    @Override
    public Link next()
    {
        return mDelegate.next();
    }
}

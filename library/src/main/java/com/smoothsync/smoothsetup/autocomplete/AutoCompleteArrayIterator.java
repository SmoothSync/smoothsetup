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

package com.smoothsync.smoothsetup.autocomplete;

import org.dmfs.iterators.ArrayIterator;
import org.dmfs.iterators.ConvertedIterator;
import org.dmfs.iterators.FilteredIterator;
import org.dmfs.iterators.filters.NonNull;

import java.util.Iterator;


/**
 * An {@link Iterator} that iterates the expanded and filtered domains for auto-completion.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class AutoCompleteArrayIterator implements Iterator<String>
{

    private final Iterator<String> mIterator;


    public AutoCompleteArrayIterator(String[] autoCompleteDomains, String localPart, String domainPart)
    {
        mIterator = new ConvertedIterator<String, String>(new FilteredIterator<String>(new FilteredIterator<String>(
                new ConvertedIterator<String, String>(new ArrayIterator<String>(autoCompleteDomains), new DomainExpansionConverter(domainPart)),
                NonNull.<String>instance()), DomainFilter.INSTANCE), new LocalPartConverter(localPart));
    }


    @Override
    public boolean hasNext()
    {
        return mIterator.hasNext();
    }


    @Override
    public String next()
    {
        return mIterator.next();
    }


    @Override
    public void remove()
    {
        mIterator.remove();
    }
}

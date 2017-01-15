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

import org.dmfs.iterators.FilteredIterator;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * An {@link FilteredIterator.IteratorFilter} that removes all {@link String}s that do not resolve to an IP address.
 * <p/>
 * Note, by design this will trigger a network request, so don't use this on the UI thread.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class DomainFilter implements FilteredIterator.IteratorFilter<String>
{
    public final static DomainFilter INSTANCE = new DomainFilter();


    @Override
    public boolean iterate(String element)
    {
        try
        {
            InetAddress.getByName(element);
            return true;
        }
        catch (UnknownHostException e)
        {
            return false;
        }
    }
}

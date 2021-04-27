/*
 * Copyright (c) 2021 dmfs GmbH
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

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.types.Link;
import org.dmfs.rfc5545.DateTime;

import java.util.Iterator;
import java.util.regex.Pattern;


/**
 * A Provider which removes any scheme from the id.
 */
public final class UnPrefixed implements Provider
{
    private final static Pattern SCHEME_PATTERN = Pattern.compile("^com.smoothsync.\\w+:");

    private final Provider mDelegate;


    public UnPrefixed(Provider delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public String id() throws ProtocolException
    {
        return SCHEME_PATTERN.matcher(mDelegate.id()).replaceFirst("");
    }


    @Override
    public String name() throws ProtocolException
    {
        return mDelegate.name();
    }


    @Override
    public String[] domains() throws ProtocolException
    {
        return mDelegate.domains();
    }


    @Override
    public Iterator<Link> links() throws ProtocolException
    {
        return mDelegate.links();
    }


    @Override
    public Iterator<Service> services() throws ProtocolException
    {
        return mDelegate.services();
    }


    @Override
    public DateTime lastModified() throws ProtocolException
    {
        return mDelegate.lastModified();
    }
}

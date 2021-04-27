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


public final class WithIdPrefix implements Provider
{
    private final String mPrefix;
    private final Provider mDelegate;


    public WithIdPrefix(String prefix, Provider delegate)
    {
        mPrefix = prefix;
        mDelegate = delegate;
    }


    @Override
    public String id() throws ProtocolException
    {
        return mPrefix + mDelegate.id();
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

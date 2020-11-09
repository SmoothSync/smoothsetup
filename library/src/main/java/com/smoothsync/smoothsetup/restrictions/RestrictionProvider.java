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

package com.smoothsync.smoothsetup.restrictions;

import android.os.Bundle;
import android.os.Parcelable;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;

import org.dmfs.httpessentials.types.Link;
import org.dmfs.iterators.EmptyIterator;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.iterable.elementary.Seq;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.rfc5545.DateTime;

import java.util.Iterator;


public final class RestrictionProvider implements Provider
{
    private final Bundle mBundle;


    public RestrictionProvider(Bundle bundle)
    {
        mBundle = bundle;
    }


    @Override
    public String id()
    {
        return mBundle.getString("id");
    }


    @Override
    public String name()
    {
        return new Backed<>(new NullSafe<>(mBundle.getString("name")), "").value();
    }


    @Override
    public String[] domains()
    {
        // not supported yet
        return new String[0];
    }


    @Override
    public Iterator<Link> links()
    {
        // not supported yet
        return new EmptyIterator<>();
    }


    @Override
    public Iterator<Service> services()
    {
        return new Mapped<Parcelable, Service>(
                parcelable -> new RestrictionService((Bundle) parcelable),
                new Joined<>(new org.dmfs.jems.optional.decorators.Mapped<>(Seq::new, new NullSafe<>(mBundle.getParcelableArray("services"))))).iterator();
    }


    @Override
    public DateTime lastModified()
    {
        return DateTime.now();
    }
}

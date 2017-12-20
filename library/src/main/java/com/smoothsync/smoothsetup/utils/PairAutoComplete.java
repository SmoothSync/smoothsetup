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

import com.smoothsync.smoothsetup.autocomplete.AbstractAutoCompleteAdapter;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.jems.pair.Pair;

import java.util.Iterator;


/**
 * @author Marten Gajda
 */
public final class PairAutoComplete implements AbstractAutoCompleteAdapter.AutoCompleteItem
{
    private final Pair<Pair<String, String>, Iterable<String>> mValue;


    public PairAutoComplete(Pair<Pair<String, String>, Iterable<String>> value)
    {
        mValue = value;
    }


    @Override
    public String autoComplete()
    {
        return hasMany() ? mValue.left().right() : mValue.left().left();
    }


    @Override
    public Iterable<String> extensions()
    {
        return hasMany() ? mValue.right() : EmptyIterable.instance();
    }


    boolean hasMany()
    {
        Iterator<String> iterator = mValue.right().iterator();
        if (!iterator.hasNext())
        {
            return false;
        }
        iterator.next();
        return iterator.hasNext();
    }

}

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

import org.dmfs.iterators.ConvertedIterator;


/**
 * A {@link ConvertedIterator.Converter} that prepends domains with a specific local part.
 * <p/>
 * Instances of this class are not thread-safe.
 *
 * @author Marten Gajda
 */
public final class LocalPartConverter implements ConvertedIterator.Converter<String, String>
{

    private final StringBuilder mBuilder;
    private final int mLen;


    /**
     * Creates a Converter that prepends domains with the given local part to form a valid email address.
     *
     * @param localPart
     *         The local part to prepend.
     */
    public LocalPartConverter(String localPart)
    {
        mBuilder = new StringBuilder(128);
        mBuilder.append(localPart);
        mBuilder.append('@');
        mLen = mBuilder.length();
    }


    @Override
    public String convert(String element)
    {
        mBuilder.append(element);
        String result = mBuilder.toString();
        // don't forget to reset the StringBuilder.
        mBuilder.setLength(mLen);
        return result;
    }
}

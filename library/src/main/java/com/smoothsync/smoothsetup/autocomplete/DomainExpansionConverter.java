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

import org.dmfs.jems.function.Function;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.Absent;
import org.dmfs.jems.optional.elementary.Present;


/**
 * A {@link Function} to expand domain name patterns with a given domain name fragment.
 * <p>
 * If the domain name fragment is <code>examp</code>, the following patterns will yield in these results
 * <p>
 * <pre>
 * Pattern                         Result
 * example.com                     example.com
 * myserver.com                    null (the pattern doesn't match)
 * *.example.com                   examp.example.com
 * **.example.com                  examp.example.com
 * </pre>
 * <p>
 * It the input is <code>myserver.ex</code> the results will be
 * <p>
 * <pre>
 * Pattern                         Result
 * example.com                     null (the pattern doesn't match)
 * myserver.ex                     myserver.ex
 * *.example.com                   myserver.example.com
 * **.example.com                  myserver.example.com
 * </pre>
 * <p>
 * It the input is <code>myserver.ex.ex</code> the results will be
 * <p>
 * <pre>
 * Pattern                         Result
 * example.com                     null (the pattern doesn't match)
 * myserver.ex                     null (the pattern doesn't match)
 * *.example.com                   null (the pattern doesn't match)
 * **.example.com                  myserver.ex.example.com
 * </pre>
 *
 * @author Marten Gajda
 */
public final class DomainExpansionConverter implements Function<String, Optional<String>>
{
    private final String mDomain;
    private final String mDomainHead;
    private final String mDomainTail;
    private final int mDotIdx;


    public DomainExpansionConverter(String domain)
    {
        mDomain = domain;
        mDotIdx = domain.indexOf('.');
        mDomainTail = mDotIdx >= 0 ? mDomain.substring(mDotIdx) : "";
        mDomainHead = mDotIdx >= 0 ? mDomain.substring(0, mDotIdx) : mDomain;
    }


    @Override
    public Optional<String> value(String pattern)
    {
        if (pattern.charAt(0) != '*' && pattern.startsWith(mDomain))
        {
            // not a wildcard pattern and pattern starts with domain
            return new Present<>(pattern);
        }

        if (pattern.length() <= 2 || pattern.charAt(0) != '*')
        {
            // not a valid wildcard pattern => no match
            return new Absent<>();
        }

        if (pattern.charAt(1) == '.')
        {
            String patternTail = pattern.substring(1);
            if (mDotIdx < 0)
            {
                // append pattern domain
                return new Present<>(mDomainHead + patternTail);
            }

            if (!patternTail.startsWith(mDomainTail))
            {
                return new Absent<>();
            }
            return new Present<>(mDomainHead + patternTail);
        }

        if (pattern.length() > 3 && pattern.charAt(1) == '*' && pattern.charAt(2) == '.')
        {
            String patternTail = pattern.substring(2);

            if (mDotIdx < 0)
            {
                // append pattern domain
                return new Present<>(mDomainHead + patternTail);
            }

            int dotIdx = mDotIdx;
            while (dotIdx > 0)
            {
                String domainTail = mDomain.substring(dotIdx);
                if (patternTail.startsWith(domainTail))
                {
                    return new Present<>(mDomain.substring(0, dotIdx) + patternTail);
                }
                dotIdx = mDomain.indexOf('.', dotIdx + 1);
            }
        }
        return new Absent<>();
    }
}

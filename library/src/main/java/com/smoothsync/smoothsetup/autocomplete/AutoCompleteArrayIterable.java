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

import org.dmfs.jems2.iterable.DelegatingIterable;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.PresentValues;
import org.dmfs.jems2.iterable.Sieved;

import java.util.Iterator;


/**
 * An {@link Iterator} that iterates the expanded and filtered domains for auto-completion.
 *
 * @author Marten Gajda
 */
public final class AutoCompleteArrayIterable extends DelegatingIterable<String>
{

    public AutoCompleteArrayIterable(Iterable<String> autoCompleteDomains, String localPart, String domainPart)
    {
        super(new Mapped<>(new LocalPartConverter(localPart),
            new PresentValues<>(
                new Mapped<>(new DomainExpansionConverter(domainPart),
                    new Sieved<>(element -> !element.startsWith("@"), autoCompleteDomains)))));
    }
}

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

import org.dmfs.iterables.decorators.DelegatingIterable;
import org.dmfs.iterables.decorators.Fluent;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.iterators.filters.NonNull;

import java.util.Iterator;


/**
 * An {@link Iterator} that iterates the expanded and filtered domains for auto-completion.
 *
 * @author Marten Gajda
 */
public final class AutoCompleteArrayIterable extends DelegatingIterable<String>
{

    public AutoCompleteArrayIterable(String[] autoCompleteDomains, String localPart, String domainPart)
    {
        super(new Fluent<>(new Seq<>(autoCompleteDomains))
                .filtered(element -> !element.startsWith("@"))
                .mapped(new DomainExpansionConverter(domainPart))
                .filtered(NonNull.instance())
                .mapped(new LocalPartConverter(localPart)));
    }
}

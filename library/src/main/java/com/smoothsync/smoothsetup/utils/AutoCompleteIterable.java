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

import org.dmfs.iterables.decorators.DelegatingIterable;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.pair.Pair;
import org.dmfs.jems.pair.elementary.ValuePair;
import org.dmfs.optional.First;


/**
 * @author Marten Gajda
 */
public final class AutoCompleteIterable extends DelegatingIterable<AbstractAutoCompleteAdapter.AutoCompleteItem>
{

    public AutoCompleteIterable(Iterable<String> autoCompleteString, String prefix)
    {
        super(new Mapped<>(
                PairAutoComplete::new,
                new Mapped<>(
                        i -> new ValuePair<>(
                                new First<>(i).value().left(),
                                new Mapped<>(Pair::right, i)),
                        new GroupedIterable<Pair<Pair<String, String>, String>, String>(
                                new Mapped<>(s ->
                                {
                                    int dotIdx = s.indexOf('.', prefix.length());
                                    return dotIdx < 0 ?
                                            new ValuePair<>(new ValuePair<>(s, s), "")
                                            :
                                            new ValuePair<>(new ValuePair<>(s, s.substring(0, dotIdx + 1)), s.substring(dotIdx + 1));
                                }, autoCompleteString),
                                pair -> pair.left().right()))));
    }
}

/*
 * Copyright (c) 2019 dmfs GmbH
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

import android.content.Context;

import org.dmfs.iterables.decorators.DelegatingIterable;
import org.dmfs.iterables.decorators.Sieved;


/**
 * @author Marten Gajda
 */
public final class ProviderAccountRestrictions extends DelegatingIterable<AccountRestriction>
{
    public ProviderAccountRestrictions(Context context, String providerId)
    {
        super(new Sieved<>(
                ar -> providerId.equals(ar.providerId()),
                new AccountRestrictions(context)));
    }
}

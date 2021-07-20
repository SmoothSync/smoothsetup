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

import android.content.Context;
import android.os.Bundle;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.utils.DelegatingObservable;

import org.dmfs.jems2.iterable.Joined;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Seq;
import org.dmfs.jems2.optional.NullSafe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.reactivex.rxjava3.core.Maybe;


/**
 * @author Marten Gajda
 */
@RequiresApi(23)
public final class ProviderRestrictions extends DelegatingObservable<Provider>
{
    public ProviderRestrictions(@NonNull Context context)
    {
        this(new AppRestrictions(context).wrapped());
    }


    public ProviderRestrictions(@NonNull Maybe<Bundle> restrictions)
    {
        super(restrictions.flattenAsObservable(
                bundle -> new Mapped<>(
                        parcelable -> new RestrictionProvider((Bundle) parcelable),
                        new Joined<>(
                                new org.dmfs.jems2.optional.Mapped<>(
                                        Seq::new,
                                        new NullSafe<>(bundle.getParcelableArray("providers")))))));
    }
}

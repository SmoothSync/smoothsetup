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
import android.content.RestrictionsManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import org.dmfs.httpessentials.executors.authorizing.UserCredentials;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.iterators.EmptyIterator;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.optional.adapters.Collapsed;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;
import org.dmfs.optional.iterable.PresentValues;

import java.util.Iterator;

import androidx.annotation.NonNull;


/**
 * @author Marten Gajda
 */
public final class AccountRestrictions implements Iterable<AccountRestriction>
{
    private final Context mContext;


    public AccountRestrictions(Context context)
    {
        mContext = context;
    }


    @NonNull
    @Override
    public Iterator<AccountRestriction> iterator()
    {
        if (Build.VERSION.SDK_INT < 23)
        {
            return EmptyIterator.instance();
        }
        RestrictionsManager restrictionsManager = (RestrictionsManager) mContext.getSystemService(Context.RESTRICTIONS_SERVICE);
        return new Mapped<Parcelable, AccountRestriction>(parcelable ->

                new AccountRestriction()
                {
                    @Override
                    public String accountId()
                    {
                        return ((Bundle) parcelable).getString("accountId");
                    }


                    @Override
                    public String providerId()
                    {
                        return ((Bundle) parcelable).getString("providerId");
                    }


                    @Override
                    public Optional<UserCredentials> credentials()
                    {
                        return new org.dmfs.jems.optional.decorators.Mapped<>(parcelable ->

                                new UserCredentials()
                                {
                                    @Override
                                    public CharSequence userName()
                                    {
                                        return parcelable.getString("username");
                                    }


                                    @Override
                                    public CharSequence password()
                                    {
                                        return parcelable.getString("password");
                                    }
                                },
                                new NullSafe<>(((Bundle) parcelable).getBundle("credentials")));
                    }
                }, new Joined<Parcelable>(
                new Mapped<>(
                        Seq::new,
                        new PresentValues<>(
                                new Collapsed<>(
                                        new org.dmfs.jems.optional.decorators.Mapped<>(
                                                bundle -> new org.dmfs.jems.optional.elementary.NullSafe<>(bundle.getParcelableArray("accounts")),
                                                new NullSafe<>(restrictionsManager.getApplicationRestrictions()))))))).iterator();

    }
}

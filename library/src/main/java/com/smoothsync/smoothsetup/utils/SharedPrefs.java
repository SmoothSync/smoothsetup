/*
 * Copyright (c) 2021 dmfs GmbH
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

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;


public final class SharedPrefs extends Single<SharedPreferences>
{
    private final Context mApplicationContext;
    private final String mName;


    public SharedPrefs(@NonNull Context context, @NonNull String name)
    {
        mApplicationContext = context.getApplicationContext();
        mName = name;
    }


    @Override
    protected void subscribeActual(@NonNull SingleObserver<? super SharedPreferences> observer)
    {
        Single.just(mApplicationContext.getSharedPreferences(mName, Context.MODE_PRIVATE)).subscribeWith(observer);
    }
}

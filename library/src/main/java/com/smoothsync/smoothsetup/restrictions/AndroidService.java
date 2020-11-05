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

/**
 * @author Marten Gajda
 */

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.MaybeSource;


@RequiresApi(23)
public final class AndroidService<T> implements MaybeSource<T>
{
    private final Context mApplicationContext;
    private final Class<T> mServiceClass;


    public AndroidService(@androidx.annotation.NonNull Context applicationContext, @androidx.annotation.NonNull Class<T> serviceClass)
    {
        mApplicationContext = applicationContext.getApplicationContext();
        mServiceClass = serviceClass;
    }


    @Override
    public void subscribe(@NonNull MaybeObserver<? super T> observer)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            T rm = mApplicationContext.getSystemService(mServiceClass);
            if (rm != null)
            {
                observer.onSuccess(rm);
            }
            else
            {
                observer.onComplete();
            }
        }
        else
        {
            observer.onComplete();
        }
    }
}

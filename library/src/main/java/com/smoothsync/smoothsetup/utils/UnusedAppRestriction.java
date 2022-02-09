/*
 * Copyright (c) 2022 dmfs GmbH
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

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

import androidx.core.content.PackageManagerCompat;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;


public final class UnusedAppRestriction extends Single<Integer>
{
    private final Context mContext;


    public UnusedAppRestriction(Context context)
    {
        mContext = context.getApplicationContext();
    }


    @Override
    protected void subscribeActual(@NonNull SingleObserver<? super Integer> observer)
    {
        ListenableFuture<Integer> result = PackageManagerCompat.getUnusedAppRestrictionsStatus(mContext);
        Single.<Integer>create(
            emitter -> {
                result.addListener(
                    () -> {
                        try
                        {
                            emitter.onSuccess(result.get());
                        }
                        catch (Exception e)
                        {
                            emitter.onError(e);
                        }
                    },
                    Executors.newSingleThreadExecutor()
                );
            }
        ).subscribe(observer);
    }
}

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

package com.smoothsync.smoothsetup.services.binders;

import android.content.Context;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.ServiceBinder;

import org.reactivestreams.Subscriber;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


/**
 * A {@link Single} {@link ProviderService}.
 */
@Keep
public final class ApiServiceBinder extends Flowable<SmoothSyncApi>
{
    private final Context mContext;


    public ApiServiceBinder(@NonNull Context context)
    {
        mContext = context;
    }


    @Override
    protected void subscribeActual(@io.reactivex.rxjava3.annotations.NonNull Subscriber<? super SmoothSyncApi> subscriber)
    {
        ServiceBinder.<SmoothSyncApi>localService(mContext, "com.smoothsync.action.BIND_API")
            .subscribe(subscriber);
    }
}

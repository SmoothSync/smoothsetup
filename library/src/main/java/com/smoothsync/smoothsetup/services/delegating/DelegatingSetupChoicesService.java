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

package com.smoothsync.smoothsetup.services.delegating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.smoothsync.smoothsetup.services.ProviderValidationService;
import com.smoothsync.smoothsetup.services.SetupChoiceService;

import org.dmfs.jems2.Function;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;


/**
 *
 */
public class DelegatingSetupChoicesService extends Service
{
    private final Function<? super Context, ? extends SetupChoiceService> mDelegate;


    public DelegatingSetupChoicesService(Function<? super Context, ? extends SetupChoiceService> delegate)
    {
        mDelegate = delegate;
    }


    @NonNull
    @Override
    public final IBinder onBind(Intent intent)
    {
        return new SetupChoiceServiceBinder(mDelegate.value(this.getApplicationContext()));
    }


    /**
     * A {@link Binder} that gives access to the {@link ProviderValidationService}
     */
    private final static class SetupChoiceServiceBinder extends Binder implements SetupChoiceService
    {

        private final SetupChoiceService mDelegate;


        public SetupChoiceServiceBinder(SetupChoiceService delegate)
        {
            mDelegate = delegate;
        }


        @NonNull
        @Override
        public Flowable<Iterable<String>> autoComplete(@NonNull String name)
        {
            return mDelegate.autoComplete(name);
        }


        @NonNull
        @Override
        public Flowable<Iterable<SetupChoice>> choices(@NonNull String domain)
        {
            return mDelegate.choices(domain);
        }
    }
}

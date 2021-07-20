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

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.services.ProviderValidationService;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;

import org.dmfs.jems2.Function;

import androidx.annotation.Nullable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;


/**
 * @author Marten Gajda
 */
public class DelegatingProviderService extends Service
{
    private final Function<? super Context, ? extends ProviderService> mDelegate;


    public DelegatingProviderService(Function<? super Context, ? extends ProviderService> delegate)
    {
        mDelegate = delegate;
    }


    @Nullable
    @Override
    public final IBinder onBind(Intent intent)
    {
        return new ProviderServiceBinder(mDelegate.value(this.getApplicationContext()));
    }


    /**
     * A {@link Binder} that gives access to the {@link ProviderValidationService}
     */
    private final static class ProviderServiceBinder extends Binder implements ProviderService
    {

        private final ProviderService mDelegate;


        public ProviderServiceBinder(ProviderService delegate)
        {
            mDelegate = delegate;
        }


        @Override
        public Maybe<Provider> byId(String id)
        {
            return mDelegate.byId(id);
        }


        @Override
        public Observable<Provider> byDomain(String domain)
        {
            return mDelegate.byDomain(domain);
        }


        @Override
        public Observable<Provider> all()
        {
            return mDelegate.all();
        }


        @Override
        public Observable<String> autoComplete(String domainFragment)
        {
            return mDelegate.autoComplete(domainFragment);
        }

    }
}

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

package com.smoothsync.smoothsetup.services.delegating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.services.ProviderValidationService;
import com.smoothsync.smoothsetup.services.VerificationService;
import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.httpessentials.executors.authorizing.UserCredentials;

import androidx.annotation.Nullable;


/**
 * An abstract service that provides an {@link ProviderValidationService} interface.
 *
 * @author Marten Gajda
 */
public abstract class DelegatingValidationService extends Service
{

    private AccountServiceBinder mBinder;
    private ValidationServiceFactory mValidationServiceFactory;


    public DelegatingValidationService(ValidationServiceFactory verificationServiceFactory)
    {
        mValidationServiceFactory = verificationServiceFactory;
    }


    @Override
    public final void onCreate()
    {
        super.onCreate();
        mBinder = new AccountServiceBinder(mValidationServiceFactory.validationService(this));
    }


    @Nullable
    @Override
    public final IBinder onBind(Intent intent)
    {
        return mBinder;
    }


    /**
     * A factory that creates {@link VerificationService} instances.
     */
    public interface ValidationServiceFactory
    {
        /**
         * Create a new {@link ProviderValidationService}.
         *
         * @param context
         *     A Context.
         *
         * @return
         */
        ProviderValidationService validationService(Context context);
    }


    /**
     * A {@link Binder} that gives access to the {@link ProviderValidationService}
     */
    private final static class AccountServiceBinder extends Binder implements ProviderValidationService
    {

        private final ProviderValidationService mVerificationService;


        public AccountServiceBinder(ProviderValidationService verificationService)
        {
            mVerificationService = verificationService;
        }


        @Override
        public AccountDetails providerForUrl(Provider provider, UserCredentials credentials) throws Exception
        {
            return mVerificationService.providerForUrl(provider, credentials);
        }
    }
}

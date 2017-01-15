/*
 * Copyright (c) 2017 dmfs GmbH
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

package com.smoothsync.smoothsetup.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.model.HttpAuthorizationFactory;


/**
 * An abstract service that provides an {@link VerificationService} interface.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractVerificationService extends Service
{

    /**
     * A {@link Binder} that gives access to the {@link VerificationService}
     */
    private final static class AccountServiceBinder extends Binder implements VerificationService
    {

        private final VerificationService mVerificationService;


        public AccountServiceBinder(VerificationService verificationService)
        {
            mVerificationService = verificationService;
        }


        @Override
        public boolean verify(Provider provider, HttpAuthorizationFactory authorizationFactory) throws Exception
        {
            return mVerificationService.verify(provider, authorizationFactory);
        }
    }


    /**
     * A factory that creates AccountService instances.
     */
    public interface VerificationServiceFactory
    {
        /**
         * Create a new {@link VerificationService}.
         *
         * @param context
         *         A Context.
         *
         * @return
         */
        VerificationService accountService(Context context);
    }


    private AccountServiceBinder mBinder;
    private VerificationServiceFactory mVerificationServiceFactory;


    public AbstractVerificationService(VerificationServiceFactory verificationServiceFactory)
    {
        mVerificationServiceFactory = verificationServiceFactory;
    }


    @Override
    public final void onCreate()
    {
        super.onCreate();
        mBinder = new AccountServiceBinder(mVerificationServiceFactory.accountService(this));
    }


    @Nullable
    @Override
    public final IBinder onBind(Intent intent)
    {
        return mBinder;
    }
}

/*
 * Copyright (c) 2018 dmfs GmbH
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
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.services.AccountService;


/**
 * An abstract service which delegates to another {@link AccountService}.
 *
 * @author Marten Gajda
 */
public abstract class DelegatingAccountService extends Service
{

    private AccountServiceBinder mBinder;
    private AccountServiceFactory mAccountServiceFactory;


    public DelegatingAccountService(AccountServiceFactory accountServiceFactory)
    {
        mAccountServiceFactory = accountServiceFactory;
    }


    @Override
    public final void onCreate()
    {
        super.onCreate();
        mBinder = new AccountServiceBinder(mAccountServiceFactory.accountService(this));
    }


    @Nullable
    @Override
    public final IBinder onBind(Intent intent)
    {
        return mBinder;
    }


    /**
     * A factory which creates AccountService instances.
     */
    public interface AccountServiceFactory
    {
        /**
         * Create a new SmoothSyncApi.
         *
         * @param context
         *         A Context.
         *
         * @return
         */
        AccountService accountService(Context context);
    }


    /**
     * A {@link Binder} that gives access to the AccountService
     */
    private final static class AccountServiceBinder extends AccountService.Stub
    {

        private final AccountService mAccountService;


        public AccountServiceBinder(AccountService accountService)
        {
            mAccountService = accountService;
        }


        @Override
        public BasicAccount providerForAccount(android.accounts.Account account) throws RemoteException
        {
            return mAccountService.providerForAccount(account);
        }


        @Override
        public void createAccount(Bundle bundle) throws RemoteException
        {
            mAccountService.createAccount(bundle);
        }
    }
}

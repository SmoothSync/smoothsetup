package com.smoothsync.smoothsetup.demo;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.services.AbstractAccountService;


/**
 * Fake AccountService that doesn't do anything.
 *
 * @author Marten Gajda
 */
public class AccountService extends AbstractAccountService
{
    public AccountService()
    {
        super(new AccountServiceFactory()
        {
            @Override
            public com.smoothsync.smoothsetup.services.AccountService accountService(Context context)
            {
                return new com.smoothsync.smoothsetup.services.AccountService.Stub()
                {
                    @Override
                    public BasicAccount providerForAccount(Account account) throws RemoteException
                    {
                        return null;
                    }


                    @Override
                    public void createAccount(Bundle bundle) throws RemoteException
                    {
                        try
                        {
                            // wait a little to emulate the account setup
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
            }
        });
    }
}

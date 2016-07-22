package com.smoothsync.smoothsetup.services;

import android.os.Parcelable;

import com.smoothsync.smoothsetup.model.Account;


/**
 * Created by marten on 30.06.16.
 */
public interface AccountService extends Parcelable
{
	public final static String ACTION_ACCOUNT_SERVICE = "com.smoothsync.action.ACCOUNT_SERVICE";


	public Account providerForAccount(android.accounts.Account account);


	public void createAccount(Account account);
}

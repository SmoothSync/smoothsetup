package com.smoothsync.smoothsetup.services;

import  com.smoothsync.smoothsetup.model.BasicAccount;

interface AccountService {

   	BasicAccount providerForAccount(in android.accounts.Account account);


   	void createAccount(in Bundle bundle);


   	void updateAccount(in Bundle bundle);
}

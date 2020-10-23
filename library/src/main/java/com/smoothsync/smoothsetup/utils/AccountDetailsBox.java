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

package com.smoothsync.smoothsetup.utils;

import android.os.Bundle;
import android.os.Parcel;

import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.utils.usercredentials.Parcelable;

import org.dmfs.android.microwizard.box.Box;
import org.dmfs.httpessentials.executors.authorizing.UserCredentials;


/**
 * @author Marten Gajda
 */
public final class AccountDetailsBox implements Box<AccountDetails>
{
    private final AccountDetails mAccountDetails;


    public AccountDetailsBox(AccountDetails accountDetails)
    {
        mAccountDetails = accountDetails;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mAccountDetails.account(), flags);
        dest.writeParcelable(new Parcelable(mAccountDetails.credentials()), flags);
        dest.writeBundle(mAccountDetails.settings());
    }


    @Override
    public AccountDetails value()
    {
        return mAccountDetails;
    }


    public final static Creator<AccountDetailsBox> CREATOR = new Creator<AccountDetailsBox>()
    {
        @Override
        public AccountDetailsBox createFromParcel(Parcel source)
        {
            ClassLoader classLoader = getClass().getClassLoader();
            Account account = source.readParcelable(classLoader);
            UserCredentials userCredentials = source.readParcelable(classLoader);
            Bundle settings = source.readBundle(classLoader);
            return new AccountDetailsBox(new AccountDetails()
            {
                @Override
                public Account account()
                {
                    return account;
                }


                @Override
                public UserCredentials credentials()
                {
                    return userCredentials;
                }


                @Override
                public Bundle settings()
                {
                    return settings;
                }


                @Override
                public Box<AccountDetails> boxed()
                {
                    return new AccountDetailsBox(this);
                }
            });
        }


        @Override
        public AccountDetailsBox[] newArray(int size)
        {
            return new AccountDetailsBox[size];
        }
    };
}

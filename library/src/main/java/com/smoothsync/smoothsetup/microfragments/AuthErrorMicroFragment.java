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

package com.smoothsync.smoothsetup.microfragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.httpessentials.exceptions.ProtocolException;


/**
 * A {@link MicroFragment} that shows a message if authentication failed.
 *
 * @author Marten Gajda
 */
public final class AuthErrorMicroFragment implements MicroFragment<AuthErrorMicroFragment.Params>
{

    public final static Creator<AuthErrorMicroFragment> CREATOR = new Creator<AuthErrorMicroFragment>()
    {
        @Override
        public AuthErrorMicroFragment createFromParcel(Parcel source)
        {
            Account account = source.readParcelable(getClass().getClassLoader());
            return new AuthErrorMicroFragment(account, new Unboxed<MicroWizard<Account>>(source).value());
        }


        @Override
        public AuthErrorMicroFragment[] newArray(int size)
        {
            return new AuthErrorMicroFragment[size];
        }
    };


    interface Params
    {
        Account account();

        MicroWizard<Account> next();
    }


    private final Account mAccount;
    private final MicroWizard<Account> mNext;


    public AuthErrorMicroFragment(Account account, MicroWizard<Account> next)
    {
        mAccount = account;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_authentication_error);
    }


    @Override
    public boolean skipOnBack()
    {
        return false;
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost host)
    {
        return new MessageFragment();
    }


    @NonNull
    @Override
    public Params parameter()
    {
        return new Params()
        {
            @Override
            public Account account()
            {
                return mAccount;
            }


            @Override
            public MicroWizard<Account> next()
            {
                return mNext;
            }
        };
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mAccount, flags);
        dest.writeParcelable(mNext.boxed(), flags);
    }


    /**
     * A Fragment that shows a message.
     */
    public final static class MessageFragment extends Fragment implements View.OnClickListener
    {

        private Params mParams;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_verify_password, container, false);

            mParams = new FragmentEnvironment<Params>(this).microFragment().parameter();

            try
            {
                ((TextView) result.findViewById(android.R.id.message))
                        .setText(
                                getString(
                                        R.string.smoothsetup_message_authentication_failure,
                                        getString(getContext().getApplicationInfo().labelRes),
                                        mParams.account().accountId(),
                                        mParams.account().provider().name()));
            }
            catch (ProtocolException e)
            {
                new FragmentEnvironment<>(this).host()
                        .execute(getContext(),
                                new Swiped(new ForwardTransition<>(new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_load_provider)))));
            }

            Button button = ((Button) result.findViewById(android.R.id.button1));
            button.setOnClickListener(this);

            return result;
        }


        @Override
        public void onClick(View v)
        {
            if (isResumed())
            {
                new FragmentEnvironment<>(this).host()
                        .execute(getContext(), new Swiped(new ForwardTransition<>(mParams.next().microFragment(getActivity(), mParams.account()))));
            }
        }
    }
}

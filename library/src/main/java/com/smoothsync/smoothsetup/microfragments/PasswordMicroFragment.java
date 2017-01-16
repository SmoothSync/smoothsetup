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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.BasicHttpAuthorizationFactory;
import com.smoothsync.smoothsetup.utils.Default;
import com.smoothsync.smoothsetup.utils.Related;

import org.dmfs.android.microfragments.BasicMicroFragmentEnvironment;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.types.Link;
import org.dmfs.iterators.AbstractConvertedIterator;
import org.dmfs.iterators.ConvertedIterator;


/**
 * A {@link MicroFragment} that prompts the user to enter a password after {@link Account} has been chosen.
 *
 * @author Marten Gajda
 */
public final class PasswordMicroFragment implements MicroFragment<Account>
{
    public final static Creator<PasswordMicroFragment> CREATOR = new Creator<PasswordMicroFragment>()
    {
        @Override
        public PasswordMicroFragment createFromParcel(Parcel source)
        {
            return new PasswordMicroFragment((Account) source.readParcelable(getClass().getClassLoader()));
        }


        @Override
        public PasswordMicroFragment[] newArray(int size)
        {
            return new PasswordMicroFragment[0];
        }
    };
    @NonNull
    private final Account mAccount;


    public PasswordMicroFragment(@NonNull Account account)
    {
        mAccount = account;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_enter_password);
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
        Fragment result = new PasswordFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(MicroFragment.ARG_ENVIRONMENT, new BasicMicroFragmentEnvironment<>(this, host));
        result.setArguments(arguments);
        return result;
    }


    @NonNull
    @Override
    public Account parameters()
    {
        return mAccount;
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
    }


    /**
     * A Fragment that prompts the user for his or her password.
     */
    public final static class PasswordFragment extends Fragment implements View.OnClickListener
    {

        private Account mAccount;
        private EditText mPassword;
        private Button mButton;
        private MicroFragmentEnvironment<Account> mMicroFragmentEnvironment;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mAccount = mMicroFragmentEnvironment.microFragment().parameters();
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_password, container, false);
            TextView messageView = ((TextView) result.findViewById(android.R.id.message));

            mButton = (Button) result.findViewById(R.id.button);
            mButton.setOnClickListener(this);

            mPassword = (EditText) result.findViewById(android.R.id.input);
            mPassword.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                    // nothing to do
                }


                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    // nothing to do
                }


                @Override
                public void afterTextChanged(Editable s)
                {
                    mButton.setEnabled(!s.toString().isEmpty());
                }
            });

            mButton.setEnabled(!mPassword.getText().toString().isEmpty());
            try
            {
                String appSpecificPasswordOption = new Default<>(new ConvertedIterator<>(
                        new Related(mAccount.provider().links(), "http://smoothsync.com/rel/app-specific-password"),
                        new AbstractConvertedIterator.Converter<String, Link>()
                        {
                            @Override
                            public String convert(Link element)
                            {
                                return element.target().toASCIIString();
                            }
                        }), "no").next();

                switch (appSpecificPasswordOption)
                {
                    case "mandatory":
                        if (new Related(mAccount.provider().links(), "http://smoothsync.com/rel/manage-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_create_app_specific_password);
                        }
                        messageView.setText(getString(R.string.smoothsetup_prompt_enter_app_specific_password, mAccount.provider().name()));
                        break;
                    case "optional":
                        if (new Related(mAccount.provider().links(), "http://smoothsync.com/rel/manage-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_create_app_specific_password);
                        }
                        if (new Related(mAccount.provider().links(), "http://smoothsync.com/rel/forgot-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_forgot_password);
                        }
                        messageView.setText(getString(R.string.smoothsetup_prompt_enter_password_or_app_specific_password, mAccount.provider().name()));
                        break;
                    default:
                        if (new Related(mAccount.provider().links(), "http://smoothsync.com/rel/forgot-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_forgot_password);
                        }
                        messageView.setText(getContext().getString(R.string.smoothsetup_prompt_enter_password, mAccount.provider().name()));
                }
            }
            catch (ProtocolException e)
            {
                // switch to an error screen when done
                result.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mMicroFragmentEnvironment.host()
                                .execute(getActivity(),
                                        new Swiped(new ForwardTransition(new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_network)))));
                    }
                });
            }

            return result;
        }


        @Override
        public void onClick(View v)
        {
            int id = v.getId();
            try
            {
                if (id == R.id.button)
                {
                    // verify entered password
                    mMicroFragmentEnvironment.host()
                            .execute(getActivity(),
                                    new Swiped(new ForwardTransition(
                                            new ApproveAuthorizationMicroFragment(mAccount,
                                                    new BasicHttpAuthorizationFactory(mAccount.accountId(), mPassword.getText().toString())))));
                }
                else if (id == R.id.smoothsetup_forgot_password)
                {
                    openLink(mAccount.provider().name(), "http://smoothsync.com/rel/forgot-password");
                }
                else if (id == R.id.smoothsetup_create_app_specific_password)
                {
                    openLink(mAccount.provider().name(), "http://smoothsync.com/rel/manage-password");
                }
            }
            catch (ProtocolException e)
            {
                mMicroFragmentEnvironment.host()
                        .execute(getActivity(),
                                new Swiped(
                                        new ForwardTransition(
                                                new ErrorRetryMicroFragment(e.getMessage()))));
            }
        }


        private void setupClickableTextView(View root, int id)
        {
            TextView passwordView = ((TextView) root.findViewById(id));
            passwordView.setVisibility(View.VISIBLE);
            passwordView.setOnClickListener(this);
        }


        private void openLink(String title, String name)
        {
            try
            {
                mMicroFragmentEnvironment.host()
                        .execute(getActivity(),
                                new Swiped(
                                        new ForwardTransition(
                                                new CreateAppSpecificPasswordMicroFragment(title,
                                                        new Related(mAccount.provider().links(), name).next().target()))));
            }
            catch (ProtocolException e)
            {
                throw new RuntimeException("Something went very wrong. We shouldn't be here because it should have crashed in onCreateView already", e);
            }
        }
    }
}

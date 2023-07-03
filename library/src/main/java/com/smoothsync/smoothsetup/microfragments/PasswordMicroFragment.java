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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.microfragments.appspecificpassword.AppSpecificWebviewFragment;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.restrictions.AccountRestriction;
import com.smoothsync.smoothsetup.restrictions.ProviderAccountRestrictions;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AccountDetailsBox;
import com.smoothsync.smoothsetup.utils.Default;
import com.smoothsync.smoothsetup.utils.Related;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.executors.authorizing.UserCredentials;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.iterator.Mapped;
import org.dmfs.jems2.optional.First;
import org.dmfs.jems2.optional.MapCollapsed;
import org.dmfs.pigeonpost.Dovecote;
import org.dmfs.pigeonpost.localbroadcast.ParcelableDovecote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} that prompts the user to enter a password after {@link Account} has been chosen.
 *
 * @author Marten Gajda
 */
public final class PasswordMicroFragment implements MicroFragment<PasswordMicroFragment.Params>
{
    public final static Creator<PasswordMicroFragment> CREATOR = new Creator<PasswordMicroFragment>()
    {
        @Override
        public PasswordMicroFragment createFromParcel(Parcel source)
        {
            return new PasswordMicroFragment((Account) source.readParcelable(getClass().getClassLoader()),
                new Unboxed<MicroWizard<AccountDetails>>(source).value());
        }


        @Override
        public PasswordMicroFragment[] newArray(int size)
        {
            return new PasswordMicroFragment[0];
        }
    };
    @NonNull
    private final Account mAccount;

    @NonNull
    private final MicroWizard<AccountDetails> mNext;


    public PasswordMicroFragment(@NonNull Account account, @NonNull MicroWizard<AccountDetails> next)
    {
        mAccount = account;
        mNext = next;
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
        return new PasswordFragment();
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
            public MicroWizard<AccountDetails> next()
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
     * A Fragment that prompts the user for his or her password.
     */
    public final static class PasswordFragment extends Fragment implements View.OnClickListener, Dovecote.OnPigeonReturnCallback<AppSpecificWebviewFragment.PasswordResult>
    {

        private Params mParams;
        private EditText mPassword;
        private Button mButton;
        private TextInputLayout mTextInputLayout;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Dovecote<AppSpecificWebviewFragment.PasswordResult> mDovecote;
        private final View.OnClickListener mPasswordToggleListener = textInputLayout -> {
            // set secure flag while the password is visible
            TransformationMethod tm = mPassword.getTransformationMethod();
            if (tm == null)
            {
                PasswordFragment.this.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            else
            {
                PasswordFragment.this.getActivity()
                    .getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                mPassword.setTransformationMethod(null);
            }
        };


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mParams = mMicroFragmentEnvironment.microFragment().parameter();
        }


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            mDovecote = new ParcelableDovecote<>(getActivity(), "passwordDoveCote", this);
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_password, container, false);
            TextView messageView = ((TextView) result.findViewById(android.R.id.message));

            mTextInputLayout = result.findViewById(R.id.text_input_layout);

            mButton = (Button) result.findViewById(R.id.button);
            mButton.setOnClickListener(this);

            mPassword = (EditText) mTextInputLayout.findViewById(android.R.id.input);
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

            mTextInputLayout.setEndIconOnClickListener(mPasswordToggleListener);

            Provider provider = mMicroFragmentEnvironment.microFragment().parameter().account().provider();
            try
            {
                String providerId = provider.id();
                Optional<UserCredentials> credentialsRestrictions = new MapCollapsed<>(
                    AccountRestriction::credentials,
                    new First<>(
                        new ProviderAccountRestrictions(getActivity(), providerId)));

                if (credentialsRestrictions.isPresent() && credentialsRestrictions.value().password().length() > 0)
                {
                    // can't override any restriction for this account
                    mPassword.setText(credentialsRestrictions.value().password());
                    mPassword.setEnabled(false);
                    mTextInputLayout.setHelperText("Provided by Managed Profile ");
                    mTextInputLayout.setEnabled(false);
                    // fast forward view
                    //          result.post(() -> authenticate(credentialsRestrictions.value().password().toString()));
                }
                else
                {
                    Log.i("LoginFragment", "no restrictions found");
                }
            }
            catch (Exception e)
            {
                Log.e("LoginFragment", "error reading profile", e);
            }

            mButton.setEnabled(!mPassword.getText().toString().isEmpty());
            try
            {
                String appSpecificPasswordOption = new Default<>(new Mapped<>(
                    element -> element.target().toASCIIString(),
                    new Related(mParams.account().provider().links(), "http://smoothsync.com/rel/app-specific-password")),
                    "no").next();

                switch (appSpecificPasswordOption)
                {
                    case "mandatory":
                        if (new Related(mParams.account().provider().links(), "http://smoothsync.com/rel/manage-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_create_app_specific_password);
                        }
                        messageView.setText(getString(R.string.smoothsetup_prompt_enter_app_specific_password, mParams.account().provider().name()));
                        break;
                    case "optional":
                        if (new Related(mParams.account().provider().links(), "http://smoothsync.com/rel/manage-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_create_app_specific_password);
                        }
                        if (new Related(mParams.account().provider().links(), "http://smoothsync.com/rel/forgot-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_forgot_password);
                        }
                        messageView.setText(
                            getString(R.string.smoothsetup_prompt_enter_password_or_app_specific_password, mParams.account().provider().name()));
                        break;
                    default:
                        if (new Related(mParams.account().provider().links(), "http://smoothsync.com/rel/forgot-password").hasNext())
                        {
                            setupClickableTextView(result, R.id.smoothsetup_forgot_password);
                        }
                        messageView.setText(getContext().getString(R.string.smoothsetup_prompt_enter_password, mParams.account().provider().name()));
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
                                new Swiped(new ForwardTransition<>(new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_network)))));
                    }
                });
            }
            return result;
        }


        @Override
        public void onResume()
        {
            super.onResume();
            mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            PasswordFragment.this.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }


        @Override
        public void onPause()
        {
            // ensure we hide the password when we leave the activity for any reason
            mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mPassword.invalidate();
            super.onPause();
        }


        @Override
        public void onDestroyView()
        {
            mDovecote.dispose();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            super.onDestroyView();
        }


        @Override
        public void onClick(View v)
        {
            int id = v.getId();
            try
            {
                if (id == R.id.button)
                {
                    authenticate(mPassword.getText().toString());
                }
                else if (id == R.id.smoothsetup_forgot_password)
                {
                    openLink(mParams.account().provider().name(), "http://smoothsync.com/rel/forgot-password");
                }
                else if (id == R.id.smoothsetup_create_app_specific_password)
                {
                    openLink(mParams.account().provider().name(), "http://smoothsync.com/rel/manage-password");
                }
            }
            catch (ProtocolException e)
            {
                mMicroFragmentEnvironment.host()
                    .execute(getActivity(),
                        new Swiped(
                            new ForwardTransition<>(
                                new ErrorRetryMicroFragment(e.getMessage()))));
            }
        }


        private void authenticate(String password)
        {
            // verify entered password
            mMicroFragmentEnvironment.host()
                .execute(getActivity(),
                    new Swiped(new ForwardTransition<>(
                        mParams.next().microFragment(
                            getActivity(),
                            new AccountDetails()
                            {
                                @Override
                                public Account account()
                                {
                                    return mParams.account();
                                }


                                @Override
                                public UserCredentials credentials()
                                {
                                    return new UserCredentials()
                                    {
                                        @Override
                                        public CharSequence userName()
                                        {
                                            return mParams.account().accountId();
                                        }


                                        @Override
                                        public CharSequence password()
                                        {
                                            return password;
                                        }
                                    };
                                }


                                @Override
                                public Bundle settings()
                                {
                                    return Bundle.EMPTY;
                                }


                                @Override
                                public Box<AccountDetails> boxed()
                                {
                                    return new AccountDetailsBox(this);
                                }
                            }))));
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
                            new ForwardTransition<>(
                                new CreateAppSpecificPasswordMicroFragment(title,
                                    mDovecote.cage(), new Related(mParams.account().provider().links(), name).next().target()))));
            }
            catch (ProtocolException e)
            {
                throw new RuntimeException("Something went very wrong. We shouldn't be here because it should have crashed in onCreateView already", e);
            }
        }


        @Override
        public void onPigeonReturn(@NonNull AppSpecificWebviewFragment.PasswordResult passwordResult)
        {
            String password = passwordResult.password();
            mPassword.setText(password);
            mPassword.setSelection(password.length());
            Snackbar.make(getView(), R.string.smoothsetup_app_specific_password_inserted, Snackbar.LENGTH_LONG).show();
        }
    }


    protected interface Params
    {
        Account account();

        MicroWizard<AccountDetails> next();
    }
}

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

package com.smoothsync.smoothsetup.microfragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.Account;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AccountDetailsBox;

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
import org.dmfs.httpessentials.types.Link;
import org.dmfs.iterators.EmptyIterator;
import org.dmfs.iterators.elementary.Seq;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.procedure.Procedure;
import org.dmfs.rfc5545.DateTime;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * A {@link MicroFragment} that prompts the user to enter a password after {@link Account} has been chosen.
 *
 * @author Marten Gajda
 */
public final class ManualSetupSimpleMicroFragment implements MicroFragment<ManualSetupSimpleMicroFragment.Params>
{
    public final static Creator<ManualSetupSimpleMicroFragment> CREATOR = new Creator<ManualSetupSimpleMicroFragment>()
    {
        @Override
        public ManualSetupSimpleMicroFragment createFromParcel(Parcel source)
        {
            boolean present = source.readInt() == 1;
            return new ManualSetupSimpleMicroFragment(present ? new Present<>(source.readString()) : absent(),
                    new Unboxed<MicroWizard<AccountDetails>>(source).value());
        }


        @Override
        public ManualSetupSimpleMicroFragment[] newArray(int size)
        {
            return new ManualSetupSimpleMicroFragment[0];
        }
    };
    @NonNull
    private final Optional<String> mUsername;

    @NonNull
    private final MicroWizard<AccountDetails> mNext;


    public ManualSetupSimpleMicroFragment(@NonNull Optional<String> username, @NonNull MicroWizard<AccountDetails> next)
    {
        mUsername = username;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsync_manual_setup);
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
        return new ManualSetupFragment();
    }


    @NonNull
    @Override
    public Params parameter()
    {
        return new Params()
        {
            @Override
            public Optional<String> username()
            {
                return mUsername;
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
        dest.writeInt(mUsername.isPresent() ? 1 : 0);
        if (mUsername.isPresent())
        {
            dest.writeString(mUsername.value());
        }
        dest.writeParcelable(mNext.boxed(), flags);
    }


    /**
     * A Fragment that prompts the user for his or her password.
     */
    public final static class ManualSetupFragment extends Fragment implements View.OnClickListener
    {
        private Params mParams;
        private EditText mUri;
        private TextInputLayout mUriInputLayout;
        private EditText mUsername;
        private EditText mPassword;
        private Button mButton;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;

        private final Runnable mButtonEnabler = () -> mButton.setEnabled(mUriInputLayout.getError() == null &&
                mUri.getText().length() > 0
                && mUsername.getText().length() > 0
                && mPassword.getText().length() > 0);

        private final TextWatcher mButtonEnableWatcher = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }


            @Override
            public void afterTextChanged(Editable editable)
            {
                mButtonEnabler.run();
            }
        };

        private final Procedure<String> mTestUrlProcedure = new Procedure<String>()
        {
            @Override
            public void process(String uriString)
            {
                try
                {
                    uriString = uriString.trim();
                    if (uriString.isEmpty())
                    {
                        mUriInputLayout.setError(null);
                        return;
                    }

                    URI uri = new URI(Uri.encode(uriString, ":/.%"));
                    if (uri.getScheme() != null && !"https".equals(uri.getScheme()))
                    {
                        mUriInputLayout.setError("Enter a URL that starts with \"https\".");
                        return;
                    }
                    if (uri.getScheme() == null)
                    {
                        uri = new URI("https://" + uri.toString());
                    }
                    URI finalUri = uri;
                    new CheckHostnameAsyncTask(
                            result -> {
                                mUriInputLayout.setError(result ? null : String.format("Host %s not found.", finalUri.getHost()));
                                mButtonEnabler.run();
                            }
                    ).execute(uri.getHost());
                }
                catch (URISyntaxException e)
                {
                    mUriInputLayout.setError("Enter a valid URI.");
                }
            }
        };

        private final Runnable mCheckUrlRunnable = () -> mTestUrlProcedure.process(mUri.getText().toString());


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
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_manual_simple, container, false);

            mButton = result.findViewById(R.id.button);
            mButton.setOnClickListener(this);

            mUriInputLayout = result.findViewById(R.id.uri_input_layout);
            mUriInputLayout.setErrorEnabled(true);
            mUri = result.findViewById(R.id.url_input);
            mUri.setOnFocusChangeListener((view, b) -> mTestUrlProcedure.process(mUri.getText().toString()));
            mUri.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {

                }


                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {

                }


                @Override
                public void afterTextChanged(Editable editable)
                {
                    mUri.removeCallbacks(mCheckUrlRunnable);
                    mUri.postDelayed(mCheckUrlRunnable, 1000);
                }
            });
            mUsername = result.findViewById(R.id.username_input);
            mPassword = result.findViewById(R.id.password_input);
            mUri.addTextChangedListener(mButtonEnableWatcher);
            mUsername.addTextChangedListener(mButtonEnableWatcher);
            mPassword.addTextChangedListener(mButtonEnableWatcher);

            Optional<String> username = mMicroFragmentEnvironment.microFragment().parameter().username();
            if (username.isPresent())
            {
                String u = username.value();
                mUsername.setText(u);
                int atIndex = u.indexOf('@');
                if (atIndex >= 0)
                {
                    mUri.setText(u.substring(atIndex + 1));
                }
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

                    String uriString = mUri.getText().toString().trim();
                    if (uriString.isEmpty())
                    {
                        return;
                    }

                    URI uri = new URI(Uri.encode(uriString, ":/.%"));
                    if (uri.getScheme() != null && !"https".equals(uri.getScheme()))
                    {
                        return;
                    }
                    if (uri.getScheme() == null)
                    {
                        uri = new URI("https://" + uri.toString());
                    }

                    verify(uri, mUsername.getText().toString(), mPassword.getText().toString());
                }
            }
            catch (ProtocolException | URISyntaxException e)
            {
                mMicroFragmentEnvironment.host()
                        .execute(getActivity(),
                                new Swiped(
                                        new ForwardTransition<>(
                                                new ErrorRetryMicroFragment(e.getMessage()))));
            }
        }


        private void verify(URI uri, String username, String password) throws ProtocolException
        {
            // TODO: return a result
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
                                                    return new BasicAccount(
                                                            mUsername.getText().toString(),
                                                            new Provider()
                                                            {
                                                                @Override
                                                                public String id()
                                                                {
                                                                    return "custom:" + uri.toString();
                                                                }


                                                                @Override
                                                                public String name()
                                                                {
                                                                    return uri.getAuthority();
                                                                }


                                                                @Override
                                                                public String[] domains()
                                                                {
                                                                    return new String[0];
                                                                }


                                                                @Override
                                                                public Iterator<Link> links()
                                                                {
                                                                    return EmptyIterator.instance();
                                                                }


                                                                @Override
                                                                public Iterator<Service> services()
                                                                {
                                                                    return new Seq<>(
                                                                            new Service()
                                                                            {
                                                                                @Override
                                                                                public String name()
                                                                                {
                                                                                    return "Contacts";
                                                                                }


                                                                                @Override
                                                                                public String serviceType()
                                                                                {
                                                                                    return "carddav";
                                                                                }


                                                                                @Override
                                                                                public URI uri()
                                                                                {
                                                                                    return uri;
                                                                                }


                                                                                @Override
                                                                                public KeyStore keyStore()
                                                                                {
                                                                                    return null;
                                                                                }
                                                                            },
                                                                            new Service()
                                                                            {
                                                                                @Override
                                                                                public String name()
                                                                                {
                                                                                    return "Authentication";
                                                                                }


                                                                                @Override
                                                                                public String serviceType()
                                                                                {
                                                                                    return "com.smoothsync.authenticate";
                                                                                }


                                                                                @Override
                                                                                public URI uri()
                                                                                {
                                                                                    return uri;
                                                                                }


                                                                                @Override
                                                                                public KeyStore keyStore()
                                                                                {
                                                                                    return null;
                                                                                }
                                                                            },
                                                                            new Service()
                                                                            {
                                                                                @Override
                                                                                public String name()
                                                                                {
                                                                                    return "Calendars";
                                                                                }


                                                                                @Override
                                                                                public String serviceType()
                                                                                {
                                                                                    return "caldav";
                                                                                }


                                                                                @Override
                                                                                public URI uri()
                                                                                {
                                                                                    return uri;
                                                                                }


                                                                                @Override
                                                                                public KeyStore keyStore()
                                                                                {
                                                                                    return null;
                                                                                }
                                                                            }
                                                                    );
                                                                }


                                                                @Override
                                                                public DateTime lastModified()
                                                                {
                                                                    return DateTime.now();
                                                                }
                                                            });
                                                }


                                                @Override
                                                public UserCredentials credentials()
                                                {
                                                    return new UserCredentials()
                                                    {
                                                        @Override
                                                        public CharSequence userName()
                                                        {
                                                            return username;
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


        private static class CheckHostnameAsyncTask extends AsyncTask<String, Void, Boolean>
        {

            private final Procedure<Boolean> mResultHandler;


            private CheckHostnameAsyncTask(Procedure<Boolean> resultHandler)
            {
                mResultHandler = resultHandler;
            }


            @Override
            protected Boolean doInBackground(String[] hostnames)
            {
                try
                {
                    return InetAddress.getByName(hostnames[0]) != null;
                }
                catch (Exception e)
                {
                    return false;
                }
            }


            @Override
            protected void onPostExecute(Boolean result)
            {
                mResultHandler.process(result);
            }
        }
    }


    protected interface Params
    {
        Optional<String> username();

        MicroWizard<AccountDetails> next();
    }

}

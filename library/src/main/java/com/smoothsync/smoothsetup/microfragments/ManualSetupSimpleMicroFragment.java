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
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
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
import com.smoothsync.smoothsetup.services.providerservice.functions.ManualProviders;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AccountDetailsBox;
import com.smoothsync.smoothsetup.utils.AfterTextChangedFlowable;

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
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.iterator.Seq;
import org.dmfs.jems2.optional.Absent;
import org.dmfs.jems2.optional.Present;
import org.dmfs.rfc5545.DateTime;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static io.reactivex.rxjava3.core.Single.just;
import static org.dmfs.jems2.iterator.EmptyIterator.emptyIterator;
import static org.dmfs.jems2.optional.Absent.absent;


/**
 * A {@link MicroFragment} that prompts the user to enter a password after {@link Account} has been chosen.
 *
 * @author Marten Gajda
 */
public final class ManualSetupSimpleMicroFragment implements MicroFragment<ManualSetupSimpleMicroFragment.Params>
{
    private final static Pattern HOST_PATTERN = Pattern.compile("[a-zA-Z0-9.-]+(:\\d+)?(/.*)?");

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
        private EditText mUsername;
        private EditText mPassword;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Disposable mObserverDisposable;
        private TextInputLayout mUriInputLayout;


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

            Button button = result.findViewById(R.id.button);
            button.setOnClickListener(this);
            button.setEnabled(false);

            mUriInputLayout = result.findViewById(R.id.uri_input_layout);
            mUriInputLayout.setErrorEnabled(true);
            mUri = result.findViewById(R.id.url_input);
            mUsername = result.findViewById(R.id.username_input);
            mPassword = result.findViewById(R.id.password_input);

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

            Flowable<Boolean> addressValid = new AfterTextChangedFlowable(mUri)
                .debounce(item -> (item.isEmpty() ? Flowable.empty() : Flowable.timer(1, TimeUnit.SECONDS)))
                .map(String::trim)
                .onBackpressureLatest()
                .switchMapSingle(urlString -> just(urlString)
                    .subscribeOn(Schedulers.io())
                    .filter(url -> !url.isEmpty())
                    .map(uri -> Uri.encode(uri, ":/.%"))
                    .map(uri -> HOST_PATTERN.matcher(uri).matches() ? URI.create("https://" + uri) : new URI(uri))
                    .map(this::ensureSecureScheme)
                    .map(uri -> InetAddress.getByName(uri.getHost()) != null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .switchIfEmpty(just(false))
                    .doOnError(this::showError)
                    .doOnSuccess(next -> mUriInputLayout.post(() -> mUriInputLayout.setError(null)))
                    .onErrorReturnItem(false)
                );
            mObserverDisposable = Flowable.combineLatest(
                    new AfterTextChangedFlowable(mUsername),
                    new AfterTextChangedFlowable(mPassword),
                    addressValid,
                    (user, password, address) -> !user.isEmpty() && !password.isEmpty() && address)
                .onErrorResumeWith(Flowable.just(false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    button::setEnabled,
                    error -> Log.e("ManualSetup", "Error while validating input", error));

            return result;
        }


        private URI ensureSecureScheme(URI uri) throws InsecureSchemeException
        {
            if ("https".equals(uri.getScheme()))
            {
                return uri;
            }
            else
            {
                throw new InsecureSchemeException();
            }
        }


        private void showError(Throwable error)
        {
            try
            {
                throw error;
            }
            catch (UnknownHostException e)
            {
                mUriInputLayout.setError(getString(R.string.smoothsetup_error_unknown_host));
            }
            catch (URISyntaxException e)
            {
                mUriInputLayout.setError(getString(R.string.smoothsetup_error_invalid_url));
            }
            catch (InsecureSchemeException e)
            {
                mUriInputLayout.setError(getString(R.string.smoothsetup_error_insecure_scheme));
            }
            catch (Throwable e)
            {
                mUriInputLayout.setError(getString(R.string.smoothsetup_error_other, e.toString()));
            }
        }


        @Override
        public void onDestroyView()
        {
            mObserverDisposable.dispose();
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
                                                return ManualProviders.PREFIX + uri.toString();
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
                                                return emptyIterator();
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
                                                        public org.dmfs.jems2.Optional<KeyStore> keyStore()
                                                        {
                                                            return new Absent<>();
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
                                                        public org.dmfs.jems2.Optional<KeyStore> keyStore()
                                                        {
                                                            return new Absent<>();
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
                                                        public org.dmfs.jems2.Optional<KeyStore> keyStore()
                                                        {
                                                            return new Absent<>();
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
    }


    protected interface Params
    {
        Optional<String> username();

        MicroWizard<AccountDetails> next();
    }


    private final static class InsecureSchemeException extends Exception
    {
    }
}

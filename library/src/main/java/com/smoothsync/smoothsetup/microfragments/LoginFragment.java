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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.AbstractAutoCompleteAdapter;
import com.smoothsync.smoothsetup.restrictions.AccountRestriction;
import com.smoothsync.smoothsetup.restrictions.ProviderAccountRestrictions;
import com.smoothsync.smoothsetup.services.FutureApiServiceConnection;
import com.smoothsync.smoothsetup.services.SmoothSyncApiProxy;
import com.smoothsync.smoothsetup.setupbuttons.AbstractSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.executors.authorizing.UserCredentials;
import org.dmfs.jems.generator.Generator;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.First;
import org.dmfs.jems.optional.decorators.MapCollapsed;
import org.dmfs.jems.single.combined.Backed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A generic Login form with a text edit field and one or more buttons.
 *
 * @author Marten Gajda
 */
public final class LoginFragment extends Fragment
{
    private AutoCompleteTextView mLogin;
    private FutureServiceConnection<SmoothSyncApi> mApiService;
    private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
        mApiService = new FutureApiServiceConnection(getActivity());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View result = inflater.inflate(R.layout.smoothsetup_microfragment_login, container, false);

        mLogin = result.findViewById(android.R.id.input);

        LoginFormAdapterFactory loginFormAdapterFactory = mMicroFragmentEnvironment.microFragment().parameter().loginFormAdapterFactory();
        AbstractAutoCompleteAdapter autoCompleteAdapter = loginFormAdapterFactory.autoCompleteAdapter(getContext(), new SmoothSyncApiProxy(mApiService));
        mLogin.setAdapter(autoCompleteAdapter);
        mLogin.setOnItemClickListener((parent, view, position, id) -> mLogin.post(() ->
        {
            // an autocomplete item has been clicked, trigger autocomplete once again by setting the same text.
            int start = mLogin.getSelectionStart();
            int end = mLogin.getSelectionEnd();
            mLogin.setText(mLogin.getText());
            mLogin.setSelection(start, end);
        }));

        RecyclerView list = result.findViewById(android.R.id.list);

        list.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(llm);

        final AbstractSmoothSetupAdapter adapter = loginFormAdapterFactory.setupButtonAdapter(getContext(), mMicroFragmentEnvironment.host(),
                new SmoothSyncApiProxy(mApiService), () -> mLogin.getText().toString());
        list.setAdapter(adapter);

        mLogin.addTextChangedListener(new TextWatcher()
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
                try
                {
                    adapter.update(s.toString());
                }
                catch (ProtocolException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mLogin.setText(new Backed<String>(mMicroFragmentEnvironment.microFragment().parameter().username(), () -> null).value());

        ((TextView) result.findViewById(android.R.id.message)).setText(loginFormAdapterFactory.promptText(getContext()));

        Optional<Provider> provider = mMicroFragmentEnvironment.microFragment().parameter().loginFormAdapterFactory().provider();
        if (provider.isPresent())
        {
            try
            {
                String providerId = provider.value().id();
                Optional<UserCredentials> credentialsRestrictions = new MapCollapsed<>(
                        AccountRestriction::credentials,
                        new First<>(
                                new ProviderAccountRestrictions(getActivity(), providerId)));

                if (credentialsRestrictions.isPresent())
                {
                    // can't override any restriction for this account
                    mLogin.setText(credentialsRestrictions.value().userName());
                    mLogin.setCompletionHint("Auto filled by managed profile");
                    adapter.update(credentialsRestrictions.value().userName().toString());
                    TextInputLayout til = result.findViewById(R.id.text_input_layout);
                    til.setHelperText("Provided by Managed Profile ");
                    til.setEnabled(false);
                    // fast forward view
                    //         result.post(() -> onProviderSelected(provider.value()));
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
        }

        return result;
    }


    @Override
    public void onDestroy()
    {
        mApiService.disconnect();
        super.onDestroy();
    }


    public interface Params
    {
        LoginFormAdapterFactory loginFormAdapterFactory();

        Optional<String> username();
    }


    public interface LoginFormAdapterFactory
    {
        @NonNull
        <T extends Adapter & Filterable> T autoCompleteAdapter(@NonNull Context context, @NonNull SmoothSyncApi api);

        @NonNull
        <T extends RecyclerView.Adapter<BasicButtonViewHolder> & SetupButtonAdapter> T setupButtonAdapter(@NonNull Context context,
                                                                                                          @NonNull MicroFragmentHost host,
                                                                                                          @NonNull SmoothSyncApi api,
                                                                                                          @NonNull Generator<String> name);

        @NonNull
        String promptText(@NonNull Context context);

        Optional<Provider> provider();
    }
}

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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.TextView;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.AbstractAutoCompleteAdapter;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.services.FutureApiServiceConnection;
import com.smoothsync.smoothsetup.services.SmoothSyncApiProxy;
import com.smoothsync.smoothsetup.setupbuttons.AbstractSmoothSetupAdapter;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;

import org.dmfs.android.bolts.service.FutureServiceConnection;
import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.httpessentials.exceptions.ProtocolException;


/**
 * A generic Login form with a text edit field and one or more buttons.
 *
 * @author Marten Gajda
 */
public final class LoginFragment extends Fragment implements SetupButtonAdapter.OnProviderSelectListener
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

        mLogin = (AutoCompleteTextView) result.findViewById(android.R.id.input);

        LoginFormAdapterFactory loginFormAdapterFactory = mMicroFragmentEnvironment.microFragment().parameter().loginFormAdapterFactory();
        AbstractAutoCompleteAdapter autoCompleteAdapter = loginFormAdapterFactory.autoCompleteAdapter(getContext(), new SmoothSyncApiProxy(mApiService));
        mLogin.setAdapter(autoCompleteAdapter);

        RecyclerView list = (RecyclerView) result.findViewById(android.R.id.list);

        list.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);

        final AbstractSmoothSetupAdapter adapter = loginFormAdapterFactory.setupButtonAdapter(getContext(), this, new SmoothSyncApiProxy(mApiService));
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
                String login = s.toString();
                final int atPos = login.indexOf('@');
                if (atPos > 0 && atPos < login.length() - 1)
                {
                    try
                    {
                        adapter.update(login.substring(atPos + 1));
                    }
                    catch (ProtocolException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        mLogin.setText(mMicroFragmentEnvironment.microFragment().parameter().accountName());

        ((TextView) result.findViewById(android.R.id.message)).setText(loginFormAdapterFactory.promptText(getContext()));

        return result;
    }


    @Override
    public void onDestroy()
    {
        mApiService.disconnect();
        super.onDestroy();
    }


    @Override
    public void onProviderSelected(@NonNull Provider provider)
    {
        mMicroFragmentEnvironment.host().execute(
                getActivity(),
                new Swiped(
                        new ForwardTransition<>(
                                new PasswordMicroFragment(new BasicAccount(mLogin.getText().toString(), provider)))));
    }


    @Override
    public void onOtherSelected()
    {
        mMicroFragmentEnvironment.host().execute(
                getActivity(),
                new Swiped(
                        new ForwardTransition<>(
                                new ProvidersLoadMicroFragment(mLogin.getText().toString()))));
    }


    public interface Params
    {
        LoginFormAdapterFactory loginFormAdapterFactory();

        String accountName();
    }


    public interface LoginFormAdapterFactory extends Parcelable
    {
        @NonNull
        <T extends Adapter & Filterable> T autoCompleteAdapter(@NonNull Context context, @NonNull SmoothSyncApi api);

        @NonNull
        <T extends RecyclerView.Adapter<BasicButtonViewHolder>, SetupButtonAdapter> T setupButtonAdapter(@NonNull Context context,
                                                                                                         @NonNull com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter.OnProviderSelectListener providerSelectListener, SmoothSyncApi api);

        @NonNull
        String promptText(@NonNull Context context);
    }
}

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.services.binders.PackageServiceBinder;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.Timestamp;
import org.dmfs.android.microfragments.timestamps.UiTimestamp;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.FragmentTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microfragments.transitions.XFaded;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.optional.NullSafe;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


/**
 * A {@link MicroFragment} that loads a provider by its id before moving on to a setup step.
 *
 * @author Marten Gajda
 */
public final class ProvidersLoadMicroFragment implements MicroFragment<ProvidersLoadMicroFragment.Params>
{
    public final static Creator<ProvidersLoadMicroFragment> CREATOR = new Creator<ProvidersLoadMicroFragment>()
    {
        @Override
        public ProvidersLoadMicroFragment createFromParcel(Parcel source)
        {
            return new ProvidersLoadMicroFragment(new NullSafe<>(source.readString()),
                    new Unboxed<MicroWizard<ChooseProviderMicroFragment.ProviderSelection>>(source).value());
        }


        @Override
        public ProvidersLoadMicroFragment[] newArray(int size)
        {
            return new ProvidersLoadMicroFragment[size];
        }
    };
    @Nullable
    private final Optional<String> mAccount;
    private final MicroWizard<ChooseProviderMicroFragment.ProviderSelection> mNext;


    protected interface Params
    {
        Optional<String> username();

        MicroWizard<ChooseProviderMicroFragment.ProviderSelection> next();
    }


    /**
     * Create a {@link ProvidersLoadMicroFragment} that loads the provider with the given id.
     *
     * @param account
     *         An optional account identifier to set up.
     * @param next
     */
    public ProvidersLoadMicroFragment(@Nullable Optional<String> account, MicroWizard<ChooseProviderMicroFragment.ProviderSelection> next)
    {
        mAccount = account;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_loading);
    }


    @Override
    public boolean skipOnBack()
    {
        return true;
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost host)
    {
        return new LoadFragment();
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
                return mAccount;
            }


            @Override
            public MicroWizard<ChooseProviderMicroFragment.ProviderSelection> next()
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
        dest.writeString(new Backed<String>(mAccount, () -> null).value());
        dest.writeParcelable(mNext.boxed(), flags);
    }


    public final static class LoadFragment extends Fragment
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
        private Timestamp mTimestamp = new UiTimestamp();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
        }


        @Override
        public void onResume()
        {
            super.onResume();
            new PackageServiceBinder(getContext()).wrapped()
                    .flatMapObservable(ProviderService::all)
                    .collect((Supplier<ArrayList<Provider>>) ArrayList::new, ArrayList::add)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::onResult,
                            e -> mMicroFragmentEnvironment.host()
                                    .execute(getActivity(),
                                            new XFaded(
                                                    new ForwardTransition<>(
                                                            new ErrorRetryMicroFragment(getString(R.string.smoothsetup_error_load_provider)),
                                                            mTimestamp)))
                    );
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_loading, container, false);
            result.findViewById(android.R.id.message).animate().setStartDelay(DELAY_WAIT_MESSAGE).alpha(1f).start();
            return result;
        }


        private void onResult(List<Provider> providers)
        {
            if (!isResumed())
            {
                return;
            }
            FragmentTransition transition = new ForwardTransition<>(
                    mMicroFragmentEnvironment.microFragment().parameter().next().microFragment(
                            getActivity(),
                            new ChooseProviderMicroFragment.ProviderSelection()
                            {
                                @NonNull
                                @Override
                                public Provider[] providers()
                                {
                                    return providers.toArray(new Provider[0]);
                                }


                                @NonNull
                                @Override
                                public Optional<String> username()
                                {
                                    return mMicroFragmentEnvironment.microFragment().parameter().username();
                                }
                            }
                    ));

            mMicroFragmentEnvironment.host().execute(getActivity(),
                    // if we were quick, swipe, otherwise fade
                    mTimestamp.nanoSeconds() + MILLISECONDS.toNanos(200) < System.nanoTime() ?
                            new XFaded(transition) : new Swiped(transition));

        }
    }
}

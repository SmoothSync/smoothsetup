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
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.ParcelableProvider;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.util.Arrays;
import java.util.Comparator;


/**
 * A {@link MicroFragment} that presents the user with a list of providers to choose from.
 *
 * @author Marten Gajda
 */
public final class ChooseProviderMicroFragment implements MicroFragment<ChooseProviderMicroFragment.ProviderListFragment.Params>
{
    public final static Creator<ChooseProviderMicroFragment> CREATOR = new Creator<ChooseProviderMicroFragment>()
    {
        @Override
        public ChooseProviderMicroFragment createFromParcel(Parcel source)
        {
            return new ChooseProviderMicroFragment((Provider[]) source.readParcelableArray(getClass().getClassLoader()), source.readString());
        }


        @Override
        public ChooseProviderMicroFragment[] newArray(int size)
        {
            return new ChooseProviderMicroFragment[size];
        }
    };
    private final ParcelableProvider[] mProviders;
    private final String mAccount;


    public ChooseProviderMicroFragment(@NonNull Provider[] providers, @NonNull String account)
    {
        mProviders = new ParcelableProvider[providers.length];
        for (int i = 0, count = providers.length; i < count; ++i)
        {
            Provider p = providers[i];
            if (!(p instanceof Parcelable))
            {
                mProviders[i] = new ParcelableProvider(p);
            }
            else
            {
                mProviders[i] = (ParcelableProvider) p;
            }
        }
        Arrays.sort(mProviders, new Comparator<ParcelableProvider>()
        {
            @Override
            public int compare(ParcelableProvider lhs, ParcelableProvider rhs)
            {
                try
                {
                    return lhs.name().compareToIgnoreCase(rhs.name());
                }
                catch (ProtocolException e)
                {
                    throw new RuntimeException("can't read provider name ", e);
                }
            }
        });
        mAccount = account;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_choose_provider);
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
        return new ProviderListFragment();
    }


    @NonNull
    @Override
    public ProviderListFragment.Params parameter()
    {
        return new ProviderListFragment.Params()
        {
            @NonNull
            @Override
            public Provider[] provider()
            {
                return mProviders;
            }


            @NonNull
            @Override
            public String account()
            {
                return mAccount;
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
        dest.writeParcelableArray(mProviders, flags);
        dest.writeString(mAccount);
    }


    /**
     * A Fragment that shows a list of providers.
     *
     * @author Marten Gajda <marten@dmfs.org>
     */
    public static final class ProviderListFragment extends Fragment implements SetupButtonAdapter.OnProviderSelectListener
    {
        private RecyclerView mView;
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mView = (RecyclerView) inflater.inflate(R.layout.smoothsetup_microfragment_provider_list, container, false);
            mView.setLayoutManager(new LinearLayoutManager(getContext()));
            mView.setAdapter(new ProvidersRecyclerViewAdapter(Arrays.asList(mMicroFragmentEnvironment.microFragment().parameter().provider()), this));
            return mView;
        }


        @Override
        public void onProviderSelected(@NonNull Provider provider)
        {
            mMicroFragmentEnvironment.host()
                    .execute(getActivity(),
                            new Swiped(
                                    new ForwardTransition<>(
                                            new ProviderLoginMicroFragment(provider, mMicroFragmentEnvironment.microFragment().parameter().account()))));
        }


        @Override
        public void onOtherSelected()
        {
            // should not be called
        }


        interface Params
        {
            @NonNull
            Provider[] provider();

            @NonNull
            String account();
        }
    }
}

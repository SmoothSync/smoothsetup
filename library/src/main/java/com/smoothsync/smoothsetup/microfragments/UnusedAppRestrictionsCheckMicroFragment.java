/*
 * Copyright (c) 2018 dmfs GmbH
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

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.UnusedAppRestriction;
import com.smoothsync.smoothsetup.wizard.RequestUnusedAppRestrictions;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.UnusedAppRestrictionsConstants;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;


/**
 * A {@link MicroFragment} that checks whether the user lifted the unused app restrictions on the app.
 */
public final class UnusedAppRestrictionsCheckMicroFragment<T extends Boxable<T>> implements MicroFragment<UnusedAppRestrictionsCheckMicroFragment.RestrictionsCheckFragment.Params<T>>
{

    public final static List<Integer> APP_RESTRICTED_VALUES = asList(
        UnusedAppRestrictionsConstants.API_30_BACKPORT,
        UnusedAppRestrictionsConstants.API_30,
        UnusedAppRestrictionsConstants.API_31
    );

    public final static Creator<UnusedAppRestrictionsCheckMicroFragment<?>> CREATOR = new Creator<UnusedAppRestrictionsCheckMicroFragment<?>>()
    {
        @SuppressWarnings("unchecked") // we don't know the generic type in static context
        @Override
        public UnusedAppRestrictionsCheckMicroFragment<?> createFromParcel(Parcel source)
        {
            return new UnusedAppRestrictionsCheckMicroFragment(
                (Boxable) new Unboxed<>(source).value(),
                new Unboxed<MicroWizard<?>>(source).value());
        }


        @Override
        public UnusedAppRestrictionsCheckMicroFragment<?>[] newArray(int size)
        {
            return new UnusedAppRestrictionsCheckMicroFragment[size];
        }
    };

    private final T mData;
    private final MicroWizard<T> mNext;


    public UnusedAppRestrictionsCheckMicroFragment(T passthroughData, MicroWizard<T> next)
    {
        mData = passthroughData;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return "Checking App Restrictions";
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
        return new RestrictionsCheckFragment<>();
    }


    @NonNull
    @Override
    public RestrictionsCheckFragment.Params<T> parameter()
    {
        return new RestrictionsCheckFragment.Params<T>()
        {
            @NonNull
            @Override
            public T data()
            {
                return mData;
            }


            @NonNull
            @Override
            public MicroWizard<T> next()
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
        dest.writeParcelable(mData.boxed(), flags);
        dest.writeParcelable(mNext.boxed(), flags);
    }


    public static final class RestrictionsCheckFragment<T> extends Fragment
    {
        private final static int DELAY_WAIT_MESSAGE = 2500;
        private View mView;
        private MicroFragmentEnvironment<Params<T>> mMicroFragmentEnvironment;
        private Disposable mDisposable;


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_loading, container, false);
            result.findViewById(android.R.id.message).animate().setStartDelay(DELAY_WAIT_MESSAGE).alpha(1f).start();
            return result;
        }


        @Override
        public void onResume()
        {
            super.onResume();
            mDisposable = new UnusedAppRestriction(getContext())
                .map(APP_RESTRICTED_VALUES::contains)
                .filter(TRUE::equals)
                .map(ignored -> (MicroWizard<T>) new RequestUnusedAppRestrictions(mMicroFragmentEnvironment.microFragment().parameter().next()))
                .switchIfEmpty(Single.just(mMicroFragmentEnvironment.microFragment().parameter().next()))
                // for now we ignore errors and move on
                .onErrorResumeWith(Single.just(mMicroFragmentEnvironment.microFragment().parameter().next()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    fragment ->
                        mMicroFragmentEnvironment.host()
                            .execute(getContext(),
                                new Swiped(
                                    new ForwardTransition<>(fragment.microFragment(
                                        getContext(),
                                        mMicroFragmentEnvironment.microFragment().parameter().data()))))
                );
        }


        @Override
        public void onPause()
        {
            if (!mDisposable.isDisposed())
            {
                mDisposable.dispose();
            }
            super.onPause();
        }


        interface Params<T>
        {
            @NonNull
            T data();

            @NonNull
            MicroWizard<T> next();
        }
    }
}

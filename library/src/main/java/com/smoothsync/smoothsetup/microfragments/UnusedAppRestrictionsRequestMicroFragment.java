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
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.AppLabel;
import com.smoothsync.smoothsetup.utils.UnusedAppRestriction;

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
import androidx.core.content.IntentCompat;
import androidx.core.content.UnusedAppRestrictionsConstants;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.disposables.Disposable;

import static java.util.Arrays.asList;


/**
 * A {@link MicroFragment} that presents the user with a list of providers to choose from.
 *
 * @author Marten Gajda
 */
public final class UnusedAppRestrictionsRequestMicroFragment<T extends Boxable<T>> implements MicroFragment<UnusedAppRestrictionsRequestMicroFragment.PermissionListFragment.Params<T>>
{

    public final static List<Integer> APP_RESTRICTED_VALUES = asList(
        UnusedAppRestrictionsConstants.API_30_BACKPORT,
        UnusedAppRestrictionsConstants.API_30,
        UnusedAppRestrictionsConstants.API_31
    );

    public final static Creator<UnusedAppRestrictionsRequestMicroFragment<?>> CREATOR = new Creator<UnusedAppRestrictionsRequestMicroFragment<?>>()
    {
        @SuppressWarnings("unchecked") // we don't know the generic type in static context
        @Override
        public UnusedAppRestrictionsRequestMicroFragment<?> createFromParcel(Parcel source)
        {
            return new UnusedAppRestrictionsRequestMicroFragment(
                (Boxable) new Unboxed<>(source).value(),
                new Unboxed<MicroWizard<?>>(source).value());
        }


        @Override
        public UnusedAppRestrictionsRequestMicroFragment<?>[] newArray(int size)
        {
            return new UnusedAppRestrictionsRequestMicroFragment[size];
        }
    };

    private final T mData;
    private final MicroWizard<T> mNext;


    public UnusedAppRestrictionsRequestMicroFragment(T passthroughData, MicroWizard<T> next)
    {
        mData = passthroughData;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.explain_android_title_disable_app_hibernation);
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
        return new PermissionListFragment<>();
    }


    @NonNull
    @Override
    public PermissionListFragment.Params<T> parameter()
    {
        return new PermissionListFragment.Params<T>()
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


    /**
     * A Fragment that asks the user to lift unused App restrictions.
     */
    public static final class PermissionListFragment<T> extends Fragment
    {
        private MicroFragmentEnvironment<Params<T>> mMicroFragmentEnvironment;
        private Disposable mDisposable;
        private View mView;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            mView = inflater.inflate(R.layout.smoothsetup_microfragment_liftrestrictions, container, false);
            mView.findViewById(R.id.button)
                .setOnClickListener(
                    view -> startActivityForResult(IntentCompat.createManageUnusedAppRestrictionsIntent(getContext(), getActivity().getPackageName()), 1));
            String appLabel = new AppLabel(getContext()).value();
            ((TextView) mView.findViewById(R.id.message1)).setText(getContext().getString(R.string.explain_android_prompt_disable_app_hibernation, appLabel));
            ((TextView) mView.findViewById(R.id.message2)).setText(
                getContext().getString(R.string.explain_android_prompt_disable_app_hibernation_reasoning, appLabel));
            checkPermission(false);
            return mView;
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 1)
            {
                checkPermission(true);
            }
        }


        private void checkPermission(boolean showReasoning)
        {
            mDisposable = new UnusedAppRestriction(getContext())
                .filter(value->!APP_RESTRICTED_VALUES.contains(value))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() ->
                    {
                        if (showReasoning)
                        {
                            mView.findViewById(R.id.message2).setVisibility(View.VISIBLE);
                        }
                        mView.findViewById(R.id.lift_restrictions_root).setAlpha(1);
                    }
                )
                .map(ignored -> mMicroFragmentEnvironment.microFragment().parameter().next())
                // for now we ignore errors and move on
                .onErrorResumeWith(Maybe.just(mMicroFragmentEnvironment.microFragment().parameter().next()))
                .subscribe(this::moveOn);
        }


        private void moveOn(MicroWizard<T> microWizard)
        {
            mMicroFragmentEnvironment
                .host()
                .execute(
                    getContext(),
                    new Swiped(new ForwardTransition<>(
                        microWizard.microFragment(getActivity(), mMicroFragmentEnvironment.microFragment().parameter().data()))));
        }


        @Override
        public void onPause()
        {
            super.onPause();
            if (mDisposable != null && !mDisposable.isDisposed())
            {
                mDisposable.dispose();
            }
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

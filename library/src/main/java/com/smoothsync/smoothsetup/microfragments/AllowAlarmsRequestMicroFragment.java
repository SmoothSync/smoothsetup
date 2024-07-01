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

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.AppLabel;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.content.IntentCompat;
import androidx.fragment.app.Fragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;


/**
 * A {@link MicroFragment} that prompts the user to allow alarms.
 *
 * @author Marten Gajda
 */
public final class AllowAlarmsRequestMicroFragment<T extends Boxable<T>> implements MicroFragment<AllowAlarmsRequestMicroFragment.PermissionListFragment.Params<T>>
{
    public final static Creator<AllowAlarmsRequestMicroFragment<?>> CREATOR = new Creator<AllowAlarmsRequestMicroFragment<?>>()
    {
        @SuppressWarnings("unchecked") // we don't know the generic type in static context
        @Override
        public AllowAlarmsRequestMicroFragment<?> createFromParcel(Parcel source)
        {
            return new AllowAlarmsRequestMicroFragment(
                (Boxable) new Unboxed<>(source).value(),
                new Unboxed<MicroWizard<?>>(source).value());
        }


        @Override
        public AllowAlarmsRequestMicroFragment<?>[] newArray(int size)
        {
            return new AllowAlarmsRequestMicroFragment[size];
        }
    };

    private final T mData;
    private final MicroWizard<T> mNext;


    public AllowAlarmsRequestMicroFragment(T passthroughData, MicroWizard<T> next)
    {
        mData = passthroughData;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_title_allow_alarms);
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
                    view -> startActivityForResult(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM), 123));
            String appLabel = new AppLabel(getContext()).value();
            ((TextView) mView.findViewById(R.id.message1)).setText(getContext().getString(R.string.smoothsetup_prompt_allow_alarms, appLabel));
            ((TextView) mView.findViewById(R.id.message2)).setText("");
            ((TextView) mView.findViewById(R.id.message_instructions)).setText("");
            ((TextView) mView.findViewById(R.id.button)).setText(R.string.smoothsetup_title_allow_alarms);
            mView.findViewById(R.id.lift_restrictions_root).setAlpha(1);

            return mView;
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 123)
            {
                moveOn(mMicroFragmentEnvironment.microFragment().parameter().next());
            }
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

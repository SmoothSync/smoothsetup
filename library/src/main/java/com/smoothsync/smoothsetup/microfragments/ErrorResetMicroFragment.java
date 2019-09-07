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

import com.smoothsync.smoothsetup.R;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ResetTransition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} shows an error message with an option to retry.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ErrorResetMicroFragment implements MicroFragment<MicroFragment<?>>
{
    public final static Creator<ErrorResetMicroFragment> CREATOR = new Creator<ErrorResetMicroFragment>()
    {
        @Override
        public ErrorResetMicroFragment createFromParcel(Parcel source)
        {
            return new ErrorResetMicroFragment((MicroFragment<?>) source.readParcelable(getClass().getClassLoader()));
        }


        @Override
        public ErrorResetMicroFragment[] newArray(int size)
        {
            return new ErrorResetMicroFragment[size];
        }
    };
    @NonNull
    private final MicroFragment<?> mRetryFragment;


    public ErrorResetMicroFragment(@NonNull MicroFragment<?> retryFragment)
    {
        this.mRetryFragment = retryFragment;
    }


    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_error);
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
        return new ErrorFragment();
    }


    @NonNull
    @Override
    public MicroFragment<?> parameter()
    {
        return mRetryFragment;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(mRetryFragment, flags);
    }


    /**
     * A Fragment that shows an error with an option to retry.
     */
    public final static class ErrorFragment extends Fragment
    {

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_error, container, false);
            MicroFragmentEnvironment<MicroFragment<?>> mMicroFragmentEnvironment = new FragmentEnvironment<>(this);

            result.findViewById(android.R.id.button1)
                    .setOnClickListener(
                            v -> mMicroFragmentEnvironment.host().execute(
                                    getActivity(),
                                    new ResetTransition<>(mMicroFragmentEnvironment.microFragment().parameter())));
            return result;
        }
    }
}

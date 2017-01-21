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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.BackTransition;


/**
 * A {@link MicroFragment} that shows an error message and a button to return to the previous step.
 *
 * @author Marten Gajda
 */
public final class ErrorRetryMicroFragment implements MicroFragment<ErrorRetryMicroFragment.ErrorFragment.Params>
{
    public final static Creator<ErrorRetryMicroFragment> CREATOR = new Creator<ErrorRetryMicroFragment>()
    {
        @Override
        public ErrorRetryMicroFragment createFromParcel(Parcel source)
        {
            return new ErrorRetryMicroFragment(source.readString(), source.readString(), source.readString());
        }


        @Override
        public ErrorRetryMicroFragment[] newArray(int size)
        {
            return new ErrorRetryMicroFragment[size];
        }
    };
    @Nullable
    private final String mTitle;
    @NonNull
    private final String mError;
    @Nullable
    private final String mButtonText;


    public ErrorRetryMicroFragment(@NonNull String error)
    {
        this(error, null, null);
    }


    public ErrorRetryMicroFragment(@NonNull String error, @Nullable String buttonText)
    {
        this(error, buttonText, null);
    }


    public ErrorRetryMicroFragment(@NonNull String error, @Nullable String buttonText, @Nullable String title)
    {
        mError = error;
        mButtonText = buttonText;
        mTitle = title;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return mTitle == null ? context.getString(R.string.smoothsetup_wizard_title_error) : mTitle;
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
    public ErrorFragment.Params parameter()
    {
        return new ErrorFragment.Params()
        {
            @NonNull
            @Override
            public String error()
            {
                return mError;
            }


            @Nullable
            @Override
            public String buttonText()
            {
                return mButtonText;
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
        dest.writeString(mError);
        dest.writeString(mButtonText);
        dest.writeString(mTitle);
    }


    /**
     * A Fragment that shows an error and a button to return to the previous step.
     */
    public final static class ErrorFragment extends Fragment implements View.OnClickListener
    {
        private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            Params params = mMicroFragmentEnvironment.microFragment().parameter();
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_error, container, false);

            ((TextView) result.findViewById(android.R.id.message)).setText(params.error());

            Button button = ((Button) result.findViewById(android.R.id.button1));
            button.setOnClickListener(this);

            String buttonText = params.buttonText();
            if (buttonText != null)
            {
                button.setText(buttonText);
            }

            return result;
        }


        @Override
        public void onClick(View v)
        {
            mMicroFragmentEnvironment.host().execute(getActivity(), new BackTransition());
        }


        interface Params
        {
            @NonNull
            String error();

            @Nullable
            String buttonText();
        }
    }
}

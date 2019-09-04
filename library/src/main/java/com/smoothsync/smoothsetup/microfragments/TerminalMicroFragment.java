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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} that shows a "complete" message.
 *
 * @author Marten Gajda
 */
public final class TerminalMicroFragment implements MicroFragment<String>
{

    public final static Creator<TerminalMicroFragment> CREATOR = new Creator<TerminalMicroFragment>()
    {
        @Override
        public TerminalMicroFragment createFromParcel(Parcel parcel)
        {
            return new TerminalMicroFragment(parcel.readString());
        }


        @Override
        public TerminalMicroFragment[] newArray(int i)
        {
            return new TerminalMicroFragment[i];
        }
    };

    private final String mMessage;


    public TerminalMicroFragment(String message)
    {
        mMessage = message;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getString(R.string.smoothsetup_wizard_title_setup_completed);
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
        return new MessageFragment();
    }


    @NonNull
    @Override
    public String parameter()
    {
        return mMessage;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mMessage);
    }


    /**
     * A Fragment that shows a message.
     */
    public final static class MessageFragment extends Fragment
    {

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_setup_completed, container, false);

            ((TextView) result.findViewById(android.R.id.message)).setText(new FragmentEnvironment<String>(this).microFragment().parameter());

            Button button = result.findViewById(android.R.id.button1);
            button.setOnClickListener(v ->
            {
                Activity activity = getActivity();
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            });

            return result;
        }
    }
}

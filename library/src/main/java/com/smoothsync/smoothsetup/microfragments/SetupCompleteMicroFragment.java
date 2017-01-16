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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;

import org.dmfs.android.microfragments.BasicMicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;


/**
 * A {@link MicroFragment} that shows a "setup complete" message.
 *
 * @author Marten Gajda
 */
public final class SetupCompleteMicroFragment implements MicroFragment<Void>
{

    public final static Creator<SetupCompleteMicroFragment> CREATOR = new Creator<SetupCompleteMicroFragment>()
    {
        @Override
        public SetupCompleteMicroFragment createFromParcel(Parcel source)
        {
            return new SetupCompleteMicroFragment();
        }


        @Override
        public SetupCompleteMicroFragment[] newArray(int size)
        {
            return new SetupCompleteMicroFragment[size];
        }
    };


    public SetupCompleteMicroFragment()
    {
        // nothing to do here
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
        Fragment result = new MessageFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(MicroFragment.ARG_ENVIRONMENT, new BasicMicroFragmentEnvironment<>(this, host));
        result.setArguments(arguments);
        return result;
    }


    @NonNull
    @Override
    public Void parameters()
    {
        return null;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
    }


    /**
     * A Fragment that shows a message.
     */
    public final static class MessageFragment extends Fragment implements View.OnClickListener
    {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_microfragment_setup_completed, container, false);

            ((TextView) result.findViewById(android.R.id.message))
                    .setText(getString(R.string.smoothsetup_message_setup_completed, getString(getContext().getApplicationInfo().labelRes)));

            Button button = ((Button) result.findViewById(android.R.id.button1));
            button.setOnClickListener(this);

            return result;
        }


        @Override
        public void onClick(View v)
        {
            Activity activity = getActivity();
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }
    }
}

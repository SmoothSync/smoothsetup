/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.wizardsteps;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardtransitions.BackWizardTransition;


/**
 * A WizardStep that shows an error message and a button to return to the previous step.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ErrorRetryWizardStep implements WizardStep
{

    private final static String ARG_TITLE = "title";
    private final static String ARG_ERROR_MESSAGE = "error";
    private final static String ARG_BUTTON_TEXT = "button";

    private final String mTitle;
    private final String mError;
    private final String mButtonText;


    public ErrorRetryWizardStep(String error)
    {
        this(error, null, null);
    }


    public ErrorRetryWizardStep(String error, String buttonText)
    {
        this(error, buttonText, null);
    }


    public ErrorRetryWizardStep(String error, String buttonText, String title)
    {
        mError = error;
        mButtonText = buttonText;
        mTitle = title;
    }


    @Override
    public String title(Context context)
    {
        return mTitle == null ? context.getString(R.string.smoothsetup_wizard_title_error) : mTitle;
    }


    @Override
    public boolean skipOnBack()
    {
        return true;
    }


    @Override
    public Fragment fragment(Context context)
    {
        Fragment result = new ErrorFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_WIZARD_STEP, this);
        arguments.putString(ARG_TITLE, mTitle);
        arguments.putString(ARG_ERROR_MESSAGE, mError);
        arguments.putString(ARG_BUTTON_TEXT, mButtonText);
        result.setArguments(arguments);
        result.setRetainInstance(true);
        return result;
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


    public final static Creator<ErrorRetryWizardStep> CREATOR = new Creator<ErrorRetryWizardStep>()
    {
        @Override
        public ErrorRetryWizardStep createFromParcel(Parcel source)
        {
            return new ErrorRetryWizardStep(source.readString(), source.readString(), source.readString());
        }


        @Override
        public ErrorRetryWizardStep[] newArray(int size)
        {
            return new ErrorRetryWizardStep[size];
        }
    };


    /**
     * A Fragment that shows an error and a button to return to the previous step.
     */
    public static class ErrorFragment extends Fragment implements View.OnClickListener
    {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View result = inflater.inflate(R.layout.smoothsetup_wizard_fragment_error, container, false);

            ((TextView) result.findViewById(android.R.id.message)).setText(getArguments().getString(ARG_ERROR_MESSAGE));

            Button button = ((Button) result.findViewById(android.R.id.button1));
            button.setOnClickListener(this);

            String buttonText = getArguments().getString(ARG_BUTTON_TEXT);
            if (buttonText != null)
            {
                button.setText(buttonText);
            }

            return result;
        }


        @Override
        public void onClick(View v)
        {
            new BackWizardTransition().execute(getContext());
        }
    }
}

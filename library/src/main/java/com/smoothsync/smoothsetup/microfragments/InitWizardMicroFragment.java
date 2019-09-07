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
import android.os.Parcel;

import com.smoothsync.smoothsetup.services.WizardService;
import com.smoothsync.smoothsetup.utils.LoadingFragment;
import com.smoothsync.smoothsetup.utils.StringMeta;

import org.dmfs.android.bolts.service.elementary.FutureLocalServiceConnection;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardResetTransition;
import org.dmfs.android.microfragments.transitions.ForwardTransition;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 * A {@link MicroFragment} to load a wizard.
 *
 * @author Marten Gajda
 */
public final class InitWizardMicroFragment implements MicroFragment<InitWizardMicroFragment.Params>
{
    public final static Creator<InitWizardMicroFragment> CREATOR = new Creator<InitWizardMicroFragment>()
    {
        @Override
        public InitWizardMicroFragment createFromParcel(Parcel source)
        {
            return new InitWizardMicroFragment(source.readString(), source.readParcelable(getClass().getClassLoader()));
        }


        @Override
        public InitWizardMicroFragment[] newArray(int size)
        {
            return new InitWizardMicroFragment[size];
        }
    };


    interface Params
    {
        String metaKey();

        Intent intent();
    }


    private final String mWizardServiceMetaKey;
    private final Intent mIntent;


    public InitWizardMicroFragment(String wizardServiceMetaKey, Intent intent)
    {
        mWizardServiceMetaKey = wizardServiceMetaKey;
        mIntent = intent;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        // this step should be fairly quick, showing a title for a few milliseconds could lead to disturbing UI glitches
        return "";
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
        return new LoadWizardFragment();
    }


    @NonNull
    @Override
    public Params parameter()
    {
        return new Params()
        {
            @Override
            public String metaKey()
            {
                return mWizardServiceMetaKey;
            }


            @Override
            public Intent intent()
            {
                return mIntent;
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
        dest.writeString(mWizardServiceMetaKey);
        dest.writeParcelable(mIntent, flags);
    }


    public final static class LoadWizardFragment extends LoadingFragment<Params, MicroFragment<?>>
    {
        public LoadWizardFragment()
        {
            super((context, env) ->
                            new FutureLocalServiceConnection<WizardService>(context,
                                    new Intent(
                                            new StringMeta(context, env.microFragment().parameter().metaKey()).value())
                                            .setPackage(context.getPackageName()))
                                    .service(1000)
                                    .initialMicroFragment(context, env.microFragment().parameter().intent()),
                    microFragmentAsyncTaskResult ->
                    {
                        try
                        {
                            return new ForwardTransition<>(microFragmentAsyncTaskResult.value());
                        }
                        catch (Exception e)
                        {
                            return new ForwardResetTransition<>(new ErrorRetryMicroFragment("Unexpected Exception:\n\n" + e.getMessage()));
                        }
                    });
        }
    }

}

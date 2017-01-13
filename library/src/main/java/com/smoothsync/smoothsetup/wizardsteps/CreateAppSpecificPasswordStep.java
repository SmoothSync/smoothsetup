package com.smoothsync.smoothsetup.wizardsteps;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;

import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.AppSpecificWebviewFragment;

import java.net.URI;


/**
 * A WizardStep to present a web site.
 *
 * @author Marten Gajda
 */
public final class CreateAppSpecificPasswordStep implements WizardStep
{
    private final String mTitle;
    private final URI mUrl;


    public CreateAppSpecificPasswordStep(String title, URI url)
    {
        mTitle = title;
        mUrl = url;
    }


    @Override
    public String title(Context context)
    {
        return mTitle;
    }


    @Override
    public Fragment fragment(Context context)
    {
        Fragment result = new AppSpecificWebviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_WIZARD_STEP, this);
        args.putString(AppSpecificWebviewFragment.ARG_URL, mUrl.toASCIIString());
        result.setArguments(args);
        return result;
    }


    @Override
    public boolean skipOnBack()
    {
        return false;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mTitle);
        dest.writeSerializable(mUrl);
    }


    public final static Creator<CreateAppSpecificPasswordStep> CREATOR = new Creator<CreateAppSpecificPasswordStep>()
    {
        @Override
        public CreateAppSpecificPasswordStep createFromParcel(Parcel source)
        {
            return new CreateAppSpecificPasswordStep(source.readString(), (URI) source.readSerializable());
        }


        @Override
        public CreateAppSpecificPasswordStep[] newArray(int size)
        {
            return new CreateAppSpecificPasswordStep[size];
        }
    };
}

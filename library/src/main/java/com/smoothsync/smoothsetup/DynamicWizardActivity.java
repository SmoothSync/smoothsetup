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

package com.smoothsync.smoothsetup;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.smoothsync.smoothsetup.microfragments.InitWizardMicroFragment;
import com.smoothsync.smoothsetup.utils.ActivityInfo;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.combined.Backed;


/**
 * An {@link Activity} which decides how to launch the setup wizard.
 *
 * @author Marten Gajda
 */
public final class DynamicWizardActivity extends Activity
{
    public final static String META_WIZARD_SERVICE_META = "com.smoothsync.meta.WIZARD_SERVICE_META";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // check if meta data contains a specific provider url, if the url is hard coded, we don't allow to override it
        Optional<Bundle> metaData = new NullSafe<>(new ActivityInfo(this, PackageManager.GET_META_DATA).value().metaData);
        if (!new Backed<>(metaData, Bundle.EMPTY).value().containsKey(META_WIZARD_SERVICE_META))
        {
            throw new RuntimeException(String.format("Missing %s meta field", META_WIZARD_SERVICE_META));
        }
        launchStep(new InitWizardMicroFragment(metaData.value().getString(META_WIZARD_SERVICE_META), getIntent()));
    }


    private void launchStep(MicroFragment<?> microFragment)
    {
        MicroFragmentHostActivity.launch(this, microFragment);
        finish();
    }
}

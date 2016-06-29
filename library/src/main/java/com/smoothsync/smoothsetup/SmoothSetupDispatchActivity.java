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

package com.smoothsync.smoothsetup;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardsteps.GenericProviderWizardStep;
import com.smoothsync.smoothsetup.wizardsteps.ProviderLoadWizardStep;
import com.smoothsync.smoothsetup.wizardsteps.WaitForBroadcastWizardStep;


/**
 * The launcher activity. It decides how to launch the setup wizard.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class SmoothSetupDispatchActivity extends AppCompatActivity
{
	public final static String META_PROVIDER = "com.smoothsync.PROVIDER";

	public final static String PREF_REFERRER = "referrer";

	public final static String PARAM_PROVIDER = "provider";

	public final static String PARAM_ACCOUNT = "account";


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// check if meta data contains a specific provider id, if the provider-id is hard coded, we don't allow to override it
		PackageManager pm = getPackageManager();
		try
		{
			ActivityInfo ai = pm.getActivityInfo(new ComponentName(this, this.getClass()), PackageManager.GET_META_DATA);
			if (ai.metaData != null && ai.metaData.containsKey(META_PROVIDER))
			{
				launchStep(new ProviderLoadWizardStep(ai.metaData.getString(META_PROVIDER), ""));
				return;
			}
		}
		catch (PackageManager.NameNotFoundException e)
		{
			throw new RuntimeException("Can't load own activity info", e);
		}

		// check if intent data has a provider id
		Uri data = getIntent().getData();
		if (data != null && data.getQueryParameter(PARAM_PROVIDER) != null)
		{
			launchStep(new ProviderLoadWizardStep(data.getQueryParameter(PARAM_PROVIDER), data.getQueryParameter(PARAM_ACCOUNT)));
			return;
		}

		// check if shared preferences contain a provider id
		SharedPreferences pref = getSharedPreferences("com.smoothsync.smoothsetup.prefs", 0);
		if (pref.contains(PREF_REFERRER))
		{
			String referrer = pref.getString(PREF_REFERRER, null);
			if (!TextUtils.isEmpty(referrer))
			{
				Uri uri = Uri.parse(referrer);
				if (uri.getQueryParameter(PARAM_PROVIDER) != null)
				{
					launchStep(new ProviderLoadWizardStep(uri.getQueryParameter(PARAM_PROVIDER), uri.getQueryParameter(PARAM_ACCOUNT)));
					return;
				}
			}
			else
			{
				// no referrer
				launchStep(new GenericProviderWizardStep());
				return;
			}
		}

		// launch wait fo the referrer braodcast
		launchStep(new WaitForBroadcastWizardStep());
	}


	private void launchStep(WizardStep wizardStep)
	{
		WizardActivity.launch(this, wizardStep);
		finish();
	}
}

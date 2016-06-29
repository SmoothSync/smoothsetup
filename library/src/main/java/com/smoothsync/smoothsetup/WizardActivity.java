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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;

import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardcontroller.BroadcastWizardController;


/**
 * An Activity to host a setup wizard.
 *
 * TODO: consider to make this concept a separate library project
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class WizardActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener
{

	private final static String KEY_INITIAL_WIZARD_STEP = "INITIAL_STEP";
	private final static String KEY_CONFIG = "com.smoothsync.ACTIVITY_CONFIG";

	private FragmentManager mFragmentManager;
	private final Handler mHandler = new Handler();
	AutoCompleteTextView mLogin;
	WizardStep mWizardStep;


	public static void launch(Context context, WizardStep initialWizardStep)
	{
		Intent intent = new Intent(context, WizardActivity.class);
		// put the wizard step into a nested bundle to avoid a crash if the Bundle is unbundled by the system.
		Bundle activityConfig = new Bundle();
		activityConfig.putParcelable(KEY_INITIAL_WIZARD_STEP, initialWizardStep);
		intent.putExtra(KEY_CONFIG, activityConfig);
		intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		context.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// set up content
		setContentView(R.layout.smoothsetup_wizard_activity);

		CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		setSupportActionBar((Toolbar) collapsingToolbar.findViewById(R.id.toolbar));

		// set up wizard
		Intent intent = getIntent();
		Bundle config = intent.getBundleExtra(KEY_CONFIG);

		mFragmentManager = getSupportFragmentManager();
		mWizardStep = config.getParcelable(KEY_INITIAL_WIZARD_STEP);

		if (savedInstanceState == null)
		{
			getSupportActionBar().setTitle(mWizardStep.title(WizardActivity.this));
			mFragmentManager.beginTransaction().add(R.id.wizards, mWizardStep.fragment(this)).commit();
		}
		else
		{
			// make sure the toolbar is setup up properly.
			onBackStackChanged();
		}

		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		// set up wizard controller
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastWizardController.NEXT_STEP_ACTION);
		filter.addAction(BroadcastWizardController.PREV_STEP_ACTION);
		filter.addAction(BroadcastWizardController.RESTART_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(mWizardControlReceiver, filter);
	}


	@Override
	protected void onPause()
	{
		// clear the broadcast receiver, to make sure we don't perform any fragment transactions after onSaveInstanceState, we do this in onPause.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mWizardControlReceiver);
		super.onPause();
	}

	private final BroadcastReceiver mWizardControlReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(final Context context, final Intent intent)
		{

			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (BroadcastWizardController.NEXT_STEP_ACTION.equals(intent.getAction()))
					{
						mWizardStep = ((WizardStep) intent.getParcelableExtra(BroadcastWizardController.EXTRA_WIZARDSTEP));

						FragmentTransaction transaction = mFragmentManager.beginTransaction();
						if (intent.getBooleanExtra(BroadcastWizardController.EXTRA_IS_AUTOMATIC, false))
						{
							transaction.setCustomAnimations(R.anim.smoothsetup_fade_in, R.anim.smoothsetup_fade_out);
						}
						else
						{
							transaction.setCustomAnimations(R.anim.smoothsetup_enter_right, R.anim.smoothsetup_exit_left, R.anim.smoothsetup_enter_left,
								R.anim.smoothsetup_exit_right);
						}
						transaction.replace(R.id.wizards, mWizardStep.fragment(WizardActivity.this));
						if (intent.getBooleanExtra(BroadcastWizardController.EXTRA_CAN_RETURN, true))
						{
							transaction.addToBackStack(null);
						}
						else
						{
							// if we don't change the back stack we have to make sure the title is still set up correctly.
							CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
							collapsingToolbar.setTitle(mWizardStep.title(WizardActivity.this));
						}
						transaction.commit();
					}
					else if (BroadcastWizardController.RESTART_ACTION.equals(intent.getAction()))
					{
						mWizardStep = ((WizardStep) intent.getParcelableExtra(BroadcastWizardController.EXTRA_WIZARDSTEP));

						while (mFragmentManager.popBackStackImmediate())
						{
						}
						FragmentTransaction transaction = mFragmentManager.beginTransaction();
						if (intent.getBooleanExtra(BroadcastWizardController.EXTRA_IS_AUTOMATIC, false))
						{
							transaction.setCustomAnimations(R.anim.smoothsetup_fade_in, R.anim.smoothsetup_fade_out);
						}
						else
						{
							transaction.setCustomAnimations(R.anim.smoothsetup_enter_left, R.anim.smoothsetup_exit_right);
						}
						transaction.replace(R.id.wizards, mWizardStep.fragment(WizardActivity.this));

						// if we don't change the back stack we have to make sure the title is still set up correctly.
						CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
						collapsingToolbar.setTitle(mWizardStep.title(WizardActivity.this));

						transaction.commit();
					}
					else if (BroadcastWizardController.PREV_STEP_ACTION.equals(intent.getAction()))
					{
						onBackPressed();
					}
				}
			});
		}
	};


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackStackChanged()
	{
		// the backstack has changed, update the UI if the fragment is a wizard fragment
		Fragment fragment = mFragmentManager.findFragmentById(R.id.wizards);
		Bundle arguments = fragment.getArguments();
		if (arguments == null)
		{
			throw new RuntimeException("Fragment doesn't have a WizardStep");
		}

		WizardStep wizardStep = arguments.getParcelable(WizardStep.ARG_WIZARD_STEP);
		if (wizardStep == null)
		{
			throw new RuntimeException("Fragment doesn't have a WizardStep");
		}

		CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		collapsingToolbar.setTitle(wizardStep.title(this));

		ActionBar actionBar = getSupportActionBar();

		actionBar.setTitle(wizardStep.title(this));
		actionBar.setDisplayHomeAsUpEnabled(mFragmentManager.getBackStackEntryCount() > 0);
		actionBar.setDisplayShowHomeEnabled(mFragmentManager.getBackStackEntryCount() > 0);
	}
}

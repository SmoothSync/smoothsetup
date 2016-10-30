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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.smoothsync.smoothsetup.model.WizardStep;
import com.smoothsync.smoothsetup.wizardtransitions.AbstractWizardTransition;


/**
 * An Activity to host a setup wizard.
 * <p>
 * TODO: consider to make this concept a separate library project
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class WizardActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener
{
    private final static String KEY_INITIAL_WIZARD_STEP = "INITIAL_STEP";
    private final static String KEY_CONFIG = "com.smoothsync.ACTIVITY_CONFIG";

    private CollapsingToolbarLayout mCollapsingToolbar;
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private final Handler mHandler = new Handler();
    private int bsdepth = 0;


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

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar((Toolbar) mCollapsingToolbar.findViewById(R.id.toolbar));
        mActionBar = getSupportActionBar();

        // set up wizard
        Intent intent = getIntent();
        Bundle config = intent.getBundleExtra(KEY_CONFIG);

        mFragmentManager = getSupportFragmentManager();
        WizardStep wizardStep = config.getParcelable(KEY_INITIAL_WIZARD_STEP);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState == null)
        {
            mActionBar.setTitle(wizardStep.title(WizardActivity.this));
            mFragmentManager.beginTransaction().add(R.id.wizards, wizardStep.fragment(this)).commit();
        }
        else
        {
            bsdepth = mFragmentManager.getBackStackEntryCount();
            updateActionBar();
        }
    }


    @Override
    protected void onResume()
    {
        // set up wizard controller
        IntentFilter filter = new IntentFilter();
        filter.addAction(AbstractWizardTransition.ACTION_WIZARD_TRANSITION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mWizardTransactionReceiver, filter);
        super.onResume();
    }


    @Override
    protected void onPause()
    {
        // clear the broadcast receiver, to make sure we don't perform any fragment transactions after onSaveInstanceState, we do this in onPause.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mWizardTransactionReceiver);
        super.onPause();
    }


    private final BroadcastReceiver mWizardTransactionReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {

            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (AbstractWizardTransition.ACTION_WIZARD_TRANSITION.equals(intent.getAction()))
                    {
                        AbstractWizardTransition wizardTransition = intent.getParcelableExtra(AbstractWizardTransition.EXTRA_WIZARD_TRANSITION);
                        wizardTransition.apply(WizardActivity.this, mFragmentManager,
                                (WizardStep) mFragmentManager.findFragmentById(R.id.wizards).getArguments().getParcelable(WizardStep.ARG_WIZARD_STEP));
                        mFragmentManager.executePendingTransactions();

                        // close keyboard if necessary
                        View view = getCurrentFocus();
                        if (view == null)
                        {
                            // no view is focused, close the keyboard
                            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
                        }

                        // not all transitions affect the back stack so make sure we still update the actionbar.
                        updateActionBar();
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
        if (mFragmentManager.getBackStackEntryCount() < bsdepth)
        {
            // the user went back, make sure we skip all skipable steps.
            if (mFragmentManager.getBackStackEntryCount() > 0
                    && "skip".equals(mFragmentManager.getBackStackEntryAt(mFragmentManager.getBackStackEntryCount() - 1).getName()))
            {
                mFragmentManager.popBackStackImmediate("skip", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        bsdepth = mFragmentManager.getBackStackEntryCount();

        updateActionBar();
    }


    private void updateActionBar()
    {
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

        mCollapsingToolbar.setTitle(wizardStep.title(this));

        mActionBar.setTitle(wizardStep.title(this));
        mActionBar.setDisplayHomeAsUpEnabled(mFragmentManager.getBackStackEntryCount() > 0);
        mActionBar.setDisplayShowHomeEnabled(mFragmentManager.getBackStackEntryCount() > 0);
    }
}

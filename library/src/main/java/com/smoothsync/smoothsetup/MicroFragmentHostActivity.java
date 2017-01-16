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

package com.smoothsync.smoothsetup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.MicroFragmentState;
import org.dmfs.android.microfragments.SimpleMicroFragmentFlow;
import org.dmfs.android.microfragments.transitions.BackTransition;
import org.dmfs.android.microfragments.utils.BooleanDovecote;
import org.dmfs.pigeonpost.Dovecote;
import org.dmfs.pigeonpost.localbroadcast.ParcelableDovecote;


/**
 * An activity that hosts a {@link MicroFragment}.
 *
 * @author Marten Gajda
 */
public final class MicroFragmentHostActivity extends AppCompatActivity implements Dovecote.OnPigeonReturnCallback<MicroFragmentState>
{
    private CollapsingToolbarLayout mCollapsingToolbar;
    private ActionBar mActionBar;
    private Dovecote<MicroFragmentState> mMicroFragmentStateDovecote;
    private Dovecote<Boolean> mBackDovecote;
    private MicroFragmentHost mMicroFragmentHost;


    public static void launch(@NonNull Context context, @NonNull MicroFragment<?> microFragment)
    {
        Intent intent = new Intent(context, MicroFragmentHostActivity.class);
        Bundle nestedBundle = new Bundle();
        nestedBundle.putParcelable("MicroFragment", microFragment);
        intent.putExtra("org.dmfs.nestedExtras", nestedBundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smoothsetup_activity_microfragment_host);

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar((Toolbar) mCollapsingToolbar.findViewById(R.id.toolbar));
        mActionBar = getSupportActionBar();
        mMicroFragmentStateDovecote = new ParcelableDovecote<>(this, "hostactivity", this);
        mBackDovecote = new BooleanDovecote(this, "backresult", new Dovecote.OnPigeonReturnCallback<Boolean>()
        {
            @Override
            public void onPigeonReturn(@NonNull Boolean aBoolean)
            {
                if (!aBoolean)
                {
                    finish();
                }
            }
        });

        if (savedInstanceState == null)
        {
            // load the initial MicroFragment
            Bundle nestedExtras = getIntent().getBundleExtra("org.dmfs.nestedExtras");
            MicroFragment<?> initialMicroFragment = nestedExtras.getParcelable("MicroFragment");
            mMicroFragmentHost = new SimpleMicroFragmentFlow(initialMicroFragment, R.id.microfragment_host).withPigeonCage(mMicroFragmentStateDovecote.cage())
                    .start(this);
        }
        else
        {
            mMicroFragmentHost = savedInstanceState.getParcelable("microfragmenthost");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable("microfragmenthost", mMicroFragmentHost);
    }


    @Override
    protected void onDestroy()
    {
        mMicroFragmentStateDovecote.dispose();
        mBackDovecote.dispose();
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            mMicroFragmentHost.execute(this, new BackTransition());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed()
    {
        mMicroFragmentHost.execute(this, new BackTransition(mBackDovecote.cage()));
    }


    @Override
    public void onPigeonReturn(@NonNull MicroFragmentState microFragmentState)
    {
        mCollapsingToolbar.setTitle(microFragmentState.currentStep().title(this));
        mActionBar.setTitle(microFragmentState.currentStep().title(this));
        mActionBar.setDisplayHomeAsUpEnabled(microFragmentState.backStackSize() > 0);
        mActionBar.setDisplayShowHomeEnabled(microFragmentState.backStackSize() > 0);
    }
}

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.MicroFragmentState;
import org.dmfs.android.microfragments.SimpleMicroFragmentFlow;
import org.dmfs.android.microfragments.transitions.BackTransition;
import org.dmfs.android.microfragments.utils.BooleanDovecote;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.procedure.ForEach;
import org.dmfs.pigeonpost.Dovecote;
import org.dmfs.pigeonpost.localbroadcast.ParcelableDovecote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


/**
 * An {@link Activity} that hosts a {@link MicroFragment}.
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
    private View mFragmentHostView;
    private AppBarLayout mAppBarLayout;


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

        setSupportActionBar(findViewById(R.id.toolbar));
        mActionBar = getSupportActionBar();
        mCollapsingToolbar = findViewById(R.id.collapsing_toolbar);
        if (mCollapsingToolbar == null)
        {
            mActionBar.setDisplayShowTitleEnabled(false);
        }
        mMicroFragmentStateDovecote = new ParcelableDovecote<>(this, "hostactivity", this);
        mBackDovecote = new BooleanDovecote(this, "backresult", aBoolean ->
        {
            if (!aBoolean)
            {
                finish();
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
        if (mCollapsingToolbar != null)
        {
            mAppBarLayout = findViewById(R.id.appbar);
            mFragmentHostView = findViewById(R.id.microfragment_host);
            mFragmentHostView.getViewTreeObserver().addOnGlobalLayoutListener(() ->
            {
                // collapse appbar if the microfragment content doesn't fit on the screen
                Rect windowRect = new Rect();
                mAppBarLayout.getWindowVisibleDisplayFrame(windowRect);

                FrameLayout bottomView = mFragmentHostView.findViewById(org.dmfs.android.microfragments.R.id.microfragments_host);
                if (bottomView.getChildCount() > 0)
                {
                    View childView = bottomView.getChildAt(0);
                    Rect childRect = new Rect();
                    childView.getGlobalVisibleRect(childRect);
                    if (!"collapsed".equals(childView.getTag()) && !windowRect.contains(childRect))
                    {
                        mAppBarLayout.postDelayed(() -> mAppBarLayout.setExpanded(false, true), 100);
                        // tag the child as "collapsed" so we don't collapse it again while visible
                        childView.setTag("collapsed");
                    }
                }
            });
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
        if (mCollapsingToolbar != null)
        {
            mCollapsingToolbar.setTitle(microFragmentState.currentStep().title(this));
        }
        new ForEach<>(new NullSafe<TextView>(findViewById(android.R.id.title))).process(view -> view.setText(microFragmentState.currentStep().title(this)));
        mActionBar.setTitle(microFragmentState.currentStep().title(this));
        mActionBar.setDisplayHomeAsUpEnabled(microFragmentState.backStackSize() > 0);
        mActionBar.setDisplayShowHomeEnabled(microFragmentState.backStackSize() > 0);
    }
}

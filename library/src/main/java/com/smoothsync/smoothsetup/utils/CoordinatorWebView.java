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

package com.smoothsync.smoothsetup.utils;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;


/**
 * A {@link WebView} that works with a {@link CollapsingToolbarLayout} inside of a {@link CoordinatorLayout}.
 *
 * @author Marten Gajda
 */
public final class CoordinatorWebView extends WebView implements NestedScrollingChild
{
    private int mLastY;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;


    public CoordinatorWebView(Context context)
    {
        super(context);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }


    public CoordinatorWebView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        MotionEvent event = MotionEvent.obtain(ev);
        switch (MotionEventCompat.getActionMasked(ev))
        {
            case MotionEvent.ACTION_DOWN:
            {
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                mLastY = (int) ev.getY();
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_MOVE:
            {
                int[] offsetInWindow = new int[2];
                int[] consumed = new int[2];
                int deltaY = (int) (mLastY - ev.getY() + 0.5 /* round properly */);
                if (dispatchNestedPreScroll(0, deltaY, consumed, offsetInWindow))
                {
                    deltaY -= consumed[1];
                }
                dispatchNestedScroll(0, offsetInWindow[1], 0, deltaY, offsetInWindow);
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                stopNestedScroll();
                return super.onTouchEvent(event);
            }
            default:
            {
                return super.onTouchEvent(event);
            }
        }
    }


    @Override
    public boolean isNestedScrollingEnabled()
    {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled)
    {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }


    @Override
    public boolean startNestedScroll(int axes)
    {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }


    @Override
    public void stopNestedScroll()
    {
        mNestedScrollingChildHelper.stopNestedScroll();
    }


    @Override
    public boolean hasNestedScrollingParent()
    {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }


    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                                        int[] offsetInWindow)
    {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }


    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow)
    {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }


    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed)
    {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }


    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY)
    {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
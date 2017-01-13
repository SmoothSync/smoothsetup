/*
 * Copyright 2016 dmfs GmbH
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

package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.CoordinatorWebView;
import com.smoothsync.smoothsetup.wizardtransitions.BackWizardTransition;

import java.net.URI;


/**
 * A fragment that presents a website to create an app specific password.
 *
 * @author Marten Gajda
 */
public final class AppSpecificWebviewFragment extends Fragment implements View.OnKeyListener
{
    public final static String ARG_URL = "url";
    public static final int PASSWORD_PROBE_PERIOD = 500;

    private WebView mWebView;
    private String mAppSpecificPassword;
    private AppSpecificPasswordProbe mPasswordProbe;
    private Handler mHandler = new Handler();
    private Snackbar mSnackbar;


    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // create and configure the WebView
        mWebView = new CoordinatorWebView(getActivity());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setOnKeyListener(this);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.addJavascriptInterface(new AppSpecificPasswordCallback()
        {
            @JavascriptInterface
            @Override
            public void onAppSpecificPassword(final String appSpecificPassword)
            {
                if (!TextUtils.isEmpty(appSpecificPassword) && !TextUtils.equals(mAppSpecificPassword, appSpecificPassword))
                {
                    mAppSpecificPassword = appSpecificPassword;
                    mSnackbar = Snackbar.make(mWebView, getString(R.string.smoothsetup_snackbar_found_app_specific_password, appSpecificPassword),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.smoothsetup_button_copy_app_specific_password,
                                    new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText(getString(R.string.smoothsetup_label_app_specific_password),
                                                    appSpecificPassword);
                                            clipboard.setPrimaryClip(clip);

                                            mSnackbar = Snackbar.make(mWebView, R.string.smoothsetup_snackbar_copied_app_specific_password,
                                                    Snackbar.LENGTH_INDEFINITE);
                                            mSnackbar.show();
                                        }
                                    });
                    mSnackbar.show();
                }
            }
        }, "SmoothSetup");
        String url = getArguments().getString(ARG_URL);
        if (savedInstanceState == null)
        {
            mWebView.loadUrl(url);
        }
        else
        {
            mWebView.restoreState(savedInstanceState);
        }
        mPasswordProbe = new DefaultPasswordProbeFactory().forUrl(URI.create(url));

        // start probing for the password
        mHandler.postDelayed(mPasswordProbeRunnable, PASSWORD_PROBE_PERIOD);

        return mWebView;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (mWebView != null)
        {
            mWebView.onResume();
        }
    }


    @Override
    public void onPause()
    {
        if (mWebView != null)
        {
            mWebView.onPause();
        }
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mHandler.removeCallbacks(mPasswordProbeRunnable);
        if (mWebView != null)
        {
            mWebView.destroy();
        }
        if (mSnackbar != null)
        {
            mSnackbar.dismiss();
        }
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                if (mWebView.canGoBack())
                {
                    // user went back a step
                    mWebView.goBack();
                }
                else
                {
                    // the user cancelled the authorization flow
                    new BackWizardTransition().execute(getActivity());
                }
            }
            return true;
        }
        return false;
    }


    private final Runnable mPasswordProbeRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (mWebView != null && mPasswordProbe != null)
            {
                mPasswordProbe.executeOn(mWebView);
            }
            mHandler.postDelayed(mPasswordProbeRunnable, PASSWORD_PROBE_PERIOD);
        }
    };
}

package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword;

import android.webkit.WebView;


/**
 * A Probe for an app-specific password.
 *
 * @author Marten Gajda
 */
public interface AppSpecificPasswordProbe
{
    /**
     * Probe the given WebView for an app-specific password.
     *
     * @param webView
     */
    void executeOn(WebView webView);
}

package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword;

import android.webkit.WebView;


/**
 * Interface of the Javascript callback that passes the extracted app-specific password.
 *
 * @author Marten Gajda
 */
public interface AppSpecificPasswordCallback
{
    /**
     * Called when an app-specific password has been found in the {@link WebView}.
     *
     * @param appSpecificPassword
     *         The app-specific password that was found.
     */
    void onAppSpecificPassword(String appSpecificPassword);
}

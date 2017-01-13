package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.providers;

import android.webkit.WebView;

import com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.AppSpecificPasswordProbe;


/**
 * An {@link AppSpecificPasswordProbe} for Fastmail.
 *
 * @author Marten Gajda
 */
public final class FastmailPasswordProbe implements AppSpecificPasswordProbe
{
    @Override
    public void executeOn(WebView webView)
    {
        webView.loadUrl("javascript:(function(){\n" +
                "   var passwordElements = document.getElementsByClassName('v-DisplayPassword');\n" +
                "   if (passwordElements.length>0)\n" +
                "   {\n" +
                "      window.SmoothSetup.onAppSpecificPassword(passwordElements[0].textContent);\n" +
                "   }\n" +
                "})();");
    }
}

package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.providers;

import android.webkit.WebView;

import com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.AppSpecificPasswordProbe;


/**
 * An {@link AppSpecificPasswordProbe} for iCloud.
 *
 * @author Marten Gajda
 */
public final class ICloudPasswordProbe implements AppSpecificPasswordProbe
{
    @Override
    public void executeOn(WebView webView)
    {
        webView.loadUrl("javascript:(function(){\n" +
                "   var passwordElement = document.getElementById('appPasswordText');\n" +
                "   if (passwordElement != null)\n" +
                "   {\n" +
                "      window.SmoothSetup.onAppSpecificPassword(passwordElement.value);\n" +
                "   }\n" +
                "})();");
    }
}

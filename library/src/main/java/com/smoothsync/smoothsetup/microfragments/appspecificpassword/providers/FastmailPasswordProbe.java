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

package com.smoothsync.smoothsetup.microfragments.appspecificpassword.providers;

import android.webkit.WebView;

import com.smoothsync.smoothsetup.microfragments.appspecificpassword.AppSpecificPasswordProbe;


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

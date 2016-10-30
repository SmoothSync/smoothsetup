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


/**
 * Receives the INSTALL_REFERRER broadcast and stores the referrer in the shared preferences.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class InstallReferrerBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String referrer = intent.getStringExtra("referrer");
        if (referrer == null)
        {
            return;
        }

        // just store the referrer as is and a timestamp
        context.getSharedPreferences("com.smoothsync.smoothsetup.prefs", 0).edit().putString("referrer", referrer)
                .putLong("timestamp", System.currentTimeMillis()).apply();
    }
}

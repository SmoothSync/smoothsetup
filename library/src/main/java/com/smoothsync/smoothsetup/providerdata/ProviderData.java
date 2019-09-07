/*
 * Copyright (c) 2019 dmfs GmbH
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

package com.smoothsync.smoothsetup.providerdata;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * @author Marten Gajda
 */
public interface ProviderData
{
    /**
     * Provides a {@link JSONObject} containing provider data.
     *
     * @param context
     *         A {@link Context}.
     *
     * @return
     */
    JSONObject providerData(Context context) throws IOException, JSONException;
}

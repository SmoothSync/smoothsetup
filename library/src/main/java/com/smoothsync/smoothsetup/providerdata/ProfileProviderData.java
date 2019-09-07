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
import android.content.RestrictionsManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import com.smoothsync.api.model.impl.JsonObjectArrayIterator;

import org.dmfs.iterables.decorators.DelegatingIterable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.RequiresApi;


/**
 * @author Marten Gajda
 */
public final class ProfileProviderData implements ProviderData
{
    @Override
    public JSONObject providerData(Context context) throws JSONException
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            RestrictionsManager restrictionsManager = (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
            if (restrictionsManager != null)
            {
                Bundle restrictions = restrictionsManager.getApplicationRestrictions();
                if (restrictions != null)
                {
                    JSONObject result = bundleToJson(restrictions);
                    for (JSONObject o : new DelegatingIterable<JSONObject>(() -> new JsonObjectArrayIterator(result.optJSONArray("providers")))
                    {
                    })
                    {
                        if (!o.has("domains"))
                        {
                            o.put("domains", new JSONArray());
                        }
                    }
                    return result;
                }
            }
        }
        return new JSONObject().put("providers", new JSONArray());
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private JSONObject bundleToJson(Bundle bundle) throws JSONException
    {
        JSONObject result = new JSONObject();
        for (String key : bundle.keySet())
        {
            Object val = bundle.get(key);
            if (val instanceof String || val instanceof Integer || val instanceof Boolean)
            {
                result.put(key, val);
            }
            else if (val instanceof Bundle)
            {
                result.put(key, bundleToJson(bundle.getBundle(key)));
            }
            else if (val instanceof Parcelable[])
            {
                Object[] valArray = (Object[]) val;
                if (valArray.length == 0)
                {
                    result.put(key, new JSONArray());
                }
                else if (valArray[0] instanceof String)
                {
                    result.put(key, new JSONArray(valArray));
                }
                else
                {
                    result.put(key, bundleArrayToJson(bundle.getParcelableArray(key)));
                }
            }
        }
        return result;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private JSONArray bundleArrayToJson(Parcelable[] bundleArray) throws JSONException
    {
        JSONArray result = new JSONArray();
        for (Parcelable parcelable : bundleArray)
        {
            result.put(bundleToJson((Bundle) parcelable));
        }
        return result;
    }
}

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

package com.smoothsync.smoothsetup.autocomplete;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;


/**
 * Base class of all auto-complete adapters. It takes care of populating the auto-complete view, which should be done the same way for all subclasses.
 *
 * @author Marten Gajda
 */
public abstract class AbstractAutoCompleteAdapter extends BaseAdapter implements Filterable
{

    public interface AutoCompleteItem
    {
        String autoComplete();

        Iterable<String> extensions();
    }


    @Override
    public final long getItemId(int position)
    {
        return position;
    }


    @Override
    public final View getView(int position, View convertView, ViewGroup parent)
    {
        View result = convertView;
        if (result == null)
        {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoothsetup_autocomplete, parent, false);
        }
        AutoCompleteItem item = (AutoCompleteItem) getItem(position);
        ((TextView) result.findViewById(R.id.text_autocomplete)).setText(item.autoComplete());
        String extensions = TextUtils.join(", …", item.extensions());
        ((TextView) result.findViewById(R.id.text_extensions)).setText(extensions.isEmpty() ? "" : "…" + extensions);
        return result;
    }

}

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

package com.smoothsync.smoothsetup.setupbuttons;

import android.view.View;
import android.widget.Button;

import com.smoothsync.smoothsetup.R;

import androidx.recyclerview.widget.RecyclerView;


/**
 * A concrete {@link ButtonViewHolder} implementation.
 *
 * @author Marten Gajda
 */
public final class BasicButtonViewHolder extends RecyclerView.ViewHolder implements ButtonViewHolder
{
    private final Button mButton;


    public BasicButtonViewHolder(View itemView)
    {
        super(itemView);
        mButton = (Button) itemView.findViewById(R.id.button);
    }


    @Override
    public void updateText(String text)
    {
        mButton.setText(text);
    }


    @Override
    public void updateText(int stringResourceId, Object... parameters)
    {
        mButton.setText(mButton.getContext().getString(stringResourceId, parameters));
    }


    @Override
    public void updateEnabled(boolean enabled)
    {
        mButton.setEnabled(enabled);
    }


    @Override
    public void updateOnClickListener(View.OnClickListener listener)
    {
        mButton.setOnClickListener(listener);
    }
}

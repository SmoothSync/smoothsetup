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

package com.smoothsync.smoothsetup.setupbuttons;

import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * The interface of a {@link RecyclerView.ViewHolder} that contains a button.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface ButtonViewHolder
{
	/**
	 * Update the text of the button.
	 * 
	 * @param text
	 *            The new butotn text.
	 */
	public void updateText(String text);


	/**
	 * Update the enabled status of the button.
	 * 
	 * @param enabled
	 *            {@code true} to enable the button, {@code false} to disable it.
	 */
	public void updateEnabled(boolean enabled);


	/**
	 * Update the {@link View.OnClickListener} of the button.
	 * 
	 * @param listener
	 *            The new OnClickListener.
	 */
	public void updateOnClickListener(View.OnClickListener listener);
}

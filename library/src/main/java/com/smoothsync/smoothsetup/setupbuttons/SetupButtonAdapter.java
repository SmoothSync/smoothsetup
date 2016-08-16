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

import com.smoothsync.api.model.Provider;

import org.dmfs.httpessentials.exceptions.ProtocolException;


/**
 * Created by marten on 12.06.16.
 */
public interface SetupButtonAdapter
{
	public interface OnProviderSelectListener
	{
		public void onProviderSelected(Provider provider);


		public void onOtherSelected();
	}


	public void update(String login) throws ProtocolException;
}

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.DomainExpansionConverter;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.iterators.ArrayIterator;
import org.dmfs.iterators.ConvertedIterator;
import org.dmfs.iterators.FilteredIterator;
import org.dmfs.iterators.filters.NonNull;

import java.util.Iterator;


/**
 * Created by marten on 12.06.16.
 */
public final class ProviderSmoothSetupAdapter extends AbstractSmoothSetupAdapter
{

	private Provider mProvider;
	private boolean mEnable;


	public ProviderSmoothSetupAdapter(Provider provider, OnProviderSelectListener listener)
	{
		super(listener);
		mProvider = provider;
	}


	@Override
	public BasicButtonViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoothsetup_provider_button, parent, false);
		return new BasicButtonViewHolder(itemView);
	}


	@Override
	public long getItemId(int position)
	{
		return 1;
	}


	@Override
	public void onBindViewHolder(BasicButtonViewHolder holder, int position)
	{
		holder.updateText("Next");
		holder.updateEnabled(mEnable);
		holder.updateOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				notifyProviderSeleteced(mProvider);
			}
		});
	}


	@Override
	public int getItemCount()
	{
		return 1;
	}


	public void update(String domain)
	{
		Iterator<String> domainIterator = null;
		try
		{
			domainIterator = new FilteredIterator<String>(
				new ConvertedIterator<String, String>(new ArrayIterator<String>(mProvider.domains()), new DomainExpansionConverter(domain)),
				NonNull.<String> instance());
		}
		catch (ProtocolException e)
		{
			if (mEnable)
			{
				mEnable = false;
				notifyItemChanged(0);
			}
		}
		while (domainIterator.hasNext())
		{
			if (domain.equals(domainIterator.next()))
			{
				// current domain matches one of the provider domains
				if (!mEnable)
				{
					mEnable = true;
					notifyItemChanged(0);
				}
				return;
			}
		}
		if (mEnable)
		{
			mEnable = false;
			notifyItemChanged(0);
		}
	}
}

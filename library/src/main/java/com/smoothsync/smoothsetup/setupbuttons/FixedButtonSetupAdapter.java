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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.wizardsteps.ProvidersLoadWizardStep;
import com.smoothsync.smoothsetup.wizardtransitions.ForwardWizardTransition;

import org.dmfs.httpessentials.exceptions.ProtocolException;


/**
 * Created by marten on 12.06.16.
 */
public final class FixedButtonSetupAdapter extends AbstractSmoothSetupAdapter
{

	private final AbstractSmoothSetupAdapter mDecorated;
	private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver()
	{
		@Override
		public void onItemRangeChanged(int positionStart, int itemCount)
		{
			notifyItemChanged(positionStart, itemCount);
		}


		@Override
		public void onItemRangeChanged(int positionStart, int itemCount, Object payload)
		{
			notifyItemRangeChanged(positionStart, itemCount, payload);
		}


		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount)
		{
			for (int i = 0; i < itemCount; ++i)
			{
				notifyItemMoved(fromPosition + i, toPosition + i);
			}
		}


		@Override
		public void onItemRangeInserted(int positionStart, int itemCount)
		{
			notifyItemRangeInserted(positionStart, itemCount);
			notifyItemChanged(getItemCount() - 1);
		}


		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount)
		{
			notifyItemRangeRemoved(positionStart, itemCount);
			notifyItemChanged(getItemCount() - 1);
		}


		@Override
		public void onChanged()
		{
			notifyDataSetChanged();
		}
	};


	public FixedButtonSetupAdapter(AbstractSmoothSetupAdapter decorated, OnProviderSelectListener listener)
	{
		super(listener);
		mDecorated = decorated;
		mDecorated.registerAdapterDataObserver(mDataObserver);
	}


	@Override
	public int getItemViewType(int position)
	{
		if (position == getItemCount() - 1)
		{
			return -1;
		}
		return mDecorated.getItemViewType(position);
	}


	@Override
	public BasicButtonViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == -1)
		{
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoothsetup_fixed_button, parent, false);
			return new BasicButtonViewHolder(itemView);
		}
		return mDecorated.onCreateViewHolder(parent, viewType);
	}


	@Override
	public long getItemId(int position)
	{
		if (position == getItemCount() - 1)
		{
			// return an id that (most likely) won't conflict with any other id
			return System.identityHashCode(this) | 0x100000000L;
		}
		return mDecorated.getItemId(position);
	}


	@Override
	public void onBindViewHolder(BasicButtonViewHolder holder, int position)
	{
		if (position == getItemCount() - 1)
		{
			holder.updateText(getItemCount() > 1 ? R.string.smoothsetup_button_other_provider : R.string.smoothsetup_button_choose_provider);
			holder.updateOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					new ForwardWizardTransition(new ProvidersLoadWizardStep("")).execute(v.getContext());
				}
			});
		}
		else
		{
			mDecorated.onBindViewHolder(holder, position);
		}
	}


	@Override
	public int getItemCount()
	{
		return mDecorated.getItemCount() + 1;
	}


	public void update(String domain) throws ProtocolException
	{
		mDecorated.update(domain);
	}
}

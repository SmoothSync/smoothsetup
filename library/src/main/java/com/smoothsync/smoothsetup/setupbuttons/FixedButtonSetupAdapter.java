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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.smoothsetup.R;

import org.dmfs.httpessentials.exceptions.ProtocolException;

import androidx.recyclerview.widget.RecyclerView;


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
    private final OnProviderSelectListener mListener;


    public FixedButtonSetupAdapter(AbstractSmoothSetupAdapter decorated, OnProviderSelectListener listener)
    {
        super(listener);
        mDecorated = decorated;
        mDecorated.registerAdapterDataObserver(mDataObserver);
        mListener = listener;
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoothsetup_button_other, parent, false);
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
                    mListener.onOtherSelected();
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

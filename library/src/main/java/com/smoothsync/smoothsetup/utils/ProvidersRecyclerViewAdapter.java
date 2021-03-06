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

package com.smoothsync.smoothsetup.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.setupbuttons.SetupButtonAdapter;

import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


/**
 *
 */
public final class ProvidersRecyclerViewAdapter extends RecyclerView.Adapter<ProvidersRecyclerViewAdapter.ViewHolder>
{

    private final List<Provider> mProviders;
    private final SetupButtonAdapter.OnProviderSelectListener mListener;


    public ProvidersRecyclerViewAdapter(List<Provider> providers, SetupButtonAdapter.OnProviderSelectListener listener)
    {
        mProviders = providers;
        mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoothsetup_provider, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        try
        {
            holder.mContentView.setText(mProviders.get(position).name());
        }
        catch (ProtocolException e)
        {
            e.printStackTrace();
        }

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    mListener.onProviderSelected(mProviders.get(holder.getAdapterPosition()));
                }
            }
        });
    }


    @Override
    public int getItemCount()
    {
        return mProviders.size();
    }


    public final class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        // public final TextView mIdView;
        public final TextView mContentView;


        public ViewHolder(View view)
        {
            super(view);
            mView = view;
            // mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }


        @Override
        public String toString()
        {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}

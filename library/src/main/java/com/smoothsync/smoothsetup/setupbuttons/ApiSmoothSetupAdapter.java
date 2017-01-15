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

import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.AsyncTaskResult;
import com.smoothsync.smoothsetup.utils.ThrowingAsyncTask;

import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.util.Collections;
import java.util.List;


/**
 * Created by marten on 12.06.16.
 */
public final class ApiSmoothSetupAdapter extends AbstractSmoothSetupAdapter
{

    private SmoothSyncApi mApi;
    private final LruCache<String, List<Provider>> mResultCache = new LruCache<String, List<Provider>>(100);

    private List<Provider> mProviders = Collections.EMPTY_LIST;


    public ApiSmoothSetupAdapter(SmoothSyncApi api, OnProviderSelectListener listener)
    {
        super(listener);
        mApi = api;
    }


    @Override
    public BasicButtonViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoothsetup_button_provider, parent, false);
        return new BasicButtonViewHolder(itemView);
    }


    @Override
    public long getItemId(int position)
    {
        try
        {
            return mProviders.get(position).id().hashCode();
        }
        catch (ProtocolException e)
        {
            return position;
        }
    }


    @Override
    public void onBindViewHolder(final BasicButtonViewHolder holder, final int position)
    {
        final Provider provider = mProviders.get(position);
        try
        {
            holder.updateText(provider.name());
            holder.updateOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    notifyProviderSeleteced(provider);
                }
            });
        }
        catch (ProtocolException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount()
    {
        return mProviders.size();
    }


    public void update(String value)
    {
        new ProviderSearchTask(mApi, new ThrowingAsyncTask.OnResultCallback<List<Provider>>()
        {
            @Override
            public void onResult(AsyncTaskResult<List<Provider>> result)
            {
                try
                {
                    mProviders = result.value();
                    notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        }).execute(value);
    }
}

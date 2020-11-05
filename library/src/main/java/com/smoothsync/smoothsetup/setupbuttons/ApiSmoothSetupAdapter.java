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

import com.smoothsync.api.model.Provider;
import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.Domain;

import org.dmfs.httpessentials.exceptions.ProtocolException;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;


/**
 * @author Marten Gajda
 */
public final class ApiSmoothSetupAdapter extends AbstractSmoothSetupAdapter
{

    private final Single<ProviderService> mProviderService;
    private final List<Provider> mProviders = new ArrayList<>();
    private final OnProviderSelectListener mListener;


    public ApiSmoothSetupAdapter(@NonNull Single<ProviderService> providerService, @NonNull OnProviderSelectListener listener)
    {
        mProviderService = providerService;
        mListener = listener;
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
            holder.updateOnClickListener(v -> mListener.onProviderSelected(provider));
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


    @Override
    public void update(@NonNull String value)
    {
        String domain = new Domain(value).value();
        if (domain.isEmpty())
        {
            return;
        }

        mProviderService.flatMapObservable(s -> s.byDomain(domain))
                .collect((Supplier<ArrayList<Provider>>) ArrayList::new, ArrayList::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(providers -> {
                    mProviders.clear();
                    mProviders.addAll(providers);
                    notifyDataSetChanged();
                }, error -> {
                    // ignore
                });
    }
}

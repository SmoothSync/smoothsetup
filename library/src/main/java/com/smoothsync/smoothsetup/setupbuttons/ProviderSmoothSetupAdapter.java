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
import com.smoothsync.smoothsetup.autocomplete.DomainExpansionConverter;
import com.smoothsync.smoothsetup.utils.Domain;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.jems.iterator.elementary.Seq;
import org.dmfs.jems2.iterator.Mapped;
import org.dmfs.jems2.iterator.PresentValues;

import java.util.Iterator;

import androidx.annotation.NonNull;


/**
 * Created by marten on 12.06.16.
 */
public final class ProviderSmoothSetupAdapter extends AbstractSmoothSetupAdapter
{

    private final Provider mProvider;
    private final OnProviderSelectListener mListener;

    /**
     * Indicates whether the button is enabled or not. At present we default to "true" which means we always have it enabled
     * <p>
     * TODO: change this one the API supports a way to indicate if the provider supports logins without domain.
     */
    private boolean mEnable = true;


    public ProviderSmoothSetupAdapter(@NonNull Provider provider, @NonNull OnProviderSelectListener listener)
    {
        mProvider = provider;
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
        return 1;
    }


    @Override
    public void onBindViewHolder(BasicButtonViewHolder holder, int position)
    {
        holder.updateText(R.string.smoothsetup_button_login_next);
        holder.updateEnabled(mEnable);
        holder.updateOnClickListener(v -> mListener.onProviderSelected(mProvider));
    }


    @Override
    public int getItemCount()
    {
        return 1;
    }


    public void update(@NonNull String value)
    {
        if (value.isEmpty())
        {
            mEnable = false;
            notifyItemChanged(0);
            return;
        }

        String domain = new Domain(value).value();

        Iterator<String> domainIterator = null;
        try
        {
            domainIterator = new PresentValues<>(new Mapped<>(new DomainExpansionConverter(domain), new Seq<>(mProvider.domains())));
        }
        catch (ProtocolException e)
        {
            if (mEnable)
            {
                mEnable = false;
                notifyItemChanged(0);
            }
            return;
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
        if (!mEnable)
        {
            // this should depend on whether the provider supports arbitrary domains or not
            mEnable = true;
            notifyItemChanged(0);
        }
    }
}

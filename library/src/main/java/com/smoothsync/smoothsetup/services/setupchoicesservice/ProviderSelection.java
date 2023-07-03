/*
 * Copyright (c) 2021 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.setupchoicesservice;

import android.content.Context;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.services.SetupChoiceService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.wizard.ChooseProvider;
import com.smoothsync.smoothsetup.wizard.EnterPassword;
import com.smoothsync.smoothsetup.wizard.LoadProviders;
import com.smoothsync.smoothsetup.wizard.UsernameLogin;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.iterable.EmptyIterable;
import org.dmfs.jems2.iterable.Just;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;


public final class ProviderSelection implements SetupChoiceService
{
    private final Context mContext;


    public ProviderSelection(@NonNull Context context)
    {
        mContext = context.getApplicationContext();
    }


    @NonNull
    @Override
    public Flowable<Iterable<String>> autoComplete(@NonNull String name)
    {
        return Flowable.just(EmptyIterable.emptyIterable());
    }


    @NonNull
    @Override
    public Flowable<Iterable<SetupChoice>> choices(@NonNull String domain)
    {
        return Flowable.just(new Just<>(new SetupChoice()
        {
            @Override
            public boolean isPrimary()
            {
                return false;
            }


            @NonNull
            @Override
            public String id()
            {
                return ProviderSelection.class.getName();
            }


            @NonNull
            @Override
            public String title()
            {
                return mContext.getString(R.string.smoothsetup_button_choose_provider);
            }


            @NonNull
            @Override
            public Function<Context, ? extends MicroFragment<?>> nextStep(@NonNull MicroWizard<AccountDetails> createAccountStep, @NonNull Optional<String> login)
            {
                return context -> new LoadProviders(new ChooseProvider(new UsernameLogin(new EnterPassword(createAccountStep)))).microFragment(context, login);
            }
        }));
    }
}

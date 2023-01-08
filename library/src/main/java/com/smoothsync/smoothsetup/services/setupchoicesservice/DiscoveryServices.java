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

import com.smoothsync.smoothsetup.microfragments.ProviderLoadMicroFragment;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.services.SetupChoiceService;
import com.smoothsync.smoothsetup.services.binders.ProviderServiceBinder;
import com.smoothsync.smoothsetup.services.providerservice.ProviderService;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.FlatMapFirst;
import com.smoothsync.smoothsetup.wizard.EnterPassword;
import com.smoothsync.smoothsetup.wizard.UsernameLogin;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.jems2.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;


public final class DiscoveryServices implements SetupChoiceService
{
    private final Flowable<ProviderService> mProviderService;


    public DiscoveryServices(@NonNull Context context)
    {
        mProviderService = new ProviderServiceBinder(context);
    }


    @NonNull
    @Override
    public Flowable<Iterable<String>> autoComplete(@NonNull String name)
    {
        return mProviderService
            .compose(new FlatMapFirst<>(
                providerService -> providerService
                    .autoComplete(name)
                    .retryWhen(throwableObservable -> throwableObservable.flatMap(error -> Observable.timer(5, TimeUnit.SECONDS)))
                    .<List<String>>collect(ArrayList::new, List::add)
                    .toFlowable()));
    }


    @NonNull
    @Override
    public Flowable<Iterable<SetupChoice>> choices(@NonNull String domain)
    {
        return mProviderService
            .compose(new FlatMapFirst<>(
                providerService -> providerService.byDomain(domain)
                    .retryWhen(throwableObservable -> throwableObservable.flatMap(error -> Observable.timer(5, TimeUnit.SECONDS)))
                    .map(provider -> new SetupChoice()
                    {
                        @Override
                        public boolean isPrimary()
                        {
                            return true;
                        }


                        @NonNull
                        @Override
                        public String id()
                        {
                            try
                            {
                                return provider.id();
                            }
                            catch (ProtocolException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }


                        @NonNull
                        @Override
                        public String title()
                        {
                            try
                            {
                                return provider.name();
                            }
                            catch (ProtocolException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }


                        @NonNull
                        @Override
                        public Function<Context, ? extends MicroFragment<?>> nextStep(@NonNull MicroWizard<AccountDetails> createAccountStep, @NonNull Optional<String> login)
                        {
                            return context ->
                                new Backed<>(
                                    new Mapped<>(
                                        name -> new EnterPassword(createAccountStep).microFragment(context, new BasicAccount(name, provider)),
                                        login
                                    ),
                                    new UsernameLogin(new EnterPassword(createAccountStep))
                                        .microFragment(context, new ProviderLoadMicroFragment.SimpleProviderInfo(provider, login)))
                                    .value();

                        }
                    })
                    .<List<SetupChoice>>collect(ArrayList::new, List::add)
                    .toFlowable()));
    }
}

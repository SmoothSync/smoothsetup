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

package com.smoothsync.smoothsetup.services;

import android.content.Context;

import com.smoothsync.smoothsetup.utils.AccountDetails;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.Optional;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Flowable;


/**
 * A service to provide login convenience.
 */
public interface SetupChoiceService
{
    /**
     * Returns auto complete domains for the given domain name fragment.
     */
    @NonNull
    Flowable<Iterable<String>> autoComplete(@NonNull String name);

    /**
     * Returns {@link SetupChoice}s for the given login domain.
     */
    @NonNull
    Flowable<Iterable<SetupChoice>> choices(@NonNull String domain);

    interface SetupChoice
    {
        boolean isPrimary();

        @NonNull
        String id();

        @NonNull
        String title();

        @NonNull
        Function<Context, ? extends MicroFragment<?>> nextStep(@NonNull MicroWizard<AccountDetails> createAccountStep, @NonNull Optional<String> login);
    }
}

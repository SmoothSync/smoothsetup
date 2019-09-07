/*
 * Copyright (c) 2018 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.delegating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.smoothsync.smoothsetup.services.VerificationService;
import com.smoothsync.smoothsetup.services.WizardService;

import org.dmfs.android.microfragments.MicroFragment;

import androidx.annotation.Nullable;


/**
 * An abstract service that provides an {@link WizardService} interface.
 *
 * @author Marten Gajda
 */
public abstract class DelegatingWizardService extends Service
{

    private WizardServiceBinder mBinder;
    private WizardServiceFactory mWizardServiceFactory;


    public DelegatingWizardService(WizardServiceFactory wizardServiceFactory)
    {
        mWizardServiceFactory = wizardServiceFactory;
    }


    @Override
    public final void onCreate()
    {
        super.onCreate();
        mBinder = new WizardServiceBinder(mWizardServiceFactory.wizardService());
    }


    @Nullable
    @Override
    public final IBinder onBind(Intent intent)
    {
        return mBinder;
    }


    /**
     * A factory that creates {@link WizardService} instances.
     */
    public interface WizardServiceFactory
    {
        /**
         * Create a new {@link VerificationService}.
         *
         * @return
         */
        WizardService wizardService();
    }


    /**
     * A {@link Binder} that gives access to the {@link WizardService}
     */
    private final static class WizardServiceBinder extends Binder implements WizardService
    {

        private final WizardService mWizardService;


        public WizardServiceBinder(WizardService wizardService)
        {
            mWizardService = wizardService;
        }


        @Override
        public MicroFragment<?> initialMicroFragment(Context context, Intent intent)
        {
            return mWizardService.initialMicroFragment(context, intent);
        }
    }
}

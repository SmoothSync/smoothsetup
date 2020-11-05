/*
 * Copyright (c) 2020 dmfs GmbH
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.dmfs.android.bolts.service.StubProxy;
import org.dmfs.android.bolts.service.elementary.LocalServiceStubProxy;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * A {@link SingleSource} providing an {@link IBinder} of a service.
 * <p>
 * The service gets disconnected when the {@link Single} gets disposed.
 */
public final class ServiceBinder implements SingleSource<IBinder>
{
    private final Context context;
    private final Intent intent;


    public ServiceBinder(@NonNull Context context, @NonNull Intent intent)
    {
        this.context = context.getApplicationContext();
        this.intent = intent;
    }


    public static <T> Single<T> service(@NonNull Context context, @NonNull Intent intent, @NonNull StubProxy<T> stubProxy)
    {
        return Single.wrap(new ServiceBinder(context, intent))
                .subscribeOn(Schedulers.newThread())
                .map(stubProxy::asInterface);
    }


    public static <T> Single<T> localService(@NonNull Context context, @NonNull Intent intent)
    {
        return service(context, intent, new LocalServiceStubProxy<>());
    }


    public static <T> Single<T> localService(@NonNull Context context, @NonNull String action)
    {
        return service(context, new Intent(action).setPackage(context.getPackageName()), new LocalServiceStubProxy<>());
    }


    public static <T> Single<T> localService(@NonNull Context context, @NonNull ComponentName service)
    {
        return service(context, new Intent().setComponent(service), new LocalServiceStubProxy<>());
    }


    public static <T> Single<T> localServiceByClassName(@NonNull Context context, @NonNull String className)
    {
        return service(context, new Intent().setComponent(new ComponentName(context, className)), new LocalServiceStubProxy<>());
    }


    public static <T> Single<T> localServiceByClassNameResource(@NonNull Context context, @StringRes int classNameResource)
    {
        return service(context, new Intent().setComponent(new ComponentName(context, context.getString(classNameResource))), new LocalServiceStubProxy<>());
    }


    @Override
    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleObserver<? super IBinder> observer)
    {
        android.content.ServiceConnection connection = new android.content.ServiceConnection()
        {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                observer.onSuccess(service);
            }


            @Override
            public void onServiceDisconnected(ComponentName name)
            {
            }
        };
        observer.onSubscribe(Disposable.fromAction(() -> context.unbindService(connection)));
        if (!context.bindService(intent, connection, Context.BIND_AUTO_CREATE))
        {
            observer.onError(new IllegalStateException("Can't connect to service " + intent.toString()));
        }
    }
}

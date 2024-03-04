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
import android.content.ServiceConnection;
import android.os.IBinder;

import org.dmfs.android.bolts.service.StubProxy;
import org.dmfs.android.bolts.service.elementary.LocalServiceStubProxy;
import org.reactivestreams.Subscriber;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * A {@link Flowable} providing an {@link IBinder} of a service.
 * <p>
 * The service gets disconnected when the {@link Flowable} gets disposed.
 */
public final class ServiceBinder extends Flowable<IBinder>
{
    private final Context context;
    private final Intent intent;


    public ServiceBinder(@NonNull Context context, @NonNull Intent intent)
    {
        this.context = context.getApplicationContext();
        this.intent = intent;
    }


    public static <T> Flowable<T> service(@NonNull Context context, @NonNull Intent intent, @NonNull StubProxy<T> stubProxy)
    {
        return new ServiceBinder(context, intent)
            .subscribeOn(Schedulers.computation())
            .map(stubProxy::asInterface);
    }


    public static <T> Flowable<T> localService(@NonNull Context context, @NonNull Intent intent)
    {
        return service(context, intent, new LocalServiceStubProxy<>());
    }


    public static <T> Flowable<T> localService(@NonNull Context context, @NonNull String action)
    {
        return service(context, new Intent(action).setPackage(context.getPackageName()), new LocalServiceStubProxy<>());
    }


    public static <T> Flowable<T> localService(@NonNull Context context, @NonNull ComponentName service)
    {
        return service(context, new Intent().setComponent(service), new LocalServiceStubProxy<>());
    }


    public static <T> Flowable<T> localServiceByClassName(@NonNull Context context, @NonNull String className)
    {
        return service(context, new Intent().setComponent(new ComponentName(context, className)), new LocalServiceStubProxy<>());
    }


    public static <T> Flowable<T> localServiceByClassNameResource(@NonNull Context context, @StringRes int classNameResource)
    {
        return service(context, new Intent().setComponent(new ComponentName(context, context.getString(classNameResource))), new LocalServiceStubProxy<>());
    }


    @Override
    protected void subscribeActual(@io.reactivex.rxjava3.annotations.NonNull Subscriber<? super IBinder> observer)
    {
        Flowable.using(
                Connection::new,
                connection -> {
                    if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE))
                    {
                        return Flowable.create(connection, BackpressureStrategy.MISSING);
                    }
                    else
                    {
                        return Flowable.error(new IllegalStateException("Can't connect to service " + intent.toString()));
                    }
                },
                context::unbindService,
                false
            )
            .subscribe(observer);
    }


    private static final class Connection implements FlowableOnSubscribe<IBinder>, ServiceConnection
    {
        private FlowableEmitter<IBinder> mEmitter;


        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            if (mEmitter != null && !mEmitter.isCancelled())
            {
                mEmitter.onNext(service);
            }
        }


        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            if (mEmitter != null && !mEmitter.isCancelled())
            {
                mEmitter.onComplete();
            }
        }


        @Override
        public void subscribe(@io.reactivex.rxjava3.annotations.NonNull FlowableEmitter<IBinder> emitter)
        {
            mEmitter = emitter;
        }
    }
}

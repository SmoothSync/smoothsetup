/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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
 *
 */

package com.smoothsync.smoothsetup.demo;

import android.content.Context;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.model.HttpAuthorizationFactory;
import com.smoothsync.smoothsetup.services.AbstractVerificationService;
import com.smoothsync.smoothsetup.utils.Trusted;

import org.dmfs.httpessentials.HttpMethod;
import org.dmfs.httpessentials.HttpStatus;
import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.client.HttpRequestEntity;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.client.HttpResponse;
import org.dmfs.httpessentials.client.HttpResponseHandler;
import org.dmfs.httpessentials.entities.EmptyHttpRequestEntity;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.FollowRedirectPolicy;
import org.dmfs.httpessentials.executors.following.policies.Secure;
import org.dmfs.httpessentials.executors.retrying.Retrying;
import org.dmfs.httpessentials.executors.retrying.policies.DefaultRetryPolicy;
import org.dmfs.httpessentials.headers.EmptyHeaders;
import org.dmfs.httpessentials.headers.Headers;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.httpessentials.httpurlconnection.factories.DefaultHttpUrlConnectionFactory;
import org.dmfs.httpessentials.httpurlconnection.factories.decorators.Finite;
import org.dmfs.httpessentials.responsehandlers.TrivialResponseHandler;
import org.dmfs.iterators.AbstractFilteredIterator;
import org.dmfs.iterators.FilteredIterator;

import java.io.IOException;


/**
 * Simple verification service that tries to send a GET request and returns true if no authentication error has been returned.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class VerificationService extends AbstractVerificationService
{
    public VerificationService()
    {
        super(new VerificationServiceFactory()
        {
            @Override
            public com.smoothsync.smoothsetup.services.VerificationService accountService(Context context)
            {
                return new com.smoothsync.smoothsetup.services.VerificationService()
                {
                    @Override
                    public boolean verify(Provider provider, HttpAuthorizationFactory authorizationFactory) throws Exception
                    {

                        Service service = new FilteredIterator<>(provider.services(), new AbstractFilteredIterator.IteratorFilter<Service>()
                        {
                            @Override
                            public boolean iterate(Service element)
                            {
                                return "com.smoothsync.authenticate".equals(element.serviceType());
                            }
                        }).next();
                        HttpRequestExecutor executor = new Following(
                                new Retrying(
                                        new HttpUrlConnectionExecutor(
                                                new Trusted(new Finite(new DefaultHttpUrlConnectionFactory(), 10000, 10000), service.keyStore())),
                                        new DefaultRetryPolicy(3)),
                                new Secure(new FollowRedirectPolicy(5)));

                        return executor.execute(service.uri(), authorizationFactory.authenticate(new HttpRequest<Boolean>()
                        {
                            @Override
                            public HttpMethod method()
                            {
                                return HttpMethod.GET;
                            }


                            @Override
                            public Headers headers()
                            {
                                return EmptyHeaders.INSTANCE;
                            }


                            @Override
                            public HttpRequestEntity requestEntity()
                            {
                                return EmptyHttpRequestEntity.INSTANCE;
                            }


                            @Override
                            public HttpResponseHandler<Boolean> responseHandler(HttpResponse response) throws IOException, ProtocolError, ProtocolException
                            {
                                return new TrivialResponseHandler<>(!HttpStatus.UNAUTHORIZED.equals(response.status()));
                            }
                        }));
                    }
                };
            }
        });
    }
}

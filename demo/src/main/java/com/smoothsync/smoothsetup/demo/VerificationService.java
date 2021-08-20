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

import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.services.delegating.DelegatingVerificationService;
import com.smoothsync.smoothsetup.utils.DefaultOkHttpGenerator;
import com.smoothsync.smoothsetup.utils.Finite;
import com.smoothsync.smoothsetup.utils.HasAny;
import com.smoothsync.smoothsetup.utils.TrivialResponseHandler;
import com.smoothsync.smoothsetup.utils.Trusted;

import org.dmfs.httpessentials.HttpMethod;
import org.dmfs.httpessentials.HttpStatus;
import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.client.HttpRequestEntity;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.client.HttpResponse;
import org.dmfs.httpessentials.client.HttpResponseHandler;
import org.dmfs.httpessentials.entities.EmptyHttpRequestEntity;
import org.dmfs.httpessentials.exceptions.UnauthorizedException;
import org.dmfs.httpessentials.executors.authorizing.Authorizing;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.Composite;
import org.dmfs.httpessentials.executors.following.policies.FollowPolicy;
import org.dmfs.httpessentials.executors.following.policies.Limited;
import org.dmfs.httpessentials.executors.following.policies.Relative;
import org.dmfs.httpessentials.executors.following.policies.Secure;
import org.dmfs.httpessentials.executors.retrying.Retrying;
import org.dmfs.httpessentials.executors.retrying.policies.DefaultRetryPolicy;
import org.dmfs.httpessentials.executors.useragent.Branded;
import org.dmfs.httpessentials.headers.EmptyHeaders;
import org.dmfs.httpessentials.headers.Headers;
import org.dmfs.httpessentials.methods.SafeMethod;
import org.dmfs.httpessentials.okhttp.OkHttpExecutor;
import org.dmfs.httpessentials.types.SimpleProduct;
import org.dmfs.iterables.Split;
import org.dmfs.iterators.decorators.Filtered;
import org.dmfs.jems.optional.adapters.First;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.optional.decorators.Sieved;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.predicate.Predicate;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.parameters.Parameter;
import org.dmfs.rfc3986.parameters.ParameterList;
import org.dmfs.rfc3986.parameters.adapters.XwfueParameterList;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;


/**
 * Simple verification service that tries to send a GET request and returns true if no authentication error has been returned.
 *
 * @author Marten Gajda
 */
public final class VerificationService extends DelegatingVerificationService
{
    private final static Collection<String> SAFE_METHODS = Arrays.asList("PROPFIND", "GET", "HEAD", "OPTIONS");
    private final static Pattern STATUS_PATTERN = Pattern.compile("^[1-5]\\d\\d$");


    public VerificationService()
    {
        super(context -> (provider, authStrategy) ->
        {

            Service service = new Filtered<>(provider.services(), element -> "com.smoothsync.authenticate".equals(element.serviceType())).next();
            HttpRequestExecutor executor = new Following(
                new Authorizing(
                    new Retrying(
                        new Branded(
                            new OkHttpExecutor(
                                new Trusted(new Finite(new DefaultOkHttpGenerator(), 10000, 10000), service.keyStore()).next()::build),
                            new SimpleProduct("Smoothsync")),
                        new DefaultRetryPolicy(3)),
                    authStrategy),
                new Limited(5,
                    new Composite(
                        new Relative(new FollowPolicy()),
                        new Secure(new FollowPolicy()))));

            try
            {
                ParameterList fragmentParameters = new XwfueParameterList(new Mapped<>(Precoded::new, new NullSafe<>(service.uri().getRawFragment())));
                return executor.execute(service.uri(), new HttpRequest<Boolean>()
                {
                    @Override
                    public HttpMethod method()
                    {
                        return new Backed<>(
                            new Mapped<>(
                                m -> new SafeMethod(m, false /* maybe later */),
                                new Sieved<>(
                                    SAFE_METHODS::contains,
                                    new Mapped<>(
                                        p -> p.textValue().toString(),
                                        new First<>(
                                            fragmentParameters,
                                            (Predicate<Parameter>) p -> "method".equals(p.name().toString()))))),
                            HttpMethod.GET).value();
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
                    public HttpResponseHandler<Boolean> responseHandler(HttpResponse response)
                    {
                        return new TrivialResponseHandler<>(
                            new Backed<>(
                                new Mapped<>(
                                    new HasAny<Integer>(t -> response.status().statusCode() == t)::satisfiedBy,
                                    new Mapped<>(
                                        // map the list of strings to a list of integers
                                        p -> new org.dmfs.jems.iterable.decorators.Mapped<>(
                                            c -> Integer.parseInt(c.toString()),
                                            new org.dmfs.iterables.decorators.Sieved<>(
                                                c -> STATUS_PATTERN.matcher(c).matches(),
                                                new Split(p.textValue(), ','))),
                                        new First<>(fragmentParameters,
                                            (Predicate<Parameter>) p -> "success_codes".equals(p.name().toString())))),
                                !HttpStatus.UNAUTHORIZED.equals(response.status())).value());
                    }
                });
            }
            catch (UnauthorizedException e)
            {
                // not authenticated
                return false;
            }
        });
    }
}

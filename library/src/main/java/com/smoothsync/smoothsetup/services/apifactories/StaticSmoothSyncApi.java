/*
 * Copyright (c) 2019 dmfs GmbH
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

package com.smoothsync.smoothsetup.services.apifactories;

import android.content.Context;

import com.smoothsync.api.SmoothSyncApi;
import com.smoothsync.api.SmoothSyncApiRequest;
import com.smoothsync.api.model.impl.JsonObjectArrayIterator;
import com.smoothsync.smoothsetup.providerdata.ProviderData;

import org.dmfs.httpessentials.HttpStatus;
import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.client.HttpResponse;
import org.dmfs.httpessentials.client.HttpResponseEntity;
import org.dmfs.httpessentials.client.HttpResponseHandler;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.exceptions.RedirectionException;
import org.dmfs.httpessentials.exceptions.UnexpectedStatusException;
import org.dmfs.httpessentials.headers.EmptyHeaders;
import org.dmfs.httpessentials.headers.Headers;
import org.dmfs.httpessentials.headers.HttpHeaders;
import org.dmfs.httpessentials.headers.SingletonHeaders;
import org.dmfs.httpessentials.types.MediaType;
import org.dmfs.httpessentials.types.StructuredMediaType;
import org.dmfs.iterators.decorators.Sieved;
import org.dmfs.optional.Optional;
import org.dmfs.optional.Present;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import static org.dmfs.optional.Absent.absent;


/**
 * @author Marten Gajda
 */
public final class StaticSmoothSyncApi implements SmoothSyncApi
{
    private final static String PROVIDERS_PATH = "/providers/";
    private final ProviderData mData;
    private final Context mContext;


    public StaticSmoothSyncApi(ProviderData data, Context context)
    {
        mData = data;
        mContext = context;
    }


    @Override
    public <T> T resultOf(SmoothSyncApiRequest<T> smoothSyncApiRequest) throws IOException, ProtocolError, ProtocolException
    {
        try
        {
            return smoothSyncApiRequest.result(
                    new StaticApiExecutor(mData.providerData(mContext)),
                    URI.create("/"));
        }
        catch (JSONException e)
        {
            throw new ProtocolException("Unhandled JSONException", e);
        }
    }


    public static final class StaticApiExecutor implements HttpRequestExecutor
    {

        private final JSONObject mData;


        private StaticApiExecutor(JSONObject data)
        {
            mData = data;
        }


        @Override
        public <T> T execute(URI uri, HttpRequest<T> request) throws IOException, ProtocolError, ProtocolException, RedirectionException, UnexpectedStatusException
        {
            HttpResponse response;
            if (uri.getPath().equals("/ping"))
            {
                // fake a ping response
                try
                {
                    response = new JsonResponse(uri,
                            new JSONObject().put("sponsored-until", "2999-01-01T00:00:00Z").toString());
                }
                catch (JSONException e)
                {
                    response = new NotFoundResponse(uri);
                }
            }
            else if (uri.getPath().equals(PROVIDERS_PATH))
            {
                response = new JsonResponse(uri, mData.toString());
            }
            else if (uri.getPath().startsWith(PROVIDERS_PATH))
            {
                String providerId = uri.getPath().substring(PROVIDERS_PATH.length());

                Iterator<JSONObject> result = new Sieved<>(o -> providerId.equals(o.optString("id")),
                        new JsonObjectArrayIterator(mData.optJSONArray("providers")));
                if (!result.hasNext())
                {
                    response = new NotFoundResponse(uri);
                }
                else
                {
                    response = new JsonResponse(uri, result.next().toString());
                }
            }
            else
            {
                response = new NotFoundResponse(uri);
            }

            HttpResponseHandler<T> handler = request.responseHandler(response);
            return handler.handleResponse(response);
        }
    }


    private static class NotFoundResponse implements HttpResponse
    {
        private final URI mUri;


        public NotFoundResponse(URI uri)
        {
            mUri = uri;
        }


        @Override
        public HttpStatus status()
        {
            return HttpStatus.NOT_FOUND;
        }


        @Override
        public Headers headers()
        {
            return EmptyHeaders.INSTANCE;
        }


        @Override
        public HttpResponseEntity responseEntity()
        {
            return new HttpResponseEntity()
            {
                @Override
                public Optional<MediaType> contentType()
                {
                    return absent();
                }


                @Override
                public Optional<Long> contentLength()
                {
                    return absent();
                }


                @Override
                public InputStream contentStream() throws IOException
                {
                    return new InputStream()
                    {
                        @Override
                        public int read() throws IOException
                        {
                            return -1;
                        }
                    };
                }
            };
        }


        @Override
        public URI requestUri()
        {
            return mUri;
        }


        @Override
        public URI responseUri()
        {
            return mUri;
        }
    }


    private final static class JsonResponse implements HttpResponse
    {

        private final URI mUri;
        private final String mResponse;


        private JsonResponse(URI uri, String response)
        {
            mUri = uri;
            mResponse = response;
        }


        @Override
        public HttpStatus status()
        {
            return HttpStatus.OK;
        }


        @Override
        public Headers headers()
        {
            return new SingletonHeaders(HttpHeaders.CONTENT_TYPE.entity(new StructuredMediaType("application", "json")));
        }


        @Override
        public HttpResponseEntity responseEntity()
        {
            return new JsonResponse.StringResponseEntity(mResponse);
        }


        @Override
        public URI requestUri()
        {
            return mUri;
        }


        @Override
        public URI responseUri()
        {
            return mUri;
        }


        private final static class StringResponseEntity implements HttpResponseEntity
        {
            private final String mResponse;


            public StringResponseEntity(String response)
            {
                mResponse = response;
            }


            @Override
            public Optional<MediaType> contentType()
            {
                return new Present<>(new StructuredMediaType("application", "json"));
            }


            @Override
            public Optional<Long> contentLength()
            {
                return absent();
            }


            @Override
            public InputStream contentStream() throws IOException
            {
                return new ByteArrayInputStream(mResponse.getBytes("UTF-8"));
            }
        }
    }

}

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

package com.smoothsync.smoothsetup.model;

import android.os.Parcel;

import net.iharder.Base64;

import org.dmfs.httpessentials.HttpMethod;
import org.dmfs.httpessentials.client.HttpRequest;
import org.dmfs.httpessentials.client.HttpRequestEntity;
import org.dmfs.httpessentials.client.HttpResponse;
import org.dmfs.httpessentials.client.HttpResponseHandler;
import org.dmfs.httpessentials.converters.PlainStringHeaderConverter;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.headers.BasicSingletonHeaderType;
import org.dmfs.httpessentials.headers.Headers;

import java.io.IOException;


/**
 * A basic implementation of HttpAuthorizationFactory.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class BasicHttpAuthorizationFactory implements HttpAuthorizationFactory
{
	private final String mAccountId;
	private final String mPassword;


	public BasicHttpAuthorizationFactory(String accountId, String password)
	{
		this.mAccountId = accountId;
		this.mPassword = password;
	}


	@Override
	public <T> HttpRequest<T> authenticate(final HttpRequest<T> request)
	{
		return new HttpRequest<T>()
		{
			@Override
			public HttpMethod method()
			{
				return request.method();
			}


			@Override
			public Headers headers()
			{
				try
				{
					return request.headers().withHeader(new BasicSingletonHeaderType<String>("Authorization", PlainStringHeaderConverter.INSTANCE)
						.entity("Basic " + Base64.encodeBytes((mAccountId + ":" + mPassword).getBytes(), Base64.URL_SAFE)));
				}
				catch (IOException e)
				{
					throw new RuntimeException("Can't encode authorization header");
				}
			}


			@Override
			public HttpRequestEntity requestEntity()
			{
				return request.requestEntity();
			}


			@Override
			public HttpResponseHandler<T> responseHandler(HttpResponse response) throws IOException, ProtocolError, ProtocolException
			{
				return request.responseHandler(response);
			}
		};
	}


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(mAccountId);
		dest.writeString(mPassword);
	}

	public final static Creator<BasicHttpAuthorizationFactory> CREATOR = new Creator<BasicHttpAuthorizationFactory>()
	{
		@Override
		public BasicHttpAuthorizationFactory createFromParcel(Parcel source)
		{
			return new BasicHttpAuthorizationFactory(source.readString(), source.readString());
		}


		@Override
		public BasicHttpAuthorizationFactory[] newArray(int size)
		{
			return new BasicHttpAuthorizationFactory[size];
		}
	};
}

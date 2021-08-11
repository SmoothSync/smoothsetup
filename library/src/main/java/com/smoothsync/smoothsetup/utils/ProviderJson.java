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

package com.smoothsync.smoothsetup.utils;

import com.smoothsync.api.model.Provider;

import net.iharder.Base64;

import org.dmfs.express.json.elementary.Array;
import org.dmfs.express.json.elementary.DelegatingJsonValue;
import org.dmfs.express.json.elementary.Member;
import org.dmfs.express.json.elementary.Null;
import org.dmfs.express.json.elementary.Object;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.iterables.EmptyIterable;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.single.Backed;
import org.dmfs.rfc5545.DateTime;

import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.util.Collections;
import java.util.Locale;

import androidx.annotation.NonNull;


public final class ProviderJson extends DelegatingJsonValue
{
    public ProviderJson(@NonNull Provider origProvider) throws ProtocolException
    {
        super(new Object(
            new Member("id", origProvider.id()),
            new Member("name", origProvider.name()),
            new Member("domains", new Array(origProvider.domains())),
            new Member("last-modified", formatDateTime(origProvider.lastModified())),
            new Member("services", new Array(
                new Mapped<>(
                    s -> {
                        return new Object(
                            new Member("service-type", s.serviceType()),
                            new Member("name", s.name()),
                            new Member("uri", s.uri().toASCIIString()),
                            new Member("com-smoothsync-certificates",
                                s.keyStore() == null ? new Null() :
                                    new Array(
                                        new Mapped<>(
                                            alias ->
                                            {
                                                try
                                                {
                                                    return new org.dmfs.express.json.elementary.String(
                                                        "-----BEGIN CERTIFICATE-----\n" +
                                                            Base64.encodeBytes(
                                                                s.keyStore().getCertificate(alias).getEncoded()) +
                                                            "\n-----END CERTIFICATE-----");
                                                }
                                                catch (CertificateEncodingException | KeyStoreException e)
                                                {
                                                    throw new RuntimeException("can't get certificate", e);

                                                }
                                            },
                                            new Backed<Iterable<String>>(
                                                new org.dmfs.jems2.optional.Mapped<>(
                                                    ks -> {
                                                        try
                                                        {
                                                            return Collections.list(ks.aliases());
                                                        }
                                                        catch (KeyStoreException e)
                                                        {
                                                            throw new RuntimeException("Error reading keystore");
                                                        }
                                                    },
                                                    new NullSafe<>(s.keyStore())),
                                                new EmptyIterable<>()).value())
                                    )
                            ));
                    },
                    () -> {
                        try
                        {
                            return origProvider.services();
                        }
                        catch (ProtocolException e)
                        {
                            throw new RuntimeException("can't get services", e);
                        }
                    })
            ))));
    }


    private static String formatDateTime(DateTime dateTime)
    {
        return String.format(Locale.ENGLISH, "%04d-%02d-%02dT%02d:%02d:%02dZ", dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDayOfMonth(),
            dateTime.getHours(), dateTime.getMinutes(), dateTime.getSeconds());
    }
}

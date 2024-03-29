package com.smoothsync.smoothsetup.demo;

import android.accounts.Account;
import android.os.Bundle;
import android.os.RemoteException;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.model.BasicAccount;
import com.smoothsync.smoothsetup.services.delegating.DelegatingAccountService;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.types.Link;
import org.dmfs.iterators.EmptyIterator;
import org.dmfs.iterators.elementary.Seq;
import org.dmfs.jems2.Optional;
import org.dmfs.rfc5545.DateTime;

import java.net.URI;
import java.security.KeyStore;
import java.util.Iterator;

import static org.dmfs.jems2.optional.Absent.absent;


/**
 * Fake AccountService that doesn't do anything.
 *
 * @author Marten Gajda
 */
public class AccountService extends DelegatingAccountService
{
    public AccountService()
    {
        super(context -> new com.smoothsync.smoothsetup.services.AccountService.Stub()
        {
            @Override
            public BasicAccount providerForAccount(Account account) throws RemoteException
            {
                return new BasicAccount("sogo1", new Provider()
                {
                    @Override
                    public String id() throws ProtocolException
                    {
                        return "xxy";
                    }


                    @Override
                    public String name() throws ProtocolException
                    {
                        return "SOGo";
                    }


                    @Override
                    public String[] domains() throws ProtocolException
                    {
                        return new String[0];
                    }


                    @Override
                    public Iterator<Link> links() throws ProtocolException
                    {
                        return EmptyIterator.instance();
                    }


                    @Override
                    public Iterator<Service> services() throws ProtocolException
                    {
                        return new Seq<>(new Service()
                        {
                            @Override
                            public String name()
                            {
                                return "caldav";
                            }


                            @Override
                            public String serviceType()
                            {
                                return "com.smoothsync.authenticate";
                            }


                            @Override
                            public URI uri()
                            {
                                return URI.create("http://sogo-demo.inverse.ca/SOGo/dav");
                            }


                            @Override
                            public Optional<KeyStore> keyStore()
                            {
                                return absent();
                            }
                        });
                    }


                    @Override
                    public DateTime lastModified() throws ProtocolException
                    {
                        return DateTime.now();
                    }
                });
            }


            @Override
            public void createAccount(Bundle bundle) throws RemoteException
            {
                try
                {
                    // wait a little to emulate the account setup
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }


            @Override
            public void updateAccount(Bundle bundle) throws RemoteException
            {
                try
                {
                    // wait a little to emulate the account setup
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}

package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword;

import com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.providers.FastmailPasswordProbe;
import com.smoothsync.smoothsetup.wizardsteps.appspecificpassword.providers.ICloudPasswordProbe;

import java.net.URI;


/**
 * A {@link PasswordProbeFactory}.
 *
 * @author Marten Gajda
 */
public class DefaultPasswordProbeFactory implements PasswordProbeFactory
{
    @Override
    public AppSpecificPasswordProbe forUrl(URI url)
    {
        switch (url.getHost())
        {
            case "appleid.apple.com":
                return new ICloudPasswordProbe();
            case "www.fastmail.com":
                return new FastmailPasswordProbe();
            default:
                return null;
        }
    }
}

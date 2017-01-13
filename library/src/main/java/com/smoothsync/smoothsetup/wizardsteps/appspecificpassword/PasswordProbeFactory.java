package com.smoothsync.smoothsetup.wizardsteps.appspecificpassword;

import java.net.URI;


/**
 * A factory that returns {@link AppSpecificPasswordProbe}s based on the URL of the service.
 *
 * @author Marten Gajda
 */
public interface PasswordProbeFactory
{
    AppSpecificPasswordProbe forUrl(URI url);
}

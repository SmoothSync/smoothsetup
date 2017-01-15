/*
 * Copyright (c) 2017 dmfs GmbH
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

package com.smoothsync.smoothsetup.services;

import com.smoothsync.api.model.Provider;
import com.smoothsync.api.model.Service;
import com.smoothsync.smoothsetup.model.HttpAuthorizationFactory;


/**
 * An Android service that verifies access to a specific {@link Service}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface VerificationService
{
    String ACTION = "com.smoothsync.SERVICE_TEST_SERVICE";

    boolean verify(Provider provider, HttpAuthorizationFactory authorizationFactory) throws Exception;
}

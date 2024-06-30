/*
 * Copyright (c) 2018 dmfs GmbH
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

import android.content.pm.PackageManager;

import org.dmfs.jems2.iterable.DelegatingIterable;
import org.dmfs.jems2.iterable.Distinct;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Sieved;


/**
 * An Iterable of permission groups of the given permissions.
 *
 * @author Marten Gajda
 */
public final class PermissionGroups extends DelegatingIterable<String>
{
    public PermissionGroups(PackageManager packageManager, Iterable<String> permissions)
    {
        super(new Sieved<>(permission -> permission.length() > 0, new Distinct<>(new Mapped<>(permission ->
        {
            try
            {
                String group = packageManager.getPermissionInfo(permission, 0).group;
                return group != null ? group : "";
            }
            catch (PackageManager.NameNotFoundException e)
            {
                return "";
            }
        }, permissions))));
    }
}

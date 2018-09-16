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

package com.smoothsync.smoothsetup.microfragments;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.utils.AppLabel;
import com.smoothsync.smoothsetup.utils.Denied;
import com.smoothsync.smoothsetup.utils.IterableBox;
import com.smoothsync.smoothsetup.utils.PermissionGroups;
import com.smoothsync.smoothsetup.utils.Size;
import com.smoothsync.smoothsetup.utils.StringBox;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentHost;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Boxable;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.iterables.Distinct;
import org.dmfs.iterables.decorators.Sieved;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.single.elementary.Reduced;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_DENIED;


/**
 * A {@link MicroFragment} that presents the user with a list of providers to choose from.
 *
 * @author Marten Gajda
 */
public final class PermissionMicroFragment<T extends Boxable<T>> implements MicroFragment<PermissionMicroFragment.PermissionListFragment.Params<T>>
{
    public final static Creator<PermissionMicroFragment<?>> CREATOR = new Creator<PermissionMicroFragment<?>>()
    {
        @SuppressWarnings("unchecked") // we don't know the generic type in static context
        @Override
        public PermissionMicroFragment<?> createFromParcel(Parcel source)
        {
            return new PermissionMicroFragment(
                    new Mapped<>(b -> b.boxed().value(), new Unboxed<Iterable<Boxable<String>>>(source).value()),
                    (Boxable) new Unboxed<>(source).value(),
                    new Unboxed<MicroWizard<?>>(source).value());
        }


        @Override
        public PermissionMicroFragment<?>[] newArray(int size)
        {
            return new PermissionMicroFragment[size];
        }
    };

    private final Iterable<String> mPermissions;
    private final T mData;
    private final MicroWizard<T> mNext;


    public PermissionMicroFragment(@NonNull Iterable<String> permissions, T passthroughData, MicroWizard<T> next)
    {
        mPermissions = permissions;
        mData = passthroughData;
        mNext = next;
    }


    @NonNull
    @Override
    public String title(@NonNull Context context)
    {
        return context.getResources()
                .getQuantityString(R.plurals.smoothsetup_wizard_title_grant_permissions,
                        new Size(new PermissionGroups(context.getPackageManager(), new Denied(context, mPermissions))).intValue());
    }


    @Override
    public boolean skipOnBack()
    {
        return false;
    }


    @NonNull
    @Override
    public Fragment fragment(@NonNull Context context, @NonNull MicroFragmentHost host)
    {
        return new PermissionListFragment<>();
    }


    @NonNull
    @Override
    public PermissionListFragment.Params<T> parameter()
    {
        return new PermissionListFragment.Params<T>()
        {
            @NonNull
            @Override
            public Iterable<String> permissions()
            {
                return mPermissions;
            }


            @NonNull
            @Override
            public T data()
            {
                return mData;
            }


            @NonNull
            @Override
            public MicroWizard<T> next()
            {
                return mNext;
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
        dest.writeParcelable(new IterableBox<>(new Mapped<>(StringBox::new, mPermissions)), flags);
        dest.writeParcelable(mData.boxed(), flags);
        dest.writeParcelable(mNext.boxed(), flags);
    }


    /**
     * A Fragment that shows a list of permissions.
     *
     * @author Marten Gajda <marten@dmfs.org>
     */
    public static final class PermissionListFragment<T> extends Fragment implements View.OnClickListener
    {
        private View mView;
        private MicroFragmentEnvironment<Params<T>> mMicroFragmentEnvironment;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            Activity activity = getActivity();
            mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
            if (!new Sieved<Integer>(i -> i == PERMISSION_DENIED,
                    new Mapped<>(perm -> ContextCompat.checkSelfPermission(activity, perm),
                            mMicroFragmentEnvironment.microFragment().parameter().permissions())).iterator().hasNext())
            {
                container.post(() ->
                {

                    mMicroFragmentEnvironment.host()
                            .execute(activity, new Swiped(
                                    new ForwardTransition<>(mMicroFragmentEnvironment.microFragment()
                                            .parameter()
                                            .next()
                                            .microFragment(getActivity(), mMicroFragmentEnvironment.microFragment().parameter().data()))));
                });
                return null;
            }
            mView = inflater.inflate(R.layout.smoothsetup_microfragment_permissions, container, false);
            mView.findViewById(R.id.button).setOnClickListener(this);
            PackageManager packageManager = activity.getPackageManager();
            Iterable<String> permissionGroups = new Distinct<>(new Mapped<>(perm ->
            {
                try
                {
                    return packageManager.getPermissionInfo(perm, 0).group;
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    return "";
                }
            }, new Sieved<>(perm -> ContextCompat.checkSelfPermission(activity, perm) == PERMISSION_DENIED,
                    mMicroFragmentEnvironment.microFragment().parameter().permissions())));
            for (String permissionGroup : permissionGroups)
            {
                View view = inflater.inflate(R.layout.smoothsetup_permission, mView.findViewById(R.id.container), false);
                ((ViewGroup) mView.findViewById(R.id.container)).addView(view);
                PermissionGroupInfo permissionGroupInfo = null;
                try
                {
                    permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionGroup, 0);
                    ((TextView) view.findViewById(R.id.content)).setText(permissionGroupInfo.loadLabel(packageManager));
                    ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(permissionGroupInfo.loadIcon(packageManager));
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    // ignore, don't show
                }
            }
            ((TextView) mView.findViewById(android.R.id.message)).setText(activity.getResources()
                    .getQuantityString(R.plurals.smoothsetup_prompt_grant_permission,
                            new Size(new PermissionGroups(activity.getPackageManager(),
                                    new Denied(activity, mMicroFragmentEnvironment.microFragment().parameter().permissions()))).intValue(),
                            new AppLabel(getActivity()).value()));
            return mView;
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
        {
            for (int i : grantResults)
            {
                if (i == PERMISSION_DENIED)
                {
                    return;
                }
            }
            mMicroFragmentEnvironment.host()
                    .execute(getActivity(), new Swiped(
                            new ForwardTransition<>(mMicroFragmentEnvironment.microFragment()
                                    .parameter()
                                    .next()
                                    .microFragment(getActivity(), mMicroFragmentEnvironment.microFragment().parameter().data()))));
        }


        @Override
        public void onClick(View view)
        {
            requestPermissions(new Reduced<>(new ArrayList<String>(), (list, value) ->
                    {
                        list.add(value);
                        return list;
                    }, mMicroFragmentEnvironment.microFragment().parameter().permissions()).value().toArray(new String[0]),
                    1);
        }


        interface Params<T>
        {
            @NonNull
            Iterable<String> permissions();

            @NonNull
            T data();

            @NonNull
            MicroWizard<T> next();
        }
    }
}

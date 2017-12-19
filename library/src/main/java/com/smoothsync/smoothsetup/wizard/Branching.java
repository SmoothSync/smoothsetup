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

package com.smoothsync.smoothsetup.wizard;

import android.content.Context;
import android.os.Parcel;

import org.dmfs.android.microfragments.MicroFragment;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.android.microwizard.box.Box;
import org.dmfs.android.microwizard.box.Unboxed;
import org.dmfs.jems.function.Function;


/**
 * A composite {@link MicroWizard} which branches into different wizards, depending on the input data and converges back into a common follow up state.
 * <p>
 * TODO revisit the concept
 *
 * @author Marten Gajda
 */
public final class Branching<V, T> implements MicroWizard<T>
{
    private final Box<Function<MicroWizard<V>, MicroWizard<T>>> mBranchFunctionBox;
    private final MicroWizard<V> mNext;


    private Branching(Box<Function<MicroWizard<V>, MicroWizard<T>>> branchFunctionBox, MicroWizard<V> next)
    {
        mBranchFunctionBox = branchFunctionBox;
        mNext = next;
    }


    @Override
    public MicroFragment<?> microFragment(Context context, T argument)
    {
        return mBranchFunctionBox.value().value(mNext).microFragment(context, argument);
    }


    @Override
    public Box<MicroWizard<T>> boxed()
    {
        return new WizardBox<>(mBranchFunctionBox, mNext);
    }


    private final static class WizardBox<V, T> implements Box<MicroWizard<T>>
    {
        private final Box<Function<MicroWizard<V>, MicroWizard<T>>> mFunctionBox;
        private final MicroWizard<V> mNext;


        private WizardBox(Box<Function<MicroWizard<V>, MicroWizard<T>>> functionBox, MicroWizard<V> next)
        {
            mFunctionBox = functionBox;
            mNext = next;
        }


        @Override
        public int describeContents()
        {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            dest.writeParcelable(mFunctionBox, flags);
            dest.writeParcelable(mNext.boxed(), flags);
        }


        @Override
        public MicroWizard<T> value()
        {
            return new Branching<>(mFunctionBox, mNext);
        }


        public final static Creator<Box<Branching>> CREATOR = new Creator<Box<Branching>>()
        {
            @Override
            public Box<Branching> createFromParcel(Parcel source)
            {
                return new WizardBox<>(source.readParcelable(getClass().getClassLoader()), new Unboxed<MicroWizard>(source).value());
            }


            @Override
            public Box<Branching>[] newArray(int size)
            {
                return new WizardBox[size];
            }
        };
    }
}

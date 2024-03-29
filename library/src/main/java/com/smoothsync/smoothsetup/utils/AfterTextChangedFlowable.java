/*
 * Copyright (c) 2020 dmfs GmbH
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

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.ObservableSource;


/**
 * An {@link ObservableSource} to monitor the changes of a {@link TextView}.
 */
public final class AfterTextChangedFlowable extends DelegatingFlowable<String>
{
    public AfterTextChangedFlowable(@NonNull EditText editText)
    {
        super(create(emitter -> {
            TextWatcher textWatcher = new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                }


                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                }


                @Override
                public void afterTextChanged(Editable editable)
                {
                    emitter.onNext(editable.toString());
                }
            };
            editText.addTextChangedListener(textWatcher);
            emitter.setCancellable(() -> editText.removeTextChangedListener(textWatcher));
        }, BackpressureStrategy.LATEST));
    }
}

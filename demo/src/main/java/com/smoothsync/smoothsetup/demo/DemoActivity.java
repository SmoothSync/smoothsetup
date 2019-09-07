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

package com.smoothsync.smoothsetup.demo;

import android.accounts.Account;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smoothsync.smoothsetup.microfragments.SetupDispatchMicroFragment;

import androidx.appcompat.app.AppCompatActivity;


public class DemoActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }


    public void add(View view)
    {
        startActivity(
                new Intent()
                        .setComponent(new ComponentName(getPackageName(), "com.smoothsync.smoothsetup.SmoothSetupDispatchActivity")));
    }


    public void update(View view)
    {
        startActivity(
                new Intent()
                        .setComponent(new ComponentName(getPackageName(), "com.smoothsync.smoothsetup.SmoothUpdateAuthDispatchActivity"))
                        .putExtra(SetupDispatchMicroFragment.DispatchFragment.PARAM_ACCOUNT, new Account("x", "y")));
    }

}

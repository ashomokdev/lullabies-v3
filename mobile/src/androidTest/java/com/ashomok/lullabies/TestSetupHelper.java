/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ashomok.lullabies;

import android.content.Context;
import android.content.SharedPreferences;

import com.ashomok.lullabies.model.MusicProvider;
import com.ashomok.lullabies.model.MusicProviderSource;

import org.mockito.Mockito;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class TestSetupHelper {
    public static MusicProvider setupMusicProvider(MusicProviderSource source)
            throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        SharedPreferences sharedPreferences = Mockito.mock(SharedPreferences.class);

        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        Mockito.when(sharedPreferences.edit()).thenReturn(editor);
        MusicProvider provider = new MusicProvider(source, sharedPreferences);
        provider.retrieveMediaAsync(success -> signal.countDown());
        signal.await();
        return provider;
    }
}
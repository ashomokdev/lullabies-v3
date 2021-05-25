package com.ashomok.lullabies.music_service;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ashomok.lullabies.model.LocalJSONSource;
import com.ashomok.lullabies.model.MusicProviderSource;
import com.ashomok.lullabies.utils.LogHelper;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MusicServiceModule {
    private static final String TAG = LogHelper.makeLogTag(MusicServiceModule.class);

    @Provides
    static @NonNull
    MusicProviderSource provideMusicProviderSource(Context context) {
       return new LocalJSONSource(context);
    }
}

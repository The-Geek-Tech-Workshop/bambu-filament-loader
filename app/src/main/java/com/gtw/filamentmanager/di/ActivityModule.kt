package com.gtw.filamentmanager.di

import android.content.Context
import android.nfc.NfcAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun provideNfcAdaptor(
        @ActivityContext context: Context
    ): NfcAdapter = NfcAdapter.getDefaultAdapter(context)
}
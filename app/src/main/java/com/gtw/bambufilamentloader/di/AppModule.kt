package com.gtw.bambufilamentloader.di

import android.content.Context
import com.gtw.bambufilamentloader.data.PrinterAuthenticationDetailsRepoSharedPreferences
import com.gtw.bambufilamentloader.data.bambu.PrinterConnectorMqtt
import com.gtw.bambufilamentloader.data.bambu.PrinterRepoSsdp
import com.gtw.bambufilamentloader.model.repos.PrinterAuthenticationDetailsRepo
import com.gtw.bambufilamentloader.model.repos.PrinterConnector
import com.gtw.bambufilamentloader.model.repos.PrinterRepo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.inject.Singleton
import javax.net.ssl.TrustManagerFactory
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModuleBindings {

    @Binds
    abstract fun bindPrinterAuthenticationDetailsRepo(
        printerAuthenticationDetailsRepoSharedPreferences: PrinterAuthenticationDetailsRepoSharedPreferences
    ): PrinterAuthenticationDetailsRepo

    @Binds
    abstract fun bindPrinterRepo(
        printerRepoSsdp: PrinterRepoSsdp
    ): PrinterRepo

    @Binds
    abstract fun bindFilamentTrayRepo(
        filamentTrayRepoMqtt: PrinterConnectorMqtt
    ): PrinterConnector


}

class DataCoroutineScope(override val coroutineContext: CoroutineContext = Dispatchers.IO) :
    CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataCoroutineScope(): DataCoroutineScope = DataCoroutineScope()

    @Provides
    @Singleton
    // A trust manager that trusts the Bambu CA certificate
    fun provideTrustManagerFactory(
        @ApplicationContext context: Context
    ): TrustManagerFactory = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, null)
                setCertificateEntry(
                    "bambu local mqtt",
                    CertificateFactory.getInstance("X.509")
                        .generateCertificate(
                            context.assets.open("ca_cert.pem")
                        )
                )
            })
        }
}
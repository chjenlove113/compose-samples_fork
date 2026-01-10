package com.news.data.di

import android.content.Context
import com.news.data.api.NewsTagService
import com.news.data.api.ShowHomeService
import com.news.data.networks.HeaderInterceptor
import com.news.data.networks.NetworkConnectionInterceptor
import com.news.domain.util.DefaultDispatcherProvider
import com.news.domain.util.DispatcherProvider
import com.news.utils.AppContants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(NetworkConnectionInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideCustomRetrofit(@ApplicationContext context: Context): Retrofit {
        val url = AppContants.baseUrl

        val nullOnEmptyConverterFactory = object : Converter.Factory() {
            fun converterFactory() = this

            override fun responseBodyConverter(
                type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
            ): Converter<ResponseBody, Any?> {
                val nextResponseBodyConverter =
                    retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)

                return Converter { value ->
                    if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value) else null
                }
            }
        }

        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .client(provideOkHttpClient(context))
            .build()
    }

    @Provides
    @Singleton
    fun provideCatsService(retrofit: Retrofit): NewsTagService {
        return retrofit.create(NewsTagService::class.java)
    }

    @Provides
    @Singleton
    fun provideShowHomeService(retrofit: Retrofit): ShowHomeService {
        return retrofit.create(ShowHomeService::class.java)
    }
}
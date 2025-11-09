package com.example.myapplication.di

import com.example.myapplication.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthHeadersInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val builder = req.newBuilder()

        // Só adiciona se ainda não tiver sido definido manualmente
        if (req.header("X-Api-Token") == null && BuildConfig.API_TOKEN.isNotEmpty()) {
            builder.addHeader("X-Api-Token", BuildConfig.API_TOKEN)
        }

        // Se quiser, já deixe um Authorization padrão (opcional).
        // if (req.header("Authorization") == null) {
        //     builder.addHeader("Authorization", "Bearer <seu_jwt_opcional>")
        // }

        return chain.proceed(builder.build())
    }
}

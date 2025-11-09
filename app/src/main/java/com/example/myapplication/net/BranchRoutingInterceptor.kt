package com.example.myapplication.net

import com.example.myapplication.data.settings.SettingsRepository
import dagger.Reusable
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * - Lê a filial ativa nas configurações
 * - Reescreve a base (scheme/host/port) da URL para o backendUrl da filial
 * - Injeta X-DB-Host e X-DB-Name com dbServer / dbName
 */
@Reusable
class BranchRoutingInterceptor @Inject constructor(
    private val settings: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalReq = chain.request()
        val originalUrl = originalReq.url

        // Snapshot síncrono das Flows (interceptor não é suspending)
        val snapshot = runBlocking {
            val currentId = settings.currentId().first()
                ?: return@runBlocking Snapshot(null, null, null)

            val branch = settings.branches().first().firstOrNull { it.id == currentId }
                ?: return@runBlocking Snapshot(null, null, null)

            // --- BACKEND HTTP ---
            // backendUrl é a URL completa da API (ex: "http://192.168.15.11:8000/")
            val rawBackend = branch.backendUrl.trim()
            val normalizedBackend = if (
                rawBackend.startsWith("http://") ||
                rawBackend.startsWith("https://")
            ) {
                rawBackend
            } else if (rawBackend.isNotEmpty()) {
                "http://$rawBackend"
            } else {
                ""
            }

            val backendHttpUrl = normalizedBackend.toHttpUrlOrNull()

            // --- DADOS DO BANCO ---
            val dbHost = branch.dbServer.trim().ifEmpty { null }
            val dbName = branch.dbName.trim().ifEmpty { null }

            Snapshot(
                baseUrl = backendHttpUrl,
                hostHeader = dbHost,
                dbName = dbName
            )
        }

        // Se não houver snapshot válido, segue a requisição original
        val base: HttpUrl? = snapshot.baseUrl
        val hostHeader: String? = snapshot.hostHeader
        val dbName: String? = snapshot.dbName

        val newUrl = if (base != null) {
            originalUrl.newBuilder()
                .scheme(base.scheme)
                .host(base.host)
                .port(base.port)
                .build()
        } else {
            originalUrl
        }

        val newReqBuilder = originalReq.newBuilder().url(newUrl)

        if (!hostHeader.isNullOrEmpty()) {
            newReqBuilder.header("X-DB-Host", hostHeader)
        }
        if (!dbName.isNullOrEmpty()) {
            newReqBuilder.header("X-DB-Name", dbName)
        }

        return chain.proceed(newReqBuilder.build())
    }

    private data class Snapshot(
        val baseUrl: HttpUrl?,
        val hostHeader: String?,
        val dbName: String?
    )
}

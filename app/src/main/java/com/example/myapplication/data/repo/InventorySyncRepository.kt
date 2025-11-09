package com.example.myapplication.data.repo

import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.local.ProductEntity
import com.example.myapplication.data.remote.inventory.InventoryApi
import com.example.myapplication.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventorySyncRepository @Inject constructor(
    private val db: AppDb,
    private val api: InventoryApi,
    private val settings: SettingsRepository
) {

    /**
     * Faz o PULL do catálogo (paginado) do backend e salva no Room.
     * Também limpa produtos e contagens locais antes de importar.
     *
     * @return Pair(processadosDaAPI, totalNoRoomAposSalvar)
     */
    suspend fun pullAndSave(estoque: String, pageSize: Int = 500): Pair<Int, Int> {
        val (dbHost, dbName) = activeDbHeaders()

        val productDao = db.productDao()
        val contagemDao = db.contagemDao()

        // 🔴 IMPORTANTE: como o app trabalha sempre com UMA filial/estoque ativo,
        // limpamos os produtos E contagens antes de sincronizar.
        contagemDao.clearAll()
        productDao.clearAll()

        var offset = 0
        var processed = 0

        while (true) {
            val resp = api.syncInventory(
                dbHost = dbHost,
                dbName = dbName,
                estoque = estoque,   // "loja" | "deposito"
                limit = pageSize,
                offset = offset
            )

            val items = resp.items.orEmpty()
            if (items.isEmpty()) break

            val entities = items.map { dto ->
                ProductEntity(
                    id   = dto.id,
                    ean  = dto.ean?.filter(Char::isDigit)?.let { d ->
                        when (d.length) {
                            8, 12 -> d.padStart(13, '0')
                            13, 14 -> d
                            else   -> d
                        }
                    },
                    nome = dto.nome.trim().ifEmpty { "SEM NOME" },
                    uom  = dto.uom?.takeIf { it.isNotBlank() } ?: "UN",
                    stq  = dto.stq ?: 0.0
                )
            }

            productDao.upsertAll(entities)
            processed += items.size
            offset    += items.size
            if (items.size < pageSize) break
        }

        val localCount = productDao.count()
        return processed to localCount
    }

    /** Diagnóstico: conta quantos produtos existem no Room. */
    suspend fun localCount(): Int = db.productDao().count()

    /**
     * Lê a filial ativa nas configurações e devolve:
     *  - primeiro: host/IP do servidor de dados
     *  - segundo: nome do banco de dados
     *
     * Esses valores são enviados para a API como dbHost/dbName.
     */
    private suspend fun activeDbHeaders(): Pair<String, String> {
        val currentId = settings.currentId().first()
            ?: error("Nenhuma filial ativa nas configurações.")

        val branches = settings.branches().first()
        val branch = branches.firstOrNull { it.id == currentId }
            ?: error("Filial ativa ($currentId) não encontrada na lista de filiais.")

        val host = branch.dbServer.trim().ifEmpty {
            error("Filial ativa não possui dbServer configurado.")
        }

        val name = branch.dbName.trim().ifEmpty {
            error("Filial ativa não possui dbName configurado.")
        }

        return host to name
    }
}

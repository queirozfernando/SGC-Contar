package com.example.myapplication.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myapplication.data.appDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.appDataStore

    companion object {
        private val KEY_CURRENT_ID   = stringPreferencesKey("current_branch_id")
        private val KEY_BRANCHES     = stringPreferencesKey("branches_json")
        private val KEY_LAST_ESTOQUE = stringPreferencesKey("last_estoque_tipo")
    }

    /** ID da filial atual (ou null se nenhuma selecionada). */
    fun currentId(): Flow<String?> =
        dataStore.data.map { prefs ->
            prefs[KEY_CURRENT_ID]
        }

    suspend fun setCurrentId(id: String?) {
        dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(KEY_CURRENT_ID)
            } else {
                prefs[KEY_CURRENT_ID] = id
            }
        }
    }

    /** Tipo de estoque usado por último ("loja" ou "deposito"). */
    fun lastEstoque(): Flow<String> =
        dataStore.data.map { prefs ->
            prefs[KEY_LAST_ESTOQUE] ?: ""
        }

    suspend fun setLastEstoque(tipo: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_ESTOQUE] = tipo
        }
    }

    /** Lista de filiais configuradas. */
    fun branches(): Flow<List<BranchConfig>> =
        dataStore.data.map { prefs ->
            val json = prefs[KEY_BRANCHES] ?: "[]"
            try {
                Json.decodeFromString(
                    ListSerializer(BranchConfig.serializer()),
                    json
                )
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun setBranches(branches: List<BranchConfig>) {
        val json = Json.encodeToString(
            ListSerializer(BranchConfig.serializer()),
            branches
        )
        dataStore.edit { prefs ->
            prefs[KEY_BRANCHES] = json
        }
    }
}

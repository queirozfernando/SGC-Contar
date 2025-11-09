package com.example.myapplication.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repo.InventorySyncRepository
import com.example.myapplication.data.settings.BranchConfig
import com.example.myapplication.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado da UI de sincronismo.
 */
data class SyncUiState(
    val syncing: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val inventorySyncRepo: InventorySyncRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SyncUiState())
    val ui: StateFlow<SyncUiState> = _ui.asStateFlow()

    // Flows de filiais
    fun branches() = settingsRepo.branches()
    fun currentId() = settingsRepo.currentId()

    // 🔹 Expor também o último tipo de estoque (vem do DataStore)
    fun lastEstoque() = settingsRepo.lastEstoque()

    fun setCurrentBranch(id: String) {
        viewModelScope.launch {
            settingsRepo.setCurrentId(id)          // ✅ nome novo
        }
    }

    fun saveBranches(list: List<BranchConfig>) {
        viewModelScope.launch {
            settingsRepo.setBranches(list)         // ✅ nome novo
        }
    }

    /**
     * Cria duas filiais de exemplo e define "loja_amadeu" como ativa.
     */
    fun saveExampleBranches() {
        viewModelScope.launch {
            val examples = listOf(
                BranchConfig(
                    id = "loja_amadeu",
                    nome = "Loja Amadeu",
                    backendUrl = "http://192.168.15.11:8000/",
                    dbServer = "192.168.15.11",
                    dbName = "amadeu",
                    apiToken = "dev"
                ),
                BranchConfig(
                    id = "loja_quinze",
                    nome = "Loja Quinze",
                    backendUrl = "http://192.168.15.12:8000/",
                    dbServer = "192.168.15.12",
                    dbName = "quinze",
                    apiToken = "dev"
                )
            )

            settingsRepo.setBranches(examples)     // ✅ nome novo
            settingsRepo.setCurrentId("loja_amadeu")
        }
    }

    /**
     * Dispara o sincronismo usando o InventorySyncRepository.
     */
    fun sync(estoque: String, filtro: String? = null) {
        viewModelScope.launch {
            // 🔹 persiste o último tipo de estoque usado
            settingsRepo.setLastEstoque(estoque)   // "loja" ou "deposito"

            _ui.value = SyncUiState(
                syncing = true,
                message = "Sincronizando estoque de $estoque…"
            )

            try {
                inventorySyncRepo.pullAndSave(estoque)

                _ui.value = SyncUiState(
                    syncing = false,
                    message = "Sincronismo concluído para $estoque."
                )
            } catch (e: Exception) {
                _ui.value = SyncUiState(
                    syncing = false,
                    message = "Erro ao sincronizar $estoque: ${e.message}"
                )
            }
        }
    }
}

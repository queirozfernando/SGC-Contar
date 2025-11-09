package com.example.myapplication.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repo.InventorySyncRepository
import com.example.myapplication.data.settings.BranchConfig
import com.example.myapplication.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class InventoryVm @Inject constructor(
    private val settings: SettingsRepository,
    private val syncRepo: InventorySyncRepository
) : ViewModel() {

    // Define a filial atual (agora id é String)
    fun setCurrent(id: String) {
        viewModelScope.launch {
            settings.setCurrentId(id)      // ✅ nome novo no SettingsRepository
        }
    }

    // Salva lista de filiais
    fun saveBranches(list: List<BranchConfig>) {
        viewModelScope.launch {
            settings.setBranches(list)     // ✅ nome novo no SettingsRepository
        }
    }

    // Sincronização principal
    suspend fun pull(estoque: String) = syncRepo.pullAndSave(estoque)
}

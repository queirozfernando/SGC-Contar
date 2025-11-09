package com.example.myapplication.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExportUi(
    val loading: Boolean = false,
    val ok: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ExportVm @Inject constructor(
    private val repo: ExportRepository   // <- está no mesmo package; não precisa de import
) : ViewModel() {

    private val _ui = MutableStateFlow(ExportUi())
    val ui: StateFlow<ExportUi> = _ui

    /**
     * Exporta o inventário para o backend via /inventory/export.
     *
     * Os parâmetros ainda existem para compatibilidade com chamadas antigas,
     * mas atualmente são ignorados: o ExportRepository usa a filial ativa,
     * o tipo de estoque e as contagens locais para montar tudo.
     */
    fun exportar(
        loja: String? = null,
        deviceId: String? = null,
        operador: String? = null,
        apiToken: String? = null,           // X-Api-Token (ignorado aqui)
        bearerJwt: String? = null,          // Authorization: Bearer <jwt> (ignorado aqui)
        nomeArquivoOpcional: String? = null // nome opcional (ignorado aqui)
    ) {
        _ui.value = ExportUi(loading = true)
        viewModelScope.launch {
            try {
                // Novo fluxo: usa /inventory/export
                repo.exportarParaERP()

                _ui.value = ExportUi(
                    loading = false,
                    ok = true,
                    message = "Exportação enviada com sucesso."
                    // contagemId, nomeArquivo, enviados ficam nulos/0,
                    // pois o endpoint /inventory/export não retorna esses dados.
                )
            } catch (e: Exception) {
                _ui.value = ExportUi(
                    loading = false,
                    ok = false,
                    message = e.message ?: "Falha ao exportar"
                )
            }
        }
    }
}

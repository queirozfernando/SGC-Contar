package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.menu.HomeMenuScreen
import com.example.myapplication.ui.inventory.InventorySyncScreen
import com.example.myapplication.ui.contagem.ContagemScreen
import com.example.myapplication.ui.export.ExportContagemScreen
import com.example.myapplication.ui.settings.ConfigFiliaisScreen   // <-- usa a tela nova
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val nav = rememberNavController()

            NavHost(
                navController = nav,
                startDestination = "home"
            ) {
                // MENU PRINCIPAL
                composable("home") {
                    HomeMenuScreen(
                        onSincronizarInventario = { nav.navigate("sync") },
                        onFazerContagem        = { nav.navigate("count") },
                        onExportarContagem     = { nav.navigate("export") },
                        onConfiguracoes        = { nav.navigate("settings") }
                    )
                }

                // SINCRONIZAÇÃO DE INVENTÁRIO
                composable("sync") {
                    InventorySyncScreen(
                        onBack = { nav.popBackStack() },
                        onOpenSettings = { nav.navigate("settings") }
                    )
                }

                // CONTAGEM
                composable("count") {
                    ContagemScreen(
                        onDone = { nav.popBackStack() }
                    )
                }

                // EXPORTAÇÃO DE CONTAGEM
                composable("export") {
                    ExportContagemScreen(
                        onBack = { nav.popBackStack() }
                    )
                }

                // CONFIGURAÇÕES -> agora abre diretamente a tela de filiais
                composable("settings") {
                    ConfigFiliaisScreen(
                        onBack = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.estoque.ImportEstoqueScreen
import com.example.myapplication.ui.menu.HomeMenuScreen
import com.example.myapplication.ui.contagem.ContagemScreen
import com.example.myapplication.ui.export.ExportContagemScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val nav = rememberNavController()

            NavHost(
                navController = nav,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeMenuScreen(
                        onCarregarEstoque = { nav.navigate("import") },
                        onFazerContagem   = { nav.navigate("count") },
                        onExportarContagem= { nav.navigate("export") }
                    )
                }

                composable("import") {
                    ImportEstoqueScreen(
                        onDone = {
                            nav.navigate("home") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ✅ ContagemScreen sem parâmetros extras
                composable("count") {
                    ContagemScreen(onDone = { nav.popBackStack() })
                }


                // ✅ ExportContagemScreen (stub)
                composable("export") {
                    ExportContagemScreen(onDone = { nav.popBackStack() })
                }
            }
        }
    }
}

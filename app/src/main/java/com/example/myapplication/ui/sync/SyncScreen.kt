@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SyncScreen(
    onBack: () -> Unit = {},
    vm: SyncViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState(initial = SyncUiState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sincronizar", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3A34),
                    titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            // 🎨 Fundo com gradiente branco → preto (topo claro, base escura)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White,        // topo
                                Color(0xFFBAC4C7),  // cinza intermediário
                                Color(0xFF1E2E38),  // cinza escuro
                                Color.Black         // base
                            )
                        )
                    )
            )

            // 📋 Conteúdo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // 🔘 Botões principais
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { vm.sync(estoque = "loja", filtro = null) },
                        enabled = !ui.syncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F3A34),
                            contentColor = Color.White
                        )
                    ) { Text("Sync Loja") }

                    Button(
                        onClick = { vm.sync(estoque = "deposito", filtro = null) },
                        enabled = !ui.syncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F3A34),
                            contentColor = Color.White
                        )
                    ) { Text("Sync Depósito") }
                }

                Spacer(Modifier.height(24.dp))

                if (ui.syncing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color(0xFF0F3A34),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sincronizando…", color = Color(0xFF0F3A34))
                    }
                }

                ui.message?.let {
                    Spacer(Modifier.height(20.dp))
                    Text(it, color = Color(0xFF0F3A34))
                }
            }
        }
    }
}

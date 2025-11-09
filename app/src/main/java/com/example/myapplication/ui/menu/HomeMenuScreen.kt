package com.example.myapplication.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuScreen(
    onSincronizarInventario: () -> Unit,
    onFazerContagem: () -> Unit,
    onExportarContagem: () -> Unit,
    onConfiguracoes: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SGC Contar", color = Color.White, fontWeight = FontWeight.SemiBold) },
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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF), // topo branco
                            Color(0xFFBAC4C7),
                            Color(0xFF1E2E38)  // base escura
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // LOGO
                Image(
                    painter = painterResource(id = R.drawable.sgc),
                    contentDescription = "Logo SGC",
                    modifier = Modifier
                        .height(100.dp)
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Fit
                )

                // Título
                Text(
                    text = "Menu Principal",
                    color = Color(0xFF0F3A34),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(40.dp))

                // Botões
                MenuButton(
                    text = "Sincronizar Inventário",
                    icon = Icons.Filled.CloudSync,
                    onClick = onSincronizarInventario
                )

                Spacer(Modifier.height(20.dp))

                MenuButton(
                    text = "Fazer Contagem",
                    icon = Icons.Filled.Inventory,
                    onClick = onFazerContagem
                )

                Spacer(Modifier.height(20.dp))

                MenuButton(
                    text = "Exportar Contagem",
                    icon = Icons.Filled.Upload,
                    onClick = onExportarContagem
                )

                Spacer(Modifier.height(20.dp))

                MenuButton(
                    text = "Configurações",
                    icon = Icons.Filled.Settings,
                    onClick = onConfiguracoes
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Botão estilizado — cantos menos arredondados e efeito de “press” suave
 */
@Composable
private fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(120)
            pressed = false
        }
    }

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.small, // cantos levemente arredondados
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) Color(0xFF145B4D) else Color(0xFF0F3A34),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (pressed) 2.dp else 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(icon, contentDescription = text, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

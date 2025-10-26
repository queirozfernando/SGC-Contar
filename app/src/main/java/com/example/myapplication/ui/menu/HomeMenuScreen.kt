package com.example.myapplication.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuScreen(
    onCarregarEstoque: () -> Unit,
    onFazerContagem: () -> Unit,
    onExportarContagem: () -> Unit,
) {
    // fundo metálico
    val gradientBackground = Brush.horizontalGradient(
        listOf(Color(0xFF1E2E38), Color(0xFFD9D9D9), Color(0xFF1E2E38))
    )

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(gradientBackground),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // logo
            Image(
                painter = painterResource(R.drawable.sgc),
                contentDescription = "Logo SGC",
                modifier = Modifier.width(200.dp).height(90.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "SGC Contar",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(Modifier.height(22.dp))
            Text(
                "O que deseja fazer?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            // tiles no formato da imagem (mais estreitos)
            MenuTile(
                text = "1) Carregar estoque (CSV)",
                iconRes = R.drawable.ic_estoque,     // seu ícone
                onClick = onCarregarEstoque
            )
            Spacer(Modifier.height(12.dp))

            MenuTile(
                text = "2) Fazer contagem",
                iconRes = R.drawable.ic_contagem,    // seu ícone
                onClick = onFazerContagem
            )
            Spacer(Modifier.height(12.dp))

            MenuTile(
                text = "3) Exportar contagem (CSV)",
                iconRes = R.drawable.ic_exportar,    // seu ícone
                onClick = onExportarContagem
            )
        }
    }
}

/** Cartão clicável no estilo do print: fundo verde escuro, cantos arredondados, ícone topo-esq e texto embaixo-esq. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTile(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileColor = Color(0xFF0F3A34)    // verde escuro
    val contentColor = Color(0xFFE6F2EF) // texto/ícone claro

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.78f)  // MAIS ESTREITO (ajuste aqui: 0.70f–0.85f)
            .height(92.dp),       // altura parecida com o print (ajuste livre)
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tileColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        // conteúdo do tile
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // ícone topo-esq
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )

            // texto embaixo-esq
            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

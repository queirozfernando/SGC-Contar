package com.example.myapplication.ui.contagem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.InventoryApp
import com.example.myapplication.data.local.ContagemEntity
import com.example.myapplication.data.local.ProductEntity
import com.example.myapplication.data.repo.ContagemRepository
import com.example.myapplication.data.repo.ProductRepository
import com.example.myapplication.ui.components.BarcodeScannerSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContagemScreen(
    onDone: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as InventoryApp

    val productRepo = remember { ProductRepository(app.db.productDao()) }
    val contagemRepo = remember { ContagemRepository(app.db.contagemDao()) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isLoading by remember { mutableStateOf(true) }
    var produtos by remember { mutableStateOf(emptyList<ProductEntity>()) }
    var contagens by remember { mutableStateOf<Map<Long, ContagemEntity>>(emptyMap()) }
    var query by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    suspend fun reload() {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                // sempre ordena por nome
                produtos = productRepo.getAll()
                    .sortedBy { it.nome.lowercase() }

                contagens = contagemRepo.getAll()
                    .associateBy { it.productId }
            }
        } finally {
            isLoading = false
        }
    }

    // ao abrir a tela: CARREGA produtos e contagens, mas NÃO limpa
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            produtos = productRepo.getAll()
                .sortedBy { it.nome.lowercase() }

            contagens = contagemRepo.getAll()
                .associateBy { it.productId }
        }
        isLoading = false
    }

    val filtered by remember(produtos, query) {
        derivedStateOf {
            val q = query.trim().lowercase()
            if (q.isEmpty()) produtos
            else produtos.filter {
                it.nome.lowercase().contains(q) || (it.ean ?: "").contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Fazer contagem", color = Color.White) },
                actions = {
                    TextButton(
                        onClick = { scope.launch { reload() } },
                        enabled = !isLoading
                    ) {
                        Text("Atualizar", color = Color.White)
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
            // Fundo gradiente superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2E38), Color(0xFFBAC4C7), Color.White)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo de busca
                OutlinedTextField(
                    value = query,
                    onValueChange = { text ->
                        query = if (text.endsWith("\n")) text.trim() else text
                    },
                    singleLine = true,
                    label = { Text("Buscar por nome ou EAN", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F3A34), shape = RoundedCornerShape(4.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F3A34),
                        unfocusedContainerColor = Color(0xFF0F3A34),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color(0xFFBAC4C7),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showScanner = true }) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Ler EAN",
                                tint = Color.White
                            )
                        }
                    }
                )

                // Botão limpar contagens (manual)
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) { contagemRepo.clearAll() }
                            contagens = emptyMap()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F3A34),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF0F3A34).copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Text("Limpar contagens")
                }

                if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

                // Lista principal
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { p ->
                        val contagemAtual = contagens[p.id]?.qty ?: 0.0
                        val qtdImportada = p.stq.takeIf { it.isFinite() } ?: 0.0

                        var qtyText by remember(p.id, contagemAtual, qtdImportada) {
                            mutableStateOf(
                                if (contagemAtual > 0) formatNumber(contagemAtual)
                                else ""
                            )
                        }
                        var savedFlash by remember(p.id) { mutableStateOf(false) }

                        suspend fun persistQty(value: Double) {
                            withContext(Dispatchers.IO) {
                                contagemRepo.upsertForProduct(p.id, p.ean, value)
                            }
                            contagens = contagens.toMutableMap().apply {
                                put(
                                    p.id,
                                    ContagemEntity(
                                        productId = p.id,
                                        ean = p.ean,
                                        qty = value
                                    )
                                )
                            }
                            qtyText = formatNumber(value)
                            savedFlash = true
                        }

                        LaunchedEffect(savedFlash) {
                            if (savedFlash) {
                                delay(1200)
                                savedFlash = false
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2C3E3B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = p.id.toString(),
                                    color = Color(0xFFB0B0B0),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .width(50.dp)
                                        .padding(top = 4.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.nome.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    val eanText =
                                        p.ean?.takeIf { it.isNotBlank() }?.padStart(13, '0')
                                    val uomText = p.uom?.takeIf { it.isNotBlank() }
                                    if (eanText != null || uomText != null) {
                                        val info = buildString {
                                            if (eanText != null) append(eanText)
                                            if (eanText != null && uomText != null) append("  ")
                                            if (uomText != null) append(uomText.uppercase())
                                        }
                                        Text(
                                            text = info,
                                            color = Color(0xFFB0B0B0),
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = qtyText,
                                        onValueChange = { qtyText = filterDecimalInput(it) },
                                        singleLine = true,
                                        placeholder = {
                                            Text(
                                                formatNumber(qtdImportada),
                                                color = Color(0xFFB0B0B0),
                                                textAlign = TextAlign.Start
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 54.dp)
                                            .onFocusChanged { f ->
                                                if (!f.isFocused) {
                                                    val v = qtyText.replace(',', '.')
                                                        .toDoubleOrNull()
                                                    val atual = contagens[p.id]?.qty
                                                    if (v != null && v != atual) {
                                                        scope.launch { persistQty(v) }
                                                    }
                                                }
                                            },
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            textAlign = TextAlign.End,
                                            fontSize = 17.sp
                                        ),
                                        suffix = { Spacer(Modifier.width(10.dp)) },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                val v = qtyText.replace(',', '.')
                                                    .toDoubleOrNull()
                                                val atual = contagens[p.id]?.qty
                                                focusManager.clearFocus()
                                                if (v != null && v != atual) {
                                                    scope.launch { persistQty(v) }
                                                }
                                            }
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = if (savedFlash)
                                                Color(0xFF2E7D32)
                                            else Color.White,
                                            unfocusedBorderColor = Color(0xFFB0B0B0),
                                            cursorColor = Color.White,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        )
                                    )

                                    AnimatedVisibility(
                                        visible = savedFlash,
                                        enter = fadeIn() + scaleIn(),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(top = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = "Salvo",
                                                tint = Color(0xFF2E7D32)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Salvo", color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Rodapé minimalista
                Text(
                    text = "Produtos: ${produtos.size}  •  Contados: ${contagens.size}",
                    color = Color(0xFF0F3A34),
                    fontWeight = FontWeight.Medium
                )
            }

            if (showScanner) {
                BarcodeScannerSheet(
                    onScanned = { ean ->
                        query = ean
                        showScanner = false
                    },
                    onDismiss = { showScanner = false }
                )
            }
        }
    }
}

private fun formatNumber(n: Double): String {
    val v = if (n.isFinite()) n else 0.0
    val raw = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
    return raw.replace(',', '.')
}

private fun filterDecimalInput(input: String): String {
    var hasSep = false
    val out = StringBuilder()
    input.forEach { ch ->
        when {
            ch.isDigit() -> out.append(ch)
            (ch == ',' || ch == '.') && !hasSep -> {
                out.append(ch)
                hasSep = true
            }
        }
    }
    return out.toString()
}

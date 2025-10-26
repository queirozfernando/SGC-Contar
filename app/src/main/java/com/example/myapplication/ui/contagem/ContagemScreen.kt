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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.InventoryApp
import com.example.myapplication.data.local.ContagemEntity
import com.example.myapplication.data.local.ProductEntity
import com.example.myapplication.data.repo.ContagemRepository
import com.example.myapplication.data.repo.ProductRepository
import com.example.myapplication.ui.components.MenuPrimaryButton
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
    val productRepo = remember { ProductRepository(ctx, app.db.productDao()) }
    val contagemRepo = remember { ContagemRepository(app.db.contagemDao()) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isLoading by remember { mutableStateOf(true) }
    var produtos by remember { mutableStateOf(emptyList<ProductEntity>()) }
    var contagens by remember { mutableStateOf<Map<Long, ContagemEntity>>(emptyMap()) }
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            produtos = productRepo.getAll()
            contagens = contagemRepo.getAll().associateBy { it.productId }
        }
        isLoading = false
        status = "Produtos: ${produtos.size}  •  Contados: ${contagens.size}"
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
                title = { Text("Fazer contagem", color = Color.White) },
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
            // Fundo com gradiente
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
                        .background(Color(0xFF0F3A34), shape = MaterialTheme.shapes.small),
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

                // Ações principais — mesmo formato da tela de importação
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Limpar contagens
                    MenuPrimaryButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) { contagemRepo.clearAll() }
                                contagens = emptyMap()
                                status = "Produtos: ${produtos.size}  •  Contados: 0"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Limpar contagens")
                    }

                    // Concluir
                    MenuPrimaryButton(
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text("Concluir") }
                }

                if (isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else {
                    Text(status, color = Color.White)
                }

                // Lista
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .padding(vertical = 8.dp)
                ) {
                    // Cabeçalho
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ID", Modifier.width(50.dp), Color.Gray, textAlign = TextAlign.End)
                            Spacer(Modifier.width(8.dp))
                            Text("Produto", Modifier.weight(1f), Color.Gray)
                            // espaço reservado pro "Salvo"
                            Spacer(Modifier.width(80.dp))
                        }
                        HorizontalDivider()
                    }

                    // Linhas
                    items(filtered, key = { it.id }) { p ->
                        val contagemAtual = contagens[p.id]?.qty ?: 0.0
                        val qtdImportada = p.stq

                        var qtyText by remember(p.id, contagemAtual, qtdImportada) {
                            mutableStateOf(if (contagemAtual > 0) formatNumber(contagemAtual) else "")
                        }
                        var savedFlash by remember(p.id) { mutableStateOf(false) } // mostra o check/verde

                        // helper: salvar valor
                        suspend fun persistQty(value: Double) {
                            withContext(Dispatchers.IO) {
                                contagemRepo.upsertForProduct(p.id, p.ean, value)
                            }
                            // atualiza cache UI
                            contagens = contagens.toMutableMap().apply {
                                put(
                                    p.id,
                                    ContagemEntity(
                                        p.id, p.ean, value, System.currentTimeMillis()
                                    )
                                )
                            }
                            status = "Produtos: ${produtos.size}  •  Contados: ${contagens.size}"
                            qtyText = formatNumber(value)
                            savedFlash = true
                        }

                        // sumir o "check" após 1,2s
                        LaunchedEffect(savedFlash) {
                            if (savedFlash) {
                                delay(1200)
                                savedFlash = false
                            }
                        }

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            // ID, nome, unidade e EAN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    p.id.toString(),
                                    modifier = Modifier.width(50.dp),
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.End
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        p.nome,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2
                                    )
                                    if (!p.uom.isNullOrBlank()) {
                                        Text(p.uom, color = Color.Gray, fontSize = 12.sp)
                                    }
                                    if (!p.ean.isNullOrBlank()) {
                                        Text(
                                            "EAN: ${p.ean.padStart(13, '0')}",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Área do feedback "Salvo"
                                AnimatedVisibility(
                                    visible = savedFlash,
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 8.dp)
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

                            Spacer(Modifier.height(6.dp))

                            // Campo quantidade (auto-save)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.width(58.dp)) // compensa a coluna do ID (50 + 8)

                                // Cores dinâmicas do campo: borda verde quando salvo recentemente
                                val fieldColors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = if (savedFlash) Color(0xFF2E7D32) else Color.Gray,
                                    unfocusedIndicatorColor = if (savedFlash) Color(0xFF2E7D32) else Color.Gray,
                                    cursorColor = Color.Black
                                )

                                OutlinedTextField(
                                    value = qtyText,
                                    onValueChange = { qtyText = filterDecimalInput(it) },
                                    singleLine = true,
                                    placeholder = { Text(formatNumber(qtdImportada), color = Color.Gray) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .onFocusChanged { f ->
                                            // auto-salvar quando perder o foco e tiver valor válido e diferente do atual
                                            if (!f.isFocused) {
                                                val v = qtyText.replace(',', '.').toDoubleOrNull()
                                                val atual = contagens[p.id]?.qty
                                                if (v != null && v != atual) {
                                                    scope.launch { persistQty(v) }
                                                }
                                            }
                                        },
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            val v = qtyText.replace(',', '.').toDoubleOrNull()
                                            val atual = contagens[p.id]?.qty
                                            if (v != null && v != atual) {
                                                scope.launch { persistQty(v) }
                                            }
                                        }
                                    ),
                                    colors = fieldColors
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }

                Text("Total listados: ${filtered.size}", color = Color(0xFF0F3A34))
            }

            if (showScanner) {
                BarcodeScannerSheet(
                    onScanned = { ean -> query = ean; showScanner = false },
                    onDismiss = { showScanner = false }
                )
            }
        }
    }
}

private fun formatNumber(n: Double): String {
    val raw = if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
    return raw.replace(',', '.')
}

/** Permite apenas dígitos e um único separador decimal (',' ou '.') */
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
